package dev.rnd.grpc.server.controller;

import java.time.LocalDate;
import java.util.logging.Logger;

import com.google.rpc.ErrorInfo;

import dev.rnd.grpc.employee.Address;
import dev.rnd.grpc.employee.Date;
import dev.rnd.grpc.employee.Employee;
import dev.rnd.grpc.employee.EmployeeControllerGrpc.EmployeeControllerImplBase;
import dev.rnd.grpc.employee.EmployeeID;
import dev.rnd.grpc.server.service.EmployeeDTO;
import dev.rnd.grpc.server.service.EmployeeService;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.stub.StreamObserver;

public class EmployeeGrpcController extends EmployeeControllerImplBase {

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
					.withDescription("Could not find location with id: "+request.getEmpId())
					.asRuntimeException(metadata));
			return;

		}
		
		Employee emp = dto2EmployeeProto(dto);
		
		responseObserver.onNext(emp);
		responseObserver.onCompleted();
  }

  @Override
  public void createEmployee(Employee request, StreamObserver<EmployeeID> responseObserver) {
  	EmployeeDTO dto = employeeProto2DTO(request);
  	
  	int employeeId = employeeService.createEmployee(dto);
  	
  	EmployeeID empID = EmployeeID.newBuilder().setEmpId(employeeId).build();
  	
  	responseObserver.onNext(empID);
  	responseObserver.onCompleted();

  	logger.info("called createEmployee: " + employeeId);
  }

//	@Override
//	public void listLocations(Empty request, StreamObserver<Location> responseObserver) {
//		Collection<EmployeeDTO> locations = employeeService.getLocations();
//		
//		for (EmployeeDTO dto: locations) {
//			Location loc = Location.newBuilder()
//					.setLocId(dto.getLocationId())
//					.setName(dto.getName())
//					.setOrgId(dto.getOrgId())
//					.setAddress(nullToEmpty(dto.getAddress()))
//					.build();
//					
//			responseObserver.onNext(loc);			
//		}
//		
//  	responseObserver.onCompleted();
//   	logger.info("called listLocations: " + locations.size() + " locations returned");
//
//	}
//
//	@Override
//	public StreamObserver<Location> bulkCreatLocation(final StreamObserver<CountHolder> responseObserver) {
//		return new StreamObserver<Location>() {
//			int count = 0;
//			
//			@Override
//			public void onNext(Location loc) {
//		  	EmployeeDTO dto = new EmployeeDTO();
//		  	dto.setName(loc.getName());
//		  	dto.setOrgId(loc.getOrgId());
//		  	dto.setAddress(loc.getAddress());
//		  	employeeService.createLocation(dto);
//		  	count++;
//			}
//
//			@Override
//			public void onError(Throwable t) {
//				logger.log(Level.WARNING, t.getMessage(), t);
//			}
//
//			@Override
//			public void onCompleted() {
//				CountHolder ch = CountHolder.newBuilder().setCount(count).build();
//				responseObserver.onNext(ch);
//				responseObserver.onCompleted();
//				
//				logger.info("called bulkCreatLocation: " + count + " locations created");
//			}
//			
//		};
//	}
//
//	@Override
//	public StreamObserver<Location> bulkCreatLocation2(final StreamObserver<BulkCreateLocationResponse> responseObserver) {
//		return new StreamObserver<Location>() {
//			private int count = 0;
//
//			@Override
//			public void onNext(Location loc) {
//				if (loc.getOrgId() <= 0) {
//					// error handling example in streams
//					com.google.rpc.Status errorStatus = com.google.rpc.Status.newBuilder()
//							.setCode(Code.INVALID_ARGUMENT_VALUE)
//							.setMessage("OrgId cannot be negative")
//							.build();
//					BulkCreateLocationResponse locCreateResponse = BulkCreateLocationResponse.newBuilder()
//							.setErrorStatus(errorStatus).build();
//					responseObserver.onNext(locCreateResponse);
//					
//				} else {
//					
//					EmployeeDTO dto = new EmployeeDTO();
//					dto.setName(loc.getName());
//					dto.setOrgId(loc.getOrgId());
//					dto.setAddress(loc.getAddress());
//					
//					int locationId = employeeService.createLocation(dto);
//					LocationID locId = LocationID.newBuilder().setLocId(locationId).build();
//
//					BulkCreateLocationResponse locCreateResponse = BulkCreateLocationResponse.newBuilder()
//							.setLocationId(locId).build();
//					responseObserver.onNext(locCreateResponse);
//					count++;
//				}
//		  	
//			}
//
//			@Override
//			public void onError(Throwable t) {
//				logger.log(Level.WARNING, t.getMessage(), t);
//			}
//
//			@Override
//			public void onCompleted() {
//				responseObserver.onCompleted();
//				
//				logger.info("called bulkCreatLocation2: " + count + " locations created");
//			}
//			
//		};
//	}

	private String nullToEmpty(String value) {
		return (value == null) ? "" : value;
	}
	
	private Employee dto2EmployeeProto(EmployeeDTO dto) {
		return Employee.newBuilder()
						.setAddress(Address.newBuilder()
								.setStreetAddress(dto.getAddress())
								.setCity(dto.getCity())
								.setState(dto.getState())
								.setZipCode(dto.getZipCode()))
						.setDepartmentId(dto.getDepartmentId())
						.setEmail(dto.getEmail())
						.setEmpId(dto.getEmpId())
						.setEmploymentStartDate(Date.newBuilder()
								.setDay(dto.getStartDate().getDayOfMonth())
								.setMonth(dto.getStartDate().getMonthValue())
								.setYear(dto.getStartDate().getYear()))
						.setFirstName(dto.getFirstName())
						.setLastName(dto.getLastName())
						.setManagerId(dto.getManagerId())
						.setPhone(dto.getPhone())
						.setTitle(dto.getTitle())
						.build();		
	}
  
	private EmployeeDTO employeeProto2DTO(Employee emp) {
		EmployeeDTO dto = new EmployeeDTO();
		dto.setAddress(emp.getAddress().getStreetAddress());
		dto.setCity(emp.getAddress().getCity());
		dto.setDepartmentId(emp.getDepartmentId());
		dto.setEmail(emp.getEmail());
		dto.setEmpId(emp.getEmpId());
		dto.setFirstName(emp.getFirstName());
		dto.setLastName(emp.getLastName());
		dto.setManagerId(emp.getManagerId());
		dto.setPhone(emp.getPhone());
		dto.setStartDate(LocalDate.of(emp.getEmploymentStartDate().getYear(), 
				emp.getEmploymentStartDate().getMonth(), 
				emp.getEmploymentStartDate().getDay()));
		dto.setState(emp.getAddress().getState());
		dto.setTitle(emp.getTitle());
		dto.setZipCode(emp.getAddress().getZipCode());
		return dto;
	}
}
