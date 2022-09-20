package dev.rnd.grpc.server.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import dev.rnd.grpc.server.controller.EmployeeUtil;

public class EmployeeService {
	
	private Map<Integer, EmployeeDTO> employeeRepo = new HashMap<>();
	
	private Random rnd = new Random(System.currentTimeMillis());
		
	public EmployeeDTO getEmployeeById(int empId) {
		return employeeRepo.get(empId);
	}

	public int getEmployeesCount() {
		return employeeRepo.size();
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
		EmployeeUtil.loadDataSet(sampleDateFileName, employeeRepo);
	}
	
}
