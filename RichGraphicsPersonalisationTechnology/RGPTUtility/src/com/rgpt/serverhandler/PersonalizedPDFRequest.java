// RGPT PACKAGES
package com.rgpt.serverhandler;

import java.util.HashMap;
import java.io.Serializable;

import com.rgpt.templateutil.TemplateInfo;

// This is the PDFPageInfoRequest to the Server. The response is the Serialized PDFPageInfo Object
public class PersonalizedPDFRequest extends ServerRequest implements Serializable
{
   public static final String m_RequestType = "PersonalizedPDFRequest";
   
   // The Values are defined in PDF View Interface
   public int m_PDFSaveMode;
   
   public String m_PDFFileName;
   public String m_PDFSourceDir;
   public String m_PDFFilePath;
   public HashMap m_UserPageData;
   public TemplateInfo m_TemplateInfo;
   public int m_CustomerBasketId;
   
   public PersonalizedPDFRequest() {}
   
   public String toString()
   {
      StringBuffer mesg = new StringBuffer();
      mesg.append("REQUEST TYPE: " + m_RequestType);
      mesg.append(" PDF Source Dir: " + m_PDFSourceDir);
      if (m_PDFSaveMode == PDFViewInterface.SAVE_PDF_IN_FILE)
         mesg.append("SAVE PDF TO FILE : " + m_PDFFilePath);
      else mesg.append("SAVE PDF IN MEMORY");
      mesg.append("\n" + m_TemplateInfo.toString());
      mesg.append("\n USER DATA: " + m_UserPageData.toString());
      return mesg.toString();
   }
}
