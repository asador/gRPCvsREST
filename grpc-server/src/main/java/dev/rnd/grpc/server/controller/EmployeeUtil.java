package dev.rnd.grpc.server.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Scanner;

import dev.rnd.grpc.employee.Address;
import dev.rnd.grpc.employee.Date;
import dev.rnd.grpc.employee.Employee;
import dev.rnd.grpc.server.service.EmployeeDTO;

public class EmployeeUtil {

	public static Employee dto2EmployeeProto(EmployeeDTO dto) {
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
  
	public static EmployeeDTO employeeProto2DTO(Employee emp) {
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

	public static EmployeeDTO csv2DTO(String recordCSV) {
		String[] fields = recordCSV.split(",");
		EmployeeDTO dto = new EmployeeDTO();
		dto.setEmpId(Integer.valueOf(fields[0]));
		dto.setFirstName(fields[1]);
		dto.setLastName(fields[2]);
		dto.setTitle(fields[3]);
		dto.setEmail(fields[4]);
		dto.setDepartmentId(Integer.valueOf(fields[5]));
		dto.setManagerId(Integer.valueOf(fields[6]));
		dto.setPhone(fields[7]);
		dto.setStartDate(LocalDate.parse(fields[8], DateTimeFormatter.ofPattern("yyyy-MM-dd")));
		dto.setAddress(fields[9]);
		dto.setCity(fields[10]);
		dto.setState(fields[11]);
		dto.setZipCode(fields[12]);
		
		return dto;
		
	}

	public static void loadDataSet(String sampleDateFileName, Map<Integer, EmployeeDTO> dataset) {
		Scanner scanner = new Scanner(EmployeeUtil.class.getClassLoader().getResourceAsStream(sampleDateFileName));
    scanner.nextLine();
		while (scanner.hasNextLine()) {
        EmployeeDTO dto = EmployeeUtil.csv2DTO(scanner.nextLine());
        dataset.put(dto.getEmpId(), dto);
    }
		scanner.close();
	}

}
