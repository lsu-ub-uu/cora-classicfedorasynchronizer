package se.uu.ub.cora.classicfedorasynchronizer.internal;

import java.util.Map;

import se.uu.ub.cora.sqlstorage.DatabaseRecordStorage;
import se.uu.ub.cora.sqlstorage.DatabaseStorageProvider;
import se.uu.ub.cora.testutils.mcr.MethodCallRecorder;

public class DatabaseStorageProviderSpy extends DatabaseStorageProvider {

	MethodCallRecorder MCR = new MethodCallRecorder();

	@Override
	public synchronized void startUsingInitInfo(Map<String, String> initInfo) {
		MCR.addCall("initInfo", initInfo);
	}

	@Override
	public DatabaseRecordStorage getRecordStorage() {
		MCR.addCall();

		DatabaseRecordStorage dataRecordStorage = new DatabaseRecordStorageSpy();
		MCR.addReturned(dataRecordStorage);
		return dataRecordStorage;
	}

}
