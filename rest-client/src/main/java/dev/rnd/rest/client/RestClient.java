package dev.rnd.rest.client;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
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
import dev.rnd.util.CpuTimeCalculator;
import dev.rnd.util.TestResult;

public class RestClient {

  private static final Logger logger = Logger.getLogger(RestClient.class.getName());
  private static final String SAMPLE_DATA_SET = "sampleEmployeeData.csv";
  
  private Map<Integer, Employee> sampleData = new HashMap<>();
  private List<Employee> sampleDataAsList = new ArrayList<>();
  private List<Integer> employeeIDs;
  
  private Random rnd = new Random(System.currentTimeMillis());
  private List<TestResult> testResults = new ArrayList<>();  
  
  @Autowired
  private ExecutorService executor;
  
  @Autowired
  private RestTemplate restTemplate;

  @Autowired
  private CpuTimeCalculator cpuTimeCalculator;
  
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

	private void runTests() {
		warmUp();
		
		// single object read/write
		int[] iterations = new int[] {10000};
		for (int count : iterations) {
			testRestMethod("getEmployeeByID", threadCount,	count, 
					() -> testGetEmployeeByID());

			testRestMethod("createEmployee", threadCount, count, 
					() -> testCreateEmployee());
		}
		
		// batch/list of objects read/write
		int[] batchSizes = new int[] {10, 100, 1000};
		for (int count : iterations)
			for (int batch : batchSizes) {
		
				testRestMethod("getEmployeesList-"+batch, threadCount, count, 
						() -> testGetEmployeesList(batch));
				
				testRestMethod("createEmployeesList-"+batch, threadCount, count, 
						() -> testCreateEmployeesList(batch));			
			}
	}
	
	private boolean isTestEnabled(String testName) {
		String testKey = "test." + testName;
		return env.getProperty(testKey) == null || Boolean.valueOf(env.getProperty(testKey));
	}
	
	private void testRestMethod(String testName, int nThreads, int iterationCount, Runnable test) {
		if (!isTestEnabled(testName)) {
			logger.log(Level.INFO, "Skipped test {0}", testName);
			return;
		}
					
		CountDownLatch latch = new CountDownLatch(nThreads);
		List<List<Long>> execTimesList = new ArrayList<>();		
		AtomicInteger errorCount = new AtomicInteger(0);
		
		startServerCpuTime();
		cpuTimeCalculator.start();
		
		long start = System.currentTimeMillis();
		for (int i=0; i< nThreads; i++) {
			List<Long> execTimes = new ArrayList<>();
			execTimesList.add(execTimes);
			
			executor.execute(() -> {
				for (int j=0; j < iterationCount; j++) {
					long t1 = System.nanoTime();
					try {
						test.run();
					}
					catch (Exception e) {
						errorCount.incrementAndGet();
						e.printStackTrace();
					}
					long t2 = System.nanoTime();
					execTimes.add(t2-t1);
				}
				
				latch.countDown();
			});
		}
		
		try {
//			latch.await(testTimeoutSeconds, TimeUnit.SECONDS);
			latch.await();

			long duration = System.currentTimeMillis() - start;
			
			long clientCpuTime = cpuTimeCalculator.stopAndGetTotalCpuTime();
			long serverCpuTime = stopAndGetServerCpuTime();
			
			// merge all exec times form all threads
			List<Long> allExecTimes = new ArrayList<>();			
			for (int i=0; i<nThreads; i++)
				allExecTimes.addAll(execTimesList.get(i));
			
			TestResult testResult = new TestResult(testName, nThreads, iterationCount, errorCount.get(), duration, 
					allExecTimes, serverCpuTime, clientCpuTime);
			testResults.add(testResult);

			logger.log(Level.INFO, "Completed {0} - threads={1}, iterationCount={2}", new Object[] {testName, nThreads, iterationCount});
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void startServerCpuTime() {
		restTemplate.headForHeaders("/cpuTime/start");
	}
	private long stopAndGetServerCpuTime() {
		return restTemplate.getForObject("/cpuTime/stopAndGet", Long.class);
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
	
	private void saveTestResults() {
		if (testResults.size() == 0)
			return ;
		
		File outFile = new File(outputFileName);
		boolean addCSVHeader = !outFile.exists() || !appendToOutputFile;
		
		try ( FileWriter fw = new FileWriter(outFile, appendToOutputFile);
					BufferedWriter bw = new BufferedWriter(fw);
					PrintWriter pw = new PrintWriter(bw);
				) {
			
			if (addCSVHeader)
				pw.println(TestResult.getCSVHeader());
			
			for (TestResult res : testResults)
				pw.println(res.getResultAsCSV());
			
			logger.log(Level.INFO, "Test results saved in {0}", outFile.getAbsolutePath());
			
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	private void shutdown() throws InterruptedException {
		executor.shutdown();
		executor.awaitTermination(30, TimeUnit.SECONDS);
		
		saveTestResults();
	}	

}
