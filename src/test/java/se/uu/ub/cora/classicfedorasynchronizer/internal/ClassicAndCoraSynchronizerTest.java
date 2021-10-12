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
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.storage.RecordNotFoundException;

public class ClassicAndCoraSynchronizerTest {

	private HttpHandlerFactorySpy httpHandlerFactory;
	private ClassicCoraSynchronizerImp synchronizer;
	private String baseURL;
	private FedoraConverterFactorySpy fedoraConverterFactory;
	private RecordStorageSpy dbStorage;

	@BeforeMethod
	public void setUp() {
		baseURL = "someBaseUrl";
		httpHandlerFactory = new HttpHandlerFactorySpy();
		fedoraConverterFactory = new FedoraConverterFactorySpy();
		dbStorage = new RecordStorageSpy();
		synchronizer = new ClassicCoraSynchronizerImp(dbStorage, httpHandlerFactory,
				fedoraConverterFactory, baseURL);

	}

	@Test
	public void testInit() {
		assertSame(synchronizer.getHttpHandlerFactory(), httpHandlerFactory);
	}

	@Test(expectedExceptions = RecordNotFoundException.class, expectedExceptionsMessageRegExp = ""
			+ "Record not found for recordType: person and recordId: someRecordId")
	public void testRecordNotFound() {
		httpHandlerFactory.responseCode = 404;
		synchronizer.synchronize("person", "someRecordId");
	}

	// credentials? only for update?
	@Test
	public void testSychronizeRecordFactoredHttpHandler() {
		synchronizer.synchronize("person", "someRecordId");
		HttpHandlerSpy httpHandler = httpHandlerFactory.factoredHttpHandlerSpy;
		assertEquals(httpHandlerFactory.url,
				baseURL + "objects/" + "someRecordId" + "/datastreams/METADATA/content");
		assertEquals(httpHandler.requestMethod, "GET");
		assertTrue(httpHandler.getResponseCodeWasCalled);
	}

	@Test
	public void testFactoredFedoraToCoraConverter() {
		synchronizer.synchronize("person", "someRecordId");
		assertEquals(fedoraConverterFactory.type, "person");
	}

	@Test
	public void testSyncronizeRecordResultHandledCorrectly() {
		synchronizer.synchronize("person", "someRecordId");
		HttpHandlerSpy factoredHttpHandler = httpHandlerFactory.factoredHttpHandlerSpy;
		FedoraToCoraConverterSpy factoredFedoraConverter = fedoraConverterFactory.factoredFedoraConverter;
		assertEquals(factoredFedoraConverter.xml, factoredHttpHandler.responseText);

		assertSame(factoredFedoraConverter.factoredGroup, dbStorage.createDataGroups.get(0));
		assertEquals(dbStorage.createRecordTypes.get(0), "person");
		assertEquals(dbStorage.createRecordIds.get(0), "someRecordId");
	}

}
