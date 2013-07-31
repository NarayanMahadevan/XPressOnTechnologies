// RGPT PACKAGES
package com.rgpt.serverhandler;

import java.io.Serializable;

// This is to set the Approval Flag for generated PDF
public class DataMappingRequest extends ServerRequest implements Serializable
{
   public static final String m_RequestType = "DataMappingRequest";
   
   // User Credentials
   public String m_DataSourceName;
   
   public DataMappingRequest() {}
   
   public String toString()
   {
      StringBuffer mesg = new StringBuffer();
      mesg.append(" REQUEST TYPE: " + m_RequestType);
      mesg.append(" Data Source Name: " + m_DataSourceName);
      return mesg.toString();
   }
}
