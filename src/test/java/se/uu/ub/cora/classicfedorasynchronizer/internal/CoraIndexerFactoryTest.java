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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

import org.testng.annotations.Test;

import se.uu.ub.cora.javaclient.cora.CoraClientFactoryImp;

public class CoraIndexerFactoryTest {

	private String apptokenVerifierUrl = "someApptokenUrl";
	private String baseUrl = "someBaseUrl";
	private String userId = "someUserId";
	private String apptoken = "someApptoken";

	@Test
	public void testInit() {
		CoraIndexerFactoryImp indexerFactory = CoraIndexerFactoryImp
				.usingApptokenVerifierUrlAndBaseUrl(apptokenVerifierUrl, baseUrl);
		CoraClientFactoryImp coraClientFactory = (CoraClientFactoryImp) indexerFactory
				.getCoraClientFactory();
		assertEquals(coraClientFactory.getAppTokenVerifierUrl(), apptokenVerifierUrl);
		assertEquals(coraClientFactory.getBaseUrl(), baseUrl);
	}

	@Test
	public void testFactor() {
		CoraIndexerFactoryImp indexerFactory = CoraIndexerFactoryImp
				.usingApptokenVerifierUrlAndBaseUrl(apptokenVerifierUrl, baseUrl);

		CoraClientFactorySpy clientFactory = new CoraClientFactorySpy();
		indexerFactory.setCoraClientFactory(clientFactory);

		CoraIndexerImp coraIndexer = (CoraIndexerImp) indexerFactory.factor(userId, apptoken);

		assertSame(coraIndexer.getCoraClient(), clientFactory.factoredCoraClient);

		assertEquals(clientFactory.userId, userId);
		assertEquals(clientFactory.appToken, apptoken);
	}
}
