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

import org.testng.annotations.Test;

import se.uu.ub.cora.javaclient.cora.CoraClientFactoryImp;

public class CoraIndexerFactoryTest {

	private String apptokenVerifierUrl = "someApptokenUrl";
	private String baseVerifierUrl = "someBaseUrl";
	private String userId = "someUserId";
	private String apptoken = "someApptoken";

	@Test
	public void testFactor() {
		CoraIndexerFactory indexerFactory = CoraIndexerFactoryImp.usingApptokenVerifierUrlAndBaseUrl(apptokenVerifierUrl, baseVerifierUrl);
		CoraIndexerImp coraIndexer = (CoraIndexerImp) indexerFactory.factor(userId, apptoken);
		assertEquals(coraIndexer.getUserId(), userId);
		assertEquals(coraIndexer.getApptoken(), apptoken);
	}

	@Test
	public void testCoraClientFactory() {
		CoraIndexerFactory indexerFactory = CoraIndexerFactoryImp.usingApptokenVerifierUrlAndBaseUrl(apptokenVerifierUrl, baseVerifierUrl);
		CoraIndexerImp coraIndexer = (CoraIndexerImp) indexerFactory.factor(userId, apptoken);
		CoraClientFactoryImp clientFactory = (CoraClientFactoryImp) coraIndexer
				.getCoraClientFactory();
		assertEquals(clientFactory.getAppTokenVerifierUrl(), apptokenVerifierUrl);
		assertEquals(clientFactory.getBaseUrl(), baseVerifierUrl);
	}
}
