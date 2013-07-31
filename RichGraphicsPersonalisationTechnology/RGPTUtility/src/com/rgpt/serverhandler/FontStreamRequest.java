// RGPT PACKAGES
package com.rgpt.serverhandler;

import java.io.Serializable;

// This is the FontStreamRequest to the Server. The response is the Serialized FontStreamHolder Object
public class FontStreamRequest extends ServerRequest implements Serializable
{
   public static final String m_RequestType = "FontStreamRequest";
   public int m_TemplateId;
   
   public FontStreamRequest() {}
   
   public String toString()
   {
      StringBuffer mesg = new StringBuffer();
      mesg.append("REQUEST TYPE: " + m_RequestType);
      mesg.append(" TEMPLATE ID: " + m_TemplateId);
      return mesg.toString();
   }
}
