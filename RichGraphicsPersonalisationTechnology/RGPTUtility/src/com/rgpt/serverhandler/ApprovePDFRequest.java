// RGPT PACKAGES
package com.rgpt.serverhandler;

import java.io.Serializable;

// This is to set the Approval Flag for generated PDF
public class ApprovePDFRequest extends ServerRequest implements Serializable
{
   public static final String m_RequestType = "ApprovePDFRequest";
   public final static int PDF_APPROVED = 1;
   public final static int PDF_DISAPPROVED = 0;
   public final static int PDF_NO_ACTION_TAKEN = 2;
   
   // Customer Data
   public int m_CustomerId;
   //public String m_SessionId;
   public int m_CustomerBasketId;
   public int m_PDFApprovalFlag;
   
   public ApprovePDFRequest() {}
   
   public String toString()
   {
      StringBuffer mesg = new StringBuffer();
      mesg.append(" REQUEST TYPE: " + m_RequestType);
      mesg.append(" Customer Id: " + m_CustomerId);
      mesg.append(" Customer Basket Id: " + m_CustomerBasketId);
      mesg.append(" PDF Approval Flag: " + m_PDFApprovalFlag);
      return mesg.toString();
   }
}
