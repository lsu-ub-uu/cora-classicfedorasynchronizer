/*
 * Copyright 2021 Uppsala University Library
 *
 * This file is part of Cora.
 *
 *     Cora is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Cora is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Cora.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.uu.ub.cora.classicfedorasynchronizer.internal;

import java.util.Map;

import se.uu.ub.cora.classicfedorasynchronizer.ClassicCoraSynchronizer;
import se.uu.ub.cora.classicfedorasynchronizer.ClassicCoraSynchronizerFactory;
import se.uu.ub.cora.classicfedorasynchronizer.FedoraConverterFactory;
import se.uu.ub.cora.httphandler.HttpHandlerFactory;
import se.uu.ub.cora.httphandler.HttpHandlerFactoryImp;
import se.uu.ub.cora.javaclient.cora.CoraClient;
import se.uu.ub.cora.javaclient.cora.CoraClientFactory;
import se.uu.ub.cora.javaclient.cora.CoraClientFactoryImp;
import se.uu.ub.cora.json.parser.JsonParser;
import se.uu.ub.cora.json.parser.org.OrgJsonParser;
import se.uu.ub.cora.sqldatabase.SqlDatabaseFactory;
import se.uu.ub.cora.sqldatabase.SqlDatabaseFactoryImp;
import se.uu.ub.cora.sqlstorage.DatabaseRecordStorage;
import se.uu.ub.cora.storage.RecordStorage;
import se.uu.ub.cora.xmlutils.transformer.CoraTransformationFactory;
import se.uu.ub.cora.xmlutils.transformer.XsltTransformationFactory;

public class SynchronizerFactory implements ClassicCoraSynchronizerFactory {

	private HttpHandlerFactory httpHandlerFactory;
	private FedoraConverterFactory fedoraConverterFactory;
	private Map<String, String> initInfo;
	private RecordStorage recordStorage;
	private CoraClient coraClient;
	private CoraClientFactory coraClientFactory;

	public static SynchronizerFactory usingInitInfo(Map<String, String> initInfo) {
		return new SynchronizerFactory(initInfo);
	}

	private SynchronizerFactory(Map<String, String> initInfo) {
		this.initInfo = initInfo;
		initializeDatabaseRecordStorage();
		initializeHttpHandlerFactory();
		initializeFedoraConverterFactory();
		initializeCoraClient();
	}

	private void initializeDatabaseRecordStorage() {
		String databaseUrl = initInfo.get("databaseUrl");
		String databaseUser = initInfo.get("databaseUser");
		String databasePassword = initInfo.get("databasePassword");

		SqlDatabaseFactory sqlDatabaseFactory = SqlDatabaseFactoryImp
				.usingUriAndUserAndPassword(databaseUrl, databaseUser, databasePassword);
		JsonParser jsonParser = new OrgJsonParser();

		recordStorage = new DatabaseRecordStorage(sqlDatabaseFactory, jsonParser);
	}

	private void initializeHttpHandlerFactory() {
		httpHandlerFactory = new HttpHandlerFactoryImp();
	}

	private void initializeFedoraConverterFactory() {
		CoraTransformationFactory coraTransformationFactory = new XsltTransformationFactory();
		this.fedoraConverterFactory = DivaFedoraConverterFactoryImp
				.usingTransformerFactory(coraTransformationFactory);
	}

	private void initializeCoraClient() {

		String apptokenVerifierURL = initInfo.get("coraApptokenVerifierURL");
		String baseURL = initInfo.get("coraBaseUrl");
		coraClientFactory = CoraClientFactoryImp
				.usingAppTokenVerifierUrlAndBaseUrl(apptokenVerifierURL, baseURL);

		factorCoraClient();
	}

	private void factorCoraClient() {
		String userId = initInfo.get("coraUserId");
		String apptoken = initInfo.get("coraApptoken");

		coraClient = coraClientFactory.factor(userId, apptoken);
	}

	@Override
	public ClassicCoraSynchronizer factorForMessaging() {

		return ClassicCoraPersonSynchronizer.createClassicCoraPersonSynchronizerForMessaging(
				recordStorage, httpHandlerFactory, fedoraConverterFactory, coraClient,
				initInfo.get("fedoraBaseUrl"));
	}

	@Override
	public ClassicCoraSynchronizer factorForBatch() {
		return ClassicCoraPersonSynchronizer.createClassicCoraPersonSynchronizerForBatch(
				recordStorage, httpHandlerFactory, fedoraConverterFactory, coraClient,
				initInfo.get("fedoraBaseUrl"));
	}

	public Map<String, String> onlyForTestGetInitInfo() {
		return initInfo;
	}

	public CoraClientFactory onlyForTestGetCoraClientFactory() {
		return coraClientFactory;
	}

	public void onlyForTestSetCoraClientFactory(CoraClientFactory coraClientFactory) {
		this.coraClientFactory = coraClientFactory;
		factorCoraClient();
	}

}
