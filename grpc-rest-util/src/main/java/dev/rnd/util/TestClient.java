package dev.rnd.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

abstract public class TestClient {

  private static final Logger logger = Logger.getLogger(TestClient.class.getName());
  
  private List<TestResult> testResults = new ArrayList<>();  
  private CpuUsageCalculator cpuUsageCalculator;
  private ExecutorService executor;
  
	abstract protected String getProperty(String key);
	abstract protected int getThreadCount();
	abstract protected int getCpuTimeSampleInterval();
	abstract protected String getOutputFilename();
	abstract protected boolean isAppendToOutputFile();
	
	protected void preTestRun() {
		executor = Executors.newFixedThreadPool(getThreadCount());
		cpuUsageCalculator = new CpuUsageCalculator(getCpuTimeSampleInterval());
	}
	
	private boolean isTestEnabled(String testName) {
		String testKey = "test." + testName;
		return getProperty(testKey) == null || Boolean.valueOf(getProperty(testKey));
	}
	
	protected void testRemoteMethod(String testName, int nThreads, int iterationCount, Runnable test) {
		if (!isTestEnabled(testName)) {
			logger.log(Level.INFO, "Skipped test {0}", testName);
			return;
		}
					
		CountDownLatch latch = new CountDownLatch(nThreads);
		List<List<Long>> execTimesList = new ArrayList<>();		
		AtomicInteger errorCount = new AtomicInteger(0);
		
		startServerCpuUsageMonitor();
		cpuUsageCalculator.start();
		
		for (int i=0; i< nThreads; i++) {
			List<Long> execTimes = new ArrayList<>();
			execTimesList.add(execTimes);
			
			executor.execute(() -> {
				for (int j=0; j < iterationCount; j++) {
					long t1 = System.nanoTime();
					try {
						test.run();
					}
					catch (Exception e) {
						errorCount.incrementAndGet();
						e.printStackTrace();
					}
					long t2 = System.nanoTime();
					execTimes.add(t2-t1);
				}
				
				latch.countDown();
			});
		}
		
		try {
//			latch.await(testTimeoutSeconds, TimeUnit.SECONDS);
			latch.await();
			
			CpuUsage clientCpuUsage = cpuUsageCalculator.stopAndGetCpuUsage();
			CpuUsage serverCpuUsage = stopAndGetServerCpuUsage();
			
			// merge all exec times form all threads
			List<Long> allExecTimes = new ArrayList<>();			
			for (int i=0; i<nThreads; i++)
				allExecTimes.addAll(execTimesList.get(i));
			
			TestResult testResult = new TestResult(testName, nThreads, iterationCount, errorCount.get(), clientCpuUsage.getDurationMillis(), 
					allExecTimes, serverCpuUsage, clientCpuUsage);
			testResults.add(testResult);

			logger.log(Level.INFO, "Completed {0} - threads={1}, iterationCount={2}", new Object[] {testName, nThreads, iterationCount});
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	abstract protected void startServerCpuUsageMonitor();	
	abstract protected CpuUsage stopAndGetServerCpuUsage();
	
	private void saveTestResults() {
		if (testResults.size() == 0)
			return ;
		
		File outFile = new File(getOutputFilename());
		boolean addCSVHeader = !outFile.exists() || !isAppendToOutputFile();
		
		try ( FileWriter fw = new FileWriter(outFile, isAppendToOutputFile());
					BufferedWriter bw = new BufferedWriter(fw);
					PrintWriter pw = new PrintWriter(bw);
				) {
			
			if (addCSVHeader)
				pw.println(TestResult.getCSVHeader());
			
			for (TestResult res : testResults)
				pw.println(res.getResultAsCSV());
			
			logger.log(Level.INFO, "Test results saved in {0}", outFile.getAbsolutePath());
			
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	protected void shutdown() throws InterruptedException {
		executor.shutdown();
		executor.awaitTermination(30, TimeUnit.SECONDS);
		
		saveTestResults();
	}	

}
