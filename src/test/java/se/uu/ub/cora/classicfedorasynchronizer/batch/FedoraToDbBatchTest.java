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

import org.testng.annotations.Test;

public class FedoraToDbBatchTest {
	@Test
	public void testInit() throws Exception {
		FedoraToDbBatch fedoraToDb = new FedoraToDbBatch();

		// we need recordType, recordId, action, dataDivider for synchronizer

		// modification date
		// creation date

		// DivaFedoraRecordStorage createUrlForPersonList
		// String query = "state=A pid~authority-person:*";
		// baseURL + "objects?pid=true&maxResults=100&resultFormat=xml&query=" + urlEncodedQuery;

		// FedoraReaderImp getFedoraUrlForType
		// return
		// String.format("%s/objects?pid=true&maxResults=%d&resultFormat=xml&query=pid%%7E%s:*",
		// baseUrl, maxResults, type);

		// FedoraReaderImp getFedoraCursorUrlForType
		// return String.format(
		// "%s/objects?sessionToken=%s&pid=true&maxResults=%d&resultFormat=xml&query=pid%%7E%s:*",
		// baseUrl, cursor.getToken(), maxResults, type);
	}
}
