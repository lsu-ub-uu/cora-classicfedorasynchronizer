/*
 * Copyright 2019, 2021 Uppsala University Library
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
package se.uu.ub.cora.classicfedorasynchronizer.messaging;

import java.text.MessageFormat;
import java.util.Map;

import se.uu.ub.cora.classicfedorasynchronizer.ClassicCoraSynchronizer;
import se.uu.ub.cora.classicfedorasynchronizer.ClassicCoraSynchronizerFactory;
import se.uu.ub.cora.classicfedorasynchronizer.messaging.parsning.MessageParser;
import se.uu.ub.cora.classicfedorasynchronizer.messaging.parsning.MessageParserFactory;
import se.uu.ub.cora.logger.Logger;
import se.uu.ub.cora.logger.LoggerProvider;
import se.uu.ub.cora.messaging.MessageReceiver;

public class FedoraMessageReceiver implements MessageReceiver {

	private static final String DATA_DIVIDER = "diva";
	private Logger logger = LoggerProvider.getLoggerForClass(FedoraMessageReceiver.class);
	private MessageParserFactory messageParserFactory;
	private ClassicCoraSynchronizerFactory classicCoraSynchronizerFactory;

	public FedoraMessageReceiver(MessageParserFactory messageParserFactory,
			ClassicCoraSynchronizerFactory classicCoraSynchronizerFactory) {
		this.messageParserFactory = messageParserFactory;
		this.classicCoraSynchronizerFactory = classicCoraSynchronizerFactory;
	}

	@Override
	public void receiveMessage(Map<String, String> headers, String message) {
		MessageParser messageParser = messageParserFactory.factor();
		messageParser.parseHeadersAndMessage(headers, message);

		if (messageParser.synchronizationRequired()) {
			tryToSynchronizeChange(messageParser);
		}
	}

	private void tryToSynchronizeChange(MessageParser messageParser) {
		try {
			synchronizeChange(messageParser);
		} catch (Exception e) {
			logger.logErrorUsingMessageAndException(
					"Message could not be synchronized. " + e.getMessage(), e);
		}
	}

	private void synchronizeChange(MessageParser messageParser) {
		String recordType = messageParser.getRecordType();
		String recordId = messageParser.getRecordId();
		String action = messageParser.getAction();
		writeLogMessage(recordType, recordId, action);
		synchronizeChangeUsingSynchronizer(recordType, recordId, action);
	}

	private void synchronizeChangeUsingSynchronizer(String recordType, String recordId,
			String action) {
		ClassicCoraSynchronizer synchronizer = classicCoraSynchronizerFactory.factorForMessaging();
		if ("update".equals(action)) {
			synchronizer.synchronizeUpdated(recordType, recordId, DATA_DIVIDER);
		} else if ("delete".equals(action)) {
			synchronizer.synchronizeDeleted(recordType, recordId, DATA_DIVIDER);
		} else {
			synchronizer.synchronizeCreated(recordType, recordId, DATA_DIVIDER);
		}
	}

	private void writeLogMessage(String recordType, String recordId, String action) {
		String logM = "Synchronizer called for type: {0}, id: {1}, action: {2} and dataDivider: diva";
		logger.logInfoUsingMessage(MessageFormat.format(logM, recordType, recordId, action));
	}

	@Override
	public void topicClosed() {
		logger.logFatalUsingMessage("Topic closed!");
	}

	public MessageParserFactory onlyForTestGetMessageParserFactory() {
		// needed for test
		return messageParserFactory;
	}

	public ClassicCoraSynchronizerFactory onlyForTestGetClassicCoraSynchronizerFactory() {
		return classicCoraSynchronizerFactory;
	}

}
