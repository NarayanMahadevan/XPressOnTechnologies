// RGPT PACKAGES
package com.rgpt.util;

import java.util.HashMap;

// This interface is used as an Interaction Point between the Caller and
// Call Back Objet. For e.g. For Document Uplad HTTPDocumentUpload is 
// the caller object sending notifications of the progress to the call back 
// Object which can be Template Upload or File Upload etc.
public interface NotificationHandlerInterface
{
   // Notification Status
   public static int STATUS_ERRORED = -1;
   public static int STATUS_STARTED = 0;
   public static int STATUS_STOPED = 1;
   public static int STATUS_IN_PROGRESS = 2;
   public static int STATUS_FINISHED = 3;
   public static int STATUS_CANCELED = 4;
   
   // This are actions to be performed during start and in progress
   public static int ACTION_START = 0;
   public static int ACTION_PAUSE = 1;
   public static int ACTION_CANCEL = 2;
   public static int ACTION_CONTINUE = 4;
   
   // This returns Any Action the Caller Object should Perform
   public int getProgressAction();
   
   // The status is notified by the Caller Object to the Call Back Object
   public void notifyProgress(String source, int status, HashMap result);
}
