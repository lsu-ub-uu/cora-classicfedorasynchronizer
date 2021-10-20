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

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.classicfedorasynchronizer.ClassicCoraSynchronizerFactorySpy;
import se.uu.ub.cora.classicfedorasynchronizer.ClassicCoraSynchronizerSpy;
import se.uu.ub.cora.classicfedorasynchronizer.log.LoggerFactorySpy;
import se.uu.ub.cora.fedora.data.FedoraReaderXmlHelperImp;
import se.uu.ub.cora.httphandler.HttpHandlerFactoryImp;
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
		FedoraToDbBatch.synchronizerFactoryClassName = "se.uu.ub.cora.classicfedorasynchronizer.ClassicCoraSynchronizerFactorySpy";
		FedoraToDbBatch.fedoraReaderFactoryClassName = "se.uu.ub.cora.classicfedorasynchronizer.batch.FedoraReaderFactorySpy";
		FedoraToDbBatch.main(args);

		assertTrue(
				FedoraToDbBatch.synchronizerFactory instanceof ClassicCoraSynchronizerFactorySpy);
		assertTrue(FedoraToDbBatch.fedoraReaderFactory instanceof FedoraReaderFactorySpy);
		assertEquals(loggerFactorySpy.getNoOfFatalLogMessagesUsingClassName(testedClassName), 0);
		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 0),
				"FedoraToDbBatch starting...");
		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 1),
				"FedoraToDbBatch started");
		assertEquals(loggerFactorySpy.getNoOfInfoLogMessagesUsingClassname(testedClassName), 2);
	}

	@Test
	public void testMainMethodUsingArgs() throws Exception {
		FedoraToDbBatch.synchronizerFactoryClassName = "se.uu.ub.cora.classicfedorasynchronizer.ClassicCoraSynchronizerFactorySpy";
		FedoraToDbBatch.fedoraReaderFactoryClassName = "se.uu.ub.cora.classicfedorasynchronizer.batch.FedoraReaderFactorySpy";

		FedoraToDbBatch.main(args);

		assertCorrectInitInfoInSynchronizerUsingPrefix("args-");

		FedoraReaderFactorySpy readerFactory = (FedoraReaderFactorySpy) FedoraToDbBatch.fedoraReaderFactory;
		assertTrue(readerFactory.httpHandlerFactory instanceof HttpHandlerFactoryImp);
		assertTrue(readerFactory.fedoraReaderXmlHelper instanceof FedoraReaderXmlHelperImp);
	}

	private void assertCorrectInitInfoInSynchronizerUsingPrefix(String prefix) {
		ClassicCoraSynchronizerFactorySpy synchronizerFactory = (ClassicCoraSynchronizerFactorySpy) FedoraToDbBatch.synchronizerFactory;
		assertEquals(synchronizerFactory.initInfo.get("databaseUrl"), prefix + "someDatabaseUrl");
		assertEquals(synchronizerFactory.initInfo.get("databaseUser"), prefix + "dbUserName");
		assertEquals(synchronizerFactory.initInfo.get("databasePassword"),
				prefix + "dbUserPassword");
		assertEquals(synchronizerFactory.initInfo.get("fedoraBaseUrl"),
				prefix + "someFedoraBaseUrl");
		assertEquals(synchronizerFactory.initInfo.get("coraApptokenVerifierURL"),
				prefix + "someApptokenVerifierUrl");
		assertEquals(synchronizerFactory.initInfo.get("coraBaseUrl"), prefix + "someCoraBaseUrl");
		assertEquals(synchronizerFactory.initInfo.get("coraUserId"), prefix + "someCoraUserId");
		assertEquals(synchronizerFactory.initInfo.get("coraApptoken"), prefix + "someCoraApptoken");
	}

	@Test
	public void testFactorSynchronizerUsingFactorySpy() throws Exception {
		FedoraToDbBatch.synchronizerFactoryClassName = "se.uu.ub.cora.classicfedorasynchronizer.ClassicCoraSynchronizerFactorySpy";
		FedoraToDbBatch.fedoraReaderFactoryClassName = "se.uu.ub.cora.classicfedorasynchronizer.batch.FedoraReaderFactorySpy";

		FedoraToDbBatch.main(args);
		ClassicCoraSynchronizerFactorySpy synchronizerFactory = (ClassicCoraSynchronizerFactorySpy) FedoraToDbBatch.synchronizerFactory;
		ClassicCoraSynchronizerSpy synchronizer = (ClassicCoraSynchronizerSpy) synchronizerFactory.MCR
				.getReturnValue("factor", 0);

		FedoraReaderFactorySpy fedoraReaderFactory = (FedoraReaderFactorySpy) FedoraToDbBatch.fedoraReaderFactory;
		assertEquals(fedoraReaderFactory.baseUrl, "args-someFedoraBaseUrl");
		FedoraReaderSpy factoredFedoraReader = fedoraReaderFactory.factoredFedoraReader;

		assertCorrectCallToSynchronizer(synchronizer, 0, factoredFedoraReader.listToReturn.get(0));
		assertCorrectCallToSynchronizer(synchronizer, 1, factoredFedoraReader.listToReturn.get(1));
		assertCorrectCallToSynchronizer(synchronizer, 2, factoredFedoraReader.listToReturn.get(2));
		assertCorrectCallToSynchronizer(synchronizer, 3, factoredFedoraReader.listToReturn.get(3));
		assertCorrectCallToSynchronizer(synchronizer, 4, factoredFedoraReader.listToReturn.get(4));

	}

	private void assertCorrectCallToSynchronizer(ClassicCoraSynchronizerSpy synchronizer,
			int callNumber, String recordId) {
		synchronizer.MCR.assertParameter("synchronize", callNumber, "recordType", "person");
		synchronizer.MCR.assertParameter("synchronize", callNumber, "recordId", recordId);
		synchronizer.MCR.assertParameter("synchronize", callNumber, "action", "create");
		synchronizer.MCR.assertParameter("synchronize", callNumber, "dataDivider", "diva");
	}

	@Test
	public void testMainMethodUsingDefaultFile() throws Exception {
		args = new String[] {};
		FedoraToDbBatch.synchronizerFactoryClassName = "se.uu.ub.cora.classicfedorasynchronizer.ClassicCoraSynchronizerFactorySpy";
		FedoraToDbBatch.fedoraReaderFactoryClassName = "se.uu.ub.cora.classicfedorasynchronizer.batch.FedoraReaderFactorySpy";

		FedoraToDbBatch.main(args);

		assertCorrectInitInfoInSynchronizerUsingPrefix("");
	}

	@Test
	public void testMainMethodUsingFileName() throws Exception {
		String argsWithFileName[] = new String[] { "divaIndexerSentIn.properties" };
		FedoraToDbBatch.synchronizerFactoryClassName = "se.uu.ub.cora.classicfedorasynchronizer.ClassicCoraSynchronizerFactorySpy";
		FedoraToDbBatch.fedoraReaderFactoryClassName = "se.uu.ub.cora.classicfedorasynchronizer.batch.FedoraReaderFactorySpy";

		FedoraToDbBatch.main(argsWithFileName);

		assertCorrectInitInfoInSynchronizerUsingPrefix("fileSentIn-");
	}

	@Test
	public void testMainMethodWrongNumberOfArguments() throws Exception {
		args = new String[] { "arg1", "arg2" };
		FedoraToDbBatch.synchronizerFactoryClassName = "se.uu.ub.cora.classicfedorasynchronizer.ClassicCoraSynchronizerFactorySpy";
		FedoraToDbBatch.fedoraReaderFactoryClassName = "se.uu.ub.cora.classicfedorasynchronizer.batch.FedoraReaderFactorySpy";

		FedoraToDbBatch.main(args);

		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 0),
				"FedoraToDbBatch starting...");
		assertEquals(loggerFactorySpy.getNoOfInfoLogMessagesUsingClassname(testedClassName), 1);
		assertEquals(loggerFactorySpy.getFatalLogMessageUsingClassNameAndNo(testedClassName, 0),
				"Unable to start FedoraToDbBatch: Number of arguments should be 8.");

	}

	@Test
	public void testMainMethodMissingParameter() throws Exception {
		args = new String[] { "propertiesForTestingMissingDatabasePassword.properties" };
		FedoraToDbBatch.synchronizerFactoryClassName = "se.uu.ub.cora.classicfedorasynchronizer.ClassicCoraSynchronizerFactorySpy";
		FedoraToDbBatch.fedoraReaderFactoryClassName = "se.uu.ub.cora.classicfedorasynchronizer.batch.FedoraReaderFactorySpy";

		FedoraToDbBatch.main(args);

		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 0),
				"FedoraToDbBatch starting...");
		assertEquals(loggerFactorySpy.getNoOfInfoLogMessagesUsingClassname(testedClassName), 1);
		assertEquals(loggerFactorySpy.getFatalLogMessageUsingClassNameAndNo(testedClassName, 0),
				"Unable to start FedoraToDbBatch: Property with name database.url not found in properties");

	}
}
