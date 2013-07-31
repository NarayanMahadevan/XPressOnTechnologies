// RGPT PACKAGES
package com.rgpt.serverhandler;

import java.io.Serializable;

// This is the PDFPageInfoRequest to the Server. The response is the Serialized PDFPageInfo Object
public class CreateThumbviewRequest extends ServerRequest implements Serializable
{
   public static final String m_RequestType = "CreateThumbviewRequest";
   public String m_SrcDir;
   public String m_FileName;
   
   public CreateThumbviewRequest() {}
   
   public String toString()
   {
      StringBuffer mesg = new StringBuffer();
      mesg.append(" REQUEST TYPE: " + m_RequestType);
      mesg.append(" Src Dir: " + m_SrcDir);
      mesg.append(" File Name: " + m_FileName);
      return mesg.toString();
   }
}
