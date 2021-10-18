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

import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import se.uu.ub.cora.classicfedorasynchronizer.ClassicCoraSynchronizerFactory;
import se.uu.ub.cora.classicfedorasynchronizer.internal.SynchronizerFactory;
import se.uu.ub.cora.classicfedorasynchronizer.messaging.parsning.FedoraMessageParserFactory;
import se.uu.ub.cora.classicfedorasynchronizer.messaging.parsning.MessageParserFactory;
import se.uu.ub.cora.logger.Logger;
import se.uu.ub.cora.logger.LoggerProvider;
import se.uu.ub.cora.messaging.JmsMessageRoutingInfo;
import se.uu.ub.cora.messaging.MessageListener;
import se.uu.ub.cora.messaging.MessageReceiver;
import se.uu.ub.cora.messaging.MessagingProvider;

public class MessengerListenerStarter {
	private static Logger logger = LoggerProvider.getLoggerForClass(MessengerListenerStarter.class);
	private static Map<String, String> initInfo = new HashMap<>();
	private static Properties properties;

	private MessengerListenerStarter() {
	}

	public static void main(String[] args) {
		logger.logInfoUsingMessage("MessengerListenerStarter starting...");
		tryToStartMessengerListener(args);
		logger.logInfoUsingMessage("MessengerListenerStarter started");
	}

	private static void tryToStartMessengerListener(String[] args) {
		try {
			startMessengerListener(args);
		} catch (Exception ex) {
			logger.logFatalUsingMessageAndException("Unable to start MessengerListenerStarter ",
					ex);
		}
	}

	private static void startMessengerListener(String[] args) throws IOException {
		properties = MessengerListenerPropertiesLoader.loadProperties(args);
		JmsMessageRoutingInfo routingInfo = createMessageRoutingInfoFromProperties(properties);

		MessageListener topicMessageListener = MessagingProvider
				.getTopicMessageListener(routingInfo);

		MessageReceiver messageReceiver = createMessageReceiver();

		logStartListeningMessages(routingInfo);
		topicMessageListener.listen(messageReceiver);
	}

	private static MessageReceiver createMessageReceiver() {
		MessageParserFactory messageParserFactory = new FedoraMessageParserFactory();

		addToInitInfoFromProperties();
		ClassicCoraSynchronizerFactory synchronizerFactory = new SynchronizerFactory(initInfo);
		return new FedoraMessageReceiver(messageParserFactory, synchronizerFactory);
	}

	private static void addToInitInfoFromProperties() {
		addPropertyToInitInfo("databaseUrl", "database.url");
		addPropertyToInitInfo("databaseUser", "database.user");
		addPropertyToInitInfo("databasePassword", "database.password");
		addPropertyToInitInfo("fedoraBaseUrl", "fedora.baseUrl");
		addPropertyToInitInfo("coraApptokenVerifierURL", "cora.apptokenVerifierUrl");
		addPropertyToInitInfo("coraBaseUrl", "cora.baseUrl");
		addPropertyToInitInfo("coraUserId", "cora.userId");
		addPropertyToInitInfo("coraApptoken", "cora.apptoken");
	}

	private static void addPropertyToInitInfo(String key, String propertyName) {
		initInfo.put(key, extractPropertyThrowErrorIfNotFound(properties, propertyName));
	}

	private static void logStartListeningMessages(JmsMessageRoutingInfo routingInfo) {
		String message = "Will listen for change messages from: {0} using port: {1}";
		String formattedLogMessage = MessageFormat.format(message, routingInfo.hostname,
				routingInfo.port);
		logger.logInfoUsingMessage(formattedLogMessage);
	}

	private static JmsMessageRoutingInfo createMessageRoutingInfoFromProperties(
			Properties properties) {
		String hostname = extractPropertyThrowErrorIfNotFound(properties, "messaging.hostname");
		String port = extractPropertyThrowErrorIfNotFound(properties, "messaging.port");
		String routingKey = extractPropertyThrowErrorIfNotFound(properties, "messaging.routingKey");
		String username = extractPropertyThrowErrorIfNotFound(properties, "messaging.username");
		String password = extractPropertyThrowErrorIfNotFound(properties, "messaging.password");
		return new JmsMessageRoutingInfo(hostname, port, routingKey, username, password);
	}

	private static String extractPropertyThrowErrorIfNotFound(Properties properties,
			String propertyName) {
		throwErrorIfPropertyNameIsMissing(properties, propertyName);
		return properties.getProperty(propertyName);
	}

	private static void throwErrorIfPropertyNameIsMissing(Properties properties,
			String propertyName) {
		if (!properties.containsKey(propertyName)) {
			throw new RuntimeException(
					"Property with name " + propertyName + " not found in properties");
		}
	}
}
