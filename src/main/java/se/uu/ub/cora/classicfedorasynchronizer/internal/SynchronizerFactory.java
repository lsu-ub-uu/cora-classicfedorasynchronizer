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
	@Override
	public ClassicCoraSynchronizer factor() {
		// new ClassicCoraPersonSynchronizer(dbStorage, httpHandlerFactory,
		// fedoraConverterFactory, coraIndexer, baseUrl);
		return null;
	}

}
