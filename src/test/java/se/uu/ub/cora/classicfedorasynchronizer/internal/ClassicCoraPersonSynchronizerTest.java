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
		assertEquals(dbStorage.alteredRecordTypes.get(0), "person");
		assertEquals(dbStorage.alteredRecordIds.get(0), "someRecordId");
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

		dbStorage.MCR.assertParameters("read", 0, "person", "someRecordId");
		dbStorage.MCR.assertMethodNotCalled("deleteByTypeAndId");

		assertEquals(coraIndexer.recordTypes.size(), 1);
		assertCorrectIndexCallForPerson();
		assertEquals(loggerFactorySpy.getNoOfErrorLogMessagesUsingClassName(testedClassName), 0);
	}

	@Test
	public void testUpdateWithLessDomainPartsThanStored() throws Exception {
		setUpFedoraPersonConverterWithDomainParts(2);
		setUpPersonInDbWithDomainParts(4);
		synchronizer.synchronize("person", "someRecordId", "update", dataDivider);

		dbStorage.MCR.assertParameters("deleteByTypeAndId", 0, "personDomainPart",
				"authority-person:2:kth2");
		dbStorage.MCR.assertParameters("deleteByTypeAndId", 1, "personDomainPart",
				"authority-person:3:kth3");
		dbStorage.MCR.assertNumberOfCallsToMethod("deleteByTypeAndId", 2);
		coraIndexer.MCR.assertParameters("handleWorkorderType", 3, "removeFromIndex",
				"personDomainPart", "authority-person:2:kth2");
		coraIndexer.MCR.assertParameters("handleWorkorderType", 4, "removeFromIndex",
				"personDomainPart", "authority-person:3:kth3");
	}

	@Test
	public void testUpdateWithMoreDomainPartsThanStored() throws Exception {
		setUpFedoraPersonConverterWithDomainParts(4);
		setUpPersonInDbWithDomainParts(2);
		synchronizer.synchronize("person", "someRecordId", "update", dataDivider);

		dbStorage.MCR.assertMethodNotCalled("deleteByTypeAndId");
		coraIndexer.MCR.assertParameters("handleWorkorderType", 3, "index", "personDomainPart",
				"authority-person:2:kth2");
		coraIndexer.MCR.assertParameters("handleWorkorderType", 4, "index", "personDomainPart",
				"authority-person:3:kth3");
	}

	private void assertCorrectIndexCallForPerson() {
		coraIndexer.MCR.assertParameters("handleWorkorderType", 0, "index", "person",
				"someRecordId");

	}

	@Test
	public void testSynchronizeIndexCalledCorrectlyForCreateNoPersonDomains() {
		synchronizer.synchronize("person", "someRecordId", "create", dataDivider);
		coraIndexer.MCR.assertNumberOfCallsToMethod("handleWorkorderType", 1);
		assertCorrectIndexCallForPerson();
		assertEquals(loggerFactorySpy.getNoOfErrorLogMessagesUsingClassName(testedClassName), 0);
	}

	@Test
	public void testSynchronizeIndexCalledCorrectlyForCreateSeveralPersonDomains() {
		setUpFedoraPersonConverterWithDomainParts(3);
		synchronizer.synchronize("person", "someRecordId", "create", dataDivider);

		coraIndexer.MCR.assertNumberOfCallsToMethod("handleWorkorderType", 4);
		assertCorrectIndexCallForPerson();
		assertEquals(loggerFactorySpy.getNoOfErrorLogMessagesUsingClassName(testedClassName), 0);
		assertCorrectIndexedDomainParts();
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

	@Test
	public void testSychronizeRecordReadFromDataBaseForDelete() {
		synchronizer.synchronize("person", "someRecordId", "delete", dataDivider);

		assertEquals(dbStorage.readRecordTypes.get(0), "person");
		assertEquals(dbStorage.readRecordIds.get(0), "someRecordId");

	}

	@Test
	public void testSynchronizeRecordResultHandledCorrectlyForDelete() {
		synchronizer.synchronize("person", "someRecordId", "delete", dataDivider);

		assertEquals(fedoraConverterFactory.factoredFedoraConverters.size(), 0);

		dbStorage.MCR.assertParameters("deleteByTypeAndId", 0, "person", "someRecordId");
	}

	@Test
	public void testSynchronizeIndexCalledCorrectlyForDelete() {
		synchronizer.synchronize("person", "someRecordId", "delete", dataDivider);

		assertEquals(coraIndexer.recordTypes.size(), 1);
		assertEquals(coraIndexer.workOrderTypes.get(0), "removeFromIndex");
		assertEquals(coraIndexer.recordTypes.get(0), "person");
		assertEquals(coraIndexer.recordIds.get(0), "someRecordId");
		assertEquals(loggerFactorySpy.getNoOfErrorLogMessagesUsingClassName(testedClassName), 0);
	}

	@Test
	public void testErrorWhenIndexingForDelete() {
		coraIndexer.typesToThrowErrorFor.add("person");
		synchronizer.synchronize("person", "someRecordId", "delete", dataDivider);

		assertEquals(loggerFactorySpy.getErrorLogMessageUsingClassNameAndNo(testedClassName, 0),
				"Error when indexing record from synchronizer, delete person.");
	}

	/************************* with domainParts *******************************/
	@Test
	public void testFactoredFedoraToCoraConverterWhenDomainPartsForUpdate() {
		setUpFedoraPersonConverterWithDomainParts(3);
		synchronizer.synchronize("person", "someRecordId", "update", dataDivider);
		assertCorrectFactoredAndUsedConverters();
	}

	private void assertCorrectFactoredAndUsedConverters() {
		assertEquals(fedoraConverterFactory.types.get(0), "person");
		assertEquals(fedoraConverterFactory.types.get(1), "personDomainPart");
		assertEquals(fedoraConverterFactory.types.get(2), "personDomainPart");
		assertEquals(fedoraConverterFactory.types.get(3), "personDomainPart");

		String responseText = httpHandlerFactory.factoredHttpHandlerSpy.responseText;

		List<FedoraToCoraConverterSpy> factoredFedoraConverters = fedoraConverterFactory.factoredFedoraConverters;
		assertEquals(factoredFedoraConverters.get(1).xml, responseText);
		assertEquals(factoredFedoraConverters.get(1).parameters.get("domainFilter"), "kth0");
		assertEquals(factoredFedoraConverters.get(2).xml, responseText);
		assertEquals(factoredFedoraConverters.get(2).parameters.get("domainFilter"), "kth1");
		assertEquals(factoredFedoraConverters.get(3).xml, responseText);
		assertEquals(factoredFedoraConverters.get(3).parameters.get("domainFilter"), "kth2");

	}

	@Test
	public void testFactoredFedoraToCoraConverterWhenDomainPartsForCreate() {
		setUpFedoraPersonConverterWithDomainParts(3);
		synchronizer.synchronize("person", "someRecordId", "create", dataDivider);
		assertCorrectFactoredAndUsedConverters();
	}

	@Test
	public void testConvertedDataGroupSentToStorageWhenDomainPartsForUpdate() {
		setUpFedoraPersonConverterWithDomainParts(3);
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
		setUpFedoraPersonConverterWithDomainParts(3);
		synchronizer.synchronize("person", "someRecordId", "create", dataDivider);
		assertConvertedGroupsAreSentToStorage();

	}

	@Test
	public void testDbCallsWhenDomainPartsForUpdate() {
		setUpFedoraPersonConverterWithDomainParts(3);
		synchronizer.synchronize("person", "someRecordId", "update", dataDivider);
		assertCorrectDomainPartDataSentToStorage();
	}

	private void assertCorrectDomainPartDataSentToStorage() {
		assertEquals(dbStorage.alteredRecordTypes.get(0), "person");
		assertEquals(dbStorage.alteredRecordIds.get(0), "someRecordId");
		assertEquals(dbStorage.dataDividers.get(0), dataDivider);

		assertEquals(dbStorage.alteredRecordTypes.get(1), "personDomainPart");
		assertEquals(dbStorage.dataDividers.get(1), dataDivider);
		assertEquals(dbStorage.alteredRecordTypes.get(2), "personDomainPart");
		assertEquals(dbStorage.dataDividers.get(2), dataDivider);
		assertEquals(dbStorage.alteredRecordTypes.get(3), "personDomainPart");
		assertEquals(dbStorage.dataDividers.get(3), dataDivider);

		List<DataGroup> domainParts = dataGroupsReturnedFromConverter.get(0).groupChildrenToReturn;
		assertEquals(dbStorage.alteredRecordIds.get(1),
				((DataGroupSpy) domainParts.get(0)).recordId);
		assertEquals(dbStorage.alteredRecordIds.get(2),
				((DataGroupSpy) domainParts.get(1)).recordId);
		assertEquals(dbStorage.alteredRecordIds.get(3),
				((DataGroupSpy) domainParts.get(2)).recordId);
	}

	@Test
	public void testDbCallsWhenDomainPartsForCreate() {
		setUpFedoraPersonConverterWithDomainParts(3);
		synchronizer.synchronize("person", "someRecordId", "update", dataDivider);
		assertCorrectDomainPartDataSentToStorage();
	}

	@Test
	public void testDbCallsWhenDomainPartsForDelete() {
		setUpPersonInDbWithDomainParts(3);
		synchronizer.synchronize("person", "someRecordId", "delete", dataDivider);

		dbStorage.MCR.assertParameters("deleteByTypeAndId", 0, "personDomainPart",
				"authority-person:0:kth0");
		dbStorage.MCR.assertParameters("deleteByTypeAndId", 1, "personDomainPart",
				"authority-person:1:kth1");
		dbStorage.MCR.assertParameters("deleteByTypeAndId", 2, "personDomainPart",
				"authority-person:2:kth2");

		dbStorage.MCR.assertParameters("deleteByTypeAndId", 3, "person", "someRecordId");
	}

	private void setUpPersonInDbWithDomainParts(int noOfDomainParts) {
		DataGroupSpy personToReturnFromDb = new DataGroupSpy("person");
		personToReturnFromDb.numberOfDomainParts = noOfDomainParts;
		dbStorage.readDataGroup = personToReturnFromDb;
	}

	@Test
	public void testSynchronizeRecordResultHandledCorrectlyWhenDomainPartsForUpdate() {
		setUpFedoraPersonConverterWithDomainParts(3);
		synchronizer.synchronize("person", "someRecordId", "update", dataDivider);

		HttpHandlerSpy factoredHttpHandler = httpHandlerFactory.factoredHttpHandlerSpy;
		FedoraToCoraConverterSpy factoredFedoraConverter = fedoraConverterFactory.factoredFedoraConverters
				.get(0);

		assertCorrectCommonResultHandledCorrectly(factoredHttpHandler, factoredFedoraConverter, 8);
		assertEquals(dbStorage.methodName, "update");

	}

	private void setUpFedoraPersonConverterWithDomainParts(int noOfDomainParts) {
		dataGroupsReturnedFromConverter = new ArrayList<>();
		DataGroupSpy person = new DataGroupSpy("person");
		person.numberOfDomainParts = noOfDomainParts;
		dataGroupsReturnedFromConverter.add(person);
		for (int i = 0; i < noOfDomainParts; i++) {
			dataGroupsReturnedFromConverter.add(new DataGroupSpy("personDomainPart"));
		}
		fedoraConverterFactory.convertedGroups = dataGroupsReturnedFromConverter;
	}

	@Test
	public void testSynchronizeRecordResultHandledCorrectlyWhenDomainPartsForCreate() {
		setUpFedoraPersonConverterWithDomainParts(3);
		synchronizer.synchronize("person", "someRecordId", "create", dataDivider);

		HttpHandlerSpy factoredHttpHandler = httpHandlerFactory.factoredHttpHandlerSpy;
		FedoraToCoraConverterSpy factoredFedoraConverter = fedoraConverterFactory.factoredFedoraConverters
				.get(0);

		assertCorrectCommonResultHandledCorrectly(factoredHttpHandler, factoredFedoraConverter, 8);
		assertEquals(dbStorage.methodName, "create");

	}

	@Test
	public void testSynchronizeIndexCalledCorrectlyWhenDomainPartsForUpdate() {
		setUpFedoraPersonConverterWithDomainParts(3);
		synchronizer.synchronize("person", "someRecordId", "update", dataDivider);

		assertCorrectIndexCallForPerson();

		assertEquals(coraIndexer.recordTypes.size(), 4);
		assertCorrectIndexedDomainParts();
	}

	private void assertCorrectIndexedDomainParts() {
		coraIndexer.MCR.assertParameters("handleWorkorderType", 1, "index", "personDomainPart",
				"authority-person:0:kth0");
		coraIndexer.MCR.assertParameters("handleWorkorderType", 2, "index", "personDomainPart",
				"authority-person:1:kth1");
		coraIndexer.MCR.assertParameters("handleWorkorderType", 3, "index", "personDomainPart",
				"authority-person:2:kth2");
	}

	private void assertCorrectIndexedDomainPartUsingIndex(int index, String workOrderType) {
		assertEquals(coraIndexer.workOrderTypes.get(index), workOrderType);
		assertEquals(coraIndexer.recordTypes.get(index), "personDomainPart");
		assertEquals(coraIndexer.recordIds.get(index), dbStorage.alteredRecordIds.get(index));
	}

	@Test
	public void testSynchronizeIndexCalledCorrectlyWhenDomainPartsForCreate() {
		setUpFedoraPersonConverterWithDomainParts(3);
		synchronizer.synchronize("person", "someRecordId", "create", dataDivider);

		assertCorrectIndexCallForPerson();
		coraIndexer.MCR.assertNumberOfCallsToMethod("handleWorkorderType", 4);
	}

	@Test
	public void testErrorWhenIndexingDomainPartForUpdate() {
		setUpFedoraPersonConverterWithDomainParts(3);
		coraIndexer.typesToThrowErrorFor.add("personDomainPart");
		synchronizer.synchronize("person", "someRecordId", "update", dataDivider);

		assertEquals(loggerFactorySpy.getErrorLogMessageUsingClassNameAndNo(testedClassName, 0),
				"Error when indexing record from synchronizer, update personDomainPart.");
	}

	@Test
	public void testSynchronizeIndexCalledCorrectlyWhenDomainPartsForDelete() {
		setUpPersonInDbWithDomainParts(3);
		synchronizer.synchronize("person", "someRecordId", "delete", dataDivider);

		assertEquals(coraIndexer.workOrderTypes.get(3), "removeFromIndex");
		assertEquals(coraIndexer.recordTypes.get(3), "person");
		assertEquals(coraIndexer.recordIds.get(3), "someRecordId");
		assertEquals(coraIndexer.recordTypes.size(), 4);

		assertCorrectIndexedDomainPartUsingIndex(0, "removeFromIndex");
		assertCorrectIndexedDomainPartUsingIndex(1, "removeFromIndex");
		assertCorrectIndexedDomainPartUsingIndex(2, "removeFromIndex");
	}

	@Test
	public void testErrorWhenIndexingDomainPartForDelete() {
		setUpPersonInDbWithDomainParts(3);
		coraIndexer.typesToThrowErrorFor.add("personDomainPart");
		synchronizer.synchronize("person", "someRecordId", "delete", dataDivider);

		assertEquals(loggerFactorySpy.getErrorLogMessageUsingClassNameAndNo(testedClassName, 0),
				"Error when indexing record from synchronizer, delete personDomainPart.");
	}

}
