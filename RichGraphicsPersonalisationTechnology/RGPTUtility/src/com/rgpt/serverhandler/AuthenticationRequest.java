// RGPT PACKAGES
package com.rgpt.serverhandler;

import java.io.Serializable;

// This is to set the Approval Flag for generated PDF
public class AuthenticationRequest extends ServerRequest implements Serializable
{
   public static final String m_RequestType = "AuthenticationRequest";
   
   // User Credentials
   public String m_UserName;
   public String m_UserType;
   public String m_UserPassword;
   
   public AuthenticationRequest() {}
   
   public String toString()
   {
      StringBuffer mesg = new StringBuffer();
      mesg.append(" REQUEST TYPE: " + m_RequestType);
      mesg.append(" User Name: " + m_UserName);
      mesg.append(" User Type: " + m_UserType);
      return mesg.toString();
   }
}
