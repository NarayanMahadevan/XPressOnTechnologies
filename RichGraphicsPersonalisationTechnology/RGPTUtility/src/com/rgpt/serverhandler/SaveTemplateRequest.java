// RGPT PACKAGES
package com.rgpt.serverhandler;

import java.io.Serializable;

import com.rgpt.templateutil.TemplateInfo;

// This is the PDFPageInfoRequest to the Server. The response is the Serialized PDFPageInfo Object
public class SaveTemplateRequest extends ServerRequest implements Serializable
{
   public static final String m_RequestType = "SaveTemplateRequest";
   public TemplateInfo m_TemplateInfo;
   
   public SaveTemplateRequest() {}
   
   public String toString()
   {
      StringBuffer mesg = new StringBuffer();
      mesg.append(" REQUEST TYPE: " + m_RequestType);
      mesg.append("\n" + m_TemplateInfo.toString());
      return mesg.toString();
   }
}
