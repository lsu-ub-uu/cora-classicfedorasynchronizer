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
import static org.testng.Assert.assertTrue;

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

public class FedoraToDbBatchTest {

	private LoggerFactorySpy loggerFactorySpy = new LoggerFactorySpy();
	private String testedClassName = "FedoraToDbBatch";

	private String[] args;

	@BeforeMethod
	public void setUp() {
		args = new String[] { "args-someDatabaseUrl", "args-dbUserName", "args-dbUserPassword",
				"args-someFedoraBaseUrl", "args-someApptokenVerifierUrl", "args-someCoraBaseUrl",
				"args-someCoraUserId", "args-someCoraApptoken" };
		loggerFactorySpy.resetLogs(testedClassName);
		LoggerProvider.setLoggerFactory(loggerFactorySpy);
	}

	@Test
	public void testConstructorIsPrivate() throws Exception {
		Constructor<FedoraToDbBatch> constructor = FedoraToDbBatch.class.getDeclaredConstructor();
		assertTrue(Modifier.isPrivate(constructor.getModifiers()));
		constructor.setAccessible(true);
		constructor.newInstance();
	}

	@Test
	public void testDefaultFactoryClassNames() {
		assertEquals(FedoraToDbBatch.synchronizerFactoryClassName,
				"se.uu.ub.cora.classicfedorasynchronizer.internal.SynchronizerFactory");
		assertEquals(FedoraToDbBatch.fedoraReaderFactoryClassName,
				"se.uu.ub.cora.fedora.reader.FedoraReaderFactoryImp");
	}

	@Test
	public void testMainMethod() throws Exception {
		setFactoryClassNamesToSpies();
		FedoraToDbBatch.main(args);

		assertTrue(
				FedoraToDbBatch.synchronizerFactory instanceof ClassicCoraSynchronizerFactorySpy);
		assertTrue(FedoraToDbBatch.fedoraReaderFactory instanceof FedoraReaderFactorySpy);
		assertEquals(getNoOfFatalLogs(), 0);
		assertEquals(getInfoLogNo(0), "FedoraToDbBatch starting...");
		assertEquals(getInfoLogNo(1), "FedoraToDbBatch started");
		assertEquals(getInfoLogNo(2), "Fetching pids for person...");
		assertEquals(getInfoLogNo(3), "Fetched 5 pids");
		assertEquals(getInfoLogNo(4), "Synchronizing(1/5) recordId: auhority-person:245");
		assertEquals(getInfoLogNo(5), "Synchronizing(2/5) recordId: auhority-person:322");
		assertEquals(getInfoLogNo(6), "Synchronizing(3/5) recordId: auhority-person:4029");
		assertEquals(getInfoLogNo(7), "Synchronizing(4/5) recordId: auhority-person:127");
		assertEquals(getInfoLogNo(8), "Synchronizing(5/5) recordId: auhority-person:1211");
		assertEquals(getInfoLogNo(9), "FedoraToDbBatch done synchronizing the found pids");
		assertEquals(getInfoLogNo(10), "Looking for pids created after batch job started");
		// assertEquals(getInfoLogNo(9), "FedoraToDbBatch done synchronizing the found pids");
		// assertEquals(getNoOfInfoLogs(), 4);
	}

	private void setFactoryClassNamesToSpies() {
		FedoraToDbBatch.synchronizerFactoryClassName = "se.uu.ub.cora.classicfedorasynchronizer.ClassicCoraSynchronizerFactorySpy";
		FedoraToDbBatch.fedoraReaderFactoryClassName = "se.uu.ub.cora.classicfedorasynchronizer.batch.FedoraReaderFactorySpy";
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

		FedoraToDbBatch.main(args);

		assertCorrectInitInfoInSynchronizerUsingPrefix("args-");

		assertTrue(FedoraToDbBatch.fedoraReaderFactory instanceof FedoraReaderFactorySpy);
	}

