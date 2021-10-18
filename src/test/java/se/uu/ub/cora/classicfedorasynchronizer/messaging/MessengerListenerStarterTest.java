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
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.classicfedorasynchronizer.ClassicCoraSynchronizerFactory;
import se.uu.ub.cora.classicfedorasynchronizer.internal.SynchronizerFactory;
import se.uu.ub.cora.classicfedorasynchronizer.messaging.parsning.FedoraMessageParserFactory;
import se.uu.ub.cora.logger.LoggerProvider;
import se.uu.ub.cora.messaging.JmsMessageRoutingInfo;
import se.uu.ub.cora.messaging.MessagingProvider;

public class MessengerListenerStarterTest {

	private LoggerFactorySpy loggerFactorySpy = new LoggerFactorySpy();
	private MessagingFactorySpy messagingFactorySpy;
	private String testedClassName = "MessengerListenerStarter";
	private String args[];

	@BeforeMethod
	public void setUp() {
		loggerFactorySpy.resetLogs(testedClassName);
		LoggerProvider.setLoggerFactory(loggerFactorySpy);

		messagingFactorySpy = new MessagingFactorySpy();
		MessagingProvider.setMessagingFactory(messagingFactorySpy);

		args = new String[] { "args-dev-diva-drafts", "args-61617", "args-fedora.apim.*",
				"args-admin", "args-admin", "args-someDatabaseUrl", "args-dbUserName",
				"args-dbUserPassword", "args-someFedoraBaseUrl", "args-someApptokenVerifierUrl",
				"args-someCoraBaseUrl", "args-someCoraUserId", "args-someCoraApptoken" };
	}

	@Test
	public void testConstructorIsPrivate() throws Exception {
		Constructor<MessengerListenerStarter> constructor = MessengerListenerStarter.class
				.getDeclaredConstructor();
		assertTrue(Modifier.isPrivate(constructor.getModifiers()));
		constructor.setAccessible(true);
		constructor.newInstance();
	}

	@Test
	public void testMainMethod() {
		MessengerListenerStarter.main(args);
		assertInfoMessagesForStartup();
		assertNoFatalErrorMessages();
	}

