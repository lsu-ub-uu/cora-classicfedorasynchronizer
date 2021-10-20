package se.uu.ub.cora.classicfedorasynchronizer.batch;

import java.io.IOException;

import org.testng.annotations.Test;

public class FedoraToDbBatchPropertiesLoaderTest {

	private String[] args;

	@Test
	public void testInit() throws IOException {
		args = new String[] { "args-someDatabaseUrl", "args-dbUserName", "args-dbUserPassword",
				"args-someFedoraBaseUrl", "args-someApptokenVerifierUrl", "args-someCoraBaseUrl",
				"args-someCoraUserId", "args-someCoraApptoken" };
		FedoraToDbBatchPropertiesLoader.loadProperties(args);
	}

}