	private void assertCorrectInitInfoInSynchronizerUsingPrefix(String prefix) {
		ClassicCoraSynchronizerFactorySpy synchronizerFactory = (ClassicCoraSynchronizerFactorySpy) FedoraToDbBatch.synchronizerFactory;
		Map<String, String> initInfo = synchronizerFactory.initInfo;
		assertEquals(initInfo.get("databaseUrl"), prefix + "someDatabaseUrl");
		assertEquals(initInfo.get("databaseUser"), prefix + "dbUserName");
		assertEquals(initInfo.get("databasePassword"), prefix + "dbUserPassword");
		assertEquals(initInfo.get("fedoraBaseUrl"), prefix + "someFedoraBaseUrl");
		assertEquals(initInfo.get("coraApptokenVerifierURL"), prefix + "someApptokenVerifierUrl");
		assertEquals(initInfo.get("coraBaseUrl"), prefix + "someCoraBaseUrl");
		assertEquals(initInfo.get("coraUserId"), prefix + "someCoraUserId");
		assertEquals(initInfo.get("coraApptoken"), prefix + "someCoraApptoken");
	}

	@Test
	public void testFactorSynchronizerUsingFactorySpy() throws Exception {
		setFactoryClassNamesToSpies();

		FedoraToDbBatch.main(args);
		ClassicCoraSynchronizerFactorySpy synchronizerFactory = (ClassicCoraSynchronizerFactorySpy) FedoraToDbBatch.synchronizerFactory;
		ClassicCoraSynchronizerSpy synchronizer = (ClassicCoraSynchronizerSpy) synchronizerFactory.MCR
				.getReturnValue("factorForBatch", 0);

		FedoraReaderFactorySpy fedoraReaderFactory = (FedoraReaderFactorySpy) FedoraToDbBatch.fedoraReaderFactory;
		assertEquals(fedoraReaderFactory.baseUrl, "args-someFedoraBaseUrl");
		FedoraReaderSpy factoredFedoraReader = fedoraReaderFactory.factoredFedoraReader;

		assertEquals(factoredFedoraReader.type, "authority-person");

		List<String> listToReturn = factoredFedoraReader.listToReturn;
		assertCorrectCallToSynchronizer(synchronizer, 0, listToReturn.get(0));
		assertCorrectCallToSynchronizer(synchronizer, 1, listToReturn.get(1));
		assertCorrectCallToSynchronizer(synchronizer, 2, listToReturn.get(2));
		assertCorrectCallToSynchronizer(synchronizer, 3, listToReturn.get(3));
		assertCorrectCallToSynchronizer(synchronizer, 4, listToReturn.get(4));

	}

	private void assertCorrectCallToSynchronizer(ClassicCoraSynchronizerSpy synchronizer,
			int callNumber, String recordId) {
		synchronizer.MCR.assertParameters("synchronize", callNumber, "person", recordId, "create",
				"diva");
	}

	@Test
	public void testMainMethodUsingDefaultFile() throws Exception {
		args = new String[] {};
		setFactoryClassNamesToSpies();

		FedoraToDbBatch.main(args);

		assertCorrectInitInfoInSynchronizerUsingPrefix("");
	}

	@Test
	public void testMainMethodUsingFileName() throws Exception {
		String argsWithFileName[] = new String[] { "divaIndexerSentIn.properties" };
		setFactoryClassNamesToSpies();

		FedoraToDbBatch.main(argsWithFileName);

		assertCorrectInitInfoInSynchronizerUsingPrefix("fileSentIn-");
	}

	@Test
	public void testMainMethodWrongNumberOfArguments() throws Exception {
		args = new String[] { "arg1", "arg2" };
		setFactoryClassNamesToSpies();

		FedoraToDbBatch.main(args);

		assertEquals(getInfoLogNo(0), "FedoraToDbBatch starting...");
		assertEquals(getNoOfInfoLogs(), 1);
		assertEquals(getFatalLogNo(0),
				"Error running FedoraToDbBatch: Number of arguments should be 8.");

	}

