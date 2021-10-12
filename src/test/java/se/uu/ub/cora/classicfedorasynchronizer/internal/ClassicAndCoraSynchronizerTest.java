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

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.data.DataGroupProvider;
import se.uu.ub.cora.storage.RecordNotFoundException;

public class ClassicAndCoraSynchronizerTest {

	private HttpHandlerFactorySpy httpHandlerFactory;
	private ClassicCoraSynchronizerImp synchronizer;
	private String baseURL;
	private FedoraConverterFactorySpy fedoraConverterFactory;
	private RecordStorageSpy dbStorage;
	private String dataDivider = "diva";
	private DataGroupFactorySpy dataGroupFactory;

	@BeforeMethod
	public void setUp() {
		baseURL = "someBaseUrl";
		httpHandlerFactory = new HttpHandlerFactorySpy();
		fedoraConverterFactory = new FedoraConverterFactorySpy();
		dbStorage = new RecordStorageSpy();
		dataGroupFactory = new DataGroupFactorySpy();
		DataGroupProvider.setDataGroupFactory(dataGroupFactory);
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
		synchronizer.synchronize("person", "someRecordId", "create", dataDivider);
	}

	@Test
	public void testSychronizeRecordFactoredHttpHandler() {
		synchronizer.synchronize("person", "someRecordId", "create", dataDivider);
		HttpHandlerSpy httpHandler = httpHandlerFactory.factoredHttpHandlerSpy;
		assertEquals(httpHandlerFactory.url,
				baseURL + "objects/" + "someRecordId" + "/datastreams/METADATA/content");
		assertEquals(httpHandler.requestMethod, "GET");
		assertTrue(httpHandler.getResponseCodeWasCalled);
	}

	@Test
	public void testFactoredFedoraToCoraConverter() {
		synchronizer.synchronize("person", "someRecordId", "create", dataDivider);
		assertEquals(fedoraConverterFactory.types.get(0), "person");
	}

	@Test
	public void testSyncronizeRecordResultHandledCorrectlyForCreate() {
		synchronizer.synchronize("person", "someRecordId", "create", dataDivider);

		HttpHandlerSpy factoredHttpHandler = httpHandlerFactory.factoredHttpHandlerSpy;
		FedoraToCoraConverterSpy factoredFedoraConverter = fedoraConverterFactory.factoredFedoraConverters
				.get(0);
		assertCorrectCommonResultHandledCorrectly(factoredHttpHandler, factoredFedoraConverter);
		assertEquals(dbStorage.methodName, "create");
	}

	private void assertCorrectCommonResultHandledCorrectly(HttpHandlerSpy factoredHttpHandler,
			FedoraToCoraConverterSpy factoredFedoraConverter) {
		assertEquals(factoredFedoraConverter.xml, factoredHttpHandler.responseText);

		assertSame(factoredFedoraConverter.convertedGroup, dbStorage.handledDataGroups.get(0));
		assertEquals(dbStorage.recordTypes.get(0), "person");
		assertEquals(dbStorage.recordIds.get(0), "someRecordId");
		assertEquals(dbStorage.dataDividers.get(0), "diva");

		assertCorrectlyFactoredAndUsedDataGroups();

	}

	private void assertCorrectlyFactoredAndUsedDataGroups() {
		assertEquals(dataGroupFactory.nameInDatas.size(), 2);
		assertEquals(dataGroupFactory.nameInDatas.get(0), "collectedData");
		assertSame(dbStorage.collectedDataDataGroups.get(0),
				dataGroupFactory.factoredDataGroups.get(0));
		assertEquals(dataGroupFactory.nameInDatas.get(1), "collectedDataLinks");
		assertSame(dbStorage.linkListDataGroups.get(0), dataGroupFactory.factoredDataGroups.get(1));
	}

	@Test
	public void testSynchronizeRecordResultHandledCorrectlyForUpdate() {
		synchronizer.synchronize("person", "someRecordId", "update", dataDivider);

		HttpHandlerSpy factoredHttpHandler = httpHandlerFactory.factoredHttpHandlerSpy;
		FedoraToCoraConverterSpy factoredFedoraConverter = fedoraConverterFactory.factoredFedoraConverters
				.get(0);
		assertCorrectCommonResultHandledCorrectly(factoredHttpHandler, factoredFedoraConverter);
		assertEquals(dbStorage.methodName, "update");
	}

