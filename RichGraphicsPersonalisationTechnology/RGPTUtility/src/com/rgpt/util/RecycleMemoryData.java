package com.rgpt.util;

import java.util.Calendar;
import java.util.HashMap;

public class RecycleMemoryData {
	public long startTime;
	public long lastAccess;
	public int setDuration;
	public HashMap memoryData;
	public RGPTMemoryHandle memoryHandler;

	public RecycleMemoryData(int duration, RGPTMemoryHandle memHdlr,
			HashMap memData) {
		startTime = Calendar.getInstance().getTimeInMillis();
		lastAccess = startTime;
		setDuration = duration;
		memoryHandler = memHdlr;
		memoryData = memData;
	}

}
