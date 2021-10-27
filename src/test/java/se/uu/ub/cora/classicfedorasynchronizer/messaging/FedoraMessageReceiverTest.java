/*
 * Copyright 2019 Uppsala University Library
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
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

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

	// @Test
	// public void testReceiveMessageCreatesCoraClientForWorkOrder() {
	// receiver.receiveMessage(headers, message);
	//
	// // assertTrue(coraClientSpy.createWasCalled);
	// // assertEquals(coraClientSpy.createdRecordType, "workOrder");
	// }

	@Test
	public void testReceiveMessageUsesMessageParserToGetTypeAndId() throws Exception {
		receiver.receiveMessage(headers, message);
		MessageParserSpy messageParserSpy = messageParserFactorySpy.messageParserSpy;
		assertSame(messageParserSpy.headers, headers);
		assertSame(messageParserSpy.message, message);
	}

	@Test
	public void testReceiveMessageUsesParserParameters() throws Exception {
		receiver.receiveMessage(headers, message);

		MessageParserSpy messageParserSpy = messageParserFactorySpy.messageParserSpy;

		messageParserSpy.MCR.assertParameters("parseHeadersAndMessage", 0, headers, message);
		messageParserSpy.MCR.assertReturn("synchronizationRequired", 0, true);

		messageParserSpy.MCR.assertReturn("getRecordType", 0, "someParsedTypeFromMessageParserSpy");
		messageParserSpy.MCR.assertReturn("getRecordId", 0, "someParsedIdFromMessageParserSpy");
		messageParserSpy.MCR.assertReturn("getAction", 0, "update");
	}

	@Test
	public void testCallSynchronizerCorrectValues() throws Exception {
		receiver.receiveMessage(headers, message);

		MessageParserSpy messageParserSpy = messageParserFactorySpy.messageParserSpy;

		ClassicCoraSynchronizerSpy synchronizer = (ClassicCoraSynchronizerSpy) synchronizerFactory.MCR
				.getReturnValue("factorForMessaging", 0);

		String recordType = (String) messageParserSpy.MCR.getReturnValue("getRecordType", 0);
		String recordId = (String) messageParserSpy.MCR.getReturnValue("getRecordId", 0);
		String action = (String) messageParserSpy.MCR.getReturnValue("getAction", 0);

		synchronizer.MCR.assertParameters("synchronize", 0, recordType, recordId, action, "diva");

	}

	@Test
	public void testWriteLogWhenSynchronizeIsCalledAndRecordHasToBeSynchronized() throws Exception {
		receiver.receiveMessage(headers, message);

		String firstInfoLogMessage = loggerFactory
				.getInfoLogMessageUsingClassNameAndNo(testedClassname, 0);
		assertEquals(firstInfoLogMessage,
				"Synchronizer called for type: someParsedTypeFromMessageParserSpy, "
						+ " id: someParsedIdFromMessageParserSpy, action: update and dataDivider: diva ");
	}

	@Test
	public void testWriteLogWhenSynchronizeIsCalledAndRecordIsNotSynchronized() throws Exception {
		messageParserFactorySpy.createWorkOrder = false;
		receiver.receiveMessage(headers, message);

		Object noOfErrorLogMessagesUsingClassName = loggerFactory
				.getNoOfErrorLogMessagesUsingClassName(testedClassname);

		assertEquals(noOfErrorLogMessagesUsingClassName, 0);
	}

	@Test
	public void testLogFatalWhenTopicGetsClosed() throws Exception {
		assertEquals(loggerFactory.getNoOfFatalLogMessagesUsingClassName(testedClassname), 0);

		receiver.topicClosed();

		assertEquals(loggerFactory.getNoOfFatalLogMessagesUsingClassName(testedClassname), 1);
	}

	@Test
	public void testLogFatalWhenTopicGetsClosedCorrectMessage() throws Exception {
		receiver.topicClosed();

		String firstFatalLogMessage = loggerFactory
				.getFatalLogMessageUsingClassNameAndNo(testedClassname, 0);
		assertEquals(firstFatalLogMessage, "Topic closed!");
	}

	@Test
	public void testErrorWhenReceiveError() {
		synchronizerFactory.throwError = true;
		receiver.receiveMessage(headers, message);
		assertEquals(loggerFactory.getNoOfErrorLogMessagesUsingClassName(testedClassname), 1);
		String firstInfoLogMessage = loggerFactory
				.getErrorLogMessageUsingClassNameAndNo(testedClassname, 0);
		assertEquals(firstInfoLogMessage,
				"Message could not be synchronized. Record not found error from spy");

	}
}
