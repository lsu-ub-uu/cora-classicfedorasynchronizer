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
import se.uu.ub.cora.classicfedorasynchronizer.FedoraConverterFactory;
import se.uu.ub.cora.classicfedorasynchronizer.FedoraToCoraConverter;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.data.DataGroupProvider;
import se.uu.ub.cora.httphandler.HttpHandler;
import se.uu.ub.cora.httphandler.HttpHandlerFactory;
import se.uu.ub.cora.storage.RecordNotFoundException;
import se.uu.ub.cora.storage.RecordStorage;

//TODO: better name for class
public class ClassicCoraSynchronizerImp implements ClassicCoraSynchronizer {

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

	public ClassicCoraSynchronizerImp(RecordStorage recordStorage,
			HttpHandlerFactory httpHandlerFactory, FedoraConverterFactory fedoraConverterFactory,
			String baseURL) {
		this.recordStorage = recordStorage;
		this.httpHandlerFactory = httpHandlerFactory;
		this.fedoraConverterFactory = fedoraConverterFactory;
		this.baseURL = baseURL;
	}

	public HttpHandlerFactory getHttpHandlerFactory() {
		return httpHandlerFactory;
	}

	@Override
	public void synchronize(String recordType, String recordId, String action, String dataDivider) {
		this.recordType = recordType;
		this.recordId = recordId;
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
		createForMainDataGroup(personDataGroup);
		for (DataGroup domainPartLink : domainParts) {
			DataGroup personDomainPart = convertDomainPart(responseText);
			createDomainPart(domainPartLink, personDomainPart);
		}
	}

	private void createForMainDataGroup(DataGroup dataGroup) {
		recordStorage.create(recordType, recordId, dataGroup, createCollectedTerms(),
				createLinkList(), dataDivider);
	}

	private void createDomainPart(DataGroup domainPartLink, DataGroup personDomainPart) {
		String linkedRecordId = domainPartLink.getFirstAtomicValueWithNameInData("linkedRecordId");
		recordStorage.create(PERSON_DOMAIN_PART, linkedRecordId, personDomainPart,
				createCollectedTerms(), createLinkList(), dataDivider);
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
		updateForMainDataGroup(personDataGroup);
		for (DataGroup domainPartLink : domainParts) {
			updateDomainPart(responseText, domainPartLink);
		}
	}

	private void updateForMainDataGroup(DataGroup dataGroup) {
		recordStorage.update(recordType, recordId, dataGroup, createCollectedTerms(),
				createLinkList(), dataDivider);
	}

	private void updateDomainPart(String responseText, DataGroup domainPartLink) {
		DataGroup personDomainPart = convertDomainPart(responseText);
		String linkedRecordId = domainPartLink.getFirstAtomicValueWithNameInData("linkedRecordId");
		recordStorage.update(PERSON_DOMAIN_PART, linkedRecordId, personDomainPart,
				createCollectedTerms(), createLinkList(), dataDivider);
	}
}
