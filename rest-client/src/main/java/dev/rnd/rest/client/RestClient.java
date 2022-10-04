package dev.rnd.rest.client;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.Environment;

import dev.rnd.rest.server.service.Employee;
import dev.rnd.rest.server.service.EmployeeUtil;

public class RestClient {

  private static final Logger logger = Logger.getLogger(RestClient.class.getName());
  private static final String SAMPLE_DATA_SET = "sampleEmployeeData.csv";
  
  private Map<Integer, Employee> sampleData = new HashMap<>();
  private List<Employee> sampleDataAsList = new ArrayList<>();
  private List<Integer> employeeIDs;
  
  private Random rnd = new Random(System.currentTimeMillis());
  private List<TestResult> testResults = new ArrayList<>();  
  
  private ExecutorService executor;
  
  @Autowired
  private Environment env;
  
  @Value("${rest.server.address}")
	private String serverAddress;
  
  @Value("${java.logging}")
  private String javaLogging;
  
  @Value("${test.numberOfThreads}")
  private int threadCount;  
  
  @Value("${test.timoutSeconds}")
  private int testTimeoutSeconds;
  
  @Value("${test.outputFile}")
	private String outputFileName;
  
  @Value("${test.appendToOutputFile}")
	private boolean appendToOutputFile;


  RestClient() {
  	
  	EmployeeUtil.loadDataSet(SAMPLE_DATA_SET, sampleData);
  	sampleDataAsList.addAll(sampleData.values());
  	
//  	executor = Executors.newFixedThreadPool(threadCount);
  }

	public static void main(String[] args) throws Exception { 
		
		AnnotationConfigApplicationContext appContext = new AnnotationConfigApplicationContext(AppConfig.class);	
		
		RestClient client = appContext.getBean(RestClient.class);
		
  	if ("OFF".equalsIgnoreCase(client.javaLogging))
  		Logger.getLogger(RestClient.class.getPackageName()).setLevel(Level.OFF);
  	
  	client.executor = Executors.newFixedThreadPool(client.threadCount);
  	
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
		iterations = new int[] {1000};
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
		List<Long> execTimes = Collections.synchronizedList(new ArrayList<>());
		AtomicInteger errorCount = new AtomicInteger(0);
		
		for (int i=0; i< nThreads; i++) {
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
			latch.await(testTimeoutSeconds, TimeUnit.SECONDS);

			TestResult testResult = new TestResult(testName, nThreads, iterationCount, errorCount.get(), execTimes);
			testResults.add(testResult);

			logger.log(Level.INFO, "Completed {0} - threads={1}, iterationCount={2}", new Object[] {testName, nThreads, iterationCount});
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void testGetEmployeeByID() {
		int idx = rnd.nextInt(employeeIDs.size());
//		logger.info(employee.toString());
	}
	
	private void testCreateEmployee() {
//		logger.info("create employee with ID: "+ empId);
	}
	
	private void testGetEmployeesList(int batchSize) {
//		logger.info("get employee list: " + empList.toString());
	}
	
	private void testCreateEmployeesList(int batchSize) {
//		logger.info("created employee IDs: " + idList);
	}
	
	
	private void warmUp() {
//		int count = employeeClient.getEmployeeCount();
//		employeeIDs = employeeClient.getEmployeeIDs();
//		logger.log(Level.INFO, "employee count on server: {0}", new Object[] {employeeIDs.size()});
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
