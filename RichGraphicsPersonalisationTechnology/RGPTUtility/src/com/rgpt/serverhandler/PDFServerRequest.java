// RGPT PACKAGES
package com.rgpt.serverhandler;

import java.util.HashMap;
import java.io.Serializable;

// This request is used to retrieve Data from the Server like the Customer Data, 
// Product Data, etc
public class PDFServerRequest extends ServerRequest implements Serializable
{
   public static final String m_RequestType = "PDFServerRequest";
   
   public final static int UPLOAD_FILE_PATH = 1;
   
   // The Request Data is the Input Parameters corresponding the Request Type.
   public HashMap m_RequestData = null;
   public int m_RequestTypeId;
   
   // reqTypeId indicates the requests like GET_CUSTOMER_ACCOUNTS defined above.
   public PDFServerRequest(int reqTypeId) 
   {
      m_RequestTypeId = reqTypeId;
      m_RequestData = new HashMap();
   }
   
   public String toString()
   {
      StringBuffer mesg = new StringBuffer();
      mesg.append(" REQUEST TYPE: " + m_RequestType);
      if (m_RequestTypeId == UPLOAD_FILE_PATH) 
         mesg.append(" Request Type: Upload File Path ");
      mesg.append(" Request Data Parameters: " + m_RequestData.toString());
      return mesg.toString();
   }
}
