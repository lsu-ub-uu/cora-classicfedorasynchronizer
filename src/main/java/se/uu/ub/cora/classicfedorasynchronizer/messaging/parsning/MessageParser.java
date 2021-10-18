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

/**
 * MessageParser is used to parse and extract data from headers and body from a JMS message
 */
public interface MessageParser {
	/**
	 * parseHeadersAndMessage is used to parse headers and a body from a JMS message
	 * 
	 * @param headers
	 *            Map of all headers from a JMS message
	 * @param body
	 *            String with the body of a JMS message
	 */
	void parseHeadersAndMessage(Map<String, String> headers, String body);

	/**
	 * synchronizationRequiered is a flag which indicates whether the parse message should be
	 * synchronized or not.
	 * 
	 * @return A boolean
	 */
	boolean synchronizationRequired();

	/**
	 * getRecordType reads the recordType from the JMS message
	 * 
	 * @return a String.
	 */
	String getRecordType();

	/**
	 * getRecordId reads the recordId from the JMS message
	 * 
	 * @return a String
	 */
	String getRecordId();

	/**
	 * getAction reads the recordId from the JMS message.
	 * 
	 * @return a String.
	 */
	String getAction();

}
