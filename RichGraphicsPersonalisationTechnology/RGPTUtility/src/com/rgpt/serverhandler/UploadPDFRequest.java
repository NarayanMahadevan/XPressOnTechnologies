// RGPT PACKAGES
package com.rgpt.serverhandler;

import java.io.Serializable;

// This is the PDFPageInfoRequest to the Server. The response is the Serialized PDFPageInfo Object
public class UploadPDFRequest extends ServerRequest implements Serializable
{
   public static final String m_RequestType = "UploadPDFRequest";
   public String m_SrcDir;
   public String m_FileName;
   public boolean m_DrawFirstPage = false;
   public boolean m_DrawPDFPage = true;
   public boolean m_Save2File = true;

   // This specifies the Pages requested from the Server. The PDF Page is always
   // returned as Image
   public java.util.Vector m_PDFPages = null;
   public int m_AssetId = -1;
   
   public UploadPDFRequest() {}
   
   public String toString()
   {
      StringBuffer mesg = new StringBuffer();
      mesg.append(" REQUEST TYPE: " + m_RequestType);
      mesg.append(" Src Dir: " + m_SrcDir);
      mesg.append(" File Name: " + m_FileName);
      return mesg.toString();
   }
}
