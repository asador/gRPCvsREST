package dev.rnd.grpc.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ApplicationProperties {
	private static final String SERVER_ADDRESS = "grpc.server.address";
	private static final String JAVA_LOGGING = "java.logging";
  
  private static final String GET_EMPLOYEE_TEST = "test.getEmployee";
  private static final String CREATE_EMPLOYEE_TEST = "test.createEmployee";
	
	private Properties config;
  
	private String serverAddress;
  private String javaLogging;
  private boolean testGetEmployeeON;
  private boolean testCreateEmployeeON;
  
	
	private static ApplicationProperties _theInstance = null;
  
	static ApplicationProperties getAppProperties() {
		if (_theInstance == null) {
			_theInstance = new ApplicationProperties();
			_theInstance.loadAppProperties();
		}
		return _theInstance;
	}
	
	private void loadAppProperties() {
		config = new Properties();
		try {
			InputStream input = ApplicationProperties.class.getClassLoader().getResourceAsStream("application.properties");
			config.load(input);
			input.close();
			
			serverAddress = config.getProperty(SERVER_ADDRESS);
			javaLogging = config.getProperty(JAVA_LOGGING);
			
			testGetEmployeeON = Boolean.valueOf(config.getProperty(GET_EMPLOYEE_TEST));
			testCreateEmployeeON = Boolean.valueOf(config.getProperty(CREATE_EMPLOYEE_TEST));
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getServerAddress() {
		return serverAddress;
	}

	public String getJavaLogging() {
		return javaLogging;
	}

	public boolean isTestGetEmployeeON() {
		return testGetEmployeeON;
	}

	public boolean isTestCreateEmployeeON() {
		return testCreateEmployeeON;
	}

}