	private void assertInfoMessagesForStartup() {
		assertEquals(loggerFactorySpy.getNoOfFatalLogMessagesUsingClassName(testedClassName), 0);
		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 0),
				"MessengerListenerStarter starting...");
		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 1),
				"Will listen for change messages from: args-dev-diva-drafts using port: args-61617");
		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 2),
				"MessengerListenerStarter started");
		assertEquals(loggerFactorySpy.getNoOfInfoLogMessagesUsingClassname(testedClassName), 3);
	}

	private void assertNoFatalErrorMessages() {
		LoggerSpy loggerSpy = loggerFactorySpy.createdLoggers.get(testedClassName);
		assertNotNull(loggerSpy);
	}

	@Test
	public void testMainMethodMessagingRoutingInfoSetUpCorrectlyArgsSent()
			throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException,
			InvocationTargetException, InstantiationException {

		MessengerListenerStarter.main(args);

		assertCorrectMessageRoutingInfo("args-");

	}

	@Test
	public void testMainMethodWithPropertiesFileNameShouldUseDefaultFilename() throws Exception {
		String emtyArgsUseDefaultFileName[] = new String[] {};
		MessengerListenerStarter.main(emtyArgsUseDefaultFileName);
		MessageListenerSpy messageListener = messagingFactorySpy.messageListenerSpy;
		FedoraMessageReceiver messageReceiver = (FedoraMessageReceiver) messageListener.messageReceiver;

		SynchronizerFactory synchronizerFactory = (SynchronizerFactory) messageReceiver
				.onlyForTestGetClassicCoraSynchronizerFactory();

		Map<String, String> initInfo = synchronizerFactory.onlyForTestGetInitInfo();
		assertCorrectInitInfo("", initInfo);
		assertCorrectMessageRoutingInfo("");
	}

	@Test
	public void testMainMethodMessagingRoutingInfoSetUpCorrectlyFromFile() throws Exception {
		String argsWithFileName[] = new String[] { "divaIndexerSentIn.properties" };
		MessengerListenerStarter.main(argsWithFileName);
		MessageListenerSpy messageListener = messagingFactorySpy.messageListenerSpy;
		FedoraMessageReceiver messageReceiver = (FedoraMessageReceiver) messageListener.messageReceiver;

		SynchronizerFactory synchronizerFactory = (SynchronizerFactory) messageReceiver
				.onlyForTestGetClassicCoraSynchronizerFactory();

		Map<String, String> initInfo = synchronizerFactory.onlyForTestGetInitInfo();
		assertCorrectInitInfo("fileSentIn-", initInfo);

		assertCorrectMessageRoutingInfo("fileSentIn-");
	}

	private void assertCorrectMessageRoutingInfo(String prefix) {

		JmsMessageRoutingInfo messagingRoutingInfo = (JmsMessageRoutingInfo) messagingFactorySpy.messagingRoutingInfo;

		assertNotNull(messagingRoutingInfo);
		assertEquals(messagingRoutingInfo.hostname, prefix + "dev-diva-drafts");
		assertEquals(messagingRoutingInfo.port, prefix + "61617");
		assertEquals(messagingRoutingInfo.routingKey, prefix + "fedora.apim.*");
		assertEquals(messagingRoutingInfo.username, prefix + "admin");
		assertEquals(messagingRoutingInfo.password, prefix + "admin");
	}

	private void assertCorrectInitInfo(String prefix, Map<String, String> initInfo) {
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
	public void testMainMethodMessageParserFactorySetUpCorrectly() throws Exception {
		MessengerListenerStarter.main(args);
		MessageListenerSpy messageListener = messagingFactorySpy.messageListenerSpy;
		FedoraMessageReceiver messageReceiver = (FedoraMessageReceiver) messageListener.messageReceiver;
		assertTrue(messageReceiver
				.onlyForTestGetMessageParserFactory() instanceof FedoraMessageParserFactory);
	}

	@Test
	public void testMainMethodSynchronizerFactorySetUpCorrectly() throws Exception {
		MessengerListenerStarter.main(args);
		MessageListenerSpy messageListener = messagingFactorySpy.messageListenerSpy;
		FedoraMessageReceiver messageReceiver = (FedoraMessageReceiver) messageListener.messageReceiver;
		assertTrue(messageReceiver
				.onlyForTestGetClassicCoraSynchronizerFactory() instanceof ClassicCoraSynchronizerFactory);
	}

	@Test
	public void testErrorHandlingNotEnoughParameters() throws Exception {
		args = new String[] { "args-dev-diva-drafts", "args-61617" };

		MessengerListenerStarter.main(args);

		Exception exception = loggerFactorySpy.getFatalLogErrorUsingClassNameAndNo(testedClassName,
				0);
		assertTrue(exception instanceof RuntimeException);
		assertEquals(exception.getMessage(), "Number of arguments should be 13.");

		assertEquals(loggerFactorySpy.getNoOfFatalLogMessagesUsingClassName(testedClassName), 1);
		assertEquals(loggerFactorySpy.getFatalLogMessageUsingClassNameAndNo(testedClassName, 0),
				"Unable to start MessengerListenerStarter ");
	}

	@Test
	public void testPropertiesErrorWhenHostnameIsMissing() {
		String propertyName = "messaging.hostname";
		String fileName = "propertiesForTestingMissingParameterHostname.properties";
		testPropertiesErrorWhenPropertyIsMissing(fileName, propertyName);
	}

	private void testPropertiesErrorWhenPropertyIsMissing(String fileName, String propertyName) {
		String args[] = new String[] { fileName };

		MessengerListenerStarter.main(args);
		assertCorrectErrorForMissingProperty(propertyName);
	}

	private void assertCorrectErrorForMissingProperty(String propertyName) {
		assertEquals(loggerFactorySpy.getNoOfFatalLogMessagesUsingClassName(testedClassName), 1);
		Exception exception = loggerFactorySpy.getFatalLogErrorUsingClassNameAndNo(testedClassName,
				0);
		assertTrue(exception instanceof RuntimeException);
		assertEquals(exception.getMessage(),
				"Property with name " + propertyName + " not found in properties");
		assertEquals(loggerFactorySpy.getFatalLogMessageUsingClassNameAndNo(testedClassName, 0),
				"Unable to start MessengerListenerStarter ");
	}

	@Test
	public void testPropertiesErrorWhenPortIsMissing() {
		String fileName = "propertiesForTestingMissingParameterPort.properties";
		String propertyName = "messaging.port";
		testPropertiesErrorWhenPropertyIsMissing(fileName, propertyName);
	}

	@Test
	public void testPropertiesErrorWhenRoutingKeyIsMissing() {
		String propertyName = "messaging.routingKey";
		String fileName = "propertiesForTestingMissingParameterRoutingKey.properties";
		testPropertiesErrorWhenPropertyIsMissing(fileName, propertyName);
	}

	@Test
	public void testPropertiesErrorWhenVirtualHostIsMissing() {
		String propertyName = "messaging.username";
		String fileName = "propertiesForTestingMissingParameterUsername.properties";
		testPropertiesErrorWhenPropertyIsMissing(fileName, propertyName);
	}

	@Test
	public void testPropertiesErrorWhenExchangeIsMissing() {
		String propertyName = "messaging.password";
		String fileName = "propertiesForTestingMissingParameterPassword.properties";
		testPropertiesErrorWhenPropertyIsMissing(fileName, propertyName);
	}

}
