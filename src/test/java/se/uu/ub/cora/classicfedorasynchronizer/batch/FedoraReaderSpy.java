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
package se.uu.ub.cora.classicfedorasynchronizer.batch;

import java.util.List;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.fedora.reader.FedoraReader;
import se.uu.ub.cora.testutils.mcr.MethodCallRecorder;

public class FedoraReaderSpy implements FedoraReader {
	MethodCallRecorder MCR = new MethodCallRecorder();

	public String type;
	public DataGroup filter;
	public List<String> listAll = List.of("auhority-person:245", "auhority-person:322",
			"auhority-person:4029", "auhority-person:127", "auhority-person:1211");
	public List<String> listCreatedAfter = List.of("auhority-person:104", "auhority-person:22",
			"auhority-person:2131");

	public List<String> listUpdatedAfter = List.of("auhority-person:245", "auhority-person:322");

	public List<String> listDeletedAfter = List.of("auhority-person:127", "auhority-person:1211");

	@Override
	public String readObject(String objectId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> readList(String type, DataGroup filter) {
		return null;
	}

	@Override
	public void setMaxResults(int count) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<String> readPidsForType(String type) {
		MCR.addCall("type", type);

		this.type = type;

		MCR.addReturned(listAll);
		return listAll;
	}

	@Override
	public List<String> readPidsForTypeCreatedAfter(String type, String dateTime) {
		MCR.addCall("type", type, "dateTime", dateTime);

		MCR.addReturned(listCreatedAfter);
		return listCreatedAfter;
	}

	@Override
	public List<String> readPidsForTypeCreatedBeforeAndUpdatedAfter(String type, String dateTime) {
		MCR.addCall("type", type, "dateTime", dateTime);
		// TODO Auto-generated method stub
		MCR.addReturned(listUpdatedAfter);
		return listUpdatedAfter;
	}

	@Override
	public List<String> readPidsForTypeDeletedAfter(String type, String dateTime) {
		MCR.addCall("type", type, "dateTime", dateTime);
		// TODO Auto-generated method stub
		MCR.addReturned(listDeletedAfter);
		return listDeletedAfter;
	}

}
