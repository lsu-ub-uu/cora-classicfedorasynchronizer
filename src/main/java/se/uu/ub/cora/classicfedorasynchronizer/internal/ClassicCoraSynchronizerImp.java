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

	private static final int NOT_FOUND = 404;
	private HttpHandlerFactory httpHandlerFactory;
	private String baseURL;
	private FedoraConverterFactory fedoraConverterFactory;
	private RecordStorage recordStorage;

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
		HttpHandler httpHandler = setUpHttpHandlerForRead(recordId);
		throwErrorIfRecordNotFound(recordType, recordId, httpHandler);
		String responseText = httpHandler.getResponseText();
		FedoraToCoraConverter toCoraConverter = fedoraConverterFactory
				.factorToCoraConverter("person");

		DataGroup dataGroup = toCoraConverter.fromXML(responseText);
		List<DataGroup> domainParts = dataGroup.getAllGroupsWithNameInData("personDomainPart");

		// DataGroup personDomainPart, convert(personDomainPart)
		if ("create".equals(action)) {
			recordStorage.create(recordType, recordId, dataGroup, createCollectedTerms(),
					createLinkList(), dataDivider);

			// for(String personDomainPartId : personDomainPartIds){
			// FedoraToCoraConverter toCoraConverter = fedoraConverterFactory // *
			// .factorToCoraConverter("personDomainPart");
			// recordStorage.create("personDomainPart", recordId, personDomainPartDataGroup,
			// createCollectedTerms(),
			// createLinkList(), dataDivider);
			// create personDomainPart
		} else {
			recordStorage.update(recordType, recordId, dataGroup, createCollectedTerms(),
					createLinkList(), dataDivider);
			for (DataGroup domainPartLink : domainParts) {
				FedoraToCoraConverter domainPartConverter = fedoraConverterFactory
						.factorToCoraConverter("personDomainPart");
				DataGroup personDomainPart = domainPartConverter.fromXML(responseText);
				String linkedRecordId = domainPartLink
						.getFirstAtomicValueWithNameInData("linkedRecordId");
				// TODO:kolla att id är samma som linkedRecordId i testet
				recordStorage.update("personDomainPart", "authority-person:0:test",
						personDomainPart, createCollectedTerms(), createLinkList(), dataDivider);
			}
			// update personDomainPart
		}
	}

	private DataGroup createLinkList() {
		return DataGroupProvider.getDataGroupUsingNameInData("collectedDataLinks");
	}

	private DataGroup createCollectedTerms() {
		return DataGroupProvider.getDataGroupUsingNameInData("collectedData");
	}

	private DataGroup convert(HttpHandler httpHandler) {
		String responseText = httpHandler.getResponseText();
		FedoraToCoraConverter toCoraConverter = fedoraConverterFactory
				.factorToCoraConverter("person");
		return toCoraConverter.fromXML(responseText);
	}

	private HttpHandler setUpHttpHandlerForRead(String recordId) {
		HttpHandler httpHandler = httpHandlerFactory
				.factor(baseURL + "objects/" + recordId + "/datastreams/METADATA/content");
		httpHandler.setRequestMethod("GET");
		return httpHandler;
	}

	private void throwErrorIfRecordNotFound(String recordType, String recordId,
			HttpHandler httpHandler) {
		if (httpHandler.getResponseCode() == NOT_FOUND) {
			throw new RecordNotFoundException("Record not found for recordType: " + recordType
					+ " and recordId: " + recordId);
		}
	}
}
