package se.uu.ub.cora.classicfedorasynchronizer.internal;

import se.uu.ub.cora.data.DataGroup;

public class StorageInformation {

	public String recordType;
	public String recordId;
	public String action;
	public String dataDivider;
	public DataGroup dataGroup;

	public StorageInformation(String recordType, String recordId, String action,
			String dataDivider) {
		this.recordType = recordType;
		this.recordId = recordId;
		this.action = action;
		this.dataDivider = dataDivider;
	}

}
