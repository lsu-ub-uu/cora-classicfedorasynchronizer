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

	@Override
	public void synchronize(String recordType, String recordId, String action, String dataDivider) {
		this.recordType = recordType;
		this.recordId = recordId;
		this.action = action;
		this.dataDivider = dataDivider;

		synchronizeDependingOnAction();
	}

	private void synchronizeDependingOnAction() {
		switch (action) {
		case "create" -> synchronizeCreate();
		case "update" -> synchronizeUpdate();
		case "delete" -> synchronizeDelete();
		}
	}

	private void synchronizeCreate() {
		readPersonFromFedora();
		createPersonInStorage();
		indexPerson();
		createAndIndexPersonDomainParts();
	}

	private void readPersonFromFedora() {
		prepareHttpHandler();
		xmlFromFedora = httpHandler.getResponseText();
		personDataGroup = convertPersonToDataGroup(xmlFromFedora);
		domainParts = getPersonDomainPartsFromPerson();
	}

	private void prepareHttpHandler() {
		createHttpHandlerToFedora();
		throwErrorIfRecordNotFound();
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

	private DataGroup convertPersonToDataGroup(String responseText) {
		FedoraToCoraConverter toCoraConverter = fedoraConverterFactory
				.factorToCoraConverter("person");
		return toCoraConverter.fromXML(responseText);
	}

	private void createPersonInStorage() {
		recordStorage.create(recordType, recordId, personDataGroup, createCollectedTerms(),
				createLinkList(), dataDivider);
	}

	private void indexPerson() {
		int responseCode = coraIndexer.handleWorkorderType(INDEX, recordType, recordId);
		logErrorIfResponseNotOk(responseCode, recordType);
	}

	private void logErrorIfResponseNotOk(int responseCode, String type) {
		if (responseCode != HTTP_STATUS_OK) {
			logger.logErrorUsingMessage(
					"Error when indexing record from synchronizer, " + action + " " + type + ".");
		}
	}

	private void createAndIndexPersonDomainParts() {
		for (DataGroup domainPartLink : domainParts) {
			createAndIndexDomainPart(domainPartLink);
		}
	}

	private void createAndIndexDomainPart(DataGroup domainPartLink) {
		String linkedRecordId = extractRecordId(domainPartLink);
		DataGroup personDomainPart = convertDomainPart(linkedRecordId);
		createPersonDomainPart(linkedRecordId, personDomainPart);
		indexPersonDomainPart(linkedRecordId);
	}

	private String extractRecordId(DataGroup personDomainPart) {
		return personDomainPart.getFirstAtomicValueWithNameInData("linkedRecordId");
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

	private void createPersonDomainPart(String linkedRecordId, DataGroup personDomainPart) {
		recordStorage.create(PERSON_DOMAIN_PART, linkedRecordId, personDomainPart,
				createCollectedTerms(), createLinkList(), dataDivider);
	}

	private void indexPersonDomainPart(String recordId) {
		int responseCode = coraIndexer.handleWorkorderType(INDEX, PERSON_DOMAIN_PART, recordId);
		logErrorIfResponseNotOk(responseCode, PERSON_DOMAIN_PART);
	}

	private void synchronizeUpdate() {
		readPersonFromFedora();
		updatePerson();
		indexPerson();
		updateAndIndexPersonDomainParts();
	}

	private void updatePerson() {
		recordStorage.update(recordType, recordId, personDataGroup, createCollectedTerms(),
				createLinkList(), dataDivider);
	}

	private void updateAndIndexPersonDomainParts() {
		for (DataGroup domainPartLink : domainParts) {
			updateAndIndexPersonDomainPart(domainPartLink);
		}
	}

	private void updateAndIndexPersonDomainPart(DataGroup domainPartLink) {
		String linkedRecordId = extractRecordId(domainPartLink);
		DataGroup personDomainPart = convertDomainPart(linkedRecordId);
		updatePersonDomainPart(linkedRecordId, personDomainPart);
		indexPersonDomainPart(linkedRecordId);
	}

	private void updatePersonDomainPart(String linkedRecordId, DataGroup personDomainPart) {
		recordStorage.update(PERSON_DOMAIN_PART, linkedRecordId, personDomainPart,
				createCollectedTerms(), createLinkList(), dataDivider);
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

	private void removeIndexRecordPersonDomainPart(String recordId) {
		int responseCode = coraIndexer.handleWorkorderType(REMOVE_FROM_INDEX, PERSON_DOMAIN_PART,
				recordId);
		logErrorIfResponseNotOk(responseCode, PERSON_DOMAIN_PART);
	}

	private void removeIndexRecord(String recordId) {
		int responseCode = coraIndexer.handleWorkorderType(REMOVE_FROM_INDEX, recordType, recordId);
		logErrorIfResponseNotOk(responseCode, recordType);
	}

	private List<DataGroup> getPersonDomainPartsFromPerson() {
		return personDataGroup.getAllGroupsWithNameInData(PERSON_DOMAIN_PART);
	}

	private DataGroup createLinkList() {
		return DataGroupProvider.getDataGroupUsingNameInData("collectedDataLinks");
	}

	private DataGroup createCollectedTerms() {
		return DataGroupProvider.getDataGroupUsingNameInData("collectedData");
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
