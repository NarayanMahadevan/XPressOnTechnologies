// RGPT PACKAGES
package com.rgpt.serverhandler;

import java.util.HashMap;
import java.io.Serializable;

// This request is used to retrieve Data from the Server like the Customer Data, 
// Product Data, etc
public class UploadTemplateRequest extends ServerRequest implements Serializable
{
   public static final String m_RequestType = "UploadTemplateRequest";
   
   public static String PDF_ASSET_TYPE = "PDF";
   public static String IMAGE_ASSET_TYPE = "IMAGE";
   
   // This id is provided by the Server for Tracking 
   public int m_FileUploaderId;
   
   // Either PDF or IMAGE
   public String m_AssetType;
   
   // Parameters Needed for Upload Templates
   public int m_CustomerAccountId;
   public int m_ProductId = -1;
   public String m_TemplateName;
   public String m_TemplateDesc;
   public String m_TemplateFileName;
   public boolean m_IsActivated;
   public boolean m_AllowBatchMode;
   
   
   // reqTypeId indicates the requests like GET_CUSTOMER_ACCOUNTS defined above.
   public UploadTemplateRequest(String assetType) 
   {
      m_AssetType = assetType;
   }
   
   public String toString()
   {
      StringBuffer mesg = new StringBuffer();
      mesg.append(" REQUEST TYPE: " + m_RequestType);
      mesg.append(" ASSET TYPE: " + m_AssetType);
      mesg.append(" CustomerAccountId: " + m_CustomerAccountId);
      mesg.append(" ProductId: " + m_ProductId);
      mesg.append("\nTemplateName: " + m_TemplateName);
      mesg.append(" TemplateFileName: " + m_TemplateFileName);
      return mesg.toString();
   }
}
