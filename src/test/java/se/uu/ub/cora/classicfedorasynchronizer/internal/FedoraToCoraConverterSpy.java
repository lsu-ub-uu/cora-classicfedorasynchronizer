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

import java.util.Map;

import se.uu.ub.cora.classicfedorasynchronizer.FedoraToCoraConverter;
import se.uu.ub.cora.data.DataGroup;

public class FedoraToCoraConverterSpy implements FedoraToCoraConverter {

	public String xml;
	public DataGroupSpy factoredGroup;

	@Override
	public DataGroup fromXML(String xml) {
		this.xml = xml;
		factoredGroup = new DataGroupSpy("someNameInData");
		return factoredGroup;
	}

	@Override
	public DataGroup fromXMLWithParameters(String xml, Map<String, Object> parameters) {
		return null;
	}
}
