// RGPT PACKAGES
package com.rgpt.serverhandler;

import java.util.Vector;
import java.io.Serializable;

// This is the FontStreamRequest to the Server. The response is the Serialized FontStreamHolder Object
public class PopulateVDPFieldRequest extends ServerRequest implements Serializable
{
   public static final String m_RequestType = "PopulateVDPFieldRequest";
   public int m_TemplateId = -1;
   public int m_CustomerId = -1;
   public String m_DataSourceName;
   public Vector<String> m_VDPTextFieldsToPopulate;
   public Vector<String> m_VDPImageFieldsToPopulate;
   
   public PopulateVDPFieldRequest(int tempId, int custId, String dataSrcName, 
                                  Vector<String> vdpTxtFlds2Populate, 
                                  Vector<String> vdpImgFlds2Populate) 
   {
      m_TemplateId = tempId;
      m_CustomerId = custId; 
      m_DataSourceName = dataSrcName;
      m_VDPTextFieldsToPopulate = vdpTxtFlds2Populate;
      m_VDPImageFieldsToPopulate = vdpImgFlds2Populate;
   }
   
   public String toString()
   {
      StringBuffer mesg = new StringBuffer();
      mesg.append("REQUEST TYPE: " + m_RequestType);
      mesg.append(" Template ID: " + m_TemplateId);
      mesg.append(" Customer ID: " + m_CustomerId);
      mesg.append(" Data Souce: " + m_DataSourceName);
      if (m_VDPTextFieldsToPopulate != null) 
         mesg.append("\nVDP Text Fields: " + m_VDPTextFieldsToPopulate.toString());
      if (m_VDPImageFieldsToPopulate != null) 
         mesg.append("\nVDP Image Fields: " + m_VDPImageFieldsToPopulate.toString());
      return mesg.toString();
   }
}