	@Test
	public void testFactoredFedoraToCoraConverterUpdateWhenDomainParts() {
		setUpPersonWithDomainParts();
		synchronizer.synchronize("person", "someRecordId", "update", dataDivider);
		assertEquals(fedoraConverterFactory.types.get(0), "person");
		assertEquals(fedoraConverterFactory.types.get(1), "personDomainPart");
		assertEquals(fedoraConverterFactory.types.get(2), "personDomainPart");
		assertEquals(fedoraConverterFactory.types.get(3), "personDomainPart");

		String responseText = httpHandlerFactory.factoredHttpHandlerSpy.responseText;

		assertEquals(fedoraConverterFactory.factoredFedoraConverters.get(1).xml, responseText);
		assertEquals(fedoraConverterFactory.factoredFedoraConverters.get(2).xml, responseText);
		assertEquals(fedoraConverterFactory.factoredFedoraConverters.get(3).xml, responseText);
	}

	@Test
	public void testConvertedDataGroupSentToUpdateWhenDomainParts() {
		setUpPersonWithDomainParts();
		synchronizer.synchronize("person", "someRecordId", "update", dataDivider);
		List<FedoraToCoraConverterSpy> factoredFedoraConverters = fedoraConverterFactory.factoredFedoraConverters;
		assertSame(factoredFedoraConverters.get(1).convertedGroup,
				dbStorage.handledDataGroups.get(1));
		assertSame(factoredFedoraConverters.get(2).convertedGroup,
				dbStorage.handledDataGroups.get(2));
		assertSame(factoredFedoraConverters.get(3).convertedGroup,
				dbStorage.handledDataGroups.get(3));

	}

	@Test
	public void testDbCallsUpdateWhenDomainParts() {
		setUpPersonWithDomainParts();
		synchronizer.synchronize("person", "someRecordId", "update", dataDivider);
		assertEquals(dbStorage.recordTypes.get(0), "person");
		assertEquals(dbStorage.recordIds.get(0), "someRecordId");
		assertEquals(dbStorage.dataDividers.get(0), "diva");

		assertEquals(dbStorage.recordTypes.get(1), "personDomainPart");
		assertEquals(dbStorage.recordIds.get(1), "authority-person:0:test");
		assertEquals(dbStorage.dataDividers.get(1), "diva");

		assertEquals(dbStorage.recordTypes.get(2), "personDomainPart");
		assertEquals(dbStorage.recordIds.get(2), "authority-person:1:test");
		assertEquals(dbStorage.dataDividers.get(2), "diva");

	}

	// TODO: kolla att persondomainparts som kommer tillbaka från converter skickas till db

	@Test
	public void testSynchronizeRecordResultHandledCorrectlyForUpdateWithDomainPart() {
		setUpPersonWithDomainParts();
		synchronizer.synchronize("person", "someRecordId", "update", dataDivider);

		HttpHandlerSpy factoredHttpHandler = httpHandlerFactory.factoredHttpHandlerSpy;
		FedoraToCoraConverterSpy factoredFedoraConverter = fedoraConverterFactory.factoredFedoraConverters
				.get(0);

		assertCorrectCommonResultHandledCorrectly(factoredHttpHandler, factoredFedoraConverter);
		assertEquals(dbStorage.methodName, "update");

	}

	private void setUpPersonWithDomainParts() {
		List<DataGroupSpy> dataGroups = new ArrayList<>();
		DataGroupSpy person = new DataGroupSpy("person");
		person.numberOfDomainParts = 3;
		dataGroups.add(person);
		dataGroups.add(new DataGroupSpy("personDomainPart"));
		dataGroups.add(new DataGroupSpy("personDomainPart"));
		dataGroups.add(new DataGroupSpy("personDomainPart"));
		fedoraConverterFactory.convertedGroups = dataGroups;
	}

}
