package dev.rnd.grpc.client;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.concurrent.ConcurrentHashMap;

public class TestResult {

	private LocalDateTime executionTime;
	private String testName;
	private int threadCount;
	private int iterationCountPerThread;
	
	private static final String CSV_HEADER = "dateTime,testName,threadCount,iterationCount,avgExecTime,errorPercentage";
	
	private Map<String, Integer[]> executionPerThread = new ConcurrentHashMap<>();
	
	public TestResult(String testName, int threadCount, int iterationCount) {
		this.testName = testName;
		this.threadCount = threadCount;
		this.iterationCountPerThread = iterationCount;
		executionTime = LocalDateTime.now();
	}
	
	public void addExecutionResult(int execTimeMillis, int errors) {
		executionPerThread.put(Thread.currentThread().getName(), new Integer[] {execTimeMillis, errors});
	}
	
	public int getAverageExecTime() {
		OptionalDouble avg = executionPerThread.values().stream().mapToInt(a -> a[0]).average();
		
		return (avg.isPresent()) ? (int)avg.getAsDouble() : 0;
	}
	
	public double getErrorsPercentage() {
		int totalErrors = executionPerThread.values().stream().mapToInt(a -> a[1]).sum();
		
		return (double)(totalErrors * 100) / (threadCount * iterationCountPerThread);
	}

	public LocalDateTime getExecutionTime() {
		return executionTime;
	}

	public String getTestName() {
		return testName;
	}

	public int getThreadCount() {
		return threadCount;
	}

	public int getIterationCountPerThread() {
		return iterationCountPerThread;
	}
	
	public static String getCSVHeader() {
		return CSV_HEADER;
	}
	
	public String getResultAsCSV() {
		StringBuilder csv = new StringBuilder();
		csv.append(executionTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss")));
		csv.append(',');
		
		csv.append(testName);
		csv.append(',');
		
		csv.append(threadCount);
		csv.append(',');
		
		csv.append(iterationCountPerThread);
		csv.append(',');
		
		csv.append(getAverageExecTime());
		csv.append(',');
		
		csv.append(getErrorsPercentage());
		
		return csv.toString();
	}
}
