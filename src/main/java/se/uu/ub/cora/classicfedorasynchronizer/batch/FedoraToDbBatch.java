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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import se.uu.ub.cora.classicfedorasynchronizer.ClassicCoraSynchronizer;
import se.uu.ub.cora.classicfedorasynchronizer.ClassicCoraSynchronizerFactory;
import se.uu.ub.cora.fedora.reader.FedoraReader;
import se.uu.ub.cora.fedora.reader.FedoraReaderFactory;
import se.uu.ub.cora.logger.Logger;
import se.uu.ub.cora.logger.LoggerProvider;

public class FedoraToDbBatch {

	private static final String DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";
	private static final String PERSON = "person";
	private static Logger logger = LoggerProvider.getLoggerForClass(FedoraToDbBatch.class);
	static String synchronizerFactoryClassName = "se.uu.ub.cora.classicfedorasynchronizer.internal.SynchronizerFactory";
	static String fedoraReaderFactoryClassName = "se.uu.ub.cora.fedora.reader.FedoraReaderFactoryImp";
	static ClassicCoraSynchronizerFactory synchronizerFactory;
	static FedoraReaderFactory fedoraReaderFactory;
	private static ClassicCoraSynchronizer synchronizer;
	private static Map<String, String> initInfo;
	private static String[] args;
	private static FedoraReader fedoraReader;
	private static DateTimeFormatter dateTimeFormatter = DateTimeFormatter
			.ofPattern(DATE_TIME_PATTERN);
	private static String startBatchTime;
	private static String action;

	FedoraToDbBatch() {
	}

	public static void main(String[] args) {
		FedoraToDbBatch.args = args;
		logger.logInfoUsingMessage("FedoraToDbBatch starting...");
		try {
			tryToSynchronize();
		} catch (Exception e) {
			logger.logFatalUsingMessageAndException(
					"Error running FedoraToDbBatch: " + e.getMessage(), e);
		}
	}

	private static void tryToSynchronize() throws Exception {
		initInfo = createInitInfoFromArgs();
		startBatchDependencies(initInfo);
		synchronize(initInfo);
		// createdAfter
		// updatedAfter
		// deletedAfter
	}

	private static void startBatchDependencies(Map<String, String> initInfo) throws IOException,
			NoSuchMethodException, ClassNotFoundException, IllegalAccessException,
			InvocationTargetException, InstantiationException, IllegalArgumentException {

		constructSynchronizerFactory(initInfo);
		constructFedoraReaderFactory();
		logger.logInfoUsingMessage("FedoraToDbBatch started");
	}

	private static Map<String, String> createInitInfoFromArgs() throws IOException {
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
		synchronizer = synchronizerFactory.factorForBatch();
	}

	private static void constructFedoraReaderFactory()
			throws NoSuchMethodException, ClassNotFoundException, IllegalAccessException,
			InvocationTargetException, InstantiationException, IllegalArgumentException {
		Constructor<?> constructor = Class.forName(fedoraReaderFactoryClassName).getConstructor();

		fedoraReaderFactory = (FedoraReaderFactory) constructor.newInstance();
		fedoraReader = fedoraReaderFactory.factor(initInfo.get("fedoraBaseUrl"));
	}

	private static void synchronize(Map<String, String> initInfo) {
		startBatchTime = getCurrentFormattedTime();
		logger.logInfoUsingMessage("Batch started at: " + startBatchTime);

		action = "create";
		fetchPidsUsingFetchType(initInfo);
		action = "delete";
		fetchPidsUsingFetchType(initInfo);

	}

	private static void fetchPidsUsingFetchType(Map<String, String> initInfo) {
		List<String> listOfPids = fetchPids(initInfo);
		synchronizePids(listOfPids);
		logger.logInfoUsingMessage("Synchronizing done");
	}

	private static String getCurrentFormattedTime() {
		LocalDateTime currentDateTime = LocalDateTime.now();
		return currentDateTime.format(dateTimeFormatter);
	}

	private static void synchronizePids(List<String> listOfPids) {
		int totalNoPids = listOfPids.size();
		int pidNo = 1;
		for (String recordId : listOfPids) {
			synchronizePid(totalNoPids, pidNo, recordId);
			pidNo++;
		}
	}

	private static void synchronizePid(int totalNoPids, int pidNo, String recordId) {
		logger.logInfoUsingMessage(
				"Synchronizing(" + pidNo + "/" + totalNoPids + ") recordId: " + recordId);
		try {
			synchronizer.synchronize(PERSON, recordId, action, "diva");
		} catch (Exception e) {
			logger.logErrorUsingMessageAndException("Error synchronizing recordId: " + recordId, e);
		}

	}

	private static List<String> fetchPids(Map<String, String> initInfo) {
		List<String> pids;
		logger.logInfoUsingMessage("Fetching pids (" + action + ")");
		if (action.equals("create")) {
			pids = getListOfPidsFromFedora(initInfo);
		} else {
			pids = fedoraReader.readPidsForTypeDeletedAfter(PERSON, startBatchTime);
		}
		logger.logInfoUsingMessage("Fetched " + pids.size() + " pids");
		return pids;
	}

	private static List<String> getListOfPidsFromFedora(Map<String, String> initInfo) {
		return fedoraReader.readPidsForType("authority-person");
	}

}
