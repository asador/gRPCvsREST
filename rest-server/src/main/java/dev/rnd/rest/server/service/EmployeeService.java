package dev.rnd.rest.server.service;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class EmployeeService {
	
	private Map<Integer, Employee> employeeRepo = new HashMap<>();
	private boolean storeRecordsOnCreate;
	
	private Random rnd = new Random(System.currentTimeMillis());
		
	public EmployeeService(boolean storeOnCreate) {
		storeRecordsOnCreate = storeOnCreate;
	}
	public Employee getEmployeeById(int empId) {
		return employeeRepo.get(empId);
	}

	public int getEmployeesCount() {
		return employeeRepo.size();
	}
	
	public int createEmployee(Employee dto) {
		int empId = rnd.nextInt(1000000);

		// to make the tests lighter and eliminate the impact of employeeRepo growth during multiple test runs
		if (storeRecordsOnCreate) {
			dto.setEmpId(empId);
			employeeRepo.put(empId, dto);
		}
		
		return empId;
	}
	
	public Collection<Employee> getEmployees() {
		return Collections.unmodifiableCollection(employeeRepo.values());
	}
	
	public void loadDataSet(String sampleDateFileName) {
		EmployeeUtil.loadDataSet(sampleDateFileName, employeeRepo);
	}
	
}
