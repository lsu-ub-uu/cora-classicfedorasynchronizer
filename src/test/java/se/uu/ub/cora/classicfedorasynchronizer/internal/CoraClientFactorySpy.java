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

import se.uu.ub.cora.javaclient.cora.CoraClient;
import se.uu.ub.cora.javaclient.cora.CoraClientFactory;
import se.uu.ub.cora.testutils.mcr.MethodCallRecorder;

public class CoraClientFactorySpy implements CoraClientFactory {
	MethodCallRecorder MCR = new MethodCallRecorder();

	public CoraClientSpy factoredCoraClient;
	public String userId;
	public String appToken;
	public boolean throwErrorOnIndex = false;
	public String errorToThrow = "CoraClientException";

	@Override
	public CoraClient factor(String userId, String appToken) {
		MCR.addCall("userId", userId, "appToken", appToken);
		this.userId = userId;
		this.appToken = appToken;
		factoredCoraClient = new CoraClientSpy();
		factoredCoraClient.throwErrorOnIndex = throwErrorOnIndex;
		factoredCoraClient.errorToThrow = errorToThrow;

		MCR.addReturned(factoredCoraClient);
		return factoredCoraClient;
	}

	@Override
	public CoraClient factorUsingAuthToken(String authToken) {
		MCR.addCall("authToken", authToken);

		MCR.addReturned(null);
		return null;
	}

}
