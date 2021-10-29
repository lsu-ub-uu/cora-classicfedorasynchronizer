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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FedoraMessageParser implements MessageParser {
	private static final String PID_REGEX = "^[a-z-]*:\\d*$";
	private static final String TEXT_TO_IDENTIFY_MESSAGES_FOR_DELETE = ""
			+ "<category term=\"D\" scheme=\"fedora-types:state\" label=\"xsd:string\"></category>";
	private String message;
	private String pid;

	private String parsedRecordId;
	private String parsedType;
	private String methodName;
	private String modificationType;
	private boolean synchronizationRequired = false;

	@Override
	public void parseHeadersAndMessage(Map<String, String> headers, String message) {
		setFieldVariables(headers, message);
		if (synchronizationRequiredForMessage()) {
			setValuesInThisClass();
		}
	}

	private void setFieldVariables(Map<String, String> headers, String message) {
		pid = headers.get("pid");
		methodName = headers.get("methodName");
		this.message = message;
	}

	private boolean synchronizationRequiredForMessage() {
		return pidExistsAndMatchFormat() && typeIsAuthorityPerson() && actionIsRelevant();
	}

	private boolean pidExistsAndMatchFormat() {
		return pid != null && idMatchFormat(pid);
	}

	private boolean idMatchFormat(String pid) {
		Pattern idPattern = Pattern.compile(PID_REGEX);
		Matcher idMatcher = idPattern.matcher(pid);
		return idMatcher.matches();
	}

	private boolean typeIsAuthorityPerson() {
		String type = extractTypePartOfId(pid);
		return "authority-person".equals(type);
	}

	private String extractTypePartOfId(String pid) {
		return pid.substring(0, pid.indexOf(':'));
	}

	private boolean actionIsRelevant() {
		return isCreateAction() || isUpdateAction() || isDeleteAction();
	}

	private boolean isCreateAction() {
		return "addDatastream".equals(methodName);
	}

	private boolean isUpdateAction() {
		return "modifyDatastreamByReference".equals(methodName);
	}

	private boolean isDeleteAction() {
		return isDelete() || isPurge();
	}

	private boolean isPurge() {
		return "purgeObject".equals(methodName);
	}

	private boolean isDelete() {
		return "modifyObject".equals(methodName)
				&& message.contains(TEXT_TO_IDENTIFY_MESSAGES_FOR_DELETE);
	}

	private void setValuesInThisClass() {
		parsedRecordId = pid;
		parsedType = "person";
		synchronizationRequired = true;
		modificationType = getModificationType();
	}

	private String getModificationType() {
		if (isDeleteAction()) {
			return "delete";
		}
		if (isCreateAction()) {
			return "create";
		}
		return "update";
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
