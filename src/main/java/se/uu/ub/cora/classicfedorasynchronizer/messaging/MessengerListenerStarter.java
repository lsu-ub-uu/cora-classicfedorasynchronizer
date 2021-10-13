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
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Properties;

import se.uu.ub.cora.logger.Logger;
import se.uu.ub.cora.logger.LoggerProvider;
import se.uu.ub.cora.messaging.JmsMessageRoutingInfo;
import se.uu.ub.cora.messaging.MessageListener;
import se.uu.ub.cora.messaging.MessageReceiver;
import se.uu.ub.cora.messaging.MessagingProvider;

public class MessengerListenerStarter {

	private static final int NUMBER_OF_ARGUMENTS = 5;

	private static Logger logger = LoggerProvider.getLoggerForClass(MessengerListenerStarter.class);

	private MessengerListenerStarter() {
	}

	public static void main(String[] args) {
		logger.logInfoUsingMessage("MessengerListenerStarter starting...");
		tryToCreateMessengerListener(args);
		logger.logInfoUsingMessage("MessengerListenerStarter started");
	}

	private static void tryToCreateMessengerListener(String[] args) {
		try {
			startMessengerListener(args);
		} catch (Exception ex) {
			logger.logFatalUsingMessageAndException("Unable to start MessengerListenerStarter ",
					ex);
		}
	}

	private static void startMessengerListener(String[] args) throws IOException {
		Properties properties = loadProperties(args);
		createIndexMessengerListener(properties);
	}

	private static Properties loadProperties(String[] args) throws IOException {
		if (propertiesShouldBeReadFromFile(args)) {
			return readPropertiesFromFile(args);
		} else if (propertiesProvidedAsArguments(args)) {
			return loadProperitesFromArgs(args);
		}
		throw new RuntimeException("Number of arguments should be " + NUMBER_OF_ARGUMENTS + ".");
	}

	private static boolean propertiesShouldBeReadFromFile(String[] args) {
		return args.length == 0 || fileNameProvidedAsArgument(args);
	}

	private static boolean fileNameProvidedAsArgument(String[] args) {
		return args.length == 1;
	}

	private static Properties readPropertiesFromFile(String[] args) throws IOException {
		String propertiesFileName = getFilenameFromArgsOrDefault(args);
		try (InputStream input = MessengerListenerStarter.class.getClassLoader()
				.getResourceAsStream(propertiesFileName)) {
			return loadProperitesFromFile(input);
		}
	}

	private static String getFilenameFromArgsOrDefault(String[] args) {
		if (args.length > 0) {
			return args[0];
		}
		return "fedoraJms.properties";
	}

	private static boolean propertiesProvidedAsArguments(String[] args) {
		return args.length == NUMBER_OF_ARGUMENTS;
	}

	private static Properties loadProperitesFromArgs(String[] args) {
		Properties properties = new Properties();
		properties.put("messaging.hostname", args[0]);
		properties.put("messaging.port", args[1]);
		properties.put("messaging.routingKey", args[2]);
		properties.put("messaging.username", args[3]);
		properties.put("messaging.password", args[4]);
		return properties;
	}

	private static Properties loadProperitesFromFile(InputStream input) throws IOException {
		Properties properties = new Properties();

		properties.load(input);
		return properties;
	}

	private static void createIndexMessengerListener(Properties properties) {
		JmsMessageRoutingInfo routingInfo = createMessageRoutingInfoFromProperties(properties);

		String logM = "Will listen for change messages from: {0} using port: {1}";
		String formattedLogMessage = MessageFormat.format(logM, routingInfo.hostname,
				routingInfo.port);
		logger.logInfoUsingMessage(formattedLogMessage);

		MessageListener topicMessageListener = MessagingProvider
				.getTopicMessageListener(routingInfo);
		MessageParserFactory messageParserFactory = new FedoraMessageParserFactory();
		MessageReceiver messageReceiver = new IndexMessageReceiver(messageParserFactory);
		topicMessageListener.listen(messageReceiver);
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

	private static JmsMessageRoutingInfo createMessageRoutingInfoFromProperties(
			Properties properties) {
		String hostname = extractPropertyThrowErrorIfNotFound(properties, "messaging.hostname");
		String port = extractPropertyThrowErrorIfNotFound(properties, "messaging.port");
		String routingKey = extractPropertyThrowErrorIfNotFound(properties, "messaging.routingKey");
		String username = extractPropertyThrowErrorIfNotFound(properties, "messaging.username");
		String password = extractPropertyThrowErrorIfNotFound(properties, "messaging.password");
		return new JmsMessageRoutingInfo(hostname, port, routingKey, username, password);
	}

}
