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

import java.util.List;

import se.uu.ub.cora.classicfedorasynchronizer.ClassicCoraSynchronizer;
import se.uu.ub.cora.classicfedorasynchronizer.CoraIndexer;
import se.uu.ub.cora.classicfedorasynchronizer.FedoraConverterFactory;
import se.uu.ub.cora.classicfedorasynchronizer.FedoraToCoraConverter;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.data.DataGroupProvider;
import se.uu.ub.cora.httphandler.HttpHandler;
import se.uu.ub.cora.httphandler.HttpHandlerFactory;
import se.uu.ub.cora.logger.Logger;
import se.uu.ub.cora.logger.LoggerProvider;
import se.uu.ub.cora.storage.RecordNotFoundException;
import se.uu.ub.cora.storage.RecordStorage;

/**
 * ClassicCoraPersonSynchronizer implements {@link ClassicCoraSynchronizer} and handles
 * synchronization of Person between classic system and Cora system.
 * 
 * ClassicCoraPersonSynchronizer is NOT threadsafe
 */
public class ClassicCoraPersonSynchronizer implements ClassicCoraSynchronizer {
	private static Logger logger = LoggerProvider
			.getLoggerForClass(ClassicCoraPersonSynchronizer.class);

	private static final String INDEX = "index";
	private static final String PERSON_DOMAIN_PART = "personDomainPart";
	private static final int NOT_FOUND = 404;
	private HttpHandlerFactory httpHandlerFactory;
	private String baseURL;
	private FedoraConverterFactory fedoraConverterFactory;
	private RecordStorage recordStorage;
	private String recordType;
	private String recordId;
	private String dataDivider;
	private HttpHandler httpHandler;
	private DataGroup personDataGroup;
	private CoraIndexer coraIndexer;
	private String action;

	// .usingXXXXX blir väldigt långt...
	public ClassicCoraPersonSynchronizer(RecordStorage recordStorage,
			HttpHandlerFactory httpHandlerFactory, FedoraConverterFactory fedoraConverterFactory,
			CoraIndexer coraIndexer, String baseURL) {
		this.recordStorage = recordStorage;
		this.httpHandlerFactory = httpHandlerFactory;
		this.fedoraConverterFactory = fedoraConverterFactory;
		this.coraIndexer = coraIndexer;
		this.baseURL = baseURL;
	}

	public HttpHandlerFactory getHttpHandlerFactory() {
		return httpHandlerFactory;
	}

	@Override
	public void synchronize(String recordType, String recordId, String action, String dataDivider) {
		this.recordType = recordType;
		this.recordId = recordId;
		this.action = action;
		this.dataDivider = dataDivider;
		createHttpHandlerForRead(recordId);
		throwErrorIfRecordNotFound(recordType, recordId, httpHandler);
		readRecordAndSynchronize(action);
	}

	private void createHttpHandlerForRead(String recordId) {
		httpHandler = httpHandlerFactory
				.factor(baseURL + "objects/" + recordId + "/datastreams/METADATA/content");
		httpHandler.setRequestMethod("GET");
	}

	private void throwErrorIfRecordNotFound(String recordType, String recordId,
			HttpHandler httpHandler) {
		if (httpHandler.getResponseCode() == NOT_FOUND) {
			throw new RecordNotFoundException("Record not found for recordType: " + recordType
					+ " and recordId: " + recordId);
		}
	}

	private void readRecordAndSynchronize(String action) {
		String responseText = httpHandler.getResponseText();
		personDataGroup = convertPersonToDataGroup(responseText);
		List<DataGroup> domainParts = personDataGroup
				.getAllGroupsWithNameInData(PERSON_DOMAIN_PART);

		if ("create".equals(action)) {
			handleCreate(responseText, domainParts);
		} else {
			handleUpdate(responseText, domainParts);
		}
	}

	private DataGroup convertPersonToDataGroup(String responseText) {
		FedoraToCoraConverter toCoraConverter = fedoraConverterFactory
				.factorToCoraConverter("person");
		return toCoraConverter.fromXML(responseText);
	}

	private void handleCreate(String responseText, List<DataGroup> domainParts) {
		createAndIndexPerson();
		for (DataGroup domainPartLink : domainParts) {
			DataGroup personDomainPart = convertDomainPart(responseText);
			createAndIndexDomainPart(domainPartLink, personDomainPart);
		}
	}

	private void createAndIndexPerson() {
		createForMainDataGroup(personDataGroup);
		int responseCode = coraIndexer.handleWorkorderType(INDEX, recordType, recordId);
		logErrorIfResponseNotOk(responseCode, recordType);
	}

	private void createForMainDataGroup(DataGroup dataGroup) {
		recordStorage.create(recordType, recordId, dataGroup, createCollectedTerms(),
				createLinkList(), dataDivider);
	}

	private void createAndIndexDomainPart(DataGroup domainPartLink, DataGroup personDomainPart) {
		String linkedRecordId = domainPartLink.getFirstAtomicValueWithNameInData("linkedRecordId");
		recordStorage.create(PERSON_DOMAIN_PART, linkedRecordId, personDomainPart,
				createCollectedTerms(), createLinkList(), dataDivider);
		indexDomainPart(linkedRecordId);
	}

	private void indexDomainPart(String linkedRecordId) {
		int responseCode = coraIndexer.handleWorkorderType(INDEX, PERSON_DOMAIN_PART,
				linkedRecordId);
		logErrorIfResponseNotOk(responseCode, PERSON_DOMAIN_PART);
	}

	private DataGroup convertDomainPart(String responseText) {
		FedoraToCoraConverter domainPartConverter = fedoraConverterFactory
				.factorToCoraConverter(PERSON_DOMAIN_PART);
		return domainPartConverter.fromXML(responseText);
	}

	private DataGroup createLinkList() {
		return DataGroupProvider.getDataGroupUsingNameInData("collectedDataLinks");
	}

	private DataGroup createCollectedTerms() {
		return DataGroupProvider.getDataGroupUsingNameInData("collectedData");
	}

	private void handleUpdate(String responseText, List<DataGroup> domainParts) {
		updateAndIndexPerson();
		for (DataGroup domainPartLink : domainParts) {
			updateAndIndexDomainPart(responseText, domainPartLink);
		}
	}

	private void updateAndIndexPerson() {
		updateForMainDataGroup(personDataGroup);
		int responseCode = coraIndexer.handleWorkorderType(INDEX, recordType, recordId);
		logErrorIfResponseNotOk(responseCode, recordType);
	}

	private void logErrorIfResponseNotOk(int responseCode, String type) {
		if (responseCode != 200) {
			logger.logErrorUsingMessage(
					"Error when indexing record from synchronizer, " + action + " " + type + ".");
		}
	}

	private void updateForMainDataGroup(DataGroup dataGroup) {
		recordStorage.update(recordType, recordId, dataGroup, createCollectedTerms(),
				createLinkList(), dataDivider);
	}

	private void updateAndIndexDomainPart(String responseText, DataGroup domainPartLink) {
		DataGroup personDomainPart = convertDomainPart(responseText);
		String linkedRecordId = domainPartLink.getFirstAtomicValueWithNameInData("linkedRecordId");
		recordStorage.update(PERSON_DOMAIN_PART, linkedRecordId, personDomainPart,
				createCollectedTerms(), createLinkList(), dataDivider);
		indexDomainPart(linkedRecordId);
	}

}
