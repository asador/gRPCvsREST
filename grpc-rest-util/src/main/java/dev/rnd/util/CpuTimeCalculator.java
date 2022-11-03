package dev.rnd.util;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.Map;

public class CpuTimeCalculator {

	private class Times {
		public long id;
		public long startCpuTime;
		public long startUserTime;
		public long endCpuTime;
		public long endUserTime;
	}

	private long interval;
	private  long threadId;
	private  Map<Long, Times> history = new HashMap<>();
	private boolean running = false;

/** Create a polling thread to track times. */
	public CpuTimeCalculator(long interval) {
		this.interval = interval;
	}

  public void start() {
  	if (running)
  		return;
  	
  	running = true;
  	history.clear();
  	
		Thread t = new Thread ( () -> {
			while (running) {
				update();
				try {
					Thread.sleep(interval);
				}
				catch (InterruptedException e) {
					break;
				}
			}
		});
		threadId = t.getId();
  	t.start();
  }
  
  public void stop() {
  	running = false;
  }
  
	/** Update the hash table of thread times. */
	private void update() {
		ThreadMXBean bean = ManagementFactory.getThreadMXBean();

		for (long id : bean.getAllThreadIds()) {
			if (id == threadId)
				continue; // Exclude polling thread
			final long c = bean.getThreadCpuTime(id);
			final long u = bean.getThreadUserTime(id);
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
	public long getTotalCpuTime() {
		long cpuTimeMillis = 0L;
		for (Times times : history.values())
			cpuTimeMillis += times.endCpuTime - times.startCpuTime;
		
		return cpuTimeMillis / 1000000;
	}

	public static int getThreadCount() {
		return ManagementFactory.getThreadMXBean().getThreadCount();
	}
}
