// RGPT PACKAGES
package com.rgpt.serverhandler;

import java.util.HashMap;
import java.io.Serializable;

// This request is used to retrieve Data from the Server like the Customer Data, 
// Product Data, etc
public class ServerDataRequest extends ServerRequest implements Serializable
{
   public static final String m_RequestType = "ServerDataRequest";
   
   public final static int GET_CUSTOMER_ACCOUNTS = 1;
   public final static int GET_PRODUCT_CATAGORIES  = 2;
   public final static int GET_CATOGORY_PRODUCTS  = 3;
   
   // The Request Data is the Input Parameters corresponding the Request Type.
   public HashMap m_RequestData = null;
   public int m_RequestTypeId;
   
   // reqTypeId indicates the requests like GET_CUSTOMER_ACCOUNTS defined above.
   public ServerDataRequest(int reqTypeId) 
   {
      m_RequestTypeId = reqTypeId;
      m_RequestData = new HashMap();
   }
   
   public String toString()
   {
      StringBuffer mesg = new StringBuffer();
      mesg.append(" REQUEST TYPE: " + m_RequestType);
      if (m_RequestTypeId == GET_CUSTOMER_ACCOUNTS) 
         mesg.append(" Request Type: Get Customer Accounts ");
      else if (m_RequestTypeId == GET_PRODUCT_CATAGORIES) 
         mesg.append(" Request Type: Get Product Categories for a Customer ");
      else if (m_RequestTypeId == GET_CATOGORY_PRODUCTS) 
         mesg.append(" Request Type: Get Products pertaining to a Category " +
                     "for a Customer ");
      mesg.append(" Request Data Parameters: " + m_RequestData.toString());
      return mesg.toString();
   }
}
