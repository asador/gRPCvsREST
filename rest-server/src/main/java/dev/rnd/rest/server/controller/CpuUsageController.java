package dev.rnd.rest.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import dev.rnd.util.CpuUsage;
import dev.rnd.util.CpuUsageCalculator;

@RestController
public class CpuUsageController {
	
	@Autowired
	private CpuUsageCalculator cpuUsageCalculator;

	@RequestMapping(path = "/cpuUsage/start", method = RequestMethod.HEAD)
	public void startCpuTimeMeasurement() {
		cpuUsageCalculator.start();
	}

	@RequestMapping(path = "/cpuUsage/stop", method = RequestMethod.HEAD)
	public void stopCpuTimeMeasurement() {
		cpuUsageCalculator.stop();
	}
	
	@GetMapping("/cpuUsage/stopAndGet")
	@ResponseBody
	public CpuUsage stopAndGetCpuUsage() {
		return cpuUsageCalculator.stopAndGetCpuUsage();
	}

}
