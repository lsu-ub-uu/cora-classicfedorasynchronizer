/*
 * Copyright 2019, 2021 Uppsala University Library
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

package se.uu.ub.cora.classicfedorasynchronizer.messaging.parsning;

import java.util.Map;

import se.uu.ub.cora.classicfedorasynchronizer.messaging.FedoraMessageException;
import se.uu.ub.cora.logger.Logger;
import se.uu.ub.cora.logger.LoggerProvider;

public class FedoraMessageParser implements MessageParser {
	private static final String TEXT_TO_IDENTIFY_MESSAGES_FOR_DELETE = ""
			+ "<category term=\"D\" scheme=\"fedora-types:state\" label=\"xsd:string\"></category>";
	private Logger logger = LoggerProvider.getLoggerForClass(FedoraMessageParser.class);
	private String parsedRecordId;
	private boolean synchronizationRequired = false;
	private String parsedType;
	private String modificationType;

	@Override
	public void parseHeadersAndMessage(Map<String, String> headers, String message) {
		try {
			tryToParseMessage(headers, message);
		} catch (FedoraMessageException exception) {
			handleError(exception);
		}
	}

	private void tryToParseMessage(Map<String, String> headers, String message) {
		throwErrorIfNoPid(headers);
		synchronizationRequired = workOrderShouldBeCreatedForMessage(headers, message);
		possibleSetValues(headers, message);
	}

	private void throwErrorIfNoPid(Map<String, String> headers) {
		if (headers.get("pid") == null) {
			throw FedoraMessageException.withMessage("No pid found in header");
		}
	}

	private boolean workOrderShouldBeCreatedForMessage(Map<String, String> headers,
			String message) {
		String methodName = headers.get("methodName");
		String typePartOfId = extractTypePartOfId(headers);

		return calculateWorkOrderShouldBeCreated(message, methodName, typePartOfId);
	}

	private String extractTypePartOfId(Map<String, String> headers) {
		String pid = headers.get("pid");
		return pid.substring(0, pid.indexOf(':'));
	}

	private boolean calculateWorkOrderShouldBeCreated(String message, String methodName,
			String typePartOfId) {
		return (methodNameIsRelevant(methodName) || isDeleteMessage(message, methodName))
				&& typeIsAuthorityPerson(typePartOfId);
	}

	private boolean methodNameIsRelevant(String methodName) {
		return "modifyDatastreamByReference".equals(methodName) || isPurgeMessage(methodName)
				|| "addDatastream".equals(methodName);
	}

	private boolean isPurgeMessage(String methodName) {
		return "purgeObject".equals(methodName);
	}

	private boolean isDeleteMessage(String message, String methodName) {
		return "modifyObject".equals(methodName)
				&& message.contains(TEXT_TO_IDENTIFY_MESSAGES_FOR_DELETE);
	}

	private boolean typeIsAuthorityPerson(String typePartOfId) {
		return "authority-person".equals(typePartOfId);
	}

	private void possibleSetValues(Map<String, String> headers, String message) {
		if (synchronizationRequired) {
			parsedRecordId = headers.get("pid");
			parsedType = "person";
			setModificationTypeFromMessageAndHeaders(message, headers);
		}
	}

	private void setModificationTypeFromMessageAndHeaders(String message,
			Map<String, String> headers) {
		String methodName = headers.get("methodName");
		modificationType = "update";
		possiblyChangeModificationTypeToDelete(message, methodName);
	}

	private void possiblyChangeModificationTypeToDelete(String message, String methodName) {
		if (messageIsFromDeleteOrPurge(message, methodName)) {
			modificationType = "delete";
		}
		if ("addDatastream".equals(methodName)) {
			modificationType = "create";
		}
	}

	private boolean messageIsFromDeleteOrPurge(String message, String methodName) {
		return isDeleteMessage(message, methodName) || isPurgeMessage(methodName);
	}

	private void handleError(FedoraMessageException e) {
		logger.logErrorUsingMessage(e.getMessage());
	}

	@Override
	public String getRecordId() {
		return parsedRecordId;
	}

	@Override
	public String getRecordType() {
		return parsedType;
	}

	@Override
	public boolean synchronizationRequired() {
		return synchronizationRequired;
	}

	@Override
	public String getAction() {
		return modificationType;
	}

}
