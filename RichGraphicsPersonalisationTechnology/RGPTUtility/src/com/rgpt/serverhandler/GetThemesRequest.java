// RGPT PACKAGES
package com.rgpt.serverhandler;

import java.util.Vector;
import java.io.Serializable;

// This is to set the Approval Flag for generated PDF
public class GetThemesRequest extends ServerRequest implements Serializable
{
   public static final String m_RequestType = "GetThemesRequest";
   
   public static int GET_THEME_NAMES = 1;
   public static int GET_THEME_DATA  = 2;
   public static int GET_THEME_IMAGES  = 3;
   
   // This request can be used to retrieve all the Themes, if no theme id is provided.
   // If theme id sis provided this request retrieves the Themes pertaing to the Theme Id.
   public Vector<Integer> m_ThemeIds = null;
   public int m_ThemeRequestType;
   
   public GetThemesRequest() {}
   
   public String toString()
   {
      StringBuffer mesg = new StringBuffer();
      mesg.append(" REQUEST TYPE: " + m_RequestType);
      if (m_ThemeRequestType == GET_THEME_NAMES) 
         mesg.append(" Request Type: Get Theme Names ");
      else if (m_ThemeRequestType == GET_THEME_DATA) 
         mesg.append(" Request Type: Get Complete Theme Info ");
      else mesg.append(" Request Type: Get Theme Images ");
      if (m_ThemeIds == null) mesg.append(" Retrieve All Themes ");
      else mesg.append(" Theme Requested: " + m_ThemeIds.toString());
      return mesg.toString();
   }
}
