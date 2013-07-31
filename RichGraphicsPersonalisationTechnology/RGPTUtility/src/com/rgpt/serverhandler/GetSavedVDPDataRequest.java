// RGPT PACKAGES
package com.rgpt.serverhandler;

import java.io.Serializable;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.lang.RuntimeException;

// This is to Save VDP Data to the Server
public class GetSavedVDPDataRequest extends ServerRequest implements Serializable
{
   public static final String m_RequestType = "GetSavedVDPDataRequest";
   
   // Template Data 
   public int m_TemplateId;
   
   // Customer Data
   public int m_CustomerId;
   public int m_SessionAssetId;
   public int m_DigitalAssetId;
   
   
   public GetSavedVDPDataRequest() {}
   
   public String toString()
   {
      StringBuffer mesg = new StringBuffer();
      mesg.append(" REQUEST TYPE: " + m_RequestType);
      mesg.append(" TEMPLATE ID: " + m_TemplateId);
      mesg.append(" Customer Id: " + m_CustomerId);
      mesg.append(" Session Asset Id: " + m_SessionAssetId);
      mesg.append(" Digital Asset Id: " + m_DigitalAssetId);
      return mesg.toString();
   }
}
