// RGPT PACKAGES
package com.rgpt.serverhandler;

import java.io.Serializable;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.lang.RuntimeException;

// This is to Save VDP Data to the Server
public class SaveVDPDataRequest extends ServerRequest implements Serializable
{
   public static final String m_RequestType = "SaveVDPDataRequest";
   
   // Template Data 
   public int m_TemplateId;
   
   // Customer Data
   public int m_CustomerId;
   //public String m_SessionId;
   public int m_CustomerBasketId;
   public boolean m_IsWorkInProgress = false;
   
   // VDP Data Data
   public java.util.HashMap m_UserPageData;
   
   public SaveVDPDataRequest() {}
   
   public String toString()
   {
      StringBuffer mesg = new StringBuffer();
      mesg.append(" REQUEST TYPE: " + m_RequestType);
      mesg.append(" TEMPLATE ID: " + m_TemplateId);
      mesg.append(" Customer Id: " + m_CustomerId);
      mesg.append(" Customer Basket Id: " + m_CustomerBasketId);
      mesg.append("\nUser VDP Data: " + m_UserPageData.toString());
      return mesg.toString();
   }
}
