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

import se.uu.ub.cora.classicfedorasynchronizer.CoraIndexer;
import se.uu.ub.cora.javaclient.cora.CoraClientFactoryImp;

public class CoraIndexerFactoryImp implements CoraIndexerFactory {

	private String apptokenVerifierUrl;
	private String baseUrl;

	public static CoraIndexerFactoryImp usingApptokenVerifierUrlAndBaseUrl(
			String apptokenVerifierUrl, String baseUrl) {
		return new CoraIndexerFactoryImp(apptokenVerifierUrl, baseUrl);
	}

	private CoraIndexerFactoryImp(String apptokenVerifierUrl, String baseUrl) {
		this.apptokenVerifierUrl = apptokenVerifierUrl;
		this.baseUrl = baseUrl;
	}

	@Override
	public CoraIndexer factor(String userId, String apptoken) {
		CoraClientFactoryImp coraClientFactory = CoraClientFactoryImp
				.usingAppTokenVerifierUrlAndBaseUrl(apptokenVerifierUrl, baseUrl);
		return new CoraIndexerImp(coraClientFactory, userId, apptoken);
	}

}
