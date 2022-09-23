package dev.rnd.grpc.server.service;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import dev.rnd.grpc.server.controller.EmployeeUtil;

public class EmployeeService {
	
	private Map<Integer, EmployeeDTO> employeeRepo = new HashMap<>();
	private boolean storeRecordsOnCreate;
	
	private Random rnd = new Random(System.currentTimeMillis());
		
	public EmployeeService(boolean storeOnCreate) {
		storeRecordsOnCreate = storeOnCreate;
	}
	public EmployeeDTO getEmployeeById(int empId) {
		return employeeRepo.get(empId);
	}

	public int getEmployeesCount() {
		return employeeRepo.size();
	}
	
	public int createEmployee(EmployeeDTO dto) {
		int empId = rnd.nextInt(1000000);

		// to make the tests lighter and eliminate the impact of employeeRepo growth during multiple test runs
		if (storeRecordsOnCreate) {
			dto.setEmpId(empId);
			employeeRepo.put(empId, dto);
		}
		
		return empId;
	}
	
	public Collection<EmployeeDTO> getEmployees() {
		return Collections.unmodifiableCollection(employeeRepo.values());
	}
	
	public void loadDataSet(String sampleDateFileName) {
		EmployeeUtil.loadDataSet(sampleDateFileName, employeeRepo);
	}
	
}
