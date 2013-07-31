// RGPT PACKAGES
package com.rgpt.serverhandler;

import java.io.Serializable;

// This is to request PDF Page Image from the Server
public class PDFPageImageRequest extends ServerRequest implements Serializable
{
   public static final String m_RequestType = "PDFPageImageRequest";
   
   public int m_PageNum;
   // Quality Mode can REGULAR_QUALITY_PDF, HIGH_QUALITY_PDF, or BEST_QUALITY_PDF
   // As defined in PDFViewInterface
   public int m_QualityMode;
   public int m_TemplateId;
   
   public PDFPageImageRequest() {}
   
   public String toString()
   {
      StringBuffer mesg = new StringBuffer();
      mesg.append(" REQUEST TYPE: " + m_RequestType);
      mesg.append(" TEMPLATE ID: " + m_TemplateId);
      mesg.append(" PAGE NUM: " + m_PageNum);
      mesg.append(" Image Mode: " + m_QualityMode);
      return mesg.toString();
   }
}
