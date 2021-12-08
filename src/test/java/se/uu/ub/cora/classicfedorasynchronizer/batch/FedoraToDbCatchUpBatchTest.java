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
package se.uu.ub.cora.classicfedorasynchronizer.batch;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static se.uu.ub.cora.classicfedorasynchronizer.batch.FedoraToDbCatchUpBatch.fedoraReaderFactoryClassName;
import static se.uu.ub.cora.classicfedorasynchronizer.batch.FedoraToDbCatchUpBatch.synchronizerFactoryClassName;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.classicfedorasynchronizer.ClassicCoraSynchronizerFactorySpy;
import se.uu.ub.cora.classicfedorasynchronizer.ClassicCoraSynchronizerSpy;
import se.uu.ub.cora.classicfedorasynchronizer.log.LoggerFactorySpy;
import se.uu.ub.cora.logger.LoggerProvider;

public class FedoraToDbCatchUpBatchTest {

	private LoggerFactorySpy loggerFactorySpy = new LoggerFactorySpy();
	private String testedClassName = "FedoraToDbCatchUpBatch";

	private String[] args;

	@BeforeMethod
	public void setUp() {
		args = new String[] { "args-someDatabaseUrl", "args-dbUserName", "args-dbUserPassword",
				"args-someFedoraBaseUrl", "args-someApptokenVerifierUrl", "args-someCoraBaseUrl",
				"args-someCoraUserId", "args-someCoraApptoken", "2021-10-10T10:10:10Z" };
		loggerFactorySpy.resetLogs(testedClassName);
		LoggerProvider.setLoggerFactory(loggerFactorySpy);
	}

	@Test
	public void testConstructorIsPrivate() throws Exception {
		Constructor<FedoraToDbCatchUpBatch> constructor = FedoraToDbCatchUpBatch.class
				.getDeclaredConstructor();
		assertFalse(constructorIsPublic(constructor));
		constructor.setAccessible(true);
		constructor.newInstance();
	}

	private boolean constructorIsPublic(Constructor<FedoraToDbCatchUpBatch> constructor) {
		return Modifier.isPublic(constructor.getModifiers());
	}

	@Test
	public void A_testDefaultFactoryClassNamesRunsFirst() {
		// TODO: Think about a better way to run this test before tests that change classnames

		assertEquals(synchronizerFactoryClassName,
				"se.uu.ub.cora.classicfedorasynchronizer.internal.SynchronizerFactory");
		assertEquals(fedoraReaderFactoryClassName,
				"se.uu.ub.cora.fedora.reader.FedoraReaderFactoryImp");
	}

