package dev.rnd.rest.server;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.rnd.rest.server.service.EmployeeService;
import dev.rnd.util.CpuUsageCalculator;

@Configuration
public class ServerConfig {

	@Bean
	public EmployeeService employeeService(@Value("${sampleDatafile}") String sampleDataset, 
			@Value("${storeOnCreate: false}") boolean storeOnCreate) {
		
		EmployeeService empService = new EmployeeService(storeOnCreate);
		empService.loadDataSet(sampleDataset);
		
		return empService;
	}
	
	@Bean
	public CpuUsageCalculator cpuTimeCalculator(@Value("${cpuTimeSampleIntervalMillisec}") int interval) {
		return new CpuUsageCalculator(interval);
	}
}
