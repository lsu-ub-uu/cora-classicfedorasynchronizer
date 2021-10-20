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
import java.io.InputStream;
import java.util.Properties;

class FedoraToDbBatchPropertiesLoader {
	private static final int NUMBER_OF_ARGUMENTS = 8;
	private String[] args;

	private FedoraToDbBatchPropertiesLoader(String[] args) {
		this.args = args;
	}

	public static Properties loadProperties(String[] args) throws IOException {
		FedoraToDbBatchPropertiesLoader loader = new FedoraToDbBatchPropertiesLoader(args);
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
		try (InputStream input = FedoraToDbBatchPropertiesLoader.class.getClassLoader()
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

	private Properties loadProperitesFromFile(InputStream input) throws IOException {
		Properties properties = new Properties();
		properties.load(input);
		return properties;
	}
}
