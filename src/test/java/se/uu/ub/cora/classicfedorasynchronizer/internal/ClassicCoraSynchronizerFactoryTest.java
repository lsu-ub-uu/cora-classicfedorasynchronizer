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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.classicfedorasynchronizer.ClassicCoraSynchronizerFactory;
import se.uu.ub.cora.httphandler.HttpHandlerFactoryImp;
import se.uu.ub.cora.javaclient.cora.CoraClient;
import se.uu.ub.cora.javaclient.cora.CoraClientFactoryImp;
import se.uu.ub.cora.json.parser.org.OrgJsonParser;
import se.uu.ub.cora.sqldatabase.SqlDatabaseFactoryImp;
import se.uu.ub.cora.sqlstorage.DatabaseRecordStorage;
import se.uu.ub.cora.xmlutils.transformer.XsltTransformationFactory;

public class ClassicCoraSynchronizerFactoryTest {

	SynchronizerFactory synchronizerFactory;
	DatabaseStorageProviderSpy databaseStorageProvider;

	private Map<String, String> initInfo;

	@BeforeMethod
	public void beforeMethod() {
		initInfo = new HashMap<>();

		initInfo.put("databaseUrl", "someUrl");
		initInfo.put("databaseUser", "someUser");
		initInfo.put("databasePassword", "somePassword");
		initInfo.put("fedoraBaseUrl", "someFedoraUrl");

		initInfo.put("coraApptokenVerifierURL", "someApptokenVerifierURL");
		initInfo.put("coraBaseUrl", "someCoraBaseUrl");
		initInfo.put("coraUserId", "someCoraUserId");
		initInfo.put("coraApptoken", "someCoraApptoken");

		databaseStorageProvider = new DatabaseStorageProviderSpy();

		synchronizerFactory = SynchronizerFactory.usingInitInfo(initInfo);
	}

	@Test
	public void testImplementsClassicCoraSynchronizerFactory() throws Exception {
		assertTrue(synchronizerFactory instanceof ClassicCoraSynchronizerFactory);
	}

	@Test
	public void testRecordStorageSetUpFromInitInfo() throws Exception {
		ClassicCoraPersonSynchronizer synchronizer = (ClassicCoraPersonSynchronizer) synchronizerFactory
				.factor();

		DatabaseRecordStorage databaseRecordStorage = (DatabaseRecordStorage) synchronizer
				.onlyForTestGetRecordStorage();
		assertTrue(databaseRecordStorage instanceof DatabaseRecordStorage);
		SqlDatabaseFactoryImp sqlDatabaseFactory = (SqlDatabaseFactoryImp) databaseRecordStorage
				.onlyForTestGetSqlDatabaseFactory();
		assertEquals(sqlDatabaseFactory.onlyForTestGetUrl(), initInfo.get("databaseUrl"));
		assertEquals(sqlDatabaseFactory.onlyForTestGetUser(), initInfo.get("databaseUser"));
		assertEquals(sqlDatabaseFactory.onlyForTestGetPassword(), initInfo.get("databasePassword"));

		assertTrue(databaseRecordStorage.onlyForTestGetJsonParser() instanceof OrgJsonParser);
	}

	@Test
	public void testHttpHandlersFactorySent() throws Exception {
		ClassicCoraPersonSynchronizer synchronizer = (ClassicCoraPersonSynchronizer) synchronizerFactory
				.factor();

		assertTrue(
				synchronizer.onlyForTestGetHttpHandlerFactory() instanceof HttpHandlerFactoryImp);

	}

	@Test
	public void testFedoraConverterFactorySent() throws Exception {
		ClassicCoraPersonSynchronizer synchronizer = (ClassicCoraPersonSynchronizer) synchronizerFactory
				.factor();

		assertFedoraConverterFactory(synchronizer);
	}

	private void assertFedoraConverterFactory(ClassicCoraPersonSynchronizer synchronize) {
		DivaFedoraConverterFactoryImp fedoraConverterFactory = (DivaFedoraConverterFactoryImp) synchronize
				.onlyForTestGetFedoraConverterFactory();
		assertTrue(fedoraConverterFactory instanceof DivaFedoraConverterFactoryImp);

		assertTrue(fedoraConverterFactory
				.getCoraTransformerFactory() instanceof XsltTransformationFactory);
	}

	@Test
	public void testDefaultCoraClientFactory() throws Exception {
		CoraClientFactoryImp coraClientFactory = (CoraClientFactoryImp) synchronizerFactory
				.onlyForTestGetCoraClientFactory();
		ClassicCoraPersonSynchronizer synchronizer = (ClassicCoraPersonSynchronizer) synchronizerFactory
				.factor();
		CoraClient coraClient = synchronizer.onlyForTestGetCoraClient();
		assertTrue(coraClient instanceof CoraClient);
		assertEquals(coraClientFactory.getAppTokenVerifierUrl(),
				initInfo.get("coraApptokenVerifierURL"));
		assertEquals(coraClientFactory.getBaseUrl(), initInfo.get("coraBaseUrl"));
	}

	@Test
	public void testCoraIndexerFactoredFromFactory() throws Exception {
		CoraClientFactorySpy coraClientFactorySpy = new CoraClientFactorySpy();
		synchronizerFactory.onlyForTestSetCoraClientFactory(coraClientFactorySpy);
		ClassicCoraPersonSynchronizer synchronizer = (ClassicCoraPersonSynchronizer) synchronizerFactory
				.factor();

		coraClientFactorySpy.MCR.assertParameters("factor", 0, initInfo.get("coraUserId"),
				initInfo.get("coraApptoken"));
		CoraClient coraClientFromSynchronizer = synchronizer.onlyForTestGetCoraClient();

		coraClientFactorySpy.MCR.assertReturn("factor", 0, coraClientFromSynchronizer);
	}

	@Test
	public void testFedoraBaseUrlSent() throws Exception {
		ClassicCoraPersonSynchronizer synchronizer = (ClassicCoraPersonSynchronizer) synchronizerFactory
				.factor();
		assertEquals(synchronizer.onlyForTestGetBaseUrl(), initInfo.get("fedoraBaseUrl"));
	}

	@Test
	public void testFactorForBatch() throws Exception {
		ClassicCoraPersonSynchronizer synchronizer = synchronizerFactory.factorForBatch();
	}

}
