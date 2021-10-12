package se.uu.ub.cora.classicfedorasynchronizer.internal;

import java.io.InputStream;

import se.uu.ub.cora.httphandler.HttpHandler;

public class HttpHandlerSpy implements HttpHandler {

	public String requestMethod;
	public int responseCode;
	public boolean getResponseCodeWasCalled = false;
	public String responseText;

	@Override
	public void setRequestMethod(String requestMethod) {
		this.requestMethod = requestMethod;
		// TODO Auto-generated method stub

	}

	@Override
	public String getResponseText() {
		responseText = "Some response text from httpHandlerSpy";
		return responseText;
	}

	@Override
	public int getResponseCode() {
		getResponseCodeWasCalled = true;
		return responseCode;
	}

	@Override
	public void setOutput(String outputString) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setRequestProperty(String key, String value) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getErrorText() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setStreamOutput(InputStream stream) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getHeaderField(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setBasicAuthorization(String username, String password) {
		// TODO Auto-generated method stub

	}

}
