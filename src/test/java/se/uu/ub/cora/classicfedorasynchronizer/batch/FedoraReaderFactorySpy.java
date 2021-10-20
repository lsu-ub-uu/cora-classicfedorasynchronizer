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

import se.uu.ub.cora.fedora.data.FedoraReaderXmlHelper;
import se.uu.ub.cora.fedora.reader.FedoraReader;
import se.uu.ub.cora.fedora.reader.FedoraReaderFactory;
import se.uu.ub.cora.httphandler.HttpHandlerFactory;

public class FedoraReaderFactorySpy implements FedoraReaderFactory {

	public static HttpHandlerFactory httpHandlerFactory;
	public static FedoraReaderXmlHelper fedoraReaderXmlHelper;

	public FedoraReaderFactorySpy(HttpHandlerFactory httpHandlerFactory,
			FedoraReaderXmlHelper fedoraReaderXmlHelper) {
		// TODO Auto-generated constructor stub
	}

	public static FedoraReaderFactorySpy usingHttpHandlerFactoryAndFedoraReaderXmlHelper(
			HttpHandlerFactory httpHandlerFactory, FedoraReaderXmlHelper fedoraReaderXmlHelper) {
		FedoraReaderFactorySpy.httpHandlerFactory = httpHandlerFactory;
		FedoraReaderFactorySpy.fedoraReaderXmlHelper = fedoraReaderXmlHelper;
		return new FedoraReaderFactorySpy(httpHandlerFactory, fedoraReaderXmlHelper);
	}

	@Override
	public FedoraReader factor(String baseUrl) {
		// TODO Auto-generated method stub
		return null;
	}

}
