// RGPT PACKAGES
package com.rgpt.serverhandler;

import java.io.Serializable;

// This is the PDFPageInfoRequest to the Server. The response is the Serialized PDFPageInfo Object
public class UploadAssetRequest extends ServerRequest implements Serializable
{
   public static final int UPLOAD_BATCH_FILE = 1;
   public static final int UPLOAD_PSP_ASSET = 2;
   public static final int UPLOAD_DIGITAL_ASSET = 3;
   public static final int UPLOAD_UPDATED_ASSET = 4;
   public static final String m_RequestType = "UploadAssetRequest";

   // Asset Types Supported  
   public static int PDF_TEMPLATE_ASSET = 0;
   public static int FAST_SHOPPING_ASSET = 1;

   // This id is provided by the Server for Tracking 
   public int m_FileUploaderId;
   
   public int m_UploadMode;
   public int m_CustomerId;
   public int m_TemplateId;
   public int m_DigitalAssetId = -1;
   public int m_CustomerAccountId = -1;
   public int m_NumberOfUploadedFiles;
   
   // This indicates the Server that will execute the request in the backend.
   public String m_ServerName;
   
   public String m_FileType;
   public String m_SessionId;

   public int m_AssetType;

   // Order and Job id Field needed when updated asset for job is uploaded
   public int m_OrderId = -1;
   public int m_OrderJobId = -1;
   
   // This variables are populated if the Netwrork File Path is uploaded to Server
   public boolean m_IsUploadFilePath = false;
   public String m_UploadFileName = "";
   public java.util.Vector<String> m_UploadFilePaths;
   
   public UploadAssetRequest() {}
   
   public String toString()
   {
      StringBuffer mesg = new StringBuffer();
      mesg.append(" REQUEST TYPE: " + m_RequestType);
      mesg.append(" Upload Mode: " + m_UploadMode);
      mesg.append(" Cust Acnt Id: " + m_CustomerAccountId);
      mesg.append(" Cust Id: " + m_CustomerId);
      mesg.append(" Num of Files Uploaded: " + m_NumberOfUploadedFiles);
      mesg.append(" Upload File Path: " + m_IsUploadFilePath);
      if (m_IsUploadFilePath) 
         mesg.append(" Local File Path: " + m_UploadFilePaths);
      mesg.append(" File Type: " + m_FileType);
      if (m_UploadMode ==  UPLOAD_PSP_ASSET) {
         mesg.append(" Upload: PDF Template File");
         mesg.append(" Session " + m_SessionId);
      }
      else if (m_UploadMode ==  UPLOAD_DIGITAL_ASSET)
         mesg.append(" Upload: All Digital Files");
      else if (m_UploadMode ==  UPLOAD_BATCH_FILE)
         mesg.append(" Upload: Batch File");
      return mesg.toString();
   }
}
