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

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.classicfedorasynchronizer.FedoraToCoraConverter;
import se.uu.ub.cora.classicfedorasynchronizer.XsltTransformationSpy;
import se.uu.ub.cora.classicfedorasynchronizer.log.LoggerFactorySpy;
import se.uu.ub.cora.converter.ConverterProvider;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.logger.LoggerProvider;

public class FedoraToCoraConverterTest {

	private LoggerFactorySpy loggerFactory = new LoggerFactorySpy();
	private ConverterFactorySpy converterFactory;
	private XsltTransformationSpy transformation;
	private FedoraToCoraConverter converter;

	@BeforeMethod
	public void setUp() {
		loggerFactory.resetLogs("FedoraToCoraConverterImp");
		LoggerProvider.setLoggerFactory(loggerFactory);
		converterFactory = new ConverterFactorySpy();
		ConverterProvider.setConverterFactory("xml", converterFactory);
		transformation = new XsltTransformationSpy();
		converter = new FedoraToCoraConverterImp(transformation);
	}

	@Test
	public void testFromXml() {

		String xml = "someXmlString";
		DataGroup fromXML = converter.fromXML(xml);
		assertEquals(transformation.inputXml, xml);

		ConverterSpy factoredConverter = converterFactory.factoredConverter;
		assertEquals(factoredConverter.dataString, transformation.stringToReturn);
		assertEquals(fromXML, factoredConverter.dataGroupToReturn);
	}

	@Test
	public void testFromXmlWithParameters() {
		String xml = "someXmlString";
		Map<String, Object> parameters = new HashMap<>();

		parameters.put("domain", "uu");

		DataGroup dataGroup = converter.fromXMLWithParameters(xml, parameters);
		assertEquals(transformation.inputXml, xml);

		ConverterSpy factoredConverter = converterFactory.factoredConverter;
		assertEquals(factoredConverter.dataString, transformation.stringToReturn);
		assertEquals(dataGroup, factoredConverter.dataGroupToReturn);

		assertSame(transformation.parameters, parameters);

	}

}
