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

import se.uu.ub.cora.classicfedorasynchronizer.ClassicCoraSynchronizer;
import se.uu.ub.cora.classicfedorasynchronizer.ClassicCoraSynchronizerFactory;
import se.uu.ub.cora.classicfedorasynchronizer.FedoraConverterFactory;
import se.uu.ub.cora.httphandler.HttpHandlerFactory;
import se.uu.ub.cora.httphandler.HttpHandlerFactoryImp;
import se.uu.ub.cora.storage.RecordStorage;
import se.uu.ub.cora.xmlutils.transformer.CoraTransformationFactory;

public class SynchronizerFactory implements ClassicCoraSynchronizerFactory {
	// TODO: dbStorage --- initinfo??
	// DatabaseStorageProvider databaseStorageProvider = new DatabaseStorageProvider();
	// databaseStorageProvider.startUsingInitInfo(initInfo);
	// DatabaseRecordStorage recordStorage = databaseStorageProvider.getRecordStorage();

	// HttpHandlerFactory factory = new HttpHandlerFactoryImp();

	// TODO: DivaFedoraConverterFactoryImp --- fedoraURL??
	// XsltTransformationFactory transformationFactory = new XsltTransformationFactory();
	// DivaFedoraConverterFactoryImp.usingFedoraURLAndTransformerFactory(fedoraURL,
	// transformationFactory)

	// TODO: coraIndexer --- apptokenVerifierUrl, baseURL (samma som baseURL i new
	// ClassicCoraPersonSynchronizer) userId, apptoken ??
	// CoraIndexerFactory indexerFactory =
	// CoraIndexerFactoryImp.usingApptokenVerifierUrlAndBaseUrl(apptokenVerifierURL, baseURL);
	// coraIndexer = indexerFactory.factor(userId, apptoken)

	private RecordStorage recordStorage;
	private HttpHandlerFactory httpHandlerFactory = new HttpHandlerFactoryImp();
	private FedoraConverterFactory fedoraConverterFactory;
	private String baseURL;

	// public SynchronizerFactory() {
	// this.recordStorage = recordStorage;
	// this.httpHandler = httpHandler;
	// }

	public SynchronizerFactory(String baseUrl, RecordStorage recordStorage,
			FedoraConverterFactory fedoraConverterFactory) {
		this.baseURL = baseUrl;
		this.recordStorage = recordStorage;

		CoraTransformationFactory coraTransformationFactory = null;

		this.fedoraConverterFactory = DivaFedoraConverterFactoryImp
				.usingFedoraURLAndTransformerFactory(coraTransformationFactory);
	}

	@Override
	public ClassicCoraSynchronizer factor() {

		return new ClassicCoraPersonSynchronizer(null, httpHandlerFactory, fedoraConverterFactory,
				null, null);
	}

	public void sethttpHandlerFactory(HttpHandlerFactory httpHandlerFactory) {
		this.httpHandlerFactory = httpHandlerFactory;
	}

	// private static CoraClientFactory createCoraClientFactoryFromProperties(Properties properties)
	// {
	// String baseUrl = extractPropertyThrowErrorIfNotFound(properties, "baseUrl");
	// String appTokenVerifierUrl = extractPropertyThrowErrorIfNotFound(properties,
	// "appTokenVerifierUrl");
	// String logM2 = "Sending indexOrders to: {0} using appToken from: {1}";
	// String formattedLogMessage2 = MessageFormat.format(logM2, baseUrl, appTokenVerifierUrl);
	// logger.logInfoUsingMessage(formattedLogMessage2);
	// return CoraClientFactoryImp.usingAppTokenVerifierUrlAndBaseUrl(appTokenVerifierUrl,
	// baseUrl);
	// }
	//
	// private static String extractPropertyThrowErrorIfNotFound(Properties properties,
	// String propertyName) {
	// throwErrorIfPropertyNameIsMissing(properties, propertyName);
	// return properties.getProperty(propertyName);
	// }
	//
	// private static void throwErrorIfPropertyNameIsMissing(Properties properties,
	// String propertyName) {
	// if (!properties.containsKey(propertyName)) {
	// throw new RuntimeException(
	// "Property with name " + propertyName + " not found in properties");
	// }
	// }

}
