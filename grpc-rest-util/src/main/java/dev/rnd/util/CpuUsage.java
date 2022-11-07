package dev.rnd.util;

public class CpuUsage {

	private long durationMillis;
	private long totalCpuTimeMillis;
	private double cpuUtilizationPercentage;
	
	public CpuUsage() {}
	
	public CpuUsage(long durationMillis, long totalCpuTime, double cpuUtilizationPercentage) {
		super();
		this.durationMillis = durationMillis;
		this.totalCpuTimeMillis = totalCpuTime;
		this.cpuUtilizationPercentage = cpuUtilizationPercentage;
	}
	
	public long getDurationMillis() {
		return durationMillis;
	}
	public long getTotalCpuTimeMillis() {
		return totalCpuTimeMillis;
	}
	public double getCpuUtilizationPercentage() {
		return cpuUtilizationPercentage;
	}

	public void setDurationMillis(long durationMillis) {
		this.durationMillis = durationMillis;
	}
	public void setTotalCpuTimeMillis(long totalCpuTime) {
		this.totalCpuTimeMillis = totalCpuTime;
	}
	public void setCpuUtilizationPercentage(double cpuUtilizationPercentage) {
		this.cpuUtilizationPercentage = cpuUtilizationPercentage;
	}
	
}
