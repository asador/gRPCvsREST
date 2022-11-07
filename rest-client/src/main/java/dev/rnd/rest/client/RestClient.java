package dev.rnd.rest.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.web.client.RestTemplate;

import dev.rnd.rest.server.service.CreateEmployeeResponse;
import dev.rnd.rest.server.service.Employee;
import dev.rnd.rest.server.service.EmployeeUtil;
import dev.rnd.util.CpuUsage;
import dev.rnd.util.TestClient;

public class RestClient extends TestClient {

  private static final Logger logger = Logger.getLogger(RestClient.class.getName());
  private static final String SAMPLE_DATA_SET = "sampleEmployeeData.csv";
  
  private Map<Integer, Employee> sampleData = new HashMap<>();
  private List<Employee> sampleDataAsList = new ArrayList<>();
  private List<Integer> employeeIDs;
  
  private Random rnd = new Random(System.currentTimeMillis());
  
  @Autowired
  private RestTemplate restTemplate;
 
  @Autowired
  private Environment env;
  
  @Value("${rest.server.address}")
	private String serverAddress;
  
  @Value("${java.logging}")
  private String javaLogging;
  
  @Value("${test.numberOfThreads}")
  private int threadCount; 
  
  @Value("${test.outputFile}")
	private String outputFileName;
  
  @Value("${test.appendToOutputFile}")
	private boolean appendToOutputFile;
  
  @Value("${cpuTimeSampleIntervalMillisec}")
  private int cputTimeSamplingInterval;


  RestClient() {
  	
  	EmployeeUtil.loadDataSet(SAMPLE_DATA_SET, sampleData);
  	sampleDataAsList.addAll(sampleData.values());
  }

	public static void main(String[] args) throws Exception { 
		
		AnnotationConfigApplicationContext appContext = new AnnotationConfigApplicationContext(AppConfig.class);	
		
		RestClient client = appContext.getBean(RestClient.class);
		
  	if ("OFF".equalsIgnoreCase(client.javaLogging))
  		Logger.getLogger(RestClient.class.getPackageName()).setLevel(Level.OFF);

		ch.qos.logback.classic.Logger logBackLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
  	logBackLogger.setLevel(ch.qos.logback.classic.Level.WARN);
  	
		try {
			client.runTests();
		}
		finally {
			client.shutdown();
			appContext.close();
		}
	}

	@Override
	protected String getProperty(String key) {
		return env.getProperty(key);
	}

	@Override
	protected int getThreadCount() {
		return threadCount;
	}

	@Override
	protected int getCpuTimeSampleInterval() {
		return cputTimeSamplingInterval;
	}

	@Override
	protected String getOutputFilename() {
		return outputFileName;
	}

	@Override
	protected boolean isAppendToOutputFile() {
		return appendToOutputFile;
	}

	private void runTests() {
		preTestRun();
		
		warmUp();
		
		// single object read/write
		int[] iterations = new int[] {10000};
		for (int count : iterations) {
			testRemoteMethod("getEmployeeByID", threadCount,	count, 
					() -> testGetEmployeeByID());

			testRemoteMethod("createEmployee", threadCount, count, 
					() -> testCreateEmployee());
		}
		
		// batch/list of objects read/write
		int[] batchSizes = new int[] {10, 100, 1000};
		for (int count : iterations)
			for (int batch : batchSizes) {
		
				testRemoteMethod("getEmployeesList-"+batch, threadCount, count, 
						() -> testGetEmployeesList(batch));
				
				testRemoteMethod("createEmployeesList-"+batch, threadCount, count, 
						() -> testCreateEmployeesList(batch));			
			}
	}
	
	@Override
	protected void startServerCpuUsageMonitor() {
		restTemplate.headForHeaders("/cpuUsage/start");
	}
	@Override
	protected CpuUsage stopAndGetServerCpuUsage() {
		return restTemplate.getForObject("/cpuUsage/stopAndGet", CpuUsage.class);
	}

	private void testGetEmployeeByID() {
		int idx = rnd.nextInt(employeeIDs.size());
		Employee emp = restTemplate.getForObject("/employees/{id}", Employee.class, idx);
//		logger.info(employee.toString());
	}
	
	private void testCreateEmployee() {
		int idx = rnd.nextInt(sampleDataAsList.size());
		Employee emp = sampleDataAsList.get(idx);

		CreateEmployeeResponse response = restTemplate.postForObject("/employees", emp, CreateEmployeeResponse.class);
//		logger.info("create employee with ID: "+ response.getEmployeeId());
	}
	
	private void testGetEmployeesList(int batchSize) {
		List<Employee> empList = restTemplate.getForObject("/employees?count="+batchSize, List.class);
//		logger.info("get employees list of size: " + empList.size());
	}
	
	private void testCreateEmployeesList(int batchSize) {
		List<Employee> empList = new ArrayList<>();
		for (int i=0; i<batchSize; i++)
			empList.add(sampleDataAsList.get(i));
		
		List<CreateEmployeeResponse> listResponse = restTemplate.postForObject("/employees/bulk", empList, List.class);
//		logger.info("created employee IDs: " + listResponse.size());
	}	
	
	private void warmUp() {
//		int count = employeeClient.getEmployeeCount();
		employeeIDs = restTemplate.getForObject("/employees/:ids", List.class);
		logger.log(Level.INFO, "employee count on server: {0}", new Object[] {employeeIDs.size()});
	}
	
}
