// RGPT PACKAGES
package com.rgpt.util;

public class ProgressInfo 
{
   public int m_ProgressValue;
	public boolean m_IsFinished;
	public double m_TranferredBytes;
	public double m_TotalNumOfBytes;
	public int m_TranferRate;
	public int m_TimeElapsed;
	public int m_TimeLeft;
	
   public ProgressInfo() {
		this.m_ProgressValue = 0;
		this.m_IsFinished = false;
		this.m_TranferredBytes = 0;
		this.m_TotalNumOfBytes = 0;
		this.m_TranferRate = 0;
		this.m_TimeElapsed = 0;
		this.m_TimeLeft = 0;
	}
}
