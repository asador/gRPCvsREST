package dev.rnd.grpc.client;


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

import dev.rnd.grpc.employee.CreateEmployeeResponse;
import dev.rnd.grpc.employee.Employee;
import dev.rnd.grpc.server.controller.EmployeeUtil;
import dev.rnd.grpc.server.service.EmployeeDTO;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;

public class GrpcClient {

  private static final Logger logger = Logger.getLogger(GrpcClient.class.getName());
  private static final String SAMPLE_DATA_SET = "sampleEmployeeData.csv";
  
  private ApplicationProperties props;
  private ManagedChannel channel;
  private EmployeeGrpcClient employeeClient;
  private ExecutorService executor;
  
  private Map<Integer, EmployeeDTO> sampleData = new HashMap<>();
  private List<EmployeeDTO> sampleDataAsList = new ArrayList<>();
  private List<Integer> employeeIDs;
  
  private Random rnd = new Random(System.currentTimeMillis());
  private List<TestResult> testResults = new ArrayList<>();  

  GrpcClient() {
  	props = ApplicationProperties.getAppProperties();
  	
  	if ("OFF".equalsIgnoreCase(props.getJavaLogging()))
  		Logger.getLogger(GrpcClient.class.getPackageName()).setLevel(Level.OFF);
  	
  	EmployeeUtil.loadDataSet(SAMPLE_DATA_SET, sampleData);
  	sampleDataAsList.addAll(sampleData.values());
  	
  	channel = Grpc.newChannelBuilder(props.getServerAddress(), InsecureChannelCredentials.create()).build();
  	employeeClient = new EmployeeGrpcClient(channel);
  	
  	executor = Executors.newFixedThreadPool(props.getThreadCount());
  }

	public static void main(String[] args) throws Exception{
		
		GrpcClient client = new GrpcClient();
		try {
			client.runTests();
		}
		finally {
			client.shutdown();
		}
	}

	private void runTests() {
		warmUp();
		
		// single object read/write
		int[] iterations = new int[] {10000};
		for (int count : iterations) {
			testGrpcMethod("getEmployeeByID", props.getThreadCount(),	count, 
					() -> testGetEmployeeByID());

			testGrpcMethod("createEmployee", props.getThreadCount(), count, 
					() -> testCreateEmployee());
		}
		
		// batch/list of objects read/write
		iterations = new int[] {1000};
		int[] batchSizes = new int[] {10, 100, 1000};
		for (int count : iterations)
			for (int batch : batchSizes) {
		
				testGrpcMethod("getEmployeesList-"+batch, props.getThreadCount(), count, 
						() -> testGetEmployeesList(batch));
				
				testGrpcMethod("createEmployeesList-"+batch, props.getThreadCount(), count, 
						() -> testCreateEmployeesList(batch));			
			}
		
		// read/write list of objects with streaming
		iterations = new int[] {100};
		for (int count : iterations) {
			testGrpcMethod("getEmployeesStreaming", props.getThreadCount(),	count, 
					() -> testGetEmployeesStreaming());

			testGrpcMethod("createEmployeesStreaming", props.getThreadCount(), count, 
					() -> testCreateEmployeesStreaming());
		}
	}
	
	private boolean isTestEnabled(String testName) {
		String testKey = "test." + testName;
		return props.getProperty(testKey) == null || Boolean.valueOf(props.getProperty(testKey));
	}
	
	private void testGrpcMethod(String testName, int nThreads, int iterationCount, Runnable test) {
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
			latch.await(120, TimeUnit.SECONDS);

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
		employeeClient.getEmployee(employeeIDs.get(idx));
//		logger.info(employee.toString());
	}
	
	private void testCreateEmployee() {
		int idx = rnd.nextInt(sampleDataAsList.size());
		EmployeeDTO emp = sampleDataAsList.get(idx);
		
		employeeClient.createEmployee(emp);
//		logger.info("create employee with ID: "+ empId);
	}
	
	private void testGetEmployeesList(int batchSize) {
		List<Employee> empList = employeeClient.getEmployeesList(batchSize);
//		logger.info("get employee list: " + empList.toString());
	}
	
	private void testCreateEmployeesList(int batchSize) {
		List<EmployeeDTO> empDTOList = new ArrayList<>();
		for (int i=0; i<batchSize; i++)
			empDTOList.add(sampleDataAsList.get(i));
		
		List<CreateEmployeeResponse> listResponse = employeeClient.createEmployeesList(empDTOList);
//		logger.info("created employee IDs: " + idList);
	}
	
	private void testGetEmployeesStreaming() {
		List<Employee> empList = employeeClient.getEmployeesStreaming();
//		logger.info("get employee streaming - received: "+empList.size());
	}
	
	private void testCreateEmployeesStreaming() {
		List<CreateEmployeeResponse> listResponse = employeeClient.createEmployeesList(sampleDataAsList);
//		logger.info("sent employees:" + sampleDataAsList.size() + " received IDs: " + listResponse.size());
	}
	
	private void warmUp() {
//		int count = employeeClient.getEmployeeCount();
		employeeIDs = employeeClient.getEmployeeIDs();
		logger.log(Level.INFO, "employee count on server: {0}", new Object[] {employeeIDs.size()});
	}
	
	private void saveTestResults() {
		if (testResults.size() == 0)
			return ;
		
		File outFile = new File(props.getOutputFileName());
		boolean addCSVHeader = !outFile.exists() || !props.isAppendToOutputFile();
		
		try ( FileWriter fw = new FileWriter(outFile, props.isAppendToOutputFile());
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
		channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
		
		saveTestResults();
	}	

}
