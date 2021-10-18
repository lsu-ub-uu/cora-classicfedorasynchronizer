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
package se.uu.ub.cora.classicfedorasynchronizer.messaging;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

class MessengerListenerPropertiesLoader {
	private static final int NUMBER_OF_ARGUMENTS = 13;
	private String[] args;

	private MessengerListenerPropertiesLoader(String[] args) {
		this.args = args;
	}

	public static Properties loadProperties(String[] args) throws IOException {
		MessengerListenerPropertiesLoader loader = new MessengerListenerPropertiesLoader(args);
		return loader.load();
	}

	private Properties load() throws IOException {
		if (propertiesShouldBeReadFromFile()) {
			return readPropertiesFromFile();
		} else if (propertiesProvidedAsArguments()) {
			return loadProperitesFromArgs();
		}
		throw new RuntimeException("Number of arguments should be " + NUMBER_OF_ARGUMENTS + ".");
	}

	private boolean propertiesShouldBeReadFromFile() {
		return args.length == 0 || fileNameProvidedAsArgument();
	}

	private boolean fileNameProvidedAsArgument() {
		return args.length == 1;
	}

	private Properties readPropertiesFromFile() throws IOException {
		String propertiesFileName = getFilenameFromArgsOrDefault();
		try (InputStream input = MessengerListenerStarter.class.getClassLoader()
				.getResourceAsStream(propertiesFileName)) {
			return loadProperitesFromFile(input);
		}
	}

	private String getFilenameFromArgsOrDefault() {
		if (args.length > 0) {
			return args[0];
		}
		return "synchronizer.properties";
	}

	private boolean propertiesProvidedAsArguments() {
		return args.length == NUMBER_OF_ARGUMENTS;
	}

	private Properties loadProperitesFromArgs() {
		Properties properties = new Properties();
		properties.put("messaging.hostname", args[0]);
		properties.put("messaging.port", args[1]);
		properties.put("messaging.routingKey", args[2]);
		properties.put("messaging.username", args[3]);
		properties.put("messaging.password", args[4]);

		properties.put("database.url", args[5]);
		properties.put("database.user", args[6]);
		properties.put("database.password", args[7]);
		properties.put("fedora.baseUrl", args[8]);
		properties.put("cora.apptokenVerifierUrl", args[9]);
		properties.put("cora.baseUrl", args[10]);
		properties.put("cora.userId", args[11]);
		properties.put("cora.apptoken", args[12]);

		return properties;
	}

	private Properties loadProperitesFromFile(InputStream input) throws IOException {
		Properties properties = new Properties();
		properties.load(input);
		return properties;
	}
}
