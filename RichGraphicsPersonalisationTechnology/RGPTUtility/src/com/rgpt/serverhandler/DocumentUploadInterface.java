// RGPT PACKAGES
package com.rgpt.serverhandler;

import java.util.Vector;
import java.util.HashMap;

import com.rgpt.util.NotificationHandlerInterface;

public interface DocumentUploadInterface
{
   // This method is called to retrieve the FileUploadId before the Upload is started
   // For HTTP Upload the connInfo must contain URL String  and Servlet Name
   public int getFileUploadId(HashMap connInfo) throws Exception;
   
   // This function is called to upload file. This can be for in-memory usage,
   // or can up to upload to Server using HTPP, FTP or any other protocol.
   public HashMap uploadDocument(HashMap connInfo, ServerInterface serverReq, 
                                 int fileUploadId, Vector files) throws Exception;

   // This function is called to register Notification Handler. 
   public void registerNotificationHandler(NotificationHandlerInterface notifyHdlr);
}