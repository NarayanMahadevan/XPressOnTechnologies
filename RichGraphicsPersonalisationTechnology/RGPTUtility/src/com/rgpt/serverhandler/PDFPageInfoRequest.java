// RGPT PACKAGES
package com.rgpt.serverhandler;

import java.io.Serializable;

import com.rgpt.util.AppletParameters;

// This is the PDFPageInfoRequest to the Server. The response is the Serialized PDFPageInfo Object
public class PDFPageInfoRequest extends ServerRequest implements Serializable
{
   public static final String m_RequestType = "PDFPageInfoRequest";
   public int m_PageNum;
   public String m_AppletRequest;
   public int m_TemplateId;
   public int m_DigitalAssetId;
   public String m_SrcDir;
   public String m_SerializedFileName;
   
   public PDFPageInfoRequest() {}
   
   public void setEditPDFRequest(int tempId, int pgNum, String srcDir, 
                                  String serFileName)
   {
      m_PageNum = pgNum;
      m_SrcDir = srcDir;
      m_TemplateId = tempId;
      m_SerializedFileName = serFileName;
      m_AppletRequest = AppletParameters.EDIT_PDF_TEMPLATE_REQUEST;
   }
   
   public void setViewPDFRequest(int assetId, int pgNum, String srcDir, 
                                  String serFileName) 
   {
      m_AppletRequest = AppletParameters.VIEW_PDF_REQUEST;
      this.setViewAndApprovePDFRequest(assetId, pgNum, srcDir, serFileName);
   }
   public void setApprovePDFRequest(int assetId, int pgNum, String srcDir, 
                                     String serFileName) 
   {
      m_AppletRequest = AppletParameters.APPROVE_PDF_REQUEST;
      this.setViewAndApprovePDFRequest(assetId, pgNum, srcDir, serFileName);
   }
   private void setViewAndApprovePDFRequest(int assetId, int pgNum, String srcDir, 
                                            String serFileName) 
   {
      m_PageNum = pgNum;
      m_SrcDir = srcDir;
      m_DigitalAssetId = assetId;
      m_SerializedFileName = serFileName;
   }
   
   public String toString()
   {
      StringBuffer mesg = new StringBuffer();
      mesg.append("REQUEST TYPE: " + m_RequestType);
      mesg.append(" APPLET REQUEST: " + m_AppletRequest);
      mesg.append(" TEMPLATE ID: " + m_TemplateId);
      mesg.append(" ASSET ID: " + m_DigitalAssetId);
      mesg.append(" PAGE NUM: " + m_PageNum);
      mesg.append(" SRC DIR: " + m_SrcDir);
      mesg.append(" SERIALIZED FILE: " + m_SerializedFileName);
      return mesg.toString();
   }
}
