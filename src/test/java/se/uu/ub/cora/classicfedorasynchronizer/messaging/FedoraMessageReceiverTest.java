/*
 * Copyright 2019, 2021 Uppsala University Library
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

package se.uu.ub.cora.classicfedorasynchronizer.messaging;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.classicfedorasynchronizer.ClassicCoraSynchronizerFactorySpy;
import se.uu.ub.cora.classicfedorasynchronizer.ClassicCoraSynchronizerSpy;
import se.uu.ub.cora.classicfedorasynchronizer.log.LoggerFactorySpy;
import se.uu.ub.cora.classicfedorasynchronizer.messaging.parsning.MessageParserFactorySpy;
import se.uu.ub.cora.classicfedorasynchronizer.messaging.parsning.MessageParserSpy;
import se.uu.ub.cora.logger.LoggerProvider;
import se.uu.ub.cora.messaging.MessageReceiver;

public class FedoraMessageReceiverTest {
	private String message;
	private Map<String, String> headers;
	private MessageReceiver receiver;

	private LoggerFactorySpy loggerFactory;
	private String testedClassname = "FedoraMessageReceiver";
	private MessageParserFactorySpy messageParserFactorySpy;
	private ClassicCoraSynchronizerFactorySpy synchronizerFactory;

	@BeforeMethod
	public void setUp() {
		loggerFactory = new LoggerFactorySpy();
		LoggerProvider.setLoggerFactory(loggerFactory);
		synchronizerFactory = ClassicCoraSynchronizerFactorySpy
				.usingInitInfo(Collections.emptyMap());

		headers = new HashMap<>();
		headers.put("__TypeId__", "epc.messaging.amqp.EPCFedoraMessage");
		headers.put("ACTION", "UPDATE");
		headers.put("PID", "alvin-place:1");
		headers.put("messageSentFrom", "Cora");

		message = "{\"pid\":\"alvin-place:1\",\"routingKey\":\"alvin.updates.place\","
				+ "\"action\":\"UPDATE\",\"dsId\":null,"
				+ "\"headers\":{\"ACTION\":\"UPDATE\",\"PID\":\"alvin-place:1\"}}";
		messageParserFactorySpy = new MessageParserFactorySpy();
		receiver = new FedoraMessageReceiver(messageParserFactorySpy, synchronizerFactory);
	}

	@Test
	public void testSynchronizerFactoryFactorsInReceiveMessage() {
		synchronizerFactory.MCR.assertMethodNotCalled("factorForMessaging");
		receiver.receiveMessage(headers, message);
		synchronizerFactory.MCR.assertMethodWasCalled("factorForMessaging");
	}

	@Test
	public void testMessageParserWasFactored() {
		receiver.receiveMessage(headers, message);
		assertTrue(messageParserFactorySpy.factorWasCalled);
	}

	@Test
	public void testSynchronizerCalledWithValuesFromParserAndCallLoggedWhenCreate()
			throws Exception {
		messageParserFactorySpy.synchronizationRequired = true;
		messageParserFactorySpy.modificationType = "create";

		receiver.receiveMessage(headers, message);

		assertSynchronizerCalledWithValuesFromParserAndCallLoggedUsingCallNoAndMethod(0,
				"synchronizeCreated");
	}

	private void assertSynchronizerCalledWithValuesFromParserAndCallLoggedUsingCallNoAndMethod(
			int callNo, String calledMethodInSynchronizer) {
		MessageParserSpy messageParserSpy = getMessageParserFromFactoryByNo(callNo);
		messageParserSpy.MCR.assertParameters("parseHeadersAndMessage", 0, headers, message);

		ClassicCoraSynchronizerSpy synchronizerSpy = getSynchronizerFromFactoryByNo(callNo);

		String recordType = (String) messageParserSpy.MCR.getReturnValue("getRecordType", 0);
		String recordId = (String) messageParserSpy.MCR.getReturnValue("getRecordId", 0);
		String action = (String) messageParserSpy.MCR.getReturnValue("getAction", 0);

		synchronizerSpy.MCR.assertParameters(calledMethodInSynchronizer, 0, recordType, recordId,
				"diva");

		String infoLogMessage = getInfoLogMessageByNo(callNo);
		String logM = "Synchronizer called for type: {0}, id: {1}, action: {2} and dataDivider: diva";
		assertEquals(infoLogMessage, MessageFormat.format(logM, recordType, recordId, action));
	}

	private MessageParserSpy getMessageParserFromFactoryByNo(int parserNo) {
		return (MessageParserSpy) messageParserFactorySpy.MCR.getReturnValue("factor", parserNo);
	}

	private ClassicCoraSynchronizerSpy getSynchronizerFromFactoryByNo(int synchronizerNo) {
		return (ClassicCoraSynchronizerSpy) synchronizerFactory.MCR
				.getReturnValue("factorForMessaging", synchronizerNo);
	}

	private String getInfoLogMessageByNo(int infoLogMessageNo) {
		return loggerFactory.getInfoLogMessageUsingClassNameAndNo(testedClassname,
				infoLogMessageNo);
	}

	@Test
	public void testSynchronizerCalledWithValuesFromParserAndCallLoggedWhenUpdate()
			throws Exception {
		messageParserFactorySpy.synchronizationRequired = true;
		messageParserFactorySpy.modificationType = "update";

		receiver.receiveMessage(headers, message);

		assertSynchronizerCalledWithValuesFromParserAndCallLoggedUsingCallNoAndMethod(0,
				"synchronizeUpdated");
	}

	@Test
	public void testSynchronizerCalledWithValuesFromParserAndCallLoggedWhenDelete()
			throws Exception {
		messageParserFactorySpy.synchronizationRequired = true;
		messageParserFactorySpy.modificationType = "delete";

		receiver.receiveMessage(headers, message);

		assertSynchronizerCalledWithValuesFromParserAndCallLoggedUsingCallNoAndMethod(0,
				"synchronizeDeleted");
	}

	@Test
	public void testParserCalledButNotSynchronizerAndLogWhenSynchronizationNotRequired()
			throws Exception {
		messageParserFactorySpy.synchronizationRequired = false;
		receiver.receiveMessage(headers, message);

		MessageParserSpy messageParserSpy = getMessageParserFromFactoryByNo(0);
		messageParserSpy.MCR.assertParameters("parseHeadersAndMessage", 0, headers, message);

		synchronizerFactory.MCR.assertMethodNotCalled("factorForMessaging");
		assertEquals(loggerFactory.getNoOfInfoLogMessagesUsingClassname(testedClassname), 0);
		assertEquals((Object) getNumberOfLoggedErrorMessages(), 0);
	}

	private int getNumberOfLoggedErrorMessages() {
		return loggerFactory.getNoOfErrorLogMessagesUsingClassName(testedClassname);
	}

	@Test
	public void testLogFatalWhenTopicGetsClosed() throws Exception {
		assertEquals(getNumberOfLoggedFatalMessages(), 0);

		receiver.topicClosed();

		assertEquals(getFatalLogMessageForNo(0), "Topic closed!");
		assertEquals(getNumberOfLoggedFatalMessages(), 1);
	}

	private int getNumberOfLoggedFatalMessages() {
		return loggerFactory.getNoOfFatalLogMessagesUsingClassName(testedClassname);
	}

	private String getFatalLogMessageForNo(int logNo) {
		return loggerFactory.getFatalLogMessageUsingClassNameAndNo(testedClassname, 0);
	}

	@Test
	public void testErrorWhenReceiveError() {
		synchronizerFactory.throwError = true;
		assertEquals(getNumberOfLoggedErrorMessages(), 0);

		receiver.receiveMessage(headers, message);

		assertEquals(getNumberOfLoggedErrorMessages(), 1);
		assertEquals(getErrorLogMessageForNo(0),
				"Message could not be synchronized. Record not found error from spy");
	}

	private String getErrorLogMessageForNo(int logNo) {
		return loggerFactory.getErrorLogMessageUsingClassNameAndNo(testedClassname, 0);
	}
}
