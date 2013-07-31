// RGPT PACKAGES
package com.rgpt.util;

import java.io.Serializable;
import java.util.HashMap;

import com.rgpt.serverhandler.ServerRequest;

// This request is used to notify the server of the PDF Pages approved by the 
// User. This are mainly for the PDF generated from the uploaded Batch request.
public class BatchApprovalRequest extends ServerRequest implements Serializable
{
   public static final String m_RequestType = "BatchApprovalRequest";
   
   public static String PDF_ASSET_TYPE = "PDF";
   public static String IMAGE_ASSET_TYPE = "IMAGE";
   
   // This specifies the Pages requested from the Server. The PDF Page is always
   // returned as Image
   public HashMap m_PDFPageApprovalStatus;
   
   // Default Asset Id is set to -1
   public int m_AssetId = -1;
   
   // Either PDF or IMAGE
   public String m_AssetType;
   
   // Batch Request Parameters 
   public int m_BatchId;
   public int m_TemplateId;
   public int m_TemplatePageCount;
   public int m_TotalPageCount;
   
   // This values are predominantly filled by the Server before final processing
   public String m_SrcDir;
   public String m_FileName;
   public String m_CSVSrcDir;
   public String m_CSVFileName;
   public String m_SerializedPDFFile;
   
   public BatchApprovalRequest() 
   {
      m_AssetType = PDF_ASSET_TYPE;
   }
   
   public String toString()
   {
      StringBuffer mesg = new StringBuffer();
      mesg.append(" REQUEST TYPE: " + m_RequestType);
      mesg.append(" AssetId: " + m_AssetId);
      mesg.append(" Asset Type: " + m_AssetType);
      mesg.append(" Batch Id: " + m_BatchId);
      mesg.append(" Template Id: " + m_TemplateId);
      mesg.append(" Template Page Count: " + m_TemplatePageCount);
      if (m_PDFPageApprovalStatus != null)
         mesg.append("\nPDF Page Approval Status: " + 
                     m_PDFPageApprovalStatus.toString());
      return mesg.toString();
   }
}
