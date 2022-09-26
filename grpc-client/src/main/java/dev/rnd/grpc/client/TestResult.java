package dev.rnd.grpc.client;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

public class TestResult {

	private DecimalFormat df = new DecimalFormat("#.##");
	
	private LocalDateTime executionTime;
	private String testName;
	private int threadCount;
	private int iterationCountPerThread;
	private int errorCount;
	private long avgExecTime;
	private long maxExecTime;
	private long p95ExecTime;
	private long p98ExecTime;
	
	private static final String CSV_HEADER = "dateTime,testName,threadCount,iterationCount,avgExecTime,p95ExecTime,p98ExecTime,maxExecTime,errorPercentage";
	
	public TestResult(String testName, int threadCount, int iterationCount, int errors, List<Long> execTimes) {
		executionTime = LocalDateTime.now();
		this.testName = testName;
		this.threadCount = threadCount;
		this.iterationCountPerThread = iterationCount;
		this.errorCount = errors;
		calcStats(execTimes);
	}
	
	private void calcStats(List<Long> execTimes) {
		Collections.sort(execTimes);
		maxExecTime = execTimes.get(execTimes.size()-1);
		avgExecTime = (long)execTimes.stream().mapToLong(a -> a).average().getAsDouble();
		
		int p95Index = (int)Math.ceil(0.95 * execTimes.size()) - 1;
		p95ExecTime = execTimes.get(p95Index);

		int p98Index = (int)Math.ceil(0.98 * execTimes.size()) - 1;
		p98ExecTime = execTimes.get(p98Index);
	}
	
	private String toMillis(double nano) {
		return df.format( nano / 1000000);
	}
	
	public double getErrorsPercentage() {
		return (double)(errorCount * 100) / (threadCount * iterationCountPerThread);
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
		
		csv.append(toMillis(avgExecTime));
		csv.append(',');
		csv.append(toMillis(p95ExecTime));
		csv.append(',');
		csv.append(toMillis(p98ExecTime));
		csv.append(',');
		csv.append(toMillis(maxExecTime));
		csv.append(',');
		
		csv.append(getErrorsPercentage());
		
		return csv.toString();
	}
	
}
