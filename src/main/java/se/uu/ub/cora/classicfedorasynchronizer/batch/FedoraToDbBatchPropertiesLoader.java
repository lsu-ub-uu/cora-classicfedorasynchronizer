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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import se.uu.ub.cora.classicfedorasynchronizer.internal.PropertiesLoader;

class FedoraToDbBatchPropertiesLoader {
	private static final int NUMBER_OF_ARGUMENTS = 8;
	private String[] args;

	private FedoraToDbBatchPropertiesLoader(String[] args) {
		this.args = args;
	}

	public static Map<String, String> createInitInfo(String[] args) throws IOException {
		FedoraToDbBatchPropertiesLoader loader = new FedoraToDbBatchPropertiesLoader(args);
		// return loader.load();
		Properties properties = loader.load();
		return createInitInfoFromProperties(properties);
	}

	private Properties load() throws IOException {
		if (PropertiesLoader.propertiesShouldBeReadFromFile(args)) {
			return PropertiesLoader.readPropertiesFromFile(args);
		} else if (propertiesProvidedAsArguments()) {
			return loadProperitesFromArgs();
		}
		throw new RuntimeException("Number of arguments should be " + NUMBER_OF_ARGUMENTS + ".");
	}

	private boolean propertiesProvidedAsArguments() {
		return args.length == NUMBER_OF_ARGUMENTS;
	}

	private Properties loadProperitesFromArgs() {
		Properties properties = new Properties();

		properties.put("database.url", args[0]);
		properties.put("database.user", args[1]);
		properties.put("database.password", args[2]);
		properties.put("fedora.baseUrl", args[3]);
		properties.put("cora.apptokenVerifierUrl", args[4]);
		properties.put("cora.baseUrl", args[5]);
		properties.put("cora.userId", args[6]);
		properties.put("cora.apptoken", args[7]);

		return properties;
	}

	private static Map<String, String> createInitInfoFromProperties(Properties properties) {
		Map<String, String> initInfo = new HashMap<>();
		addPropertyToInitInfo(initInfo, properties, "databaseUrl", "database.url");
		addPropertyToInitInfo(initInfo, properties, "databaseUser", "database.user");
		addPropertyToInitInfo(initInfo, properties, "databasePassword", "database.password");
		addPropertyToInitInfo(initInfo, properties, "fedoraBaseUrl", "fedora.baseUrl");
		addPropertyToInitInfo(initInfo, properties, "coraApptokenVerifierURL",
				"cora.apptokenVerifierUrl");
		addPropertyToInitInfo(initInfo, properties, "coraBaseUrl", "cora.baseUrl");
		addPropertyToInitInfo(initInfo, properties, "coraUserId", "cora.userId");
		addPropertyToInitInfo(initInfo, properties, "coraApptoken", "cora.apptoken");
		return initInfo;
	}

	private static void addPropertyToInitInfo(Map<String, String> initInfo, Properties properties,
			String key, String propertyName) {
		initInfo.put(key, extractPropertyThrowErrorIfNotFound(properties, propertyName));
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
