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
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import se.uu.ub.cora.classicfedorasynchronizer.ClassicCoraSynchronizer;
import se.uu.ub.cora.classicfedorasynchronizer.ClassicCoraSynchronizerFactory;
import se.uu.ub.cora.fedora.reader.FedoraReader;
import se.uu.ub.cora.fedora.reader.FedoraReaderFactory;
import se.uu.ub.cora.logger.Logger;
import se.uu.ub.cora.logger.LoggerProvider;

public class FedoraToDbBatch {

	private static Logger logger = LoggerProvider.getLoggerForClass(FedoraToDbBatch.class);
	static String synchronizerFactoryClassName = "se.uu.ub.cora.classicfedorasynchronizer.internal.SynchronizerFactory";
	static String fedoraReaderFactoryClassName = "se.uu.ub.cora.fedora.reader.FedoraReaderFactoryImp";
	static ClassicCoraSynchronizerFactory synchronizerFactory;
	static FedoraReaderFactory fedoraReaderFactory;

	private FedoraToDbBatch() {
	}

	public static void main(String[] args) {
		logger.logInfoUsingMessage("FedoraToDbBatch starting...");
		tryToReadAndSynchronize(args);

	}

	private static void tryToReadAndSynchronize(String[] args) {
		try {
			readAndSynchronize(args);
			logger.logInfoUsingMessage("FedoraToDbBatch started");
		} catch (Exception e) {
			logger.logFatalUsingMessage("Unable to start FedoraToDbBatch: " + e.getMessage());
		}
	}

	private static void readAndSynchronize(String[] args) throws IOException, NoSuchMethodException,
			ClassNotFoundException, IllegalAccessException, InvocationTargetException,
			InstantiationException, IllegalArgumentException {
		Map<String, String> initInfo = createInitInfo(args);

		constructSynchronizerFactory(initInfo);
		constructFedoraReaderFactory();
		synchronize(initInfo);
	}

	private static Map<String, String> createInitInfo(String[] args) throws IOException {
		return FedoraToDbBatchPropertiesLoader.createInitInfo(args);
	}

	private static void constructSynchronizerFactory(Map<String, String> initInfo)
			throws NoSuchMethodException, ClassNotFoundException, IllegalAccessException,
			InvocationTargetException {
		Class<?>[] cArg = new Class[1];
		cArg[0] = Map.class;
		Method constructor = Class.forName(synchronizerFactoryClassName).getMethod("usingInitInfo",
				cArg);
		synchronizerFactory = (ClassicCoraSynchronizerFactory) constructor.invoke(null, initInfo);
	}

	private static void constructFedoraReaderFactory()
			throws NoSuchMethodException, ClassNotFoundException, IllegalAccessException,
			InvocationTargetException, InstantiationException, IllegalArgumentException {
		Constructor<?> constructor = Class.forName(fedoraReaderFactoryClassName).getConstructor();

		fedoraReaderFactory = (FedoraReaderFactory) constructor.newInstance();
	}

	private static void synchronize(Map<String, String> initInfo) {
		ClassicCoraSynchronizer synchronizer = synchronizerFactory.factorForBatch();
		List<String> pids = getListOfPidsFromFedora(initInfo);
		for (String recordId : pids) {
			// TODO: catch errors from synchronizer, keep calm, log and carry on...
			synchronizer.synchronize("person", recordId, "create", "diva");
		}
	}

	private static List<String> getListOfPidsFromFedora(Map<String, String> initInfo) {
		FedoraReader fedoraReader = fedoraReaderFactory.factor(initInfo.get("fedoraBaseUrl"));
		return fedoraReader.readPidsForType("authority-person");
	}

}
