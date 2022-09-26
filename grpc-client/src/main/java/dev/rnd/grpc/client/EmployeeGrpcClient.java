package dev.rnd.grpc.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.protobuf.Empty;

import dev.rnd.grpc.employee.Count;
import dev.rnd.grpc.employee.CreateEmployeeListResponse;
import dev.rnd.grpc.employee.CreateEmployeeResponse;
import dev.rnd.grpc.employee.Employee;
import dev.rnd.grpc.employee.EmployeeGrpcServiceGrpc;
import dev.rnd.grpc.employee.EmployeeGrpcServiceGrpc.EmployeeGrpcServiceBlockingStub;
import dev.rnd.grpc.employee.EmployeeGrpcServiceGrpc.EmployeeGrpcServiceStub;
import dev.rnd.grpc.employee.EmployeeID;
import dev.rnd.grpc.employee.EmployeeList;
import dev.rnd.grpc.server.controller.EmployeeUtil;
import dev.rnd.grpc.server.service.EmployeeDTO;
import io.grpc.Channel;
import io.grpc.stub.StreamObserver;

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
  	CreateEmployeeResponse createResponse = blockingStub.createEmployee(EmployeeUtil.dto2EmployeeProto(dto));
  	
  	return createResponse.getEmployeeId().getEmpId();
  }
  
	public List<Integer> getEmployeeIDs() {
		List<Integer> empIDs = new ArrayList<>();
		Iterator<EmployeeID> iterator = blockingStub.getEmployeeIDs(Empty.newBuilder().build());
		while (iterator.hasNext()) {
			empIDs.add(iterator.next().getEmpId());
		}
		return empIDs;
	}
  
	public List<Employee> getEmployeesList(int count) {
		EmployeeList empList = blockingStub.getEmployeesList(Count.newBuilder().setCount(count).build());
		
		return empList.getEmployeeList();
	}
	
	public List<CreateEmployeeResponse> createEmployeesList(List<EmployeeDTO> dtoList) {
		EmployeeList.Builder builder = EmployeeList.newBuilder();
		for (EmployeeDTO dto : dtoList)
			builder.addEmployee(EmployeeUtil.dto2EmployeeProto(dto));
		
		CreateEmployeeListResponse createListResponse = blockingStub.createEmployeesList(builder.build());
		return createListResponse.getCreateResponseList();
	}

	public List<Employee> getEmployeesStreaming() {
		Iterator<Employee> iterator = blockingStub.getEmployeesStreaming(Empty.newBuilder().build());
		List<Employee> empList = new ArrayList<>();
		while (iterator.hasNext())
			empList.add(iterator.next());
		
		return empList;
	}

	public List<CreateEmployeeResponse> createEmployeesStreaming(List<EmployeeDTO> empList) {
  	final CountDownLatch finishLatch = new CountDownLatch(1);
  	final List<CreateEmployeeResponse> empIDs = new ArrayList<>();
  	
  	StreamObserver<CreateEmployeeResponse> responseObserver = new StreamObserver<CreateEmployeeResponse>() {

			@Override
			public void onNext(CreateEmployeeResponse createResponse) {
				empIDs.add(createResponse);
			}

			@Override
			public void onError(Throwable t) {
				logger.log(Level.WARNING, "", t);
				finishLatch.countDown();
			}

			@Override
			public void onCompleted() {
				finishLatch.countDown();
			}
		};
		
		StreamObserver<Employee> requestObserver = asyncStub.creatEmployeesStreaming(responseObserver);

		for (EmployeeDTO dto : empList) {
			if (finishLatch.getCount() == 0)
				break;
			requestObserver.onNext(EmployeeUtil.dto2EmployeeProto(dto));
		}
		requestObserver.onCompleted();
		
		try {
			finishLatch.await(1, TimeUnit.MINUTES);
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		return empIDs;

  }
}
