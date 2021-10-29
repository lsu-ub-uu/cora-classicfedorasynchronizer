/*
 * Copyright 2019 Uppsala University Library
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
package se.uu.ub.cora.classicfedorasynchronizer.messaging.parsning;

import se.uu.ub.cora.testutils.mcr.MethodCallRecorder;

public class MessageParserFactorySpy implements MessageParserFactory {
	public MethodCallRecorder MCR = new MethodCallRecorder();
	public boolean factorWasCalled = false;
	public boolean synchronizationRequired = true;
	public MessageParserSpy messageParserSpy;
	public String modificationType = "update";

	@Override
	public MessageParser factor() {
		MCR.addCall();
		factorWasCalled = true;
		messageParserSpy = new MessageParserSpy();
		messageParserSpy.synchronizationRequired = synchronizationRequired;
		messageParserSpy.modificationType = modificationType;
		MCR.addReturned(messageParserSpy);
		return messageParserSpy;
	}

}
