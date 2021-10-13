package se.uu.ub.cora.classicfedorasynchronizer.internal;

import se.uu.ub.cora.javaclient.cora.CoraClient;
import se.uu.ub.cora.javaclient.cora.CoraClientException;
import se.uu.ub.cora.javaclient.cora.CoraClientFactory;
import se.uu.ub.cora.logger.Logger;
import se.uu.ub.cora.logger.LoggerProvider;

//import se.uu.ub.cora.synchronizer.initialize.SynchronizerInstanceProvider;
public class CoraIndexerImp {
	private static final int HTTP_STATUS_UNAUTHORIZED = 401;
	private static final int HTTP_STATUS_BAD_REQUEST = 400;
	private static final int HTTP_STATUS_OK = 200;

	private static Logger logger = LoggerProvider.getLoggerForClass(CoraIndexerImp.class);

	// private String recordType;
	// private String recordId;
	private String typeAndIdLogPart;
	private CoraClient coraClient;

	private CoraClientFactory clientFactory;

	public CoraIndexerImp(String recordType, String recordId) {
		// this.recordType = recordType;
		// this.recordId = recordId;
		// typeAndIdLogPart = " RecordType: " + recordType + " and recordId: " + recordId;
		coraClient = factorCoraClient("", "");
	}

	public CoraIndexerImp(CoraClientFactory clientFactory, String userId, String apptoken) {
		this.clientFactory = clientFactory;
		coraClient = factorCoraClient(userId, apptoken);
	}

	private CoraClient factorCoraClient(String userId, String apptoken) {
		return clientFactory.factor(userId, apptoken);
	}

	public int handleWorkorderType(String workOrderType, String recordType, String recordId) {
		if (isRemove(workOrderType)) {
			return tryToRemoveFromIndex(recordType, recordId);
		}
		return tryToAddToIndex(recordType, recordId);
	}

	private boolean isRemove(String workOrderType) {
		return "removeFromIndex".equals(workOrderType);
	}

	private int tryToRemoveFromIndex(String recordType, String recordId) {
		try {
			return removeFromIndex(recordType, recordId);
		} catch (Exception e) {
			return handleRemoveError(e);
		}
	}

	private int removeFromIndex(String recordType, String recordId) {
		logBeforeRemovingFromIndex();
		coraClient.removeFromIndex(recordType, recordId);
		logAfterRemovingFromIndex();
		return HTTP_STATUS_OK;
	}

	private void logBeforeRemovingFromIndex() {
		logToInfoAppendTypeAndId("Removing from index.");
	}

	private void logToInfoAppendTypeAndId(String messagePart) {
		logger.logInfoUsingMessage(messagePart + typeAndIdLogPart);
	}

	private void logAfterRemovingFromIndex() {
		logToInfoAppendTypeAndId("Finished removing.");
	}

	private int handleRemoveError(Exception e) {
		logToErrorAppendTypeAndIdAndError("Error when removing from index.", e);
		return HTTP_STATUS_BAD_REQUEST;
	}

	private void logToErrorAppendTypeAndIdAndError(String message, Exception e) {
		logger.logErrorUsingMessage(message + typeAndIdLogPart + ". " + e.getMessage());
	}

	private int tryToAddToIndex(String recordType, String recordId) {
		try {
			return addToIndex(recordType, recordId);
		} catch (CoraClientException cce) {
			return handleClientErrorDuringIndexing(cce);
		} catch (Exception e) {
			return handleIndexError(e);
		}
	}

	private int addToIndex(String recordType, String recordId) {
		logBeforeIndexing();
		coraClient.indexData(recordType, recordId);
		logAfterIndexing();
		return HTTP_STATUS_OK;
	}

	private void logBeforeIndexing() {
		logToInfoAppendTypeAndId("Indexing record.");
	}

	private void logAfterIndexing() {
		logToInfoAppendTypeAndId("Indexing finished.");
	}

	private int handleClientErrorDuringIndexing(CoraClientException cce) {
		logToErrorAppendTypeAndIdAndError("CoraClient error when indexing record.", cce);
		return HTTP_STATUS_UNAUTHORIZED;
	}

	private int handleIndexError(Exception e) {
		logToErrorAppendTypeAndIdAndError("Error when indexing record.", e);
		return HTTP_STATUS_BAD_REQUEST;
	}

	public CoraClient getCoraClient() {
		return coraClient;
	}
}
