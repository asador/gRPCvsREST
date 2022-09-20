package dev.rnd.grpc.server.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

public class EmployeeService {
	
	private Map<Integer, EmployeeDTO> employeeRepo = new HashMap<>();
	
	private Random rnd = new Random(System.currentTimeMillis());
		
	public EmployeeDTO getEmployeeById(int empId) {
		return employeeRepo.get(empId);
	}

	
	public int createEmployee(EmployeeDTO dto) {
		int empId = rnd.nextInt(1000000);
		dto.setEmpId(empId);
		employeeRepo.put(empId, dto);
		
		return empId;
	}
	
//	public Collection<EmployeeDTO> getLocations() {
//		return Collections.unmodifiableCollection(employeeRepo.values());
//	}
	
	public void loadDataSet(String sampleDateFileName) {
			Scanner scanner = new Scanner(EmployeeService.class.getClassLoader().getResourceAsStream(sampleDateFileName));
	    scanner.nextLine();
			while (scanner.hasNextLine()) {
	        EmployeeDTO dto = csv2DTO(scanner.nextLine());
	        employeeRepo.put(dto.getEmpId(), dto);
	    }
			scanner.close();
	}
	
	private EmployeeDTO csv2DTO(String recordCSV) {
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
}
