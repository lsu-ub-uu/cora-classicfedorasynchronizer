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

package se.uu.ub.cora.classicfedorasynchronizer.internal;

import se.uu.ub.cora.classicfedorasynchronizer.FedoraConverterFactory;
import se.uu.ub.cora.classicfedorasynchronizer.FedoraToCoraConverter;
import se.uu.ub.cora.classicfedorasynchronizer.NotImplementedException;
import se.uu.ub.cora.xmlutils.transformer.CoraTransformation;
import se.uu.ub.cora.xmlutils.transformer.CoraTransformationFactory;

public class DivaFedoraConverterFactoryImp implements FedoraConverterFactory {

	private static final String PERSON_XSLT_PATH = "person/coraPerson.xsl";
	private static final String PERSON_DOMAIN_PART_XSLT_PATH = "person/coraPersonDomainPart.xsl";
	private String fedoraURL;
	private CoraTransformationFactory coraTransformationFactory;

	public static DivaFedoraConverterFactoryImp usingFedoraURLAndTransformerFactory(String fedoraURL,
			CoraTransformationFactory transformationFactory) {
		return new DivaFedoraConverterFactoryImp(fedoraURL, transformationFactory);
	}

	private DivaFedoraConverterFactoryImp(String fedoraURL,
			CoraTransformationFactory coraTransformationFactory) {
		this.fedoraURL = fedoraURL;
		this.coraTransformationFactory = coraTransformationFactory;
	}

	@Override
	public FedoraToCoraConverter factorToCoraConverter(String type) {
		if ("person".equals(type)) {
			return getFedoraToCoraConverterUsingPath(PERSON_XSLT_PATH);
		}
		if ("personDomainPart".equals(type)) {
			return getFedoraToCoraConverterUsingPath(PERSON_DOMAIN_PART_XSLT_PATH);
		}
		throw NotImplementedException.withMessage("No converter implemented for: " + type);
	}

	private FedoraToCoraConverter getFedoraToCoraConverterUsingPath(String personXsltPath) {
		CoraTransformation coraTransformation = coraTransformationFactory.factor(personXsltPath);
		return new FedoraToCoraConverterImp(coraTransformation);
	}

	// @Override
	// public CoraToFedoraConverter factorToFedoraConverter(String type) {
	// if ("person".equals(type)) {
	// HttpHandlerFactoryImp httpHandlerFactory = new HttpHandlerFactoryImp();
	// return CoraToFedoraPersonConverter
	// .usingHttpHandlerFactoryAndFedoraUrl(httpHandlerFactory, fedoraURL);
	// }
	// throw NotImplementedException.withMessage("No converter implemented for: " + type);
	// }
	//
	// public String getFedoraURL() {
	// // needed for tests
	// return fedoraURL;
	// }

	public CoraTransformationFactory getCoraTransformerFactory() {
		return coraTransformationFactory;
	}

}
