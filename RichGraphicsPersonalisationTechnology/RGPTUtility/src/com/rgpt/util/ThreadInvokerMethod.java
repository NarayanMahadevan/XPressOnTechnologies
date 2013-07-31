// RGPT PACKAGES
package com.rgpt.util;

import java.util.HashMap;

// This interface is used to start processing on new thread.

public interface ThreadInvokerMethod
{
   // This function is called by the new thread to process request
   public void processThreadRequest(HashMap requestData) throws Exception;
}