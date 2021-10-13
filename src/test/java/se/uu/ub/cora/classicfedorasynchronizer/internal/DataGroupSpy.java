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

import se.uu.ub.cora.data.DataAtomic;
import se.uu.ub.cora.data.DataAttribute;
import se.uu.ub.cora.data.DataElement;
import se.uu.ub.cora.data.DataGroup;

public class DataGroupSpy implements DataGroup {

	public String nameInData;
	public String recordType;
	public String recordId;
	public int numberOfDomainParts = 0;
	private String repeatId;
	public List<DataGroup> groupChildrenToReturn;

	public DataGroupSpy(String nameInData) {
		this.nameInData = nameInData;
	}

	public DataGroupSpy(String nameInData, String recordType, String recordId) {
		this.nameInData = nameInData;
		this.recordType = recordType;
		this.recordId = recordId;
	}

	@Override
	public void setRepeatId(String repeatId) {
		this.repeatId = repeatId;

	}

	@Override
	public String getRepeatId() {
		// TODO Auto-generated method stub
		return repeatId;
	}

	@Override
	public String getNameInData() {
		return nameInData;
	}

	@Override
	public boolean hasChildren() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsChildWithNameInData(String nameInData) {
		return false;
	}

	@Override
	public void addChildren(Collection<DataElement> dataElements) {

	}

	@Override
	public List<DataElement> getChildren() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<DataElement> getAllChildrenWithNameInData(String nameInData) {
		List<DataElement> childrenToReturn = new ArrayList<>();
		if ("personDomainPart".equals(nameInData)) {
			for (int i = 0; i < numberOfDomainParts; i++) {
				DataGroupSpy dataGroupSpy = new DataGroupSpy("personDomainPart");
				dataGroupSpy.setRepeatId(String.valueOf(i));
				childrenToReturn.add(dataGroupSpy);
			}
		}
		return childrenToReturn;
	}

	@Override
	public List<DataElement> getAllChildrenWithNameInDataAndAttributes(String nameInData,
			DataAttribute... childAttributes) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataElement getFirstChildWithNameInData(String nameInData) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getFirstAtomicValueWithNameInData(String nameInData) {
		if ("linkedRecordId".equals(nameInData)) {
			return recordId;
		}
		return null;
	}

	@Override
	public List<DataAtomic> getAllDataAtomicsWithNameInData(String childNameInData) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataGroup getFirstGroupWithNameInData(String childNameInData) {
		return null;
	}

	@Override
	public void addChild(DataElement dataElement) {

	}

	@Override
	public List<DataGroup> getAllGroupsWithNameInData(String nameInData) {
		groupChildrenToReturn = new ArrayList<>();
		if ("personDomainPart".equals(nameInData)) {
			for (int i = 0; i < numberOfDomainParts; i++) {
				DataGroupSpy dataGroupSpy = new DataGroupSpy("personDomainPart", "personDomainPart",
						"authority-person:" + i + ":test");
				dataGroupSpy.setRepeatId(String.valueOf(i));
				groupChildrenToReturn.add(dataGroupSpy);
			}
		}
		return groupChildrenToReturn;
	}

	@Override
	public Collection<DataGroup> getAllGroupsWithNameInDataAndAttributes(String childNameInData,
			DataAttribute... childAttributes) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean removeFirstChildWithNameInData(String childNameInData) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removeAllChildrenWithNameInData(String childNameInData) {
		// removeAllGroupsUsedNameInDatas.add(childNameInData);
		return false;
	}

	@Override
	public boolean removeAllChildrenWithNameInDataAndAttributes(String childNameInData,
			DataAttribute... childAttributes) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public DataAtomic getFirstDataAtomicWithNameInData(String childNameInData) {
		// TODO Auto-generated method stub
		return null;
	}

}
