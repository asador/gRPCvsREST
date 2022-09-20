package dev.rnd.grpc.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.protobuf.Empty;

import dev.rnd.grpc.employee.Employee;
import dev.rnd.grpc.employee.EmployeeGrpcServiceGrpc;
import dev.rnd.grpc.employee.EmployeeGrpcServiceGrpc.EmployeeGrpcServiceBlockingStub;
import dev.rnd.grpc.employee.EmployeeGrpcServiceGrpc.EmployeeGrpcServiceStub;
import dev.rnd.grpc.employee.EmployeeID;
import dev.rnd.grpc.server.controller.EmployeeUtil;
import dev.rnd.grpc.server.service.EmployeeDTO;
import io.grpc.Channel;

public class EmployeeGrpcClient {
	
	private static final Logger logger = Logger.getLogger(EmployeeGrpcClient.class.getName());

	private EmployeeGrpcServiceBlockingStub blockingStub;
	private EmployeeGrpcServiceStub asyncStub;

  public EmployeeGrpcClient(Channel channel) {
    blockingStub = EmployeeGrpcServiceGrpc.newBlockingStub(channel);
    asyncStub = EmployeeGrpcServiceGrpc.newStub(channel);
  }

  void info(String msg, Object... params) {
    logger.log(Level.INFO, msg, params);
  }

  void warning(String msg, Object... params) {
    logger.log(Level.WARNING, msg, params);
  }
 
  public Employee getEmployee(int empId) {
  	return blockingStub.getEmployee(EmployeeID.newBuilder().setEmpId(empId).build());
  }
  
  public int getEmployeeCount() {
  	return blockingStub.getEmployeesCount(Empty.newBuilder().build()).getCount();
  }

  public int createEmployee(EmployeeDTO dto) {
  	EmployeeID employeeID = blockingStub.createEmployee(EmployeeUtil.dto2EmployeeProto(dto));
  	
  	return employeeID.getEmpId();
  }
  
	public List<Integer> getEmployeeIDs() {
		List<Integer> empIDs = new ArrayList<>();
		Iterator<EmployeeID> iterator = blockingStub.getEmployeeIDs(Empty.newBuilder().build());
		while (iterator.hasNext()) {
			empIDs.add(iterator.next().getEmpId());
		}
		return empIDs;
	}
  
//  public List<Location> getLocations() {
//  	List<Location> locations = new ArrayList<Location>();
//  	Iterator<Location> iterator = blockingStub.listLocations(Empty.newBuilder().build());
//  	while (iterator.hasNext()) {
//  		locations.add(iterator.next());
//  	}
//  	return locations;
//  }
//  
//  public int bulkCreateLocation(List<Location> locations) {
//  	final CountDownLatch finishLatch = new CountDownLatch(1);
//  	final CountHolder.Builder createdLocations = CountHolder.newBuilder();
//  	
//  	StreamObserver<CountHolder> responseObserver = new StreamObserver<CountHolder>() {
//
//			@Override
//			public void onNext(CountHolder countHolder) {
//				createdLocations.setCount(countHolder.getCount());
//			}
//
//			@Override
//			public void onError(Throwable t) {
//				logger.log(Level.WARNING, "", t);
//				finishLatch.countDown();
//			}
//
//			@Override
//			public void onCompleted() {
//				finishLatch.countDown();
//			}
//		};
//		
//		StreamObserver<Location> requestObserver = asyncStub.bulkCreatLocation(responseObserver);
//
//		for (Location loc : locations) {
//			if (finishLatch.getCount() == 0)
//				break;
//			requestObserver.onNext(loc);
//		}
//		requestObserver.onCompleted();
//		
//		try {
//			finishLatch.await(1, TimeUnit.MINUTES);
//		}
//		catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//		
//		return createdLocations.build().getCount();
//  }
//  
//  public List<Integer> bulkCreateLocation2(List<Location> locations) {
//  	final CountDownLatch finishLatch = new CountDownLatch(1);
//  	final List<Integer> locationIDs = new ArrayList<>();
//  	
//  	StreamObserver<BulkCreateLocationResponse> responseObserver = new StreamObserver<BulkCreateLocationResponse>() {
//
//			@Override
//			public void onNext(BulkCreateLocationResponse createResponse) {
//				// streaming response with errors 
//				switch (createResponse.getMessageCase()) {
//					case LOCATIONID:
//						locationIDs.add(createResponse.getLocationId().getLocId());
//						break;
//					case ERRORSTATUS:
//						Status errorStatus = createResponse.getErrorStatus();
//						warning("status code " + Code.forNumber(errorStatus.getCode()));
//						warning("Error message: " + errorStatus.getMessage());
//						break;
//						
//				}
//			}
//
//			@Override
//			public void onError(Throwable t) {
//				logger.log(Level.WARNING, "", t);
//				finishLatch.countDown();
//			}
//
//			@Override
//			public void onCompleted() {
//				finishLatch.countDown();
//			}
//		};
//		
//		StreamObserver<Location> requestObserver = asyncStub.bulkCreatLocation2(responseObserver);
//
//		for (Location loc : locations) {
//			if (finishLatch.getCount() == 0)
//				break;
//			requestObserver.onNext(loc);
//		}
//		requestObserver.onCompleted();
//		
//		try {
//			finishLatch.await(1, TimeUnit.MINUTES);
//		}
//		catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//		
//		return locationIDs;
//
//  }
}
