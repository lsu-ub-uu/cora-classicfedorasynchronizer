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
import java.util.Collection;
import java.util.List;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.storage.RecordStorage;
import se.uu.ub.cora.storage.StorageReadResult;
import se.uu.ub.cora.testutils.mcr.MethodCallRecorder;

public class RecordStorageSpy implements RecordStorage {
	MethodCallRecorder MCR = new MethodCallRecorder();

	public List<String> alteredRecordTypes = new ArrayList<>();
	public List<String> readRecordTypes = new ArrayList<>();
	public List<String> alteredRecordIds = new ArrayList<>();
	public List<String> readRecordIds = new ArrayList<>();
	public List<DataGroup> handledDataGroups = new ArrayList<>();
	public List<String> dataDividers = new ArrayList<>();
	public List<DataGroup> collectedDataDataGroups = new ArrayList<>();
	public List<DataGroup> linkListDataGroups = new ArrayList<>();
	public DataGroupSpy readDataGroup;

	@Override
	public DataGroup read(String type, String id) {
		MCR.addCall("type", type, "id", id);
		readRecordTypes.add(type);
		readRecordIds.add(id);
		if (readDataGroup == null) {
			readDataGroup = new DataGroupSpy("someNameInData");
		}
		MCR.addReturned(readDataGroup);
		return readDataGroup;
	}

	@Override
	public void create(String type, String id, DataGroup record, DataGroup collectedTerms,
			DataGroup linkList, String dataDivider) {
		MCR.addCall("type", type, "id", id, "record", record, "collectedTerms", collectedTerms,
				"linkList", linkList, "dataDivider", dataDivider);
		alteredRecordTypes.add(type);
		alteredRecordIds.add(id);
		handledDataGroups.add(record);
		collectedDataDataGroups.add(collectedTerms);
		linkListDataGroups.add(linkList);
		dataDividers.add(dataDivider);
	}

	@Override
	public void deleteByTypeAndId(String type, String id) {
		MCR.addCall("type", type, "id", id);
		alteredRecordTypes.add(type);
		alteredRecordIds.add(id);
	}

	@Override
	public boolean linksExistForRecord(String type, String id) {
		MCR.addCall("type", type, "id", id);
		MCR.addReturned(false);
		return false;
	}

	@Override
	public void update(String type, String id, DataGroup record, DataGroup collectedTerms,
			DataGroup linkList, String dataDivider) {
		MCR.addCall("type", type, "id", id, "record", record, "collectedTerms", collectedTerms,
				"linkList", linkList, "dataDivider", dataDivider);
		alteredRecordTypes.add(type);
		alteredRecordIds.add(id);
		handledDataGroups.add(record);
		collectedDataDataGroups.add(collectedTerms);
		linkListDataGroups.add(linkList);
		dataDividers.add(dataDivider);
	}

	@Override
	public StorageReadResult readList(String type, DataGroup filter) {
		MCR.addCall("type", type, "filter", filter);
		MCR.addReturned(null);
		return null;
	}

	@Override
	public StorageReadResult readAbstractList(String type, DataGroup filter) {
		MCR.addCall("type", type, "filter", filter);
		MCR.addReturned(null);
		return null;
	}

	@Override
	public DataGroup readLinkList(String type, String id) {
		MCR.addCall("type", type, "id", id);
		MCR.addReturned(null);
		return null;
	}

	@Override
	public Collection<DataGroup> generateLinkCollectionPointingToRecord(String type, String id) {
		MCR.addCall("type", type, "id", id);
		MCR.addReturned(null);
		return null;
	}

	@Override
	public boolean recordExistsForAbstractOrImplementingRecordTypeAndRecordId(String type,
			String id) {
		MCR.addCall("type", type, "id", id);
		MCR.addReturned(false);
		return false;
	}

	@Override
	public long getTotalNumberOfRecordsForType(String type, DataGroup filter) {
		MCR.addCall("type", type, "filter", filter);
		MCR.addReturned(0);
		return 0;
	}

	@Override
	public long getTotalNumberOfRecordsForAbstractType(String abstractType,
			List<String> implementingTypes, DataGroup filter) {
		MCR.addCall("abstractType", abstractType, "implementingTypes", implementingTypes, "filter",
				filter);
		MCR.addReturned(0);
		return 0;
	}

}
