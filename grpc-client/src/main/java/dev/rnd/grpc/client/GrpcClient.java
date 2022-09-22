package dev.rnd.grpc.client;


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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import dev.rnd.grpc.employee.Employee;
import dev.rnd.grpc.employee.EmployeeID;
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
  
  private Map<Integer, EmployeeDTO> sampleData = new HashMap<>();
  private List<EmployeeDTO> sampleDataAsList = new ArrayList<>();
  private List<Integer> employeeIDs;
  private ExecutorService executor;
  
  private List<TestResult> testResults = new ArrayList<>();
  
  private Random rnd = new Random(System.currentTimeMillis());

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
		
		if (props.isTestGetEmployeeON()) {			
			testGrpcMethod("getEmployeeByID", 
					props.getThreadCount(), 
					props.getIterationCount(), 
					() -> {
				testGetEmployeeByID();
			});
		}
		
		if (props.isTestCreateEmployeeON()) {
			testGrpcMethod("createEmployee", 
					props.getThreadCount(), 
					props.getIterationCount(), 
					() -> {
				testCreateEmployee();
			});
		}
		
		int[] batchSizes = new int[] {10, 100, 200, 1000};
		
		for (int batchSize: batchSizes) {
			testGrpcMethod("getEmployeesList-"+batchSize, 1, 10, () -> {
				testGetEmployeesList(batchSize);
			});
			
			testGrpcMethod("createEmployeesList-"+batchSize, 1, 10, () -> {
				testCreateEmployeesList(batchSize);
			});
			
		}
				
//		EmployeeGrpcClient employeeClient = new EmployeeGrpcClient(channel);
//		try {
//			Random rnd = new Random(System.currentTimeMillis());
//
//			int locId1 = employeeClient.createLocation(10, "Location A-" + rnd.nextInt(1000));
//			int locId2 = employeeClient.createLocation(20, "Location B-" + rnd.nextInt(1000));
//			int locId3 = employeeClient.createLocation(30, "Location C-" + rnd.nextInt(1000));
//
//			Location l1 = employeeClient.getLocation(locId1);
//			printLocation(l1);
//
//			Location l2 = employeeClient.getLocation(locId2);
//			printLocation(l2);
//
//			int numBulkLocation = 5;
//			List<Location> locations = generateLocations(numBulkLocation, false);
//			int createdLocations = employeeClient.bulkCreateLocation(locations);
//			System.out.printf("BulkCreateLocation: expected=%d, actual=%d \n", numBulkLocation, createdLocations);
//
//			numBulkLocation = 3;
//			locations = generateLocations(numBulkLocation, true);
//			List<Integer> locationIDs = employeeClient.bulkCreateLocation2(locations);
//			System.out.printf("BulkCreateLocation2: expected=%d, actual=%d \n", numBulkLocation + 1, locationIDs.size());
//			for (int locId : locationIDs)
//				System.out.printf("locId=%d \n", locId);
//
//			locations = employeeClient.getLocations();
//			System.out.printf("All locations count: %d\n", locations.size());
//			for (Location l : locations)
//				printLocation(l);
//
//			// calling api that requires an auth token passed as "Authorization" header;
//			Location l3 = employeeClient.getLocationSecure(locId1);
//			printLocation(l3);
//
//			// error scenarios which causes a runtime exception
//			employeeClient.getLocation(9999);
//
//		}
//		catch (Exception e) {
//			if (e instanceof StatusRuntimeException) {
//				StatusRuntimeException sre = (StatusRuntimeException) e;
//
//				// System.out.println("Exception status: " + sre.getStatus().toString());
//				Metadata metadata = sre.getTrailers();
//				ErrorInfo errInfo = metadata.get(ProtoUtils.keyForProto(ErrorInfo.getDefaultInstance()));
//				if (errInfo != null)
//					System.out.println("Exception metadata: " + errInfo.toString());
//			}
//			e.printStackTrace();
//		}
//		finally {
//			channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
//		}
	}
	
	private void testGrpcMethod(String testMethodName, int nThreads, int iterationCount, Runnable r) {
		TestResult testResult = new TestResult(testMethodName, nThreads, iterationCount);
		testResults.add(testResult);
		
		for (int i=0; i< nThreads; i++) {
			executor.execute(() -> {
				int errorCount = 0;
				long start = System.currentTimeMillis();
				for (int j=0; j<iterationCount; j++) {
					try {
						r.run();
					}
					catch (Exception e) {
						errorCount++;
						e.printStackTrace();
					}
				}
				long duration = System.currentTimeMillis() - start;
				testResult.addExecutionResult((int)duration, errorCount);				
			});
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
//		logger.info("Get employee list: " + empList.toString());
	}
	
	private void testCreateEmployeesList(int batchSize) {
		List<EmployeeDTO> empDTOList = new ArrayList<>();
		for (int i=0; i<batchSize; i++)
			empDTOList.add(sampleDataAsList.get(i));
		
		List<EmployeeID> idList = employeeClient.createEmployeesList(empDTOList);
//		logger.info("created employee IDs: " + idList);
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
