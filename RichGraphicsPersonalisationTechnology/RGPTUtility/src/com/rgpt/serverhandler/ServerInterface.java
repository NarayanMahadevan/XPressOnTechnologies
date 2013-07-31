// RGPT PACKAGES
package com.rgpt.serverhandler;

// This files are added to support serialization
import java.io.Serializable;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.lang.RuntimeException;

public class ServerInterface implements Serializable
{
   public static final long serialVersionUID = 6873131385651359146L;
   
   public int m_ServiceIdentifier;
   public int m_ServerRequestIndicater;
   public String m_ServerName;
   public ServerRequest m_ServerRequest;
   
   public static final int PDF_PAGE_INFO_REQUEST = 1;
   public static final int PDF_PAGE_IMAGE_REQUEST = 2;
   public static final int SAVE_IMAGE_REQUEST = 3;
   public static final int SAVE_VDP_DATA_REQUEST = 4;
   public static final int SAVE_TEMPLATE_REQUEST = 5;
   public static final int PERSONALIZED_PDF_REQUEST = 6;
   public static final int UPLOAD_PDF_REQUEST = 7;
   public static final int CUSTOMER_ASSET_REQUEST = 8;
   public static final int GET_SAVED_VDP_DATA_REQUEST = 9;
   public static final int APPROVE_PDF_REQUEST = 10;
   public static final int UPLOAD_ASSET_REQUEST = 11;
   public static final int BATCH_APPROVAL_REQUEST = 12;
   public static final int FONT_STREAM_REQUEST = 13;
   public static final int POPULATE_VDP_FIELDS_REQUEST = 14;
   public static final int DATA_MAPPING_REQUEST = 15;
   public static final int GET_THEME_REQUEST = 16;
   public static final int SERVER_DATA_REQUEST = 17;
   public static final int UPLOAD_TEMPLATE_REQUEST = 18;
   public static final int PDF_SERVER_REQUEST = 19;
   public static final int PERSONALIZED_PDF_FORM_REQUEST = 20;
   
   public ServerInterface(int pspId, int serverRequestIndicator) 
   {
      m_ServiceIdentifier = pspId;
      m_ServerRequestIndicater = serverRequestIndicator;
   }
   
   public ServerInterface(int pspId, String serverName, int serverRequestIndicator) 
   {
      m_ServerName = serverName;
      m_ServiceIdentifier = pspId;
      m_ServerRequestIndicater = serverRequestIndicator;
   }
   
   //Serialization
   public void save(ObjectOutputStream objstream) throws IOException 
   {
      objstream.writeObject(this);
   }
   
   //De-Serialization
   public static ServerInterface load(ObjectInputStream objstream ) throws Exception 
   {
      ServerInterface reqObj = (ServerInterface) objstream.readObject();
      return reqObj;
   }
   
   public String toString()
   {
      StringBuffer mesg = new StringBuffer();
      mesg.append("PSP ID: " + m_ServiceIdentifier + " ");
      mesg.append(" Server Name: " + m_ServerName + " ");
      mesg.append(" Server Req Id: " + m_ServerRequestIndicater + " ");
      mesg.append(m_ServerRequest.toString());
      return mesg.toString();
   }
}
