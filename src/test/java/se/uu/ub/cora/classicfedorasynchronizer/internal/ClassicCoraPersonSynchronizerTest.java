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

import se.uu.ub.cora.classicfedorasynchronizer.log.LoggerFactorySpy;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.data.DataGroupProvider;
import se.uu.ub.cora.logger.LoggerProvider;
import se.uu.ub.cora.storage.RecordNotFoundException;

public class ClassicCoraPersonSynchronizerTest {

	private LoggerFactorySpy loggerFactorySpy = new LoggerFactorySpy();

	private HttpHandlerFactorySpy httpHandlerFactory;
	private ClassicCoraPersonSynchronizer synchronizer;
	private String baseURL;
	private FedoraConverterFactorySpy fedoraConverterFactory;
	private RecordStorageSpy dbStorage;
	private String dataDivider = "diva";
	private DataGroupFactorySpy dataGroupFactory;
	private List<DataGroupSpy> dataGroupsReturnedFromConverter;
	private CoraIndexerSpy coraIndexer;
	private String testedClassName = "ClassicCoraPersonSynchronizer";

	@BeforeMethod
	public void setUp() {
		loggerFactorySpy.resetLogs(testedClassName);
		LoggerProvider.setLoggerFactory(loggerFactorySpy);

		baseURL = "someBaseUrl";
		httpHandlerFactory = new HttpHandlerFactorySpy();
		fedoraConverterFactory = new FedoraConverterFactorySpy();
		dbStorage = new RecordStorageSpy();
		dataGroupFactory = new DataGroupFactorySpy();
		DataGroupProvider.setDataGroupFactory(dataGroupFactory);
		coraIndexer = new CoraIndexerSpy();
		synchronizer = new ClassicCoraPersonSynchronizer(dbStorage, httpHandlerFactory,
				fedoraConverterFactory, coraIndexer, baseURL);

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
		assertCorrectCommonResultHandledCorrectly(factoredHttpHandler, factoredFedoraConverter, 2);
		assertEquals(dbStorage.methodName, "create");
	}

	private void assertCorrectCommonResultHandledCorrectly(HttpHandlerSpy factoredHttpHandler,
			FedoraToCoraConverterSpy factoredFedoraConverter, int numberOfFactoredDataGroups) {
		assertEquals(factoredFedoraConverter.xml, factoredHttpHandler.responseText);

		assertSame(factoredFedoraConverter.convertedGroup, dbStorage.handledDataGroups.get(0));
		assertEquals(dbStorage.recordTypes.get(0), "person");
		assertEquals(dbStorage.recordIds.get(0), "someRecordId");
		assertEquals(dbStorage.dataDividers.get(0), "diva");

		assertCorrectlyFactoredAndUsedDataGroups(numberOfFactoredDataGroups);

	}

	private void assertCorrectlyFactoredAndUsedDataGroups(int numberOfFactoredDataGroups) {
		assertEquals(dataGroupFactory.nameInDatas.size(), numberOfFactoredDataGroups);
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
		assertCorrectCommonResultHandledCorrectly(factoredHttpHandler, factoredFedoraConverter, 2);
		assertEquals(dbStorage.methodName, "update");
	}

	@Test
	public void testSynchronizeIndexCalledCorrectlyForUpdate() {
		synchronizer.synchronize("person", "someRecordId", "update", dataDivider);

		assertEquals(coraIndexer.recordTypes.size(), 1);
		assertCorrectIndexCallForPerson();
		assertEquals(loggerFactorySpy.getNoOfErrorLogMessagesUsingClassName(testedClassName), 0);
	}

	private void assertCorrectIndexCallForPerson() {
		assertEquals(coraIndexer.workOrderTypes.get(0), "index");
		assertEquals(coraIndexer.recordTypes.get(0), "person");
		assertEquals(coraIndexer.recordIds.get(0), "someRecordId");
	}

	@Test
	public void testSynchronizeIndexCalledCorrectlyForCreate() {
		synchronizer.synchronize("person", "someRecordId", "create", dataDivider);

		assertEquals(coraIndexer.recordTypes.size(), 1);
		assertCorrectIndexCallForPerson();
		assertEquals(loggerFactorySpy.getNoOfErrorLogMessagesUsingClassName(testedClassName), 0);
	}

