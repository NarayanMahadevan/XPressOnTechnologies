// RGPT PACKAGES
package com.rgpt.util;

import java.util.Map;
import java.util.HashMap;
import java.util.Date;
import java.util.Calendar;

import java.io.Serializable;

public class FileUploadInfoHolder implements Serializable
{
   public static transient int STATUS_ERRORED = -1;
   public static transient int STATUS_INITIATED = 0;
   public static transient int STATUS_STARTED = 1;
   public static transient int STATUS_IN_PROGRESS = 2;
   public static transient int STATUS_FINISHED = 3;
   public static transient int STATUS_STOPED = 4;
   public static transient int STATUS_CANCELED = 5;
   
   public final static Map<Integer, String> m_FileUploadStatusMap;
   
   static
   {
      m_FileUploadStatusMap = new HashMap<Integer, String>();
      m_FileUploadStatusMap.put(STATUS_ERRORED, "Error in Uploading Document");
      m_FileUploadStatusMap.put(STATUS_INITIATED, "Initaited File Upload");
      m_FileUploadStatusMap.put(STATUS_STARTED, "Uploading Document Started");
      m_FileUploadStatusMap.put(STATUS_STOPED, "Uploading Document Stoped");
      m_FileUploadStatusMap.put(STATUS_IN_PROGRESS, "Uploading Document in Progress");
      m_FileUploadStatusMap.put(STATUS_FINISHED, "Uploading Documents Finished");
      m_FileUploadStatusMap.put(STATUS_CANCELED, "Uploading Documents Canceled");
   }
   
   public long m_FileUploadInstatiatedTime;
   public long m_FileUploadStartedTime;
   public long m_FileUploadUpdatedTime;
   
   public int m_FileUploadStatus;
   public double m_TotalNumOfBytesUploaded;
   public int m_TotalNumOfFilesUploaded;
   
   // Measured in Seconds
   public double m_TotalTimeTaken;
   
   // Measured in KB/s
   public double m_TransferRate;
   
   public FileUploadInfoHolder()
   {
      m_FileUploadStatus = STATUS_INITIATED;
      Date instTime = Calendar.getInstance().getTime();
      m_FileUploadInstatiatedTime = instTime.getTime();
      System.out.println("Instatiated Time: " + instTime.toString());
   }

   public void updateFileUploadStatus(int upldStatus, int numOfFileUploaded, 
                                      double numOfBytesUploaded)
   {
      m_FileUploadStatus = upldStatus;
      Date instTime = Calendar.getInstance().getTime();
      if (upldStatus == STATUS_STARTED)
      {
         m_FileUploadStartedTime = instTime.getTime();
         return;
      }
      m_TotalNumOfBytesUploaded = numOfBytesUploaded;
      m_TotalNumOfFilesUploaded = numOfFileUploaded;
      m_FileUploadUpdatedTime = instTime.getTime();
      m_TotalTimeTaken = (m_FileUploadUpdatedTime - m_FileUploadStartedTime)/1000;
      m_TransferRate = m_TotalNumOfBytesUploaded/(m_TotalTimeTaken*1000);
   }
   
   public String toString()
   {
      StringBuffer mesg = new StringBuffer();
      mesg.append("File Upload Status: " + m_FileUploadStatusMap.get(m_FileUploadStatus));
      mesg.append("\nFile Upload Started at: " + m_FileUploadStartedTime);
      mesg.append(" File Upload Updated at: " + m_FileUploadUpdatedTime);
      mesg.append("\nNumber of Files Uploaded: " + m_TotalNumOfFilesUploaded);
      mesg.append(" Number of KB Uploaded: " + m_TotalNumOfBytesUploaded/1000);
      mesg.append("\nTime Taken(secs): " + m_TotalTimeTaken);
      mesg.append(" Transfer Rate (KB/s): " + m_TransferRate);
      return mesg.toString();
   }
}
