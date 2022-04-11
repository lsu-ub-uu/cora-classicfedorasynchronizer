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
import se.uu.ub.cora.fedoralegacy.reader.FedoraReader;
import se.uu.ub.cora.fedoralegacy.reader.FedoraReaderFactory;
import se.uu.ub.cora.logger.Logger;
import se.uu.ub.cora.logger.LoggerProvider;

public class FedoraToDbCatchUpBatch {

	private static final int NUMBER_OF_ARGUMENTS_FOR_CATCHUP = 9;
	private static final String AUTHORITY_PERSON = "authority-person";
	private static final String PERSON = "person";
	private static final String PERSON_DOMAIN_PART = "personDomainPart";
	private static final String DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";
	private static Logger logger = LoggerProvider.getLoggerForClass(FedoraToDbCatchUpBatch.class);
	static String synchronizerFactoryClassName = "se.uu.ub.cora.classicfedorasynchronizer.internal.SynchronizerFactory";
	static String fedoraReaderFactoryClassName = "se.uu.ub.cora.fedoralegacy.reader.FedoraReaderFactoryImp";
	static ClassicCoraSynchronizerFactory synchronizerFactory;
	static FedoraReaderFactory fedoraReaderFactory;
	private static ClassicCoraSynchronizer synchronizer;
	private static String[] args;
	private static FedoraReader fedoraReader;
	private static DateTimeFormatter dateTimeFormatter = DateTimeFormatter
			.ofPattern(DATE_TIME_PATTERN);
	private static String afterTimestamp;

	FedoraToDbCatchUpBatch() {
	}

	public static void main(String[] args) {
		FedoraToDbCatchUpBatch.args = args;
		logger.logInfoUsingMessage("FedoraToDbCatchUpBatch starting...");
		try {
			tryToSynchronize();
		} catch (Exception e) {
			logger.logFatalUsingMessageAndException(
					"Error running FedoraToDbCatchUpBatch: " + e.getMessage(), e);
		}
	}

	private static void tryToSynchronize() throws Exception {
		startBatchDependencies();
		synchronize();
	}

	private static void startBatchDependencies() throws NoSuchMethodException,
			ClassNotFoundException, IllegalAccessException, InvocationTargetException,
			InstantiationException, IllegalArgumentException, IOException {

		Map<String, String> initInfo = createInitInfoFromArgs();
		constructSynchronizerFactory(initInfo);
		constructFedoraReaderFactory(initInfo);
		afterTimestamp = initInfo.get("afterTimestamp");
		logger.logInfoUsingMessage("FedoraToDbCatchUpBatch started");
	}

	private static Map<String, String> createInitInfoFromArgs() throws IOException {
		return FedoraToDbBatchPropertiesLoader.createInitInfo(args, NUMBER_OF_ARGUMENTS_FOR_CATCHUP,
				"fedoraToDbCatchUp.properties");
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

	private static void constructFedoraReaderFactory(Map<String, String> initInfo)
			throws NoSuchMethodException, ClassNotFoundException, IllegalAccessException,
			InvocationTargetException, InstantiationException, IllegalArgumentException {
		Constructor<?> constructor = Class.forName(fedoraReaderFactoryClassName).getConstructor();

		fedoraReaderFactory = (FedoraReaderFactory) constructor.newInstance();
		fedoraReader = fedoraReaderFactory.factor(initInfo.get("fedoraBaseUrl"));
	}

	private static void synchronize() {
		recordAndLogBatchStartTime();
		synchronizeRecordsForCreatedAfter();
		synchronizeRecordsForUpdatedAfter();
		synchronizeRecordsForDeletedAfter();
		indexAllRecords();
	}

	private static void indexAllRecords() {
		indexPersons();
		indexPersonDomainParts();
		logger.logInfoUsingMessage("See API for status of batchJobs");
	}

	private static void indexPersons() {
		logMessageForIndexing(PERSON);
		synchronizer.indexAllRecordsForType(PERSON);
	}

	private static void logMessageForIndexing(String recordType) {
		logger.logInfoUsingMessage("Start indexing for all " + recordType + "s");
	}

	private static void indexPersonDomainParts() {
		logMessageForIndexing(PERSON_DOMAIN_PART);
		synchronizer.indexAllRecordsForType(PERSON_DOMAIN_PART);
	}

	private static void recordAndLogBatchStartTime() {
		String startBatchTime = getCurrentFormattedTime();
		logger.logInfoUsingMessage("Batch started at: " + startBatchTime);
	}

	private static String getCurrentFormattedTime() {
		LocalDateTime currentDateTime = LocalDateTime.now();
		return currentDateTime.format(dateTimeFormatter);
	}

	private static void synchronizeRecordsForCreatedAfter() {
		List<String> listOfPids = getListOfActivePidsForCreatedAfterFromFedora();
		synchronizeRecords("create", listOfPids);
	}

	private static List<String> getListOfActivePidsForCreatedAfterFromFedora() {
		logger.logInfoUsingMessage("Fetching created records after timestamp: " + afterTimestamp);
		return fedoraReader.readPidsForTypeCreatedAfter(AUTHORITY_PERSON, afterTimestamp);
	}

	private static void synchronizeRecords(String action, List<String> listOfPids) {
		logger.logInfoUsingMessage("Synchronizing " + listOfPids.size() + " records");
		int totalNoPids = listOfPids.size();
		int pidNo = 1;
		for (String recordId : listOfPids) {
			logger.logInfoUsingMessage(
					"Synchronizing(" + pidNo + "/" + totalNoPids + ") recordId: " + recordId);
			synchronizeRecord(action, recordId);
			pidNo++;
		}
		logger.logInfoUsingMessage("Synchronizing done");
	}

	private static void synchronizeRecordsForUpdatedAfter() {
		List<String> listOfPids = getListOfActivePidsForUpdatedAfterFromFedora();
		synchronizeRecords("update", listOfPids);
	}

	private static List<String> getListOfActivePidsForUpdatedAfterFromFedora() {
		logger.logInfoUsingMessage("Fetching updated records after timestamp: " + afterTimestamp);
		return fedoraReader.readPidsForTypeUpdatedAfter(AUTHORITY_PERSON, afterTimestamp);
	}

	private static void synchronizeRecordsForDeletedAfter() {
		List<String> listOfPids = getListOfDeletedPidsForPersonAfterTime();
		synchronizeRecords("delete", listOfPids);
	}

	private static List<String> getListOfDeletedPidsForPersonAfterTime() {
		logger.logInfoUsingMessage("Fetching deleted records after timestamp: " + afterTimestamp);
		return fedoraReader.readPidsForTypeDeletedAfter(AUTHORITY_PERSON, afterTimestamp);
	}

	private static void synchronizeRecord(String action, String recordId) {
		try {
			if ("create".equals(action)) {
				synchronizer.synchronizeCreated(PERSON, recordId, "diva");
			} else if ("update".equals(action)) {
				synchronizer.synchronizeUpdated(PERSON, recordId, "diva");
			} else {
				synchronizer.synchronizeDeleted(PERSON, recordId, "diva");
			}
		} catch (Exception e) {
			logger.logErrorUsingMessageAndException("Error synchronizing recordId: " + recordId, e);
		}
	}
}
