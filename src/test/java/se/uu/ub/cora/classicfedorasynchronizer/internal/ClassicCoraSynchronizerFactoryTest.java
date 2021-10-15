package se.uu.ub.cora.classicfedorasynchronizer.internal;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.classicfedorasynchronizer.ClassicCoraSynchronizerFactory;
import se.uu.ub.cora.httphandler.HttpHandlerFactoryImp;
import se.uu.ub.cora.json.parser.org.OrgJsonParser;
import se.uu.ub.cora.sqldatabase.SqlDatabaseFactoryImp;
import se.uu.ub.cora.sqlstorage.DatabaseRecordStorage;
import se.uu.ub.cora.xmlutils.transformer.XsltTransformationFactory;

public class ClassicCoraSynchronizerFactoryTest {

	SynchronizerFactory synchronizerFactory;
	DatabaseStorageProviderSpy databaseStorageProvider;
	// HttpHandlerFactorySpy httpHandlerFactorySpy;
	// FedoraConverterFactorySpy fedoraConverterFactorySpy;

	private Map<String, String> initInfo;

	@BeforeMethod
	public void beforeMethod() {
		initInfo = new HashMap<>();

		initInfo.put("databaseUrl", "someUrl");
		initInfo.put("databaseUser", "someUser");
		initInfo.put("databasePassword", "somePassword");
		initInfo.put("fedoraBaseUrl", "someFedoraUrl");

		initInfo.put("apptokenVerifierURL", "someApptokenVerifierURL");
		initInfo.put("coraBaseUrl", "someCoraBaseUrl");
		initInfo.put("coraUserId", "someCoraUserId");
		initInfo.put("coraApptoken", "someCoraApptoken");

		databaseStorageProvider = new DatabaseStorageProviderSpy();

		synchronizerFactory = new SynchronizerFactory(initInfo);
	}

	@Test
	public void testImplementsClassicCoraSynchronizerFactory() throws Exception {
		assertTrue(synchronizerFactory instanceof ClassicCoraSynchronizerFactory);
	}

	@Test
	public void testRecordStorageSetUpFromInitInfo() throws Exception {
		ClassicCoraPersonSynchronizer synchronizer = (ClassicCoraPersonSynchronizer) synchronizerFactory
				.factor();

		DatabaseRecordStorage databaseRecordStorage = (DatabaseRecordStorage) synchronizer
				.onlyForTestGetRecordStorage();
		assertTrue(databaseRecordStorage instanceof DatabaseRecordStorage);
		SqlDatabaseFactoryImp sqlDatabaseFactory = (SqlDatabaseFactoryImp) databaseRecordStorage
				.onlyForTestGetSqlDatabaseFactory();
		assertEquals(sqlDatabaseFactory.onlyForTestGetUrl(), initInfo.get("databaseUrl"));
		assertEquals(sqlDatabaseFactory.onlyForTestGetUser(), initInfo.get("databaseUser"));
		assertEquals(sqlDatabaseFactory.onlyForTestGetPassword(), initInfo.get("databasePassword"));

		assertTrue(databaseRecordStorage.onlyForTestGetJsonParser() instanceof OrgJsonParser);
	}

	@Test
	public void testHttpHandlersFactorySent() throws Exception {
		ClassicCoraPersonSynchronizer synchronizer = (ClassicCoraPersonSynchronizer) synchronizerFactory
				.factor();

		assertTrue(
				synchronizer.onlyForTestGetHttpHandlerFactory() instanceof HttpHandlerFactoryImp);

	}

	@Test
	public void testFedoraConverterFactorySent() throws Exception {
		ClassicCoraPersonSynchronizer synchronizer = (ClassicCoraPersonSynchronizer) synchronizerFactory
				.factor();

		assertFedoraConverterFactory(synchronizer);
	}

	private void assertFedoraConverterFactory(ClassicCoraPersonSynchronizer synchronize) {
		DivaFedoraConverterFactoryImp fedoraConverterFactory = (DivaFedoraConverterFactoryImp) synchronize
				.onlyForTestGetFedoraConverterFactory();
		assertTrue(fedoraConverterFactory instanceof DivaFedoraConverterFactoryImp);

		assertTrue(fedoraConverterFactory
				.getCoraTransformerFactory() instanceof XsltTransformationFactory);
	}

	@Test
	public void testCoraIndexerSent() throws Exception {
		ClassicCoraPersonSynchronizer synchronizer = (ClassicCoraPersonSynchronizer) synchronizerFactory
				.factor();
		CoraIndexerImp coraIndexer = (CoraIndexerImp) synchronizer.onlyForTestGetCoraIndexer();
		// coraIndexer.
		// TODO: check cora indexer
		// assertFedoraConverterFactory(synchronizer);
	}

	@Test
	public void testFedoraBaseUrlSent() throws Exception {
		ClassicCoraPersonSynchronizer synchronizer = (ClassicCoraPersonSynchronizer) synchronizerFactory
				.factor();
		assertEquals(synchronizer.onlyForTestGetBaseUrl(), initInfo.get("fedoraBaseUrl"));
	}

}
