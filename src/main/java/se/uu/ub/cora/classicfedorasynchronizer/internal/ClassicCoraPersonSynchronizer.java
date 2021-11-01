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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.uu.ub.cora.classicfedorasynchronizer.ClassicCoraSynchronizer;
import se.uu.ub.cora.classicfedorasynchronizer.FedoraConverterFactory;
import se.uu.ub.cora.classicfedorasynchronizer.FedoraToCoraConverter;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.data.DataGroupProvider;
import se.uu.ub.cora.httphandler.HttpHandler;
import se.uu.ub.cora.httphandler.HttpHandlerFactory;
import se.uu.ub.cora.javaclient.cora.CoraClient;
import se.uu.ub.cora.storage.RecordNotFoundException;
import se.uu.ub.cora.storage.RecordStorage;

/**
 * ClassicCoraPersonSynchronizer implements {@link ClassicCoraSynchronizer} and handles
 * synchronization of Person between classic system and Cora system.
 * 
 * ClassicCoraPersonSynchronizer is NOT threadsafe
 */
public class ClassicCoraPersonSynchronizer implements ClassicCoraSynchronizer {
	private static final String EMPTY_FILTER = "{\"name\":\"filter\",\"children\":[]}";
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
	private CoraClient coraClient;
	private String xmlFromFedora;
	private List<String> domainPartIds;
	private boolean indexRecordImmediately;

	public static ClassicCoraPersonSynchronizer createClassicCoraPersonSynchronizerForMessaging(
			RecordStorage recordStorage, HttpHandlerFactory httpHandlerFactory,
			FedoraConverterFactory fedoraConverterFactory, CoraClient coraClient, String baseURL) {

		return new ClassicCoraPersonSynchronizer(recordStorage, httpHandlerFactory,
				fedoraConverterFactory, coraClient, baseURL, true);
	}

	public static ClassicCoraPersonSynchronizer createClassicCoraPersonSynchronizerForBatch(
			RecordStorage recordStorage, HttpHandlerFactory httpHandlerFactory,
			FedoraConverterFactory fedoraConverterFactory, CoraClient coraClient, String baseURL) {
		return new ClassicCoraPersonSynchronizer(recordStorage, httpHandlerFactory,
				fedoraConverterFactory, coraClient, baseURL, false);
	}

	private ClassicCoraPersonSynchronizer(RecordStorage recordStorage,
			HttpHandlerFactory httpHandlerFactory, FedoraConverterFactory fedoraConverterFactory,
			CoraClient coraClient, String baseURL, boolean indexRecordImmediately) {
		this.recordStorage = recordStorage;
		this.httpHandlerFactory = httpHandlerFactory;
		this.fedoraConverterFactory = fedoraConverterFactory;
		this.coraClient = coraClient;
		this.baseURL = baseURL;
		this.indexRecordImmediately = indexRecordImmediately;

	}

	@Override
	public void synchronizeCreated(String recordType, String recordId, String dataDivider) {
		this.recordType = recordType;
		this.recordId = recordId;
		this.dataDivider = dataDivider;

		synchronizeCreate();
	}

	@Override
	public void synchronizeUpdated(String recordType, String recordId, String dataDivider) {
		this.recordType = recordType;
		this.recordId = recordId;
		this.dataDivider = dataDivider;

		synchronizeUpdate();
	}

	@Override
	public void synchronizeDeleted(String recordType, String recordId, String dataDivider) {
		this.recordType = recordType;
		this.recordId = recordId;
		this.dataDivider = dataDivider;

		synchronizeDelete();
	}

	private void synchronizeCreate() {
		readPersonFromFedora();
		createPersonInStorage();
		possiblyIndexRecord(recordType, recordId);
		createAndPossiblyIndexPersonDomainParts();
	}

