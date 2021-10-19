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
import se.uu.ub.cora.classicfedorasynchronizer.internal.SynchronizerFactory;
import se.uu.ub.cora.classicfedorasynchronizer.log.LoggerFactorySpy;
import se.uu.ub.cora.logger.LoggerProvider;

public class FedoraToDbBatchTest {

	private LoggerFactorySpy loggerFactorySpy = new LoggerFactorySpy();
	private String testedClassName = "FedoraToDbBatch";

	private String[] args;

	@BeforeMethod
	public void setUp() {
		FedoraToDbBatch.setSynchronizerFactory(null);
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
	public void testDefaultSynchronizerFactoryClassName() {
		assertEquals(FedoraToDbBatch.synchronizerFactoryClassName,
				"se.uu.ub.cora.classicfedorasynchronizer.internal.SynchronizerFactory");
	}

	@Test
	public void testMainMethod() throws Exception {
		FedoraToDbBatch.synchronizerFactoryClassName = "se.uu.ub.cora.classicfedorasynchronizer.ClassicCoraSynchronizerFactorySpy";
		// args = new String[] {};
		FedoraToDbBatch.main(args);

		assertTrue(
				FedoraToDbBatch.synchronizerFactory instanceof ClassicCoraSynchronizerFactorySpy);

		assertEquals(loggerFactorySpy.getNoOfFatalLogMessagesUsingClassName(testedClassName), 0);
		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 0),
				"FedoraToDbBatch starting...");
		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 1),
				"FedoraToDbBatch started");
		assertEquals(loggerFactorySpy.getNoOfInfoLogMessagesUsingClassname(testedClassName), 2);

		// we need recordType, recordId, action, dataDivider for synchronizer

		// modification date
		// creation date

		// DivaFedoraRecordStorage createUrlForPersonList
		// String query = "state=A pid~authority-person:*";
		// baseURL + "objects?pid=true&maxResults=100&resultFormat=xml&query=" + urlEncodedQuery;

		// FedoraReaderImp getFedoraUrlForType
		// return
		// String.format("%s/objects?pid=true&maxResults=%d&resultFormat=xml&query=pid%%7E%s:*",
		// baseUrl, maxResults, type);

		// FedoraReaderImp getFedoraCursorUrlForType
		// return String.format(
		// "%s/objects?sessionToken=%s&pid=true&maxResults=%d&resultFormat=xml&query=pid%%7E%s:*",
		// baseUrl, cursor.getToken(), maxResults, type);
	}

	@Test
	public void testMainMethodUsingArgs() throws Exception {
		FedoraToDbBatch.main(args);

		SynchronizerFactory synchronizerFactory = (SynchronizerFactory) FedoraToDbBatch.synchronizerFactory;
		assertEquals(synchronizerFactory.onlyForTestGetInitInfo().get("databaseUrl"),
				"args-someDatabaseUrl");
		assertEquals(synchronizerFactory.onlyForTestGetInitInfo().get("databaseUser"),
				"args-dbUserName");
		assertEquals(synchronizerFactory.onlyForTestGetInitInfo().get("databasePassword"),
				"args-dbUserPassword");
		assertEquals(synchronizerFactory.onlyForTestGetInitInfo().get("fedoraBaseUrl"),
				"args-someFedoraBaseUrl");
		assertEquals(synchronizerFactory.onlyForTestGetInitInfo().get("coraApptokenVerifierURL"),
				"args-someApptokenVerifierUrl");
		assertEquals(synchronizerFactory.onlyForTestGetInitInfo().get("coraBaseUrl"),
				"args-someCoraBaseUrl");
		assertEquals(synchronizerFactory.onlyForTestGetInitInfo().get("coraUserId"),
				"args-someCoraUserId");
		assertEquals(synchronizerFactory.onlyForTestGetInitInfo().get("coraApptoken"),
				"args-someCoraApptoken");
	}

	@Test
	public void testFactorSynchronizerUsingFactorySpy() throws Exception {
		ClassicCoraSynchronizerFactorySpy synchronizerFactory = new ClassicCoraSynchronizerFactorySpy();
		FedoraToDbBatch.setSynchronizerFactory(synchronizerFactory);
		FedoraToDbBatch.main(args);
		ClassicCoraSynchronizerSpy synchronizer = (ClassicCoraSynchronizerSpy) synchronizerFactory.MCR
				.getReturnValue("factor", 0);
		assertCorrectCallToSynchronizer(synchronizer, 0, "auhority-person:45");
		assertCorrectCallToSynchronizer(synchronizer, 1, "auhority-person:32");
		assertCorrectCallToSynchronizer(synchronizer, 2, "auhority-person:409");
		assertCorrectCallToSynchronizer(synchronizer, 3, "auhority-person:17");
		assertCorrectCallToSynchronizer(synchronizer, 4, "auhority-person:111");

	}

	private void assertCorrectCallToSynchronizer(ClassicCoraSynchronizerSpy synchronizer,
			int callNumber, String recordId) {
		synchronizer.MCR.assertParameter("synchronize", callNumber, "recordType", "person");
		synchronizer.MCR.assertParameter("synchronize", callNumber, "recordId", recordId);
		synchronizer.MCR.assertParameter("synchronize", callNumber, "action", "create");
		synchronizer.MCR.assertParameter("synchronize", callNumber, "dataDivider", "diva");
	}
}
