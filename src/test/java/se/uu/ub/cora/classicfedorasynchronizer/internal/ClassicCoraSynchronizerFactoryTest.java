package se.uu.ub.cora.classicfedorasynchronizer.internal;

import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.classicfedorasynchronizer.ClassicCoraSynchronizerFactory;
import se.uu.ub.cora.httphandler.HttpHandlerFactoryImp;
import se.uu.ub.cora.storage.RecordStorage;

public class ClassicCoraSynchronizerFactoryTest {

	SynchronizerFactory synchronizerFactory;
	RecordStorage recordStorage = new RecordStorageSpy();
	HttpHandlerFactorySpy httpHandlerFactorySpy;
	FedoraConverterFactorySpy fedoraConverterFactorySpy;

	private String baseURL = "http://some.url/";

	@BeforeMethod
	public void beforeMethod() {
		httpHandlerFactorySpy = new HttpHandlerFactorySpy();
		fedoraConverterFactorySpy = new FedoraConverterFactorySpy();

		synchronizerFactory = new SynchronizerFactory(baseURL, recordStorage,
				fedoraConverterFactorySpy);
	}

	@Test
	public void testImplementsClassicCoraSynchronizerFactory() throws Exception {
		assertTrue(synchronizerFactory instanceof ClassicCoraSynchronizerFactory);
	}

	@Test
	public void testHttpHandlersFactorySent() throws Exception {
		ClassicCoraPersonSynchronizer synchronize = (ClassicCoraPersonSynchronizer) synchronizerFactory
				.factor();
		assertTrue(synchronize.onlyForTestGetHttpHandlerFactory() instanceof HttpHandlerFactoryImp);
		assertTrue(synchronize
				.onlyForTestGetFedoraConverterFactory() instanceof DivaFedoraConverterFactoryImp);
	}

	// @Test
	// public void testCallFactorReturnsASynchronizerWithAllParametersSet() throws Exception {
	// synchronizerFactory.sethttpHandlerFactory(httpHandlerFactorySpy);
	//
	// ClassicCoraPersonSynchronizer synchronizer = (ClassicCoraPersonSynchronizer)
	// synchronizerFactory
	// .factor(recordId);
	//
	// assertNotNull(synchronizer);
	//
	// assertEquals(synchronizer.onlyForTestGetRecordStorage(), recordStorage);
	// assertEquals(synchronizer.onlyForTestGetFedoraConverterFactory(),
	// fedoraConverterFactorySpy);
	// }
	//
	// @Test
	// public void testHttpHandlersFactorsWithCorrectURL() throws Exception {
	// synchronizerFactory.sethttpHandlerFactory(httpHandlerFactorySpy);
	//
	// ClassicCoraPersonSynchronizer synchronizer = (ClassicCoraPersonSynchronizer)
	// synchronizerFactory
	// .factor(recordId);
	//
	// httpHandlerFactorySpy.MCR.assertParameters("factor", 0,
	// baseURL + "objects/" + recordId + "/datastreams/METADATA/content");
	//
	// httpHandlerFactorySpy.MCR.assertReturn("factor", 0,
	// synchronizer.onlyForTestGetHttpHandler());
	// }
	//
	// private HttpHandler getHttpHandlerFromFactorySpy() {
	// return (HttpHandler) httpHandlerFactorySpy.MCR.getReturnValue("factor", 0);
	// }

}
