// RGPT PACKAGES
package com.rgpt.serverhandler;

import java.util.Vector;
import java.util.HashMap;
import java.io.Serializable;

import com.rgpt.templateutil.TemplateInfo;

// This is the PDFPageInfoRequest to the Server. The response is the Serialized PDFPageInfo Object
public class PersonalizedPDFFormRequest extends ServerRequest implements Serializable
{
   public static final String m_RequestType = "PersonalizedPDFFormRequest";
      
   public int m_ProjectId;
   public String m_ImageSourceDir;
   public String m_PDFSourceDir;
   public String m_PDFFileName;
   public String m_PDFFilePath;
   public Vector<Integer> m_Pages;
   public HashMap m_VDPFieldData;
   public TemplateInfo m_TemplateInfo;
   public boolean m_CreatePersonalizedPDF;
   
   public PersonalizedPDFFormRequest() {}
   
   public String toString()
   {
      StringBuffer mesg = new StringBuffer();
      mesg.append("REQUEST TYPE: " + m_RequestType);
      mesg.append(" PDF Source Dir: " + m_PDFSourceDir);
      mesg.append("\n" + m_TemplateInfo.toString());
      mesg.append("\n VDP Field DATA: " + m_VDPFieldData.toString());
      return mesg.toString();
   }
}
