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
	
	private static final String CSV_HEADER = "dateTime,testName,threadCount,iterationCount,execTime";
	
	private Map<String, Long> executionTimePerThread = new ConcurrentHashMap<>();
	
	public TestResult(String testName, int threadCount, int iterationCount) {
		this.testName = testName;
		this.threadCount = threadCount;
		this.iterationCountPerThread = iterationCount;
		executionTime = LocalDateTime.now();
	}
	
	public void addExecutionTime(long execTimeMillis) {
		executionTimePerThread.put(Thread.currentThread().getName(), execTimeMillis);
	}
	
	public long getAverageExecTime() {
		OptionalDouble avg = executionTimePerThread.values().stream().mapToLong(a -> a).average();
		
		return (avg.isPresent()) ? (long)avg.getAsDouble() : 0;
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
		csv.append(executionTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm")));
		csv.append(',');
		
		csv.append(testName);
		csv.append(',');
		
		csv.append(threadCount);
		csv.append(',');
		
		csv.append(iterationCountPerThread);
		csv.append(',');
		
		csv.append(getAverageExecTime());
		
		return csv.toString();
	}
}
