package dev.rnd.rest.server.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import dev.rnd.rest.server.service.CreateEmployeeResponse;
import dev.rnd.rest.server.service.Employee;
import dev.rnd.rest.server.service.EmployeeService;

@RestController
public class EmployeeController {
	private static final Logger logger = LoggerFactory.getLogger(EmployeeController.class);

	@Autowired
	private EmployeeService empService;
	
	@GetMapping("/employees/:count")
	@ResponseBody 
	public int getEmployeeCount() {
		return empService.getEmployeesCount();
	}
	
	@GetMapping("/employees/:ids")
	@ResponseBody 
	public List<Integer> getEmployeeIDs() {
		return empService.getEmployees().stream().map( e -> e.getEmpId()).collect(Collectors.toList());
	}
	
	@GetMapping(value="/employees/{id}")
	@ResponseBody 
	public Employee getEmployee(@PathVariable("id") int empId) {
		return empService.getEmployeeById(empId);
	}
	
	@PostMapping(value="/employees")
	public ResponseEntity<CreateEmployeeResponse> createEmployee(@RequestBody Employee emp) {
		int empId = empService.createEmployee(emp);
		CreateEmployeeResponse response = new CreateEmployeeResponse();
		response.setEmployeeId(empId);
		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

	@GetMapping(value="/employees")
	@ResponseBody 
	public List<Employee> getEmployeesList(@RequestParam("count") int count) {
		Collection<Employee> allEmployees = empService.getEmployees();
		count = Math.min(count, allEmployees.size());
		
		List<Employee> result = new ArrayList<>();
		Iterator<Employee> iterator = allEmployees.iterator();
		for (int i=0; i<count; i++)
			result.add(iterator.next());
			
		return result;
	}

	@PostMapping(value="/employees/bulk")
	@ResponseBody
	public List<CreateEmployeeResponse> createEmployeeList(@RequestBody List<Employee> empList) {
		List<CreateEmployeeResponse> result = new ArrayList<>();
		for (Employee emp: empList) {
			int empId = empService.createEmployee(emp);
			CreateEmployeeResponse response = new CreateEmployeeResponse();
			response.setEmployeeId(empId);
			
			result.add(response);
		}
		logger.info("Created " + empList.size() + " employees!");
		
		return result;
	}
	
}
