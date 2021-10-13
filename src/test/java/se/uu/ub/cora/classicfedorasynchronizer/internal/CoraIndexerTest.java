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

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.classicfedorasynchronizer.log.LoggerFactorySpy;
import se.uu.ub.cora.logger.LoggerProvider;

public class CoraIndexerTest {

	private CoraClientFactorySpy clientFactory;
	private LoggerFactorySpy loggerFactorySpy = new LoggerFactorySpy();

	private String testedClassName = "CoraIndexerImp";
	private CoraIndexerImp coraIndexer;
	private String userId = "somePredefinedUserId";
	private String apptoken = "someKnownApptoken";

	@BeforeMethod
	public void setup() {
		loggerFactorySpy.resetLogs(testedClassName);
		LoggerProvider.setLoggerFactory(loggerFactorySpy);

		clientFactory = new CoraClientFactorySpy();
		coraIndexer = new CoraIndexerImp(clientFactory, userId, apptoken);

	}

	@Test
	public void testInit() {
		assertSame(coraIndexer.getCoraClientFactory(), clientFactory);
	}

	@Test
	public void testCoraClient() {
		coraIndexer.handleWorkorderType("index", "someRecordType", "someRecordId");
		assertSame(coraIndexer.getCoraClient(), clientFactory.factoredCoraClient);
		assertEquals(clientFactory.userId, userId);
		assertEquals(clientFactory.appToken, apptoken);
	}

	@Test
	public void testIndexData() {
		int response = coraIndexer.handleWorkorderType("index", "someRecordType", "someRecordId");
		CoraClientSpy coraClient = clientFactory.factoredCoraClient;
		assertEquals(coraClient.recordTypes.get(0), "someRecordType");
		assertEquals(coraClient.recordIds.get(0), "someRecordId");
		assertEquals(coraClient.methodCalled, "index");

		assertEquals(response, 200);

	}

	@Test
	public void testLogging() {
		coraIndexer.handleWorkorderType("index", "someRecordType", "someRecordId");

		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 0),
				"Indexing record. RecordType: someRecordType and recordId: someRecordId.");
		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 1),
				"Indexing finished. RecordType: someRecordType and recordId: someRecordId.");
	}

	@Test
	public void testIndexWithNoIndexlink() {
		clientFactory.throwErrorOnIndex = true;
		coraIndexer = new CoraIndexerImp(clientFactory, userId, apptoken);

		int response = coraIndexer.handleWorkorderType("index", "someRecordType", "someRecordId");

		assertEquals(response, 401);

		assertEquals(loggerFactorySpy.getErrorLogMessageUsingClassNameAndNo(testedClassName, 0),
				"CoraClient error when indexing record. "
						+ "RecordType: someRecordType and recordId: someRecordId. Some error from spy");
	}

	@Test
	public void testIndexWhenOtherErrorIsThrown() {
		clientFactory.throwErrorOnIndex = true;
		clientFactory.errorToThrow = "RuntimeException";
		coraIndexer = new CoraIndexerImp(clientFactory, userId, apptoken);

		int response = coraIndexer.handleWorkorderType("index", "someRecordType", "someRecordId");

		assertEquals(response, 400);

		assertEquals(loggerFactorySpy.getErrorLogMessageUsingClassNameAndNo(testedClassName, 0),
				"Error when indexing record. "
						+ "RecordType: someRecordType and recordId: someRecordId. Some runtime error from spy");
	}

	@Test
	public void testRemoveFromIndex() {
		int response = coraIndexer.handleWorkorderType("removeFromIndex", "someRecordType",
				"someRecordId");

		CoraClientSpy coraClient = clientFactory.factoredCoraClient;
		assertEquals(coraClient.recordTypes.get(0), "someRecordType");
		assertEquals(coraClient.recordIds.get(0), "someRecordId");
		assertEquals(coraClient.methodCalled, "removeFromIndex");

		assertEquals(response, 200);
	}

	@Test
	public void testLoggingWhenRemovingIndex() {
		coraIndexer.handleWorkorderType("removeFromIndex", "someRecordType", "someRecordId");

		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 0),
				"Removing from index. RecordType: someRecordType and recordId: someRecordId.");
		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 1),
				"Finished removing. RecordType: someRecordType and recordId: someRecordId.");
	}

	@Test
	public void testRemoveFromIndexWhenErrorIsThrown() {
		clientFactory.throwErrorOnIndex = true;
		clientFactory.errorToThrow = "RuntimeException";
		coraIndexer = new CoraIndexerImp(clientFactory, userId, apptoken);

		int response = coraIndexer.handleWorkorderType("removeFromIndex", "someRecordType",
				"someRecordId");

		assertEquals(response, 400);

		assertEquals(loggerFactorySpy.getErrorLogMessageUsingClassNameAndNo(testedClassName, 0),
				"Error when removing from index. "
						+ "RecordType: someRecordType and recordId: someRecordId. Some runtime error from spy");
	}

}
