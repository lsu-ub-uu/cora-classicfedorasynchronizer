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
package se.uu.ub.cora.classicfedorasynchronizer;

import java.util.Map;

import se.uu.ub.cora.testutils.mcr.MethodCallRecorder;

public class ClassicCoraSynchronizerFactorySpy implements ClassicCoraSynchronizerFactory {
	public MethodCallRecorder MCR = new MethodCallRecorder();
	public Map<String, String> initInfo;
	public boolean throwError = false;

	public static ClassicCoraSynchronizerFactorySpy usingInitInfo(Map<String, String> initInfo) {
		return new ClassicCoraSynchronizerFactorySpy(initInfo);
	}

	private ClassicCoraSynchronizerFactorySpy(Map<String, String> initInfo) {
		this.initInfo = initInfo;

	}

	@Override
	public ClassicCoraSynchronizer factorForMessaging() {
		MCR.addCall();

		ClassicCoraSynchronizerSpy synchronizer = new ClassicCoraSynchronizerSpy();
		synchronizer.throwError = throwError;
		MCR.addReturned(synchronizer);
		return synchronizer;

	}

	@Override
	public ClassicCoraSynchronizer factorForBatch() {
		MCR.addCall();

		ClassicCoraSynchronizerSpy synchronizer = new ClassicCoraSynchronizerSpy();
		synchronizer.throwError = throwError;
		MCR.addReturned(synchronizer);
		return synchronizer;
	}
}
