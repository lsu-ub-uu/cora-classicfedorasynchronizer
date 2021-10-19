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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import se.uu.ub.cora.classicfedorasynchronizer.ClassicCoraSynchronizer;
import se.uu.ub.cora.classicfedorasynchronizer.ClassicCoraSynchronizerFactory;
import se.uu.ub.cora.classicfedorasynchronizer.internal.SynchronizerFactory;
import se.uu.ub.cora.logger.Logger;
import se.uu.ub.cora.logger.LoggerProvider;

public class FedoraToDbBatch {

	private static Logger logger = LoggerProvider.getLoggerForClass(FedoraToDbBatch.class);
	protected static ClassicCoraSynchronizerFactory synchronizerFactory;
	protected static String synchronizerFactoryClassName = "se.uu.ub.cora.classicfedorasynchronizer.internal.SynchronizerFactory";

	private FedoraToDbBatch() {
	}

	public static void main(String[] args) throws IOException {
		logger.logInfoUsingMessage("FedoraToDbBatch starting...");
		Properties properties = FedoraToDbBatchPropertiesLoader.loadProperties(args);

		// possiblyCreateSynchronizerFactory(properties);
		//
		// synchronize();
		// logger.logInfoUsingMessage("FedoraToDbBatch started");

	}

	private static void constructSynchronizerFactory(String finderClassName)
			throws NoSuchMethodException, ClassNotFoundException, InstantiationException,
			IllegalAccessException, InvocationTargetException {
		Constructor<?> constructor = Class.forName(finderClassName).getConstructor();
		synchronizerFactory = (ClassicCoraSynchronizerFactory) constructor.newInstance();
	}

	private static void possiblyCreateSynchronizerFactory(Properties properties) {
		Map<String, String> initInfo = createInitInfo(properties);
		if (synchronizerFactory == null) {
			synchronizerFactory = new SynchronizerFactory(initInfo);
		}
	}

	private static void synchronize() {
		ClassicCoraSynchronizer synchronizer = synchronizerFactory.factor();
		List<String> pids = getListOfPidsFromFedora();
		for (String recordId : pids) {
			synchronizer.synchronize("person", recordId, "create", "diva");
		}
	}

	// TODO:change this dummy method to real call to some reader
	private static List<String> getListOfPidsFromFedora() {
		return List.of("auhority-person:45", "auhority-person:32", "auhority-person:409",
				"auhority-person:17", "auhority-person:111");
	}

	private static Map<String, String> createInitInfo(Properties properties) {
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
		// throwErrorIfPropertyNameIsMissing(properties, propertyName);
		return properties.getProperty(propertyName);
	}

	public static void setSynchronizerFactory(ClassicCoraSynchronizerFactory factory) {
		FedoraToDbBatch.synchronizerFactory = factory;

	}

	// readPidsForType (i FedoraReader eller annat)
	// loop alla pids

	// readPidsForTypeCreatedAfter
	// readPidsForTypeUpdatedAfter
}
