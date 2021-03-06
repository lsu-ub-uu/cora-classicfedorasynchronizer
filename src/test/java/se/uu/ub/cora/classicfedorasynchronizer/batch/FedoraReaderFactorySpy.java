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

import se.uu.ub.cora.fedoralegacy.reader.FedoraReader;
import se.uu.ub.cora.fedoralegacy.reader.FedoraReaderFactory;
import se.uu.ub.cora.httphandler.HttpHandlerFactory;
import se.uu.ub.cora.testutils.mcr.MethodCallRecorder;

public class FedoraReaderFactorySpy implements FedoraReaderFactory {
	MethodCallRecorder MCR = new MethodCallRecorder();

	public HttpHandlerFactory httpHandlerFactory;
	public FedoraReaderSpy factoredFedoraReader;
	public String baseUrl;

	@Override
	public FedoraReader factor(String baseUrl) {
		MCR.addCall("baseUrl", baseUrl);

		this.baseUrl = baseUrl;
		factoredFedoraReader = new FedoraReaderSpy();

		MCR.addReturned(factoredFedoraReader);
		return factoredFedoraReader;
	}

}
