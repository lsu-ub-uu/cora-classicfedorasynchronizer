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

import java.util.ArrayList;
import java.util.List;

import se.uu.ub.cora.classicfedorasynchronizer.CoraIndexer;
import se.uu.ub.cora.testutils.mcr.MethodCallRecorder;

public class CoraIndexerSpy implements CoraIndexer {

	public List<String> workOrderTypes = new ArrayList<>();
	public List<String> recordTypes = new ArrayList<>();
	public List<String> recordIds = new ArrayList<>();
	public List<String> typesToThrowErrorFor = new ArrayList<>();
	MethodCallRecorder MCR = new MethodCallRecorder();

	@Override
	public int handleWorkorderType(String workOrderType, String recordType, String recordId) {
		MCR.addCall("workOrderType", workOrderType, "recordType", recordType, "recordId", recordId);
		this.workOrderTypes.add(workOrderType);
		this.recordTypes.add(recordType);
		this.recordIds.add(recordId);
		int code = 200;
		if (typesToThrowErrorFor.contains(recordType)) {
			code = 401;
		}
		MCR.addReturned(code);
		return code;
	}

}
