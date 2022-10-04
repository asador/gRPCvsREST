package dev.rnd.rest.server.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmployeeUtil {
	
	private static final Logger logger = LoggerFactory.getLogger(EmployeeUtil.class);

	public static Employee csv2DTO(String recordCSV) {
		String[] fields = recordCSV.split(",");
		Employee dto = new Employee();
		dto.setEmpId(Integer.valueOf(fields[0]));
		dto.setFirstName(fields[1]);
		dto.setLastName(fields[2]);
		dto.setTitle(fields[3]);
		dto.setEmail(fields[4]);
		dto.setDepartmentId(Integer.valueOf(fields[5]));
		dto.setManagerId(Integer.valueOf(fields[6]));
		dto.setPhone(fields[7]);
		dto.setStartDate(LocalDate.parse(fields[8], DateTimeFormatter.ofPattern("yyyy-MM-dd")));
		
		Address address = new Address();
		address.setStreetAddress(fields[9]);
		address.setCity(fields[10]);
		address.setState(fields[11]);
		address.setZipCode(fields[12]);
		
		dto.setAddress(address);
		
		return dto;
	}

	public static void loadDataSet(String sampleDateFileName, Map<Integer, Employee> dataset) {
		Random rnd = new Random(System.currentTimeMillis());
		
		Scanner scanner = new Scanner(EmployeeUtil.class.getClassLoader().getResourceAsStream(sampleDateFileName));
    scanner.nextLine();
    int count = 0;
		while (scanner.hasNextLine()) {
        Employee dto = EmployeeUtil.csv2DTO(scanner.nextLine());
//        if (dataset.containsKey(dto.getEmpId()))
//        	System.out.println(dto.getEmpId());
        while (dataset.containsKey(dto.getEmpId()))
        	dto.setEmpId(rnd.nextInt(10000000));
        dataset.put(dto.getEmpId(), dto);
        
        count++;
    }
		scanner.close();
		logger.info("Loaded " + count + " sample employee records");
	}

}
