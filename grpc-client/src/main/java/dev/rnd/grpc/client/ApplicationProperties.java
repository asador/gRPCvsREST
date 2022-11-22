package dev.rnd.grpc.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ApplicationProperties {
	private static final String SERVER_ADDRESS = "grpc.server.address";
	private static final String JAVA_LOGGING = "java.logging";
	private static final String CPU_TIME_SAMPLE_INTERVAL_MS = "cpuTimeSampleIntervalMillisec";
  
	private static final String THREAD_COUNT = "test.numberOfThreads";
	private static final String TEST_TIMEOUT = "test.timoutSeconds";
  private static final String OUTPUT_FILE_NAME = "test.outputFile";
  private static final String APPEND_TO_OUTPUT_FILE = "test.appendToOutputFile";
  private static final String TLS_ENABLED = "tls.enabled";
  private static final String TLS_CERT_FILES_PATH = "tls.certFilesPath";
  
	private Properties config;
  
	private String serverAddress;
  private String javaLogging;
  private int cpuTimeInterval;
  
  private int threadCount;  
//  private int testTimeoutSeconds;
	private String outputFileName;
	private boolean appendToOutputFile;
	
	private boolean tlsEnabled;
	private String certFilesPath;
	
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
			config.putAll(System.getProperties());
			
			serverAddress = config.getProperty(SERVER_ADDRESS);
			javaLogging = config.getProperty(JAVA_LOGGING);
			cpuTimeInterval = Integer.valueOf(config.getProperty(CPU_TIME_SAMPLE_INTERVAL_MS));
			
			threadCount = Integer.valueOf(config.getProperty(THREAD_COUNT));
//			testTimeoutSeconds = Integer.valueOf(config.getProperty(TEST_TIMEOUT));
			outputFileName = config.getProperty(OUTPUT_FILE_NAME);
			appendToOutputFile = Boolean.valueOf(config.getProperty(APPEND_TO_OUTPUT_FILE));
			
			tlsEnabled = Boolean.valueOf(config.getProperty(TLS_ENABLED));
			certFilesPath = config.getProperty(TLS_CERT_FILES_PATH);
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

	public int getThreadCount() {
		return threadCount;
	}

	public String getOutputFileName() {
		return outputFileName;
	}

	public boolean isAppendToOutputFile() {
		return appendToOutputFile;
	}

	public int getCpuTimeInterval() {
		return cpuTimeInterval;
	}

	public String getProperty(String key) {
		return config.getProperty(key);
	}

	public boolean isTlsEnabled() {
		return tlsEnabled;
	}

	public String getCertFilesPath() {
		return certFilesPath;
	}
	
}
