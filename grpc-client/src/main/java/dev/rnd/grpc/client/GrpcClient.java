package dev.rnd.grpc.client;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

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
  
  private Map<Integer, EmployeeDTO> sampleData = new HashMap<>();

  GrpcClient() {
  	props = ApplicationProperties.getAppProperties();
  	
  	if ("OFF".equalsIgnoreCase(props.getJavaLogging()))
  		Logger.getLogger(GrpcClient.class.getPackageName()).setLevel(Level.OFF);
  	
  	EmployeeUtil.loadDataSet(SAMPLE_DATA_SET, sampleData);
  	
  	channel = Grpc.newChannelBuilder(props.getServerAddress(), InsecureChannelCredentials.create()).build();
  	employeeClient = new EmployeeGrpcClient(channel);
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
		
		testGetEmployeeByID();
		
		testCreateEmployee();
		
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
	
	private void testGetEmployeeByID() {
		Employee employee = employeeClient.getEmployee(804206);
		logger.info(employee.toString());
	}
	
	private void testCreateEmployee() {
		EmployeeDTO emp = sampleData.values().iterator().next();
		
		int empId = employeeClient.createEmployee(emp);
		logger.info("create employee wiht ID: "+ empId);
	}
	
	private void shutdown() throws InterruptedException {
		channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
	}	

}
