package dev.rnd.grpc.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ApplicationProperties {
	private static final String SERVER_ADDRESS = "grpc.server.address";
	private static final String JAVA_LOGGING = "java.logging";
  
	private static final String THREAD_COUNT = "test.numberOfThreads";
	private static final String ITERATION_COUNT = "test.iterationCount";
  private static final String GET_EMPLOYEE_TEST = "test.getEmployee";
  private static final String CREATE_EMPLOYEE_TEST = "test.createEmployee";

  private static final String OUTPUT_FILE_NAME = "test.outputFile";
  private static final String APPEND_TO_OUTPUT_FILE = "test.appendToOutputFile";
  
	private Properties config;
  
	private String serverAddress;
  private String javaLogging;
  
  private int threadCount;
  private int iterationCount;
  private boolean testGetEmployeeON;
  private boolean testCreateEmployeeON;
  
	private String outputFileName;
	private boolean appendToOutputFile;
	
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
		try (InputStream input = 
				ApplicationProperties.class.getClassLoader().getResourceAsStream("application.properties")) {
			
			config.load(input);
			
			serverAddress = config.getProperty(SERVER_ADDRESS);
			javaLogging = config.getProperty(JAVA_LOGGING);
			
			threadCount = Integer.valueOf(config.getProperty(THREAD_COUNT));
			iterationCount = Integer.valueOf(config.getProperty(ITERATION_COUNT));
			testGetEmployeeON = Boolean.valueOf(config.getProperty(GET_EMPLOYEE_TEST));
			testCreateEmployeeON = Boolean.valueOf(config.getProperty(CREATE_EMPLOYEE_TEST));
			
			outputFileName = config.getProperty(OUTPUT_FILE_NAME);
			appendToOutputFile = Boolean.valueOf(config.getProperty(APPEND_TO_OUTPUT_FILE));
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

	public int getThreadCount() {
		return threadCount;
	}

	public int getIterationCount() {
		return iterationCount;
	}

	public String getOutputFileName() {
		return outputFileName;
	}

	public boolean isAppendToOutputFile() {
		return appendToOutputFile;
	}

}