	@Test
	public void testErrorWhenIndexingForUpdate() {
		coraIndexer.typesToThrowErrorFor.add("person");
		synchronizer.synchronize("person", "someRecordId", "update", dataDivider);

		assertEquals(loggerFactorySpy.getErrorLogMessageUsingClassNameAndNo(testedClassName, 0),
				"Error when indexing record from synchronizer, update person.");

	}

	@Test
	public void testErrorWhenIndexingForCreate() {
		coraIndexer.typesToThrowErrorFor.add("person");
		synchronizer.synchronize("person", "someRecordId", "create", dataDivider);

		assertEquals(loggerFactorySpy.getErrorLogMessageUsingClassNameAndNo(testedClassName, 0),
				"Error when indexing record from synchronizer, create person.");
	}

	/************************* with domainParts *******************************/
	@Test
	public void testFactoredFedoraToCoraConverterWhenDomainPartsForUpdate() {
		setUpPersonWithDomainParts();
		synchronizer.synchronize("person", "someRecordId", "update", dataDivider);
		assertCorrectFactoredAndUsedConverters();
	}

	private void assertCorrectFactoredAndUsedConverters() {
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
	public void testFactoredFedoraToCoraConverterWhenDomainPartsForCreate() {
		setUpPersonWithDomainParts();
		synchronizer.synchronize("person", "someRecordId", "create", dataDivider);
		assertCorrectFactoredAndUsedConverters();
	}

	@Test
	public void testConvertedDataGroupSentToStorageWhenDomainPartsForUpdate() {
		setUpPersonWithDomainParts();
		synchronizer.synchronize("person", "someRecordId", "update", dataDivider);
		assertConvertedGroupsAreSentToStorage();

	}

	private void assertConvertedGroupsAreSentToStorage() {
		List<FedoraToCoraConverterSpy> factoredFedoraConverters = fedoraConverterFactory.factoredFedoraConverters;
		assertSame(factoredFedoraConverters.get(1).convertedGroup,
				dbStorage.handledDataGroups.get(1));
		assertSame(factoredFedoraConverters.get(2).convertedGroup,
				dbStorage.handledDataGroups.get(2));
		assertSame(factoredFedoraConverters.get(3).convertedGroup,
				dbStorage.handledDataGroups.get(3));
	}

	@Test
	public void testConvertedDataGroupSentToStorageWhenDomainPartsForCreate() {
		setUpPersonWithDomainParts();
		synchronizer.synchronize("person", "someRecordId", "create", dataDivider);
		assertConvertedGroupsAreSentToStorage();

	}

	@Test
	public void testDbCallsWhenDomainPartsForUpdate() {
		setUpPersonWithDomainParts();
		synchronizer.synchronize("person", "someRecordId", "update", dataDivider);
		assertCorrectDomainPartDataSentToStorage();
	}

	private void assertCorrectDomainPartDataSentToStorage() {
		assertEquals(dbStorage.recordTypes.get(0), "person");
		assertEquals(dbStorage.recordIds.get(0), "someRecordId");
		assertEquals(dbStorage.dataDividers.get(0), dataDivider);

		assertEquals(dbStorage.recordTypes.get(1), "personDomainPart");
		assertEquals(dbStorage.dataDividers.get(1), dataDivider);
		assertEquals(dbStorage.recordTypes.get(2), "personDomainPart");
		assertEquals(dbStorage.dataDividers.get(2), dataDivider);
		assertEquals(dbStorage.recordTypes.get(3), "personDomainPart");
		assertEquals(dbStorage.dataDividers.get(3), dataDivider);

		List<DataGroup> domainParts = dataGroupsReturnedFromConverter.get(0).groupChildrenToReturn;
		assertEquals(dbStorage.recordIds.get(1), ((DataGroupSpy) domainParts.get(0)).recordId);
		assertEquals(dbStorage.recordIds.get(2), ((DataGroupSpy) domainParts.get(1)).recordId);
		assertEquals(dbStorage.recordIds.get(3), ((DataGroupSpy) domainParts.get(2)).recordId);
	}

	@Test
	public void testDbCallsWhenDomainPartsForCreate() {
		setUpPersonWithDomainParts();
		synchronizer.synchronize("person", "someRecordId", "update", dataDivider);
		assertCorrectDomainPartDataSentToStorage();
	}

	@Test
	public void testSynchronizeRecordResultHandledCorrectlyWhenDomainPartsForUpdate() {
		setUpPersonWithDomainParts();
		synchronizer.synchronize("person", "someRecordId", "update", dataDivider);

		HttpHandlerSpy factoredHttpHandler = httpHandlerFactory.factoredHttpHandlerSpy;
		FedoraToCoraConverterSpy factoredFedoraConverter = fedoraConverterFactory.factoredFedoraConverters
				.get(0);

		assertCorrectCommonResultHandledCorrectly(factoredHttpHandler, factoredFedoraConverter, 8);
		assertEquals(dbStorage.methodName, "update");

	}

	private void setUpPersonWithDomainParts() {
		dataGroupsReturnedFromConverter = new ArrayList<>();
		DataGroupSpy person = new DataGroupSpy("person");
		person.numberOfDomainParts = 3;
		dataGroupsReturnedFromConverter.add(person);
		dataGroupsReturnedFromConverter.add(new DataGroupSpy("personDomainPart"));
		dataGroupsReturnedFromConverter.add(new DataGroupSpy("personDomainPart"));
		dataGroupsReturnedFromConverter.add(new DataGroupSpy("personDomainPart"));
		fedoraConverterFactory.convertedGroups = dataGroupsReturnedFromConverter;
	}

	@Test
	public void testSynchronizeRecordResultHandledCorrectlyWhenDomainPartsForCreate() {
		setUpPersonWithDomainParts();
		synchronizer.synchronize("person", "someRecordId", "create", dataDivider);

		HttpHandlerSpy factoredHttpHandler = httpHandlerFactory.factoredHttpHandlerSpy;
		FedoraToCoraConverterSpy factoredFedoraConverter = fedoraConverterFactory.factoredFedoraConverters
				.get(0);

		assertCorrectCommonResultHandledCorrectly(factoredHttpHandler, factoredFedoraConverter, 8);
		assertEquals(dbStorage.methodName, "create");

	}

	@Test
	public void testSynchronizeIndexCalledCorrectlyWhenDomainPartsForUpdate() {
		setUpPersonWithDomainParts();
		synchronizer.synchronize("person", "someRecordId", "update", dataDivider);

		assertCorrectIndexCallForPerson();
		assertEquals(coraIndexer.recordTypes.size(), 4);
		assertCorrectIndexedDomainParts();
	}

	private void assertCorrectIndexedDomainParts() {
		assertCorrectIndexedDomainPartUsingIndex(1);
		assertCorrectIndexedDomainPartUsingIndex(2);
		assertCorrectIndexedDomainPartUsingIndex(3);
	}

	private void assertCorrectIndexedDomainPartUsingIndex(int index) {
		assertEquals(coraIndexer.workOrderTypes.get(index), "index");
		assertEquals(coraIndexer.recordTypes.get(index), "personDomainPart");
		assertEquals(coraIndexer.recordIds.get(index), dbStorage.recordIds.get(index));
	}

	@Test
	public void testSynchronizeIndexCalledCorrectlyWhenDomainPartsForCreate() {
		setUpPersonWithDomainParts();
		synchronizer.synchronize("person", "someRecordId", "create", dataDivider);

		assertCorrectIndexCallForPerson();
		assertEquals(coraIndexer.recordTypes.size(), 4);
		assertCorrectIndexedDomainParts();
	}

	@Test
	public void testErrorWhenIndexingDomainPartForUpdate() {
		setUpPersonWithDomainParts();
		coraIndexer.typesToThrowErrorFor.add("personDomainPart");
		synchronizer.synchronize("person", "someRecordId", "update", dataDivider);

		assertEquals(loggerFactorySpy.getErrorLogMessageUsingClassNameAndNo(testedClassName, 0),
				"Error when indexing record from synchronizer, update personDomainPart.");
	}

	@Test
	public void testErrorWhenIndexingDomainPartForCreate() {
		setUpPersonWithDomainParts();
		coraIndexer.typesToThrowErrorFor.add("personDomainPart");
		synchronizer.synchronize("person", "someRecordId", "create", dataDivider);

		assertEquals(loggerFactorySpy.getErrorLogMessageUsingClassNameAndNo(testedClassName, 0),
				"Error when indexing record from synchronizer, create personDomainPart.");
	}

}
