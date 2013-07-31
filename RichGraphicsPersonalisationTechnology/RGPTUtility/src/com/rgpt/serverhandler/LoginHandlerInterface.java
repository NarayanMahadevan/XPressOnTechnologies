// RGPT PACKAGES
package com.rgpt.serverhandler;

public interface LoginHandlerInterface
{
   // This function is called to retrieve serialized user name to avaoid
   // re-entry.
   public String getUserName();
   
   // This function is called to retrieve serialized user name to avaoid
   // re-entry.
   public String getUserPassword();
   
   // This function is called to check the validity of user login.
   public boolean validateUser(String userName, String Password);
}