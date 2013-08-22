// RGPT PACKAGES
package com.rgpt.util;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Vector;

public class RGPTMemoryManager implements ThreadInvokerMethod {
	private int m_MemoryCheckInterval;
	public Vector<RecycleMemoryData> m_MemoryData;

	private static RGPTMemoryManager m_RGPTMemoryManager;

	private RGPTMemoryManager() {
		m_MemoryData = new Vector<RecycleMemoryData>();
		m_MemoryCheckInterval = RGPTParams
				.getIntVal("MemoryCheckInterval");
		RGPTThreadWorker threadWorker = null;
		HashMap requestData = new HashMap();
		threadWorker = new RGPTThreadWorker(Thread.MIN_PRIORITY, this,
				requestData);
		threadWorker.startThreadInvocation();
	}

	public static RGPTMemoryManager getInstance() {
		if (m_RGPTMemoryManager != null)
			return m_RGPTMemoryManager;
		m_RGPTMemoryManager = new RGPTMemoryManager();
		return m_RGPTMemoryManager;
	}

	public RecycleMemoryData handleMemory(int duration,
			RGPTMemoryHandle memoryHandler, HashMap memoryData) {
		RecycleMemoryData recycleMemData = new RecycleMemoryData(duration,
				memoryHandler, memoryData);
		m_MemoryData.addElement(recycleMemData);
		return recycleMemData;
	}

	public void processThreadRequest(HashMap requestData) throws Exception {
		RecycleMemoryData recycleMemData = null;
		while (true) {
			long currTime = Calendar.getInstance().getTimeInMillis();
			for (int i = 0; i < m_MemoryData.size(); i++) {
				recycleMemData = m_MemoryData.elementAt(i);
				if (currTime - recycleMemData.lastAccess > (long) recycleMemData.setDuration) {
					boolean res = recycleMemData.memoryHandler
							.handleMamory(recycleMemData.memoryData);
					if (res) {
						m_MemoryData.removeElementAt(i);
						recycleMemData = null;
					}
				}
			}
			try {
				Thread.sleep(m_MemoryCheckInterval * 1000);
			} catch (Exception e) {
			}
		}
	}

}
