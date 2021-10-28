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
 *     MERCHANTABILITY or FIT@Override
	NESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Cora.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.uu.ub.cora.classicfedorasynchronizer;

/**
 * ClassicCorsSynschronizer is intended to be used for synchronization of data stored in two
 * different systems.
 */
public interface ClassicCoraSynchronizer {

	/**
	 * Synchronize handles synchronization of one record between two systems.
	 * <p>
	 * If the synchronization fails MUST an error be thrown detailing what went wrong.
	 * 
	 * @param recordType
	 *            A String with the recordType of the record to synchronize
	 * @param recordId
	 *            A String with the recordId of the record to synchronize
	 * @param dataDivider
	 *            A String with the dataDivider to store the record under
	 */
	void synchronizeCreated(String recordType, String recordId, String dataDivider);

	void synchronizeUpdated(String recordType, String recordId, String dataDivider);

	void synchronizeDeleted(String recordType, String recordId, String dataDivider);

}