	private String getFatalLogNo(int messageNo) {
		return loggerFactorySpy.getFatalLogMessageUsingClassNameAndNo(testedClassName, messageNo);
	}

	@Test
	public void testMainMethodMissingParameter() throws Exception {
		args = new String[] { "propertiesForTestingMissingDatabasePassword.properties" };
		setFactoryClassNamesToSpies();

		FedoraToDbBatch.main(args);

		assertEquals(getInfoLogNo(0), "FedoraToDbBatch starting...");
		assertEquals(getNoOfInfoLogs(), 1);
		assertEquals(getFatalLogNo(0),
				"Error running FedoraToDbBatch: Property with name database.url not found in properties");
		Exception exception = getFatalExceptionNo(0);
		assertEquals(exception.getMessage(),
				"Property with name database.url not found in properties");
	}

	private Exception getFatalExceptionNo(int exceptionNo) {
		return loggerFactorySpy.getFatalExceptionUsingClassNameAndNo(testedClassName, exceptionNo);
	}

	@Test
	public void testMainMethodWrongClassNameForSynchronizerFactory() throws Exception {
		FedoraToDbBatch.synchronizerFactoryClassName = "se.uu.ub.cora.classicfedorasynchronizer.NOTClassicCoraSynchronizerFactorySpy";
		FedoraToDbBatch.fedoraReaderFactoryClassName = "se.uu.ub.cora.classicfedorasynchronizer.batch.FedoraReaderFactorySpy";

		FedoraToDbBatch.main(args);
		assertEquals(getInfoLogNo(0), "FedoraToDbBatch starting...");
		assertEquals(getNoOfInfoLogs(), 1);
		assertEquals(getFatalLogNo(0),
				"Error running FedoraToDbBatch: se.uu.ub.cora.classicfedorasynchronizer.NOTClassicCoraSynchronizerFactorySpy");

	}

	@Test
	public void testMainMethodWrongClassNameForFedoraReaderFactory() throws Exception {
		FedoraToDbBatch.synchronizerFactoryClassName = "se.uu.ub.cora.classicfedorasynchronizer.ClassicCoraSynchronizerFactorySpy";
		FedoraToDbBatch.fedoraReaderFactoryClassName = "se.uu.ub.cora.classicfedorasynchronizer.batch.NOTFedoraReaderFactorySpy";

		FedoraToDbBatch.main(args);
		assertEquals(getInfoLogNo(0), "FedoraToDbBatch starting...");
		assertEquals(getNoOfInfoLogs(), 1);
		assertEquals(getFatalLogNo(0),
				"Error running FedoraToDbBatch: se.uu.ub.cora.classicfedorasynchronizer.batch.NOTFedoraReaderFactorySpy");

	}

	@Test
	public void testErrorWhileSynchronizingShouldContinueToNextOne() throws Exception {

		setFactoryClassNamesToSpies();
		FedoraToDbBatch.synchronizerFactoryClassName = "se.uu.ub.cora.classicfedorasynchronizer.ClassicCoraSynchronizerFactoryThrowExceptionSpy";

		FedoraToDbBatch.main(args);
		int exceptionNo = 0;
		assertEquals(getErrorLogNo(exceptionNo),
				"Error synchronizing recordId: auhority-person:245");
		Exception exception = getErrorExceptionNo(exceptionNo);
		assertEquals(exception.getMessage(), "Record not found error from spy");
		int noOfRecordsInFakeFedora = 5;
		assertEquals(getNoOfErrorLogs(), noOfRecordsInFakeFedora);
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
	//
	// @Test
	// public void testCreatedAfter() throws Exception {
	// setFactoryClassNamesToSpies();
	//
	// FedoraToDbBatch.main(args);
	//
	// FedoraReaderFactorySpy fedoraReaderFactory = (FedoraReaderFactorySpy)
	// FedoraToDbBatch.fedoraReaderFactory;
	// FedoraReaderSpy fedoraReader = (FedoraReaderSpy) fedoraReaderFactory.MCR
	// .getReturnValue("factor", 0);
	//
	// fedoraReader
	// }
}
