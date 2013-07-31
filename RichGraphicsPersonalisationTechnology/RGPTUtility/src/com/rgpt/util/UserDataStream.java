// RGPT PACKAGES
package com.rgpt.util;

import java.util.Map; 
import java.util.HashMap; 
import java.io.Serializable; 

public class UserDataStream implements Serializable
{
   public Map<String, String> m_UserData = new HashMap<String, String>();
   public String m_SerializeFilePath;
   
   public UserDataStream(String serFile)
   {
      m_SerializeFilePath = serFile;
   }
   
   public void put(String name, String value)
   {
      String oldVal = m_UserData.get(name);
      if (oldVal != null && oldVal.equals(value)) return;
      m_UserData.put(name, value);
      AppletParameters.m_RequestParamValues.put(name, value);
      try 
      {
         if (m_SerializeFilePath == null || m_SerializeFilePath.trim().isEmpty()) 
            m_SerializeFilePath = AppletParameters.getVal("DataFilePath");
         RGPTUtil.serializeObject(m_SerializeFilePath, this);
      }
      catch (Exception ex) 
      {
         ex.printStackTrace();
      }
   }
}