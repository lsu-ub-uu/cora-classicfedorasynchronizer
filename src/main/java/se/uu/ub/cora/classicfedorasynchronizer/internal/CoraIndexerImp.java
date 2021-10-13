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

import se.uu.ub.cora.classicfedorasynchronizer.CoraIndexer;
import se.uu.ub.cora.javaclient.cora.CoraClient;
import se.uu.ub.cora.javaclient.cora.CoraClientException;
import se.uu.ub.cora.javaclient.cora.CoraClientFactory;
import se.uu.ub.cora.logger.Logger;
import se.uu.ub.cora.logger.LoggerProvider;

public class CoraIndexerImp implements CoraIndexer {
	private static Logger logger = LoggerProvider.getLoggerForClass(CoraIndexerImp.class);

	private static final int HTTP_STATUS_OK = 200;
	private static final int HTTP_STATUS_BAD_REQUEST = 400;
	private static final int HTTP_STATUS_UNAUTHORIZED = 401;

	private CoraClient coraClient;
	private CoraClientFactory clientFactory;
	private String userId;
	private String apptoken;

	public CoraIndexerImp(CoraClientFactory clientFactory, String userId, String apptoken) {
		this.clientFactory = clientFactory;
		this.userId = userId;
		this.apptoken = apptoken;
	}

	@Override
	public int handleWorkorderType(String workOrderType, String recordType, String recordId) {
		coraClient = factorCoraClient(getUserId(), getApptoken());
		if (isRemove(workOrderType)) {
			return tryToRemoveFromIndex(recordType, recordId);
		}
		return tryToAddToIndex(recordType, recordId);
	}

	private CoraClient factorCoraClient(String userId, String apptoken) {
		return clientFactory.factor(userId, apptoken);
	}

	private boolean isRemove(String workOrderType) {
		return "removeFromIndex".equals(workOrderType);
	}

	private int tryToRemoveFromIndex(String recordType, String recordId) {
		try {
			return removeFromIndex(recordType, recordId);
		} catch (Exception e) {
			return handleRemoveError(e, recordType, recordId);
		}
	}

	private int removeFromIndex(String recordType, String recordId) {
		String typeAndIdPart = composeTypeAndIdPart(recordType, recordId);
		logBeforeRemovingFromIndex(typeAndIdPart);
		coraClient.removeFromIndex(recordType, recordId);
		logAfterRemovingFromIndex(typeAndIdPart);
		return HTTP_STATUS_OK;
	}

	private void logBeforeRemovingFromIndex(String typeAndIdPart) {
		logToInfoAppendTypeAndId("Removing from index.", typeAndIdPart);
	}

	private void logToInfoAppendTypeAndId(String messagePart, String typeAndIdPart) {
		logger.logInfoUsingMessage(messagePart + typeAndIdPart);
	}

	private String composeTypeAndIdPart(String recordType, String recordId) {
		return " RecordType: " + recordType + " and recordId: " + recordId + ".";
	}

	private void logAfterRemovingFromIndex(String typeAndIdPart) {
		logToInfoAppendTypeAndId("Finished removing.", typeAndIdPart);
	}

	private int handleRemoveError(Exception e, String recordType, String recordId) {
		String message = composeRemoveErrorMessage(recordType, recordId);
		logger.logErrorUsingMessage(message + " " + e.getMessage());
		return HTTP_STATUS_BAD_REQUEST;
	}

	private String composeRemoveErrorMessage(String recordType, String recordId) {
		return "Error when removing from index." + composeTypeAndIdPart(recordType, recordId);
	}

	private int tryToAddToIndex(String recordType, String recordId) {
		try {
			String typeAndIdPart = composeTypeAndIdPart(recordType, recordId);
			return addToIndex(recordType, recordId, typeAndIdPart);
		} catch (CoraClientException cce) {
			return handleClientErrorDuringIndexing(cce, recordType, recordId);
		} catch (Exception e) {
			return handleIndexError(e, recordType, recordId);
		}
	}

	private int addToIndex(String recordType, String recordId, String typeAndIdPart) {
		logBeforeIndexing(typeAndIdPart);
		coraClient.indexData(recordType, recordId);
		logAfterIndexing(typeAndIdPart);
		return HTTP_STATUS_OK;
	}

	private void logBeforeIndexing(String typeAndIdPart) {
		logToInfoAppendTypeAndId("Indexing record.", typeAndIdPart);
	}

	private void logAfterIndexing(String typeAndIdPart) {
		logToInfoAppendTypeAndId("Indexing finished.", typeAndIdPart);
	}

	private int handleClientErrorDuringIndexing(CoraClientException cce, String recordType,
			String recordId) {
		String message = composeCoraClientErrorMessage(recordType, recordId);
		logger.logErrorUsingMessage(message + " " + cce.getMessage());
		return HTTP_STATUS_UNAUTHORIZED;
	}

	private String composeCoraClientErrorMessage(String recordType, String recordId) {
		return "CoraClient error when indexing record."
				+ composeTypeAndIdPart(recordType, recordId);
	}

	private int handleIndexError(Exception e, String recordType, String recordId) {
		String message = composeErrorMessage(recordType, recordId);
		logger.logErrorUsingMessage(message + " " + e.getMessage());
		return HTTP_STATUS_BAD_REQUEST;
	}

	private String composeErrorMessage(String recordType, String recordId) {
		return "Error when indexing record." + composeTypeAndIdPart(recordType, recordId);
	}

	CoraClient getCoraClient() {
		return coraClient;
	}

	CoraClientFactory getCoraClientFactory() {
		return clientFactory;
	}

	String getUserId() {
		return userId;
	}

	String getApptoken() {
		return apptoken;
	}
}
