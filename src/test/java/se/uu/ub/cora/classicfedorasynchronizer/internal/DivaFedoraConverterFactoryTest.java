/*
 * Copyright 2018, 2021 Uppsala University Library
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
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.classicfedorasynchronizer.FedoraToCoraConverter;
import se.uu.ub.cora.classicfedorasynchronizer.NotImplementedException;
import se.uu.ub.cora.classicfedorasynchronizer.TransformationFactorySpy;

public class DivaFedoraConverterFactoryTest {
	private DivaFedoraConverterFactoryImp fedoraToCoraConverterFactoryImp;
	private String fedoraURL = "someFedoraUrl";
	private TransformationFactorySpy transformationFactory;

	@BeforeMethod
	public void beforeMethod() {
		transformationFactory = new TransformationFactorySpy();
		fedoraToCoraConverterFactoryImp = DivaFedoraConverterFactoryImp
				.usingFedoraURLAndTransformerFactory(fedoraURL, transformationFactory);
	}

	@Test
	public void testInit() {
		assertSame(fedoraToCoraConverterFactoryImp.getCoraTransformerFactory(),
				transformationFactory);
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "No converter implemented for: someType")
	public void factorUnknownTypeThrowsException() throws Exception {
		fedoraToCoraConverterFactoryImp.factorToCoraConverter("someType");
	}

	@Test
	public void testFactoryPerson() throws Exception {
		FedoraToCoraConverter converter = fedoraToCoraConverterFactoryImp
				.factorToCoraConverter("person");
		FedoraToCoraConverterImp personConverter = (FedoraToCoraConverterImp) converter;

		assertNotNull(personConverter.getCoraTransformation());
		assertSame(personConverter.getCoraTransformation(),
				transformationFactory.transformationSpy);

		assertEquals(transformationFactory.xsltPath, "person/coraPerson.xsl");
	}

	@Test
	public void testFactoryPersonDomainPart() throws Exception {
		FedoraToCoraConverter converter = fedoraToCoraConverterFactoryImp
				.factorToCoraConverter("personDomainPart");
		assertTrue(converter instanceof FedoraToCoraConverterImp);
		FedoraToCoraConverterImp personDomainPartConverter = (FedoraToCoraConverterImp) converter;
		//
		assertNotNull(personDomainPartConverter.getCoraTransformation());
		assertSame(personDomainPartConverter.getCoraTransformation(),
				transformationFactory.transformationSpy);

		assertEquals(transformationFactory.xsltPath, "person/coraPersonDomainPart.xsl");
	}

	// @Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp =
	// ""
	// + "No converter implemented for: someType")
	// public void factorToFedoraUnknownTypeThrowsException() throws Exception {
	// fedoraToCoraConverterFactoryImp.factorToFedoraConverter("someType");
	// }

	// @Test
	// public void testFactoryToFedoraPerson() throws Exception {
	// CoraToFedoraConverter converter = fedoraToCoraConverterFactoryImp
	// .factorToFedoraConverter("person");
	// assertTrue(converter instanceof CoraToFedoraPersonConverter);
	// }
	//
	// @Test
	// public void testFactorToFedoraForPersonHasCorrectDependencies() throws Exception {
	// CoraToFedoraPersonConverter converter = (CoraToFedoraPersonConverter)
	// fedoraToCoraConverterFactoryImp
	// .factorToFedoraConverter("person");
	// assertTrue(converter.getHttpHandlerFactory() instanceof HttpHandlerFactoryImp);
	// assertEquals(converter.getFedorURL(), fedoraURL);
	// }
	//
	// @Test
	// public void testGetFedoraURLNeededForTests() throws Exception {
	// assertEquals(fedoraToCoraConverterFactoryImp.getFedoraURL(), fedoraURL);
	// }
}
