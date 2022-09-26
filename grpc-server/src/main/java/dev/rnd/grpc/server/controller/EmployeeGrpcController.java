package dev.rnd.grpc.server.controller;

import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.protobuf.Empty;
import com.google.rpc.ErrorInfo;

import dev.rnd.grpc.employee.Count;
import dev.rnd.grpc.employee.CreateEmployeeListResponse;
import dev.rnd.grpc.employee.CreateEmployeeResponse;
import dev.rnd.grpc.employee.Employee;
import dev.rnd.grpc.employee.EmployeeGrpcServiceGrpc.EmployeeGrpcServiceImplBase;
import dev.rnd.grpc.employee.EmployeeID;
import dev.rnd.grpc.employee.EmployeeList;
import dev.rnd.grpc.server.service.EmployeeDTO;
import dev.rnd.grpc.server.service.EmployeeService;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.stub.StreamObserver;

public class EmployeeGrpcController extends EmployeeGrpcServiceImplBase {

  private static final Logger logger = Logger.getLogger(EmployeeGrpcController.class.getName());
  
	private EmployeeService employeeService;
	
	public EmployeeGrpcController(EmployeeService empService) {
		this.employeeService = empService;
	}
	
	@Override
  public void getEmployee(EmployeeID request, StreamObserver<Employee> responseObserver) {
		logger.info("called getEmployee: " + request.getEmpId());

		EmployeeDTO dto = employeeService.getEmployeeById(request.getEmpId());
		if (dto == null) {
			// error handling example
			Metadata.Key<ErrorInfo> errorKey = ProtoUtils.keyForProto(ErrorInfo.getDefaultInstance());
			Metadata metadata = new Metadata();
			ErrorInfo errInfo = ErrorInfo.newBuilder()
					.setReason("invalid parameter")
					.putMetadata("locationId", String.valueOf(request.getEmpId()))
					.build();
			metadata.put(errorKey, errInfo);
			
			responseObserver.onError(Status.NOT_FOUND
					.withDescription("Could not find employee with id: "+request.getEmpId())
					.asRuntimeException(metadata));
			return;

		}
		
		Employee emp = EmployeeUtil.dto2EmployeeProto(dto);
		
		responseObserver.onNext(emp);
		responseObserver.onCompleted();
  }

  @Override
	public void getEmployeesCount(Empty request, StreamObserver<Count> responseObserver) {
		Count count = Count.newBuilder().setCount(employeeService.getEmployeesCount()).build();
		
		responseObserver.onNext(count);
		responseObserver.onCompleted();
	}

	@Override
  public void createEmployee(Employee request, StreamObserver<CreateEmployeeResponse> responseObserver) {
  	EmployeeDTO dto = EmployeeUtil.employeeProto2DTO(request);
  	
  	int empId = employeeService.createEmployee(dto);
  	
		CreateEmployeeResponse createResponse = CreateEmployeeResponse.newBuilder()
				.setEmployeeId(EmployeeID.newBuilder().setEmpId(empId))
				.build();
		responseObserver.onNext(createResponse);
  	responseObserver.onCompleted();

  	logger.info("called createEmployee: " + empId);
  }

	@Override
	public void getEmployeeIDs(Empty request, StreamObserver<EmployeeID> responseObserver) {
		Collection<EmployeeDTO> emps = employeeService.getEmployees();

		for (EmployeeDTO dto : emps) {
			responseObserver.onNext(EmployeeID.newBuilder().setEmpId(dto.getEmpId()).build());
		}

		responseObserver.onCompleted();
		logger.info("called getEmployeeIDs: " + emps.size() + " emp IDs returned");
	}	
	
	@Override
	public void getEmployeesList(Count request, StreamObserver<EmployeeList> responseObserver) {
		int count = request.getCount();
		Collection<EmployeeDTO> emps = employeeService.getEmployees();
		count = Math.min(count, emps.size());
		
		EmployeeList.Builder builder = EmployeeList.newBuilder();
		Iterator<EmployeeDTO> iterator = emps.iterator();
		for (int i=0; i<count; i++) {
			builder.addEmployee(EmployeeUtil.dto2EmployeeProto(iterator.next()));
		}
		
		responseObserver.onNext(builder.build());
		responseObserver.onCompleted();
	}

	@Override
	public void createEmployeesList(EmployeeList request, StreamObserver<CreateEmployeeListResponse> responseObserver) {
		CreateEmployeeListResponse.Builder builder = CreateEmployeeListResponse.newBuilder();
		
		for (Employee emp : request.getEmployeeList()) {
			int empId = employeeService.createEmployee(EmployeeUtil.employeeProto2DTO(emp));
			builder.addCreateResponse(CreateEmployeeResponse.newBuilder()
					.setEmployeeId(EmployeeID.newBuilder().setEmpId(empId)));
		}
		
		responseObserver.onNext(builder.build());
		responseObserver.onCompleted();

	}

	@Override
	public void getEmployeesStreaming(Empty request, StreamObserver<Employee> responseObserver) {
		Collection<EmployeeDTO> emps = employeeService.getEmployees();

		for (EmployeeDTO dto : emps) {
			responseObserver.onNext(EmployeeUtil.dto2EmployeeProto(dto));
		}

		responseObserver.onCompleted();
		logger.info("called getEmployeesStreaming: " + emps.size() + " employees returned");
	}
	
	private com.google.rpc.Status validate(Employee e) {
//		com.google.rpc.Status errorStatus = com.google.rpc.Status.newBuilder().setCode(Code.INVALID_ARGUMENT_VALUE)
//				.setMessage("Employee object is invalid").build();

		return null;
	}

	@Override
	public StreamObserver<Employee> creatEmployeesStreaming(StreamObserver<CreateEmployeeResponse> responseObserver) {
		return new StreamObserver<Employee>() {
			private int count = 0;

			@Override
			public void onNext(Employee emp) {
				com.google.rpc.Status errorStatus = validate(emp);
				if (errorStatus != null) {
					CreateEmployeeResponse createResponse = CreateEmployeeResponse.newBuilder()
							.setErrorStatus(errorStatus).build();
					responseObserver.onNext(createResponse);
				} else {

					int empId = employeeService.createEmployee(EmployeeUtil.employeeProto2DTO(emp));
					
					CreateEmployeeResponse createResponse = CreateEmployeeResponse.newBuilder()
							.setEmployeeId(EmployeeID.newBuilder().setEmpId(empId))
							.build();
					responseObserver.onNext(createResponse);
					count++;
				}
			}

			@Override
			public void onError(Throwable t) {
				logger.log(Level.WARNING, t.getMessage(), t);
			}

			@Override
			public void onCompleted() {
				responseObserver.onCompleted();

				logger.info("called creatEmployeesStreaming: " + count + " employees created");
			}
		};
	}
	
	String nullToEmpty(String value) {
		return (value == null) ? "" : value;
	}
	
}
