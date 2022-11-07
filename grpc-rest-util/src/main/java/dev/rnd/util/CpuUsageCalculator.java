package dev.rnd.util;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.Map;

public class CpuUsageCalculator {

	private class Times {
		public long id;
		public long startCpuTime;
		public long startUserTime;
		public long endCpuTime;
		public long endUserTime;
	}

	private Map<Long, Times> history = new HashMap<>();
	private long interval;
	private Thread timerThread;
	private long startTime;
	
	private int numProcessors;

	public CpuUsageCalculator(long interval) {
		this.interval = interval;
		numProcessors = Runtime.getRuntime().availableProcessors();
		
//		System.out.println("Number of Cores: " + numProcessors);
	}

	public void start() {
		if (timerThread != null && timerThread.getState() != Thread.State.TERMINATED)
			return;

		startTime = System.currentTimeMillis();
		history.clear();

		timerThread = new Thread(() -> {
			while (!Thread.interrupted()) {
				update();
				try {
					Thread.sleep(interval);
				}
				catch (InterruptedException e) {
					break;
				}
			}
			update();
		});

		timerThread.start();
	}

	public void stop() {
		timerThread.interrupt();
		while (timerThread.getState() != Thread.State.TERMINATED)
			;
	}

	public CpuUsage stopAndGetCpuUsage() {
		stop();
		
		long duration = System.currentTimeMillis() - startTime;
		long totalCpuTime = getTotalCpuTime();
		double cpuUtilization = (double)totalCpuTime / (duration * numProcessors);
		
		return new CpuUsage(duration, totalCpuTime, cpuUtilization);
	}

	/** Update the hash table of thread times. */
	private void update() {
		ThreadMXBean bean = ManagementFactory.getThreadMXBean();

		for (long id : bean.getAllThreadIds()) {
			if (id == Thread.currentThread().getId())
				continue; // Exclude polling thread

			long c = bean.getThreadCpuTime(id);
			long u = bean.getThreadUserTime(id);
			if (c == -1 || u == -1)
				continue; // Thread died

			Times times = history.get(id);
			if (times == null) {
				times = new Times();
				times.id = id;
				times.startCpuTime = c;
				times.startUserTime = u;
				times.endCpuTime = c;
				times.endUserTime = u;
				history.put(id, times);
			} else {
				times.endCpuTime = c;
				times.endUserTime = u;
			}
		}
	}
	
	/** Get total CPU time so far in milliseconds */
	private long getTotalCpuTime() {
		long cpuTimeMillis = 0L;
		for (Times times : history.values())
			cpuTimeMillis += times.endCpuTime - times.startCpuTime;

		return cpuTimeMillis / 1000000;
	}

	public static int getThreadCount() {
		return ManagementFactory.getThreadMXBean().getThreadCount();
	}
}