	@Test
	public void testMainMethod() throws Exception {
		setFactoryClassNamesToSpies();
		FedoraToDbCatchUpBatch.main(args);

		assertTrue(
				FedoraToDbCatchUpBatch.synchronizerFactory instanceof ClassicCoraSynchronizerFactorySpy);
		assertTrue(FedoraToDbCatchUpBatch.fedoraReaderFactory instanceof FedoraReaderFactorySpy);
		assertEquals(getNoOfFatalLogs(), 0);
		assertEquals(getInfoLogNo(0), "FedoraToDbCatchUpBatch starting...");
		assertEquals(getInfoLogNo(1), "FedoraToDbCatchUpBatch started");
		assertTrue(getInfoLogNo(2).startsWith("Batch started at: "));
		assertTrue(getInfoLogNo(2).endsWith("Z"));
		assertEquals(getInfoLogNo(3),
				"Fetching created records after timestamp: 2021-10-10T10:10:10Z");
		assertEquals(getInfoLogNo(4), "Synchronizing 3 records");
		assertEquals(getInfoLogNo(5), "Synchronizing(1/3) recordId: auhority-person:104");
		assertEquals(getInfoLogNo(6), "Synchronizing(2/3) recordId: auhority-person:22");
		assertEquals(getInfoLogNo(7), "Synchronizing(3/3) recordId: auhority-person:2131");
		assertEquals(getInfoLogNo(8), "Synchronizing done");
		assertEquals(getInfoLogNo(9),
				"Fetching updated records after timestamp: 2021-10-10T10:10:10Z");
		assertEquals(getInfoLogNo(10), "Synchronizing 5 records");
		assertEquals(getInfoLogNo(11), "Synchronizing(1/5) recordId: auhority-person:104");
		assertEquals(getInfoLogNo(12), "Synchronizing(2/5) recordId: auhority-person:22");
		assertEquals(getInfoLogNo(13), "Synchronizing(3/5) recordId: auhority-person:2131");
		assertEquals(getInfoLogNo(14), "Synchronizing(4/5) recordId: auhority-person:245");
		assertEquals(getInfoLogNo(15), "Synchronizing(5/5) recordId: auhority-person:322");
		assertEquals(getInfoLogNo(16), "Synchronizing done");
		assertEquals(getInfoLogNo(17),
				"Fetching deleted records after timestamp: 2021-10-10T10:10:10Z");
		assertEquals(getInfoLogNo(18), "Synchronizing 2 records");
		assertEquals(getInfoLogNo(19), "Synchronizing(1/2) recordId: auhority-person:127");
		assertEquals(getInfoLogNo(20), "Synchronizing(2/2) recordId: auhority-person:1211");
		assertEquals(getInfoLogNo(21), "Synchronizing done");
		assertEquals(getInfoLogNo(22), "Start indexing for all persons");
		assertEquals(getInfoLogNo(23), "Start indexing for all personDomainParts");
		assertEquals(getInfoLogNo(24), "See API for status of batchJobs");
	}

	private void setFactoryClassNamesToSpies() {
		synchronizerFactoryClassName = "se.uu.ub.cora.classicfedorasynchronizer.ClassicCoraSynchronizerFactorySpy";
		fedoraReaderFactoryClassName = "se.uu.ub.cora.classicfedorasynchronizer.batch.FedoraReaderFactorySpy";
	}

	private Object getNoOfFatalLogs() {
		return loggerFactorySpy.getNoOfFatalLogMessagesUsingClassName(testedClassName);
	}

	private int getNoOfInfoLogs() {
		return loggerFactorySpy.getNoOfInfoLogMessagesUsingClassname(testedClassName);
	}

