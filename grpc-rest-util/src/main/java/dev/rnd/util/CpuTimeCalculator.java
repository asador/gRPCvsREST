package dev.rnd.util;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

public class CpuTimeCalculator {

	public static long getTotalCpuTime() {
		ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
		if (!threadBean.isThreadCpuTimeSupported())
			return -1;
		
		long cpuTimeMillis = 0;
		for (long threadId : threadBean.getAllThreadIds()) {
			cpuTimeMillis += threadBean.getThreadCpuTime(threadId) / 1000000;
		}
		
		// result in millisecond
		return cpuTimeMillis;
	}
	
	public static int getThreadCount() {
		return ManagementFactory.getThreadMXBean().getThreadCount();
	}
}
