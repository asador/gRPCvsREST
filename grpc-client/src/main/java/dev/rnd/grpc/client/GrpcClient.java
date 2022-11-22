package dev.rnd.grpc.client;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.protobuf.Empty;

import dev.rnd.grpc.employee.CreateEmployeeResponse;
import dev.rnd.grpc.employee.Employee;
import dev.rnd.grpc.server.controller.EmployeeUtil;
import dev.rnd.grpc.server.service.EmployeeDTO;
import dev.rnd.grpc.system.CpuUsageProto;
import dev.rnd.grpc.system.SystemGrpcServiceGrpc;
import dev.rnd.grpc.system.SystemGrpcServiceGrpc.SystemGrpcServiceBlockingStub;
import dev.rnd.util.CpuUsage;
import dev.rnd.util.TestClient;
import io.grpc.ChannelCredentials;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import io.grpc.TlsChannelCredentials;

public class GrpcClient extends TestClient {

  private static final Logger logger = Logger.getLogger(GrpcClient.class.getName());
  private static final String SAMPLE_DATA_SET = "sampleEmployeeData.csv";
  
  private ApplicationProperties props;
  private ManagedChannel channel;
  private EmployeeGrpcClient employeeClient;
  private SystemGrpcServiceBlockingStub systemBlockingStub;
  
  private Map<Integer, EmployeeDTO> sampleData = new HashMap<>();
  private List<EmployeeDTO> sampleDataAsList = new ArrayList<>();
  private List<Integer> employeeIDs;
  
  private Random rnd = new Random(System.currentTimeMillis());

  GrpcClient() {
  	props = ApplicationProperties.getAppProperties();
  	
  	if ("OFF".equalsIgnoreCase(props.getJavaLogging()))
  		Logger.getLogger(GrpcClient.class.getPackageName()).setLevel(Level.OFF);
  	
  	EmployeeUtil.loadDataSet(SAMPLE_DATA_SET, sampleData);
  	sampleDataAsList.addAll(sampleData.values());

  	ChannelCredentials channelCreds = InsecureChannelCredentials.create();
  	
  	try {
			if (props.isTlsEnabled())
				channelCreds = TlsChannelCredentials.newBuilder().trustManager(new File(props.getCertFilesPath() + "/ca-cert.pem")).build();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
  	
  	channel = Grpc.newChannelBuilder(props.getServerAddress(), channelCreds).build();
  	employeeClient = new EmployeeGrpcClient(channel);
  	systemBlockingStub = SystemGrpcServiceGrpc.newBlockingStub(channel);
  	
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

	@Override
	protected String getProperty(String key) {
		return props.getProperty(key);
	}

	@Override
	protected int getThreadCount() {
		return props.getThreadCount();
	}

	@Override
	protected int getCpuTimeSampleInterval() {
		return props.getCpuTimeInterval();
	}

	@Override
	protected String getOutputFilename() {
		return props.getOutputFileName();
	}

	@Override
	protected boolean isAppendToOutputFile() {
		return props.isAppendToOutputFile();
	}

	private void runTests() {
		preTestRun();
		warmUp();
		
		// single object read/write
		int[] iterations = new int[] {10000};
		for (int count : iterations) {
			testRemoteMethod("getEmployeeByID", props.getThreadCount(),	count, 
					() -> testGetEmployeeByID());

			testRemoteMethod("createEmployee", props.getThreadCount(), count, 
					() -> testCreateEmployee());
		}
		
		// batch/list of objects read/write
		int[] batchSizes = new int[] {10, 100, 1000};
		for (int count : iterations)
			for (int batch : batchSizes) {
		
				testRemoteMethod("getEmployeesList-"+batch, props.getThreadCount(), count, 
						() -> testGetEmployeesList(batch));
				
				testRemoteMethod("createEmployeesList-"+batch, props.getThreadCount(), count, 
						() -> testCreateEmployeesList(batch));			
			}
		
		// read/write list of objects with streaming
		iterations = new int[] {1000};
		int[] numRecords = {100, 1000};
		for (int count : iterations) 
			for (int numRec: numRecords) {
				testRemoteMethod("getEmployeesStreaming-"+numRec, props.getThreadCount(),	count, 
						() -> testGetEmployeesStreaming(numRec));
	
				testRemoteMethod("createEmployeesStreaming-"+numRec, props.getThreadCount(), count, 
						() -> testCreateEmployeesStreaming(numRec));
			}
	}
	
	@Override
	protected void startServerCpuUsageMonitor() {
		systemBlockingStub.startCpuUsageMonitor(Empty.newBuilder().build());
	}
	@Override
	protected CpuUsage stopAndGetServerCpuUsage() {
		CpuUsageProto serverCpuUsageProto = systemBlockingStub.stopAndGetCpuUsage(Empty.newBuilder().build());
		return new CpuUsage(serverCpuUsageProto.getDuration(), serverCpuUsageProto.getTotalCpuTime(), 
				serverCpuUsageProto.getUtilization());
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
	
	private void testGetEmployeesStreaming(int count) {
		List<Employee> empList = employeeClient.getEmployeesStreaming(count);
//		logger.info("get employee streaming - received: "+empList.size());
	}
	
	private void testCreateEmployeesStreaming(int count) {
		List<CreateEmployeeResponse> listResponse = employeeClient.createEmployeesStreaming(sampleDataAsList, count);
//		logger.info("sent employees:" + sampleDataAsList.size() + " received IDs: " + listResponse.size());
	}
	
	private void warmUp() {
//		int count = employeeClient.getEmployeeCount();
		employeeIDs = employeeClient.getEmployeeIDs();
		logger.log(Level.INFO, "employee count on server: {0}", new Object[] {employeeIDs.size()});
	}
	
	@Override
	protected void shutdown() throws InterruptedException {
		super.shutdown();
		channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
	}	

}
