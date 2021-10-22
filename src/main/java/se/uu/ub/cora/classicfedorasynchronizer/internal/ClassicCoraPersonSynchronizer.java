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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	private static final int HTTP_STATUS_OK = 200;

	private static Logger logger = LoggerProvider
			.getLoggerForClass(ClassicCoraPersonSynchronizer.class);

	private static final String INDEX = "index";
	private static final String REMOVE_FROM_INDEX = "removeFromIndex";
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
	private String xmlFromFedora;
	private List<DataGroup> domainParts;

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

		synchronizeDependingOnAction();
	}

	private void synchronizeDependingOnAction() {
		possiblySynchronizeCreateOrUpdate();
		possiblySynchronizeDelete();
	}

	private void possiblySynchronizeDelete() {
		if (isDeleteAction()) {
			synchronizeDelete();
		}
	}

	private boolean isDeleteAction() {
		return "delete".equals(action);
	}

	private void possiblySynchronizeCreateOrUpdate() {
		if (isCreateOrUpdateAction()) {
			synchronizeCreateOrUpdate();
		}
	}

	private void prepareHttpHandler() {
		createHttpHandlerToFedora();
		throwErrorIfRecordNotFound();
	}

	private boolean isCreateOrUpdateAction() {
		return isCreateAction() || isUpdateAction();
	}

	private boolean isUpdateAction() {
		return "update".equals(action);
	}

	private void synchronizeDelete() {
		DataGroup readDataGroup = recordStorage.read(recordType, recordId);
		removeRecordAndLinks(readDataGroup);
		removeIndexRecord(recordId);
	}

	private void removeRecordAndLinks(DataGroup readDataGroup) {
		removeDomainPartsAndIndexes(readDataGroup);
		recordStorage.deleteByTypeAndId(recordType, recordId);
	}

	private void removeIndexRecord(String recordId) {
		indexRecord(REMOVE_FROM_INDEX, this.recordType, recordId);
	}

	private void createIndexRecord(String recordId) {
		indexRecord(INDEX, this.recordType, recordId);
	}

	private void createIndexRecordPersonDomainPart(String recordId) {
		indexRecord(INDEX, PERSON_DOMAIN_PART, recordId);
	}

	private void removeIndexRecordPersonDomainPart(String recordId) {
		indexRecord(REMOVE_FROM_INDEX, PERSON_DOMAIN_PART, recordId);
	}

	private void removeDomainPartsAndIndexes(DataGroup readDataGroup) {
		List<DataGroup> personDomainParts = readDataGroup
				.getAllGroupsWithNameInData(PERSON_DOMAIN_PART);
		for (DataGroup personDomainPart : personDomainParts) {
			removeDomainPartAndIndex(personDomainPart);
		}
	}

	private void removeDomainPartAndIndex(DataGroup personDomainPart) {
		String linkedRecordId = extractRecordId(personDomainPart);
		recordStorage.deleteByTypeAndId(PERSON_DOMAIN_PART, linkedRecordId);
		removeIndexRecordPersonDomainPart(linkedRecordId);
	}

	private String extractRecordId(DataGroup personDomainPart) {
		return personDomainPart.getFirstAtomicValueWithNameInData("linkedRecordId");
	}

	private void logErrorIfResponseNotOk(int responseCode, String type) {
		if (responseCode != HTTP_STATUS_OK) {
			logger.logErrorUsingMessage(
					"Error when indexing record from synchronizer, " + action + " " + type + ".");
		}
	}

	private void createHttpHandlerToFedora() {
		String fedoraUrl = baseURL + "objects/" + recordId + "/datastreams/METADATA/content";
		httpHandler = httpHandlerFactory.factor(fedoraUrl);
		httpHandler.setRequestMethod("GET");
	}

	private void throwErrorIfRecordNotFound() {
		if (httpHandler.getResponseCode() == NOT_FOUND) {
			throw new RecordNotFoundException("Record not found for recordType: " + recordType
					+ " and recordId: " + recordId);
		}
	}

	private void synchronizeCreateOrUpdate() {
		prepareHttpHandler();
		xmlFromFedora = httpHandler.getResponseText();
		personDataGroup = convertPersonToDataGroup(xmlFromFedora);
		domainParts = getPersonDomainPartsFromPerson();

		possiblySynchronizeCreate();
		possiblySynchronizeUpdate();
	}

	private void possiblySynchronizeCreate() {
		if (isCreateAction()) {
			synchronizeCreate();
		}
	}

	private void possiblySynchronizeUpdate() {
		if (isUpdateAction()) {
			synchronizeUpdate();
		}
	}

	private boolean isCreateAction() {
		return "create".equals(action);
	}

	private List<DataGroup> getPersonDomainPartsFromPerson() {
		return personDataGroup.getAllGroupsWithNameInData(PERSON_DOMAIN_PART);
	}

	private DataGroup convertPersonToDataGroup(String responseText) {
		FedoraToCoraConverter toCoraConverter = fedoraConverterFactory
				.factorToCoraConverter("person");
		return toCoraConverter.fromXML(responseText);
	}

	private void synchronizeCreate() {
		storeAndIndexPerson();
		createAndIndexPersonDomainParts();
	}

	private void indexRecord(String indexAction, String recordType, String recordId) {
		int responseCode = coraIndexer.handleWorkorderType(indexAction, recordType, recordId);
		logErrorIfResponseNotOk(responseCode, recordType);
	}

	private void createAndIndexPersonDomainParts() {
		for (DataGroup domainPartLink : domainParts) {
			storeDomainPartsAndIndex(domainPartLink);
		}
	}

	private void storeDomainPartsAndIndex(DataGroup domainPartLink) {
		String linkedRecordId = extractRecordId(domainPartLink);
		DataGroup personDomainPart = convertDomainPart(linkedRecordId);
		storePersonDomainParts(linkedRecordId, personDomainPart);
		createIndexRecordPersonDomainPart(linkedRecordId);
	}

	private void storePersonDomainParts(String linkedRecordId, DataGroup personDomainPart) {
		possiblyCreatePersonDomainParts(linkedRecordId, personDomainPart);
		possiblyUpdatePersonDomainParts(linkedRecordId, personDomainPart);
	}

	private void possiblyUpdatePersonDomainParts(String linkedRecordId,
			DataGroup personDomainPart) {
		if (isUpdateAction()) {
			recordStorage.update(PERSON_DOMAIN_PART, linkedRecordId, personDomainPart,
					createCollectedTerms(), createLinkList(), dataDivider);
		}
	}

	private void possiblyCreatePersonDomainParts(String linkedRecordId,
			DataGroup personDomainPart) {
		if (isCreateAction()) {
			recordStorage.create(PERSON_DOMAIN_PART, linkedRecordId, personDomainPart,
					createCollectedTerms(), createLinkList(), dataDivider);
		}
	}

	private void storeAndIndexPerson() {
		storePerson();
		createIndexRecord(recordId);
	}

	private void storePerson() {
		possiblyCreatePerson();
		possiblyUpdatePerson();
	}

	private void possiblyUpdatePerson() {
		if (isUpdateAction()) {
			recordStorage.update(recordType, recordId, personDataGroup, createCollectedTerms(),
					createLinkList(), dataDivider);
		}
	}

	private void possiblyCreatePerson() {
		if (isCreateAction()) {
			recordStorage.create(recordType, recordId, personDataGroup, createCollectedTerms(),
					createLinkList(), dataDivider);
		}
	}

	private DataGroup convertDomainPart(String linkedRecordId) {
		FedoraToCoraConverter domainPartConverter = fedoraConverterFactory
				.factorToCoraConverter(PERSON_DOMAIN_PART);
		Map<String, Object> parameters = createXSLTParameters(linkedRecordId);
		return domainPartConverter.fromXMLWithParameters(xmlFromFedora, parameters);
	}

	private Map<String, Object> createXSLTParameters(String id) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("domainFilter", extractDomainFromId(id));
		return parameters;
	}

	private String extractDomainFromId(String id) {
		int domainStartIndex = id.lastIndexOf(':');
		return id.substring(domainStartIndex + 1);
	}

	private DataGroup createLinkList() {
		return DataGroupProvider.getDataGroupUsingNameInData("collectedDataLinks");
	}

	private DataGroup createCollectedTerms() {
		return DataGroupProvider.getDataGroupUsingNameInData("collectedData");
	}

	private void synchronizeUpdate() {
		storeAndIndexPerson();
		updateAndIndexPersonDomainParts();
	}

	private void updateAndIndexPersonDomainParts() {
		for (DataGroup domainPartLink : domainParts) {
			storeDomainPartsAndIndex(domainPartLink);
		}
	}

	public RecordStorage onlyForTestGetRecordStorage() {
		return recordStorage;
	}

	public FedoraConverterFactory onlyForTestGetFedoraConverterFactory() {
		return fedoraConverterFactory;
	}

	public HttpHandlerFactory onlyForTestGetHttpHandlerFactory() {
		return httpHandlerFactory;
	}

	public String onlyForTestGetBaseUrl() {
		return baseURL;
	}

	public CoraIndexer onlyForTestGetCoraIndexer() {
		return coraIndexer;
	}

}
