// RGPT PACKAGES
package com.rgpt.util;

import java.util.HashMap;

// This interface is used to handle memory.

public interface RGPTMemoryHandle
{
   // This function is called by the Memory Manager Thread to handleMemory
   public boolean handleMamory(HashMap memoryData);
}