// RGPT PACKAGES
package com.rgpt.util;

import java.util.HashMap;

public class RGPTThreadWorker implements Runnable
{
   int m_ThreadPriority;
   HashMap m_RequestData;
   ThreadInvokerMethod m_ThreadInvokerMethod;
   
   public RGPTThreadWorker(int priority, ThreadInvokerMethod threadInvoker, HashMap data)
   {
      m_RequestData = data;
      m_ThreadPriority = priority;
      m_ThreadInvokerMethod = threadInvoker;
   }
   
   public void startThreadInvocation() 
   {
      /* Construct an instance of Thread, passing the current class (i.e. the Runnable) as an argument. */
      Thread t = new Thread(this);
      t.setPriority(m_ThreadPriority);
      t.start(); 
   }    
   
   public void run() 
   {
      try 
      {
         System.out.println("In new thread for UploadDigitalAsset " + m_RequestData.toString());
         m_ThreadInvokerMethod.processThreadRequest(m_RequestData);
      }
      catch (Exception ex)
      {
         ex.printStackTrace();
      }
   }
}
