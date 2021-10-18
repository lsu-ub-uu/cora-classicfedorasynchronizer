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

import se.uu.ub.cora.testutils.mcr.MethodCallRecorder;

public class MessageParserSpy implements MessageParser {

	public Map<String, String> headers;
	public String message;
	public boolean getParsedIdWasCalled = false;
	public boolean getParsedTypeWasCalled = false;
	public boolean getModificationTypeWasCalled = false;
	public boolean synchronizationRequired = true;
	public String modificationType = "update";

	public MethodCallRecorder MCR = new MethodCallRecorder();

	@Override
	public void parseHeadersAndMessage(Map<String, String> headers, String message) {
		MCR.addCall("headers", headers, "message", message);

		this.headers = headers;
		this.message = message;
	}

	@Override
	public String getRecordId() {
		MCR.addCall();
		getParsedIdWasCalled = true;

		String recordId = "someParsedIdFromMessageParserSpy";
		MCR.addReturned(recordId);
		return recordId;
	}

	@Override
	public String getRecordType() {
		MCR.addCall();
		getParsedTypeWasCalled = true;

		String recordType = "someParsedTypeFromMessageParserSpy";
		MCR.addReturned(recordType);
		return recordType;
	}

	@Override
	public boolean synchronizationRequired() {
		MCR.addCall();

		MCR.addReturned(synchronizationRequired);
		return synchronizationRequired;
	}

	@Override
	public String getAction() {
		MCR.addCall();
		getModificationTypeWasCalled = true;

		MCR.addReturned(modificationType);
		return modificationType;
	}

}
