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

import java.util.ArrayList;
import java.util.List;

import se.uu.ub.cora.classicfedorasynchronizer.FedoraConverterFactory;
import se.uu.ub.cora.classicfedorasynchronizer.FedoraToCoraConverter;

public class FedoraConverterFactorySpy implements FedoraConverterFactory {

	public List<String> types = new ArrayList<>();
	public List<FedoraToCoraConverterSpy> factoredFedoraConverters = new ArrayList<>();
	// public DataGroupSpy convertedGroup;
	public List<DataGroupSpy> convertedGroups = new ArrayList<>();
	private int numberOfConvertersReturned = 0;

	@Override
	public FedoraToCoraConverter factorToCoraConverter(String type) {
		this.types.add(type);
		FedoraToCoraConverterSpy factoredFedoraConverter = new FedoraToCoraConverterSpy();
		if (!convertedGroups.isEmpty()) {
			factoredFedoraConverter.convertedGroup = convertedGroups
					.get(numberOfConvertersReturned);
		}
		numberOfConvertersReturned++;
		factoredFedoraConverters.add(factoredFedoraConverter);
		return factoredFedoraConverter;
	}
	//
	// @Override
	// public CoraToFedoraConverter factorToFedoraConverter(String type) {
	// // TODO Auto-generated method stub
	// return null;
	// }

}
