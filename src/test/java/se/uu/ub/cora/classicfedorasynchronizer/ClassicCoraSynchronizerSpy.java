package se.uu.ub.cora.classicfedorasynchronizer;

import se.uu.ub.cora.testutils.mcr.MethodCallRecorder;

public class ClassicCoraSynchronizerSpy implements ClassicCoraSynchronizer {

	public MethodCallRecorder MCR = new MethodCallRecorder();

	@Override
	public void synchronize(String recordType, String recordId, String action, String dataDivider) {
		MCR.addCall("recordType", recordType, "recordId", recordId, "action", action, "dataDivider",
				dataDivider);
	}

}