	private String getInfoLogNo(int messageNo) {
		return loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, messageNo);
	}

	@Test
	public void testMainMethodUsingArgs() throws Exception {
		setFactoryClassNamesToSpies();

		FedoraToDbCatchUpBatch.main(args);

		assertCorrectInitInfoInSynchronizerUsingPrefix("args-");

		assertTrue(FedoraToDbCatchUpBatch.fedoraReaderFactory instanceof FedoraReaderFactorySpy);
	}

	private void assertCorrectInitInfoInSynchronizerUsingPrefix(String prefix) {
		ClassicCoraSynchronizerFactorySpy synchronizerFactory = (ClassicCoraSynchronizerFactorySpy) FedoraToDbCatchUpBatch.synchronizerFactory;
		Map<String, String> initInfo = synchronizerFactory.initInfo;
		assertEquals(initInfo.get("databaseUrl"), prefix + "someDatabaseUrl");
		assertEquals(initInfo.get("databaseUser"), prefix + "dbUserName");
		assertEquals(initInfo.get("databasePassword"), prefix + "dbUserPassword");
		assertEquals(initInfo.get("fedoraBaseUrl"), prefix + "someFedoraBaseUrl");
		assertEquals(initInfo.get("coraApptokenVerifierURL"), prefix + "someApptokenVerifierUrl");
		assertEquals(initInfo.get("coraBaseUrl"), prefix + "someCoraBaseUrl");
		assertEquals(initInfo.get("coraUserId"), prefix + "someCoraUserId");
		assertEquals(initInfo.get("coraApptoken"), prefix + "someCoraApptoken");
		assertEquals(initInfo.get("afterTimestamp"), "2021-10-10T10:10:10Z");
	}

	@Test
	public void testCreatedAfter() throws Exception {
		setFactoryClassNamesToSpies();

		FedoraToDbCatchUpBatch.main(args);
		ClassicCoraSynchronizerFactorySpy synchronizerFactory = (ClassicCoraSynchronizerFactorySpy) FedoraToDbCatchUpBatch.synchronizerFactory;
		ClassicCoraSynchronizerSpy synchronizer = (ClassicCoraSynchronizerSpy) synchronizerFactory.MCR
				.getReturnValue("factorForBatch", 0);

		FedoraReaderFactorySpy fedoraReaderFactory = (FedoraReaderFactorySpy) FedoraToDbCatchUpBatch.fedoraReaderFactory;
		assertEquals(fedoraReaderFactory.baseUrl, "args-someFedoraBaseUrl");
		FedoraReaderSpy factoredFedoraReader = fedoraReaderFactory.factoredFedoraReader;

		factoredFedoraReader.MCR.assertParameters("readPidsForTypeCreatedAfter", 0,
				"authority-person", "2021-10-10T10:10:10Z");

		List<String> listToReturn = (List<String>) factoredFedoraReader.MCR
				.getReturnValue("readPidsForTypeCreatedAfter", 0);
		assertEquals(listToReturn.size(), 3);
		assertCorrectCallToSynchronizer(synchronizer, 0, listToReturn.get(0));
		assertCorrectCallToSynchronizer(synchronizer, 1, listToReturn.get(1));
		assertCorrectCallToSynchronizer(synchronizer, 2, listToReturn.get(2));
	}

	private void assertCorrectCallToSynchronizer(ClassicCoraSynchronizerSpy synchronizer,
			int callNumber, String recordId) {
		synchronizer.MCR.assertParameters("synchronizeCreated", callNumber, "person", recordId,
				"diva");
	}

	@Test
	public void testMainMethodUsingDefaultFileWhenNoArgumentsAreGiven() throws Exception {
		args = new String[] {};
		setFactoryClassNamesToSpies();

		FedoraToDbCatchUpBatch.main(args);

		assertCorrectInitInfoInSynchronizerUsingPrefix("");
	}

	@Test
	public void testMainMethodUsingFileName() throws Exception {
		String argsWithFileName[] = new String[] { "fedoraToDbCatchUpSentIn.properties" };
		setFactoryClassNamesToSpies();

		FedoraToDbCatchUpBatch.main(argsWithFileName);

		assertCorrectInitInfoInSynchronizerUsingPrefix("fileSentIn-");
	}

	@Test
	public void testMainMethodWrongNumberOfArguments() throws Exception {
		args = new String[] { "arg1", "arg2" };
		setFactoryClassNamesToSpies();

		FedoraToDbCatchUpBatch.main(args);

		assertEquals(getInfoLogNo(0), "FedoraToDbCatchUpBatch starting...");
		assertEquals(getNoOfInfoLogs(), 1);
		assertEquals(getFatalLogNo(0),
				"Error running FedoraToDbCatchUpBatch: Number of arguments should be 9.");

	}

	private String getFatalLogNo(int messageNo) {
		return loggerFactorySpy.getFatalLogMessageUsingClassNameAndNo(testedClassName, messageNo);
	}

	@Test
	public void testMainMethodMissingParameter() throws Exception {
		args = new String[] { "propertiesForTestingMissingDatabasePassword.properties" };
		setFactoryClassNamesToSpies();

		FedoraToDbCatchUpBatch.main(args);

		assertEquals(getInfoLogNo(0), "FedoraToDbCatchUpBatch starting...");
		assertEquals(getNoOfInfoLogs(), 1);
		assertEquals(getFatalLogNo(0),
				"Error running FedoraToDbCatchUpBatch: Property with name database.url not found in properties");
		Exception exception = getFatalExceptionNo(0);
		assertEquals(exception.getMessage(),
				"Property with name database.url not found in properties");
	}

	private Exception getFatalExceptionNo(int exceptionNo) {
		return loggerFactorySpy.getFatalExceptionUsingClassNameAndNo(testedClassName, exceptionNo);
	}

	@Test
	public void testMainMethodWrongClassNameForSynchronizerFactory() throws Exception {
		synchronizerFactoryClassName = "se.uu.ub.cora.classicfedorasynchronizer.NOTClassicCoraSynchronizerFactorySpy";
		fedoraReaderFactoryClassName = "se.uu.ub.cora.classicfedorasynchronizer.batch.FedoraReaderFactorySpy";

		FedoraToDbCatchUpBatch.main(args);
		assertEquals(getInfoLogNo(0), "FedoraToDbCatchUpBatch starting...");
		assertEquals(getNoOfInfoLogs(), 1);
		assertEquals(getFatalLogNo(0),
				"Error running FedoraToDbCatchUpBatch: se.uu.ub.cora.classicfedorasynchronizer.NOTClassicCoraSynchronizerFactorySpy");

	}

	@Test
	public void testMainMethodWrongClassNameForFedoraReaderFactory() throws Exception {
		synchronizerFactoryClassName = "se.uu.ub.cora.classicfedorasynchronizer.ClassicCoraSynchronizerFactorySpy";
		fedoraReaderFactoryClassName = "se.uu.ub.cora.classicfedorasynchronizer.batch.NOTFedoraReaderFactorySpy";

		FedoraToDbCatchUpBatch.main(args);
		assertEquals(getInfoLogNo(0), "FedoraToDbCatchUpBatch starting...");
		assertEquals(getNoOfInfoLogs(), 1);
		assertEquals(getFatalLogNo(0),
				"Error running FedoraToDbCatchUpBatch: se.uu.ub.cora.classicfedorasynchronizer.batch.NOTFedoraReaderFactorySpy");

	}

	@Test
	public void testErrorWhileSynchronizingShouldContinueToNextOne() throws Exception {

		setFactoryClassNamesToSpies();
		synchronizerFactoryClassName = "se.uu.ub.cora.classicfedorasynchronizer.ClassicCoraSynchronizerFactoryThrowExceptionSpy";

		FedoraToDbCatchUpBatch.main(args);
		int exceptionNo = 0;
		assertEquals(getErrorLogNo(exceptionNo),
				"Error synchronizing recordId: auhority-person:104");
		Exception exception = getErrorExceptionNo(exceptionNo);
		assertEquals(exception.getMessage(), "Record not found error from spy");

		int noOfRecordsInFakeFedora = 8;
		int noOfRecordsDeleteddAfterInFakeFedora = 2;

		int noOfRecords = noOfRecordsInFakeFedora + noOfRecordsDeleteddAfterInFakeFedora;

		assertEquals(getNoOfErrorLogs(), noOfRecords);
	}

	private Exception getErrorExceptionNo(int exceptionNo) {
		return loggerFactorySpy.getErrorExceptionUsingClassNameAndNo(testedClassName, exceptionNo);
	}

	private int getNoOfErrorLogs() {
		return loggerFactorySpy.getNoOfErrorLogMessagesUsingClassName(testedClassName);
	}

	private String getErrorLogNo(int messageNo) {
		return loggerFactorySpy.getErrorLogMessageUsingClassNameAndNo(testedClassName, messageNo);
	}

	private FedoraReaderSpy getFedoraReader() {
		FedoraReaderFactorySpy fedoraReaderFactory = (FedoraReaderFactorySpy) FedoraToDbCatchUpBatch.fedoraReaderFactory;
		FedoraReaderSpy fedoraReader = (FedoraReaderSpy) fedoraReaderFactory.MCR
				.getReturnValue("factor", 0);
		return fedoraReader;
	}

	@Test
	public void testUpdatedAfter() throws Exception {
		setFactoryClassNamesToSpies();

		FedoraToDbCatchUpBatch.main(args);

		ClassicCoraSynchronizerSpy synchronizer = getSynchronizerSpy();
		FedoraReaderSpy fedoraReader = getFedoraReader();

		fedoraReader.MCR.assertParameters("readPidsForTypeUpdatedAfter", 0, "authority-person",
				"2021-10-10T10:10:10Z");

		List<String> listToReturn = (List<String>) fedoraReader.MCR
				.getReturnValue("readPidsForTypeUpdatedAfter", 0);

		synchronizer.MCR.assertParameters("synchronizeUpdated", 0, "person", listToReturn.get(0),
				"diva");
		synchronizer.MCR.assertParameters("synchronizeUpdated", 1, "person", listToReturn.get(1),
				"diva");
		synchronizer.MCR.assertParameters("synchronizeUpdated", 2, "person", listToReturn.get(2),
				"diva");
		synchronizer.MCR.assertParameters("synchronizeUpdated", 3, "person", listToReturn.get(3),
				"diva");
		synchronizer.MCR.assertParameters("synchronizeUpdated", 4, "person", listToReturn.get(4),
				"diva");

		synchronizer.MCR.assertNumberOfCallsToMethod("synchronizeDeleted", 2);
		synchronizer.MCR.assertNumberOfCallsToMethod("synchronizeCreated", 3);
	}

	@Test
	public void testDeletedAfter() throws Exception {
		setFactoryClassNamesToSpies();

		FedoraToDbCatchUpBatch.main(args);

		ClassicCoraSynchronizerSpy synchronizer = getSynchronizerSpy();
		FedoraReaderSpy fedoraReader = getFedoraReader();

		fedoraReader.MCR.assertParameters("readPidsForTypeDeletedAfter", 0, "authority-person",
				"2021-10-10T10:10:10Z");

		List<String> listToReturn = (List<String>) fedoraReader.MCR
				.getReturnValue("readPidsForTypeDeletedAfter", 0);

		synchronizer.MCR.assertParameters("synchronizeDeleted", 0, "person", listToReturn.get(0),
				"diva");
		synchronizer.MCR.assertParameters("synchronizeDeleted", 1, "person", listToReturn.get(1),
				"diva");

		synchronizer.MCR.assertNumberOfCallsToMethod("synchronizeCreated", 3);
		synchronizer.MCR.assertNumberOfCallsToMethod("synchronizeUpdated", 5);
		synchronizer.MCR.assertNumberOfCallsToMethod("synchronizeDeleted", 2);
	}

	private ClassicCoraSynchronizerSpy getSynchronizerSpy() {
		ClassicCoraSynchronizerFactorySpy synchronizerFactory = (ClassicCoraSynchronizerFactorySpy) FedoraToDbCatchUpBatch.synchronizerFactory;
		return (ClassicCoraSynchronizerSpy) synchronizerFactory.MCR.getReturnValue("factorForBatch",
				0);
	}

	@Test
	public void testStartIndexBatchJobForPerson() throws Exception {
		setFactoryClassNamesToSpies();

		FedoraToDbCatchUpBatch.main(args);

		ClassicCoraSynchronizerSpy synchronizer = getSynchronizerSpy();

		synchronizer.MCR.assertParameters("indexAllRecordsForType", 0, "person");

	}

	@Test
	public void testStartIndexBatchJobForPersonDomainPart() throws Exception {
		setFactoryClassNamesToSpies();

		FedoraToDbCatchUpBatch.main(args);

		ClassicCoraSynchronizerSpy synchronizer = getSynchronizerSpy();

		synchronizer.MCR.assertParameters("indexAllRecordsForType", 1, "personDomainPart");
	}
}