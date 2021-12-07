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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesFileLoader {

	private PropertiesFileLoader() {

	}

	public static boolean propertiesShouldBeReadFromFile(String[] args) {
		return args.length == 0 || fileNameProvidedAsArgument(args);
	}

	private static boolean fileNameProvidedAsArgument(String[] args) {
		return args.length == 1;
	}

	public static Properties readPropertiesFromFile(String[] args, String defaultFileName)
			throws IOException {
		String propertiesFileName = getFilenameFromArgsOrDefault(args, defaultFileName);
		try (InputStream input = PropertiesFileLoader.class.getClassLoader()
				.getResourceAsStream(propertiesFileName)) {
			return loadProperitesFromFile(input);
		}
	}

	private static String getFilenameFromArgsOrDefault(String[] args, String defaultFileName) {
		if (args.length > 0) {
			return args[0];
		}
		return defaultFileName;
	}

	private static Properties loadProperitesFromFile(InputStream input) throws IOException {
		Properties properties = new Properties();
		properties.load(input);
		return properties;
	}

}