	private void readPersonFromFedora() {
		prepareHttpHandler();
		xmlFromFedora = httpHandler.getResponseText();
		personDataGroup = convertPersonToDataGroup(xmlFromFedora);
		domainPartIds = getPersonDomainPartIdsFromPerson(personDataGroup);
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

	private void possiblyIndexRecord(String recordType2, String recordId2) {
		if (indexRecordImmediately) {
			coraClient.indexData(recordType2, recordId2);
		}
	}

	private void createAndPossiblyIndexPersonDomainParts() {
		for (String domainPartId : domainPartIds) {
			createAndIndexDomainPart(domainPartId);
		}
	}

	private void createAndIndexDomainPart(String domainPartId) {
		DataGroup personDomainPart = convertDomainPart(domainPartId);
		createPersonDomainPart(domainPartId, personDomainPart);
		indexPersonDomainPart(domainPartId);
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
		possiblyIndexRecord(PERSON_DOMAIN_PART, recordId);
	}

	private void synchronizeUpdate() {
		DataGroup oldPerson = recordStorage.read(recordType, recordId);

		List<String> oldDomainPartIds = getPersonDomainPartIdsFromPerson(oldPerson);
		readPersonFromFedora();
		updatePerson();
		possiblyIndexRecord(recordType, recordId);
		updateAndIndexPersonDomainParts(oldDomainPartIds);
	}

	private void updatePerson() {
		recordStorage.update(recordType, recordId, personDataGroup, createCollectedTerms(),
				createLinkList(), dataDivider);
	}

	private void updateAndIndexPersonDomainParts(List<String> oldDomainPartIds) {
		handleNewDomainParts(oldDomainPartIds);
		removeOldDomainParts(oldDomainPartIds);
	}

	private void handleNewDomainParts(List<String> oldDomainPartIds) {
		for (String domainPartId : domainPartIds) {
			oldDomainPartIds.remove(domainPartId);
			updateAndIndexPersonDomainPart(domainPartId);
		}
	}

	private void removeOldDomainParts(List<String> oldDomainPartIds) {
		for (String partId : oldDomainPartIds) {
			removeDomainPartAndIndex(partId);
		}
	}

	private void updateAndIndexPersonDomainPart(String domainPartId) {
		DataGroup personDomainPart = convertDomainPart(domainPartId);
		updatePersonDomainPart(domainPartId, personDomainPart);
		indexPersonDomainPart(domainPartId);
	}

	private void updatePersonDomainPart(String linkedRecordId, DataGroup personDomainPart) {
		recordStorage.update(PERSON_DOMAIN_PART, linkedRecordId, personDomainPart,
				createCollectedTerms(), createLinkList(), dataDivider);
	}

	private void synchronizeDelete() {
		DataGroup readDataGroup = recordStorage.read(recordType, recordId);
		removeRecordAndLinks(readDataGroup);
		removeFromIndexUsingTypeAndId(recordType, recordId);
	}

	private void removeRecordAndLinks(DataGroup readDataGroup) {
		removeDomainPartsAndIndexes(readDataGroup);
		recordStorage.deleteByTypeAndId(recordType, recordId);
	}

	private void removeDomainPartsAndIndexes(DataGroup readDataGroup) {
		List<String> partIds = getPersonDomainPartIdsFromPerson(readDataGroup);
		removeOldDomainParts(partIds);
	}

	private void removeDomainPartAndIndex(String domainPartId) {
		recordStorage.deleteByTypeAndId(PERSON_DOMAIN_PART, domainPartId);
		removeFromIndexUsingTypeAndId(PERSON_DOMAIN_PART, domainPartId);
	}

	private void removeFromIndexUsingTypeAndId(String type, String recordId) {
		coraClient.removeFromIndex(type, recordId);
	}

	private List<String> getPersonDomainPartIdsFromPerson(DataGroup dataGroup) {
		List<DataGroup> allGroupsWithNameInData = dataGroup
				.getAllGroupsWithNameInData(PERSON_DOMAIN_PART);
		List<String> partIds = new ArrayList<>(allGroupsWithNameInData.size());
		for (DataGroup domainPart : allGroupsWithNameInData) {
			partIds.add(domainPart.getFirstAtomicValueWithNameInData("linkedRecordId"));
		}
		return partIds;
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

	public CoraClient onlyForTestGetCoraClient() {
		return coraClient;
	}

	public boolean onlyForTestGetExplicitIndexCommit() {
		return indexRecordImmediately;
	}

	@Override
	public void indexAllRecordsForType(String recordType) {
		coraClient.indexRecordsOfType(recordType, EMPTY_FILTER);
	}

}
