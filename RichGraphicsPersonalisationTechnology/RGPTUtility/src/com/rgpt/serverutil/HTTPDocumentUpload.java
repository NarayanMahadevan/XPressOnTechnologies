// RGPT PACKAGES
package com.rgpt.serverutil;

import java.io.*;
import java.net.*;
import java.util.zip.*;
import java.util.Map;
import java.util.Vector;
import java.util.HashMap;

import javax.imageio.ImageIO;

import com.rgpt.serverhandler.DocumentUploadInterface;
import com.rgpt.serverhandler.ServerInterface;
import com.rgpt.util.FileUploadInfoHolder;
import com.rgpt.util.NotificationHandlerInterface;
import com.rgpt.util.ProgressInfo;
import com.rgpt.util.RGPTLogger;
import com.rgpt.util.RGPTThreadWorker;
import com.rgpt.util.ThreadInvokerMethod;
import com.rgpt.viewer.RGPTProgressBar;

import java.awt.image.BufferedImage;

// This classes are needed to make URL Connection and read the Stream
import java.net.URL;
import java.net.URLConnection;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.lang.RuntimeException;
import java.util.Properties;

public class HTTPDocumentUpload implements DocumentUploadInterface,
                                           ThreadInvokerMethod
{
   NotificationHandlerInterface m_NotificationHandler;
   public final static Map<Integer, String> m_NotificationStatus;
   
   static
   {
      m_NotificationStatus = new HashMap<Integer, String>();
      m_NotificationStatus.put(NotificationHandlerInterface.STATUS_ERRORED, 
                               "Error in Uploading Document");
      m_NotificationStatus.put(NotificationHandlerInterface.STATUS_STARTED, 
                               "Uploading Document Started");
      m_NotificationStatus.put(NotificationHandlerInterface.STATUS_STOPED, 
                               "Uploading Document Stoped");
      m_NotificationStatus.put(NotificationHandlerInterface.STATUS_IN_PROGRESS, 
                               "Uploading Document in Progress");
      m_NotificationStatus.put(NotificationHandlerInterface.STATUS_FINISHED, 
                               "Uploading Documents Finished");
      m_NotificationStatus.put(NotificationHandlerInterface.STATUS_CANCELED, 
                               "Uploading Documents Canceled");
   }
   
   public int getFileUploadId(HashMap connInfo) throws Exception
   {
      ObjectInputStream dataStr = null;
      InputStream fileUpldInfoStream = null;
      try
      {
         fileUpldInfoStream = makeFileUpldInfoServerCall(
                              connInfo, "GET_FILE_UPLOAD_ID", -1);
         // Response Steam from the server
         dataStr = new ObjectInputStream(fileUpldInfoStream);
         int fileUploadId = dataStr.readInt();
         RGPTLogger.logToFile("File Upload Id is: " + fileUploadId);
         return fileUploadId;
      }
      finally 
      {
         // Once we are done we want to make sure to disconnect from the server.
         if (dataStr != null) dataStr.close();
      }
   }
   
   public void registerNotificationHandler(NotificationHandlerInterface notifyHdlr)
   {
      m_NotificationHandler = notifyHdlr;
   }
   
   // Currently Directory files are not handled. This method assumes each item 
   // in the Vector is a File and not Directory
   public double getTotalFileSize(Vector files)
   {
      String ext = "";
      File f;
      double totalFileSize = 0.0;
      for (int i = 0; i < files.size(); i++) 
      {
         f = (File) files.elementAt(i);
         //if (f.isDirectory()) 
         totalFileSize = totalFileSize + (double) f.length();
      }
      return totalFileSize;
   }
   
   // This method needs to gather up each of the files the user has selected,
   // zip those files together, and send that zip file up to the server.
   // Currently Directory files are not handled. This method assumes each item 
   // in the Vector is a File and not Directory
   public HashMap uploadDocument(HashMap connInfo, ServerInterface serverReq, 
                                 int fileUploadId, Vector files) throws Exception
   {
      return this.uploadDocument(connInfo, serverReq, fileUploadId, files, false);
   }
   
   public HashMap uploadDocument(HashMap connInfo, ServerInterface serverReq, 
                                 int fileUploadId, Vector files, boolean upldPath) 
                                 throws Exception
   {
      File f = null;
      ZipEntry entry = null;
      HashMap result = new HashMap();
      String urlStr = "", servName = ""; 
      HttpURLConnection conn = null;
      try
      {
         if (files.size() == 0 && !upldPath) return null;
         
         // Uploading al the remaining files
         double totalFileSize = 0.0;
         double totalFileLength = getTotalFileSize(files);
         
         // Instamtiate the ProgressBar with Total Files and Total File Size
         ProgressInfo progInfo = new ProgressInfo();
         String title = "File Upload Progress Info";
         progInfo.m_TotalNumOfBytes = totalFileLength/1000;
         RGPTProgressBar progBar = new RGPTProgressBar(progInfo, title);
         progBar.setVisible(true);
         String srcName = this.getClass().getSimpleName();
         if (m_NotificationHandler != null)
            m_NotificationHandler.notifyProgress(
               srcName, NotificationHandlerInterface.STATUS_STARTED, new HashMap());
         
         urlStr = (String) connInfo.get("URLCodeBase");
         servName = (String) connInfo.get("FileUpldServletName");
         if (urlStr == null || servName == null)
            throw new RuntimeException("URL Code Base or Servlet Name is not set");
         URL serverURL = ServerProxy.getURL(urlStr, servName, null);
         conn = this.getHttpConnection(serverURL);
         // This zip output stream will server as our stream to the server and
         // will zip each file while it sends it to the server.
         ZipOutputStream out = new ZipOutputStream(conn.getOutputStream());
         
         // Serializing the Server Object and adding to the Zipped Output Stream
         // Serializing, creating Zip Entry and adding to Zip O/p Stream
         entry = new ZipEntry("ServerReq.ser");
         out.setLevel(9);
         out.putNextEntry(entry);
         ObjectOutputStream objStream = null;
         objStream = new ObjectOutputStream(out); 
         serverReq.save(objStream);
         objStream.flush();
         out.closeEntry();
         
         for (int i = 0; i < files.size(); i++) 
         {
            // For each file we will create a new entry in the ZIP archive and
            // stream the file into that entry.
            f = (File) files.elementAt(i);
            entry = new ZipEntry(f.getName());
            out.putNextEntry(entry);
            InputStream in = new FileInputStream(f);
            int read;
            byte[] buf = new byte[1024];
            double indFileSize = 0.0;
            while ((read = in.read(buf)) > 0) {
               indFileSize = indFileSize + read;
               totalFileSize = totalFileSize + read;
               out.write(buf, 0, read);
            }
            System.out.println("Number of Files Written into Stream: " + i+1 +
                               " Individual File Size: " + indFileSize/1000 +
                               " Total File Size Written: " + totalFileSize/1000 +
                               " Total File Size is: " +totalFileLength/1000);
            out.closeEntry();
         }

         // Once we are done writing out our stream we will finish building the 
         // archive and close the stream.
         out.finish();
         out.close();
         objStream.close();

         // Now that we have set all the connection parameters and prepared all
         // the data we are ready to connect to the server.
         conn.connect();
         
         this.getFileUploadData(connInfo, fileUploadId, progBar);
         
         // Printing the Response Codes
         System.out.println("conn.getResponseMessage(): " + conn.getResponseMessage());
      } 
      finally 
      {
         // Once we are done we want to make sure to disconnect from the server.
         if (conn != null) conn.disconnect();
      }
      return result;
   }

   // Loading the Font Stream in a new Thread
   public void getFileUploadData(HashMap connInfo, int fileUploadId, 
                                 RGPTProgressBar progBar)
   {
      RGPTThreadWorker threadWorker = null; 
      HashMap requestData = new HashMap();
      requestData.put("RequestType", "GetFileUploadData");
      requestData.put("ConnInfo", connInfo);
      requestData.put("FileUploadId", fileUploadId);
      requestData.put("RGPTProgressBar", progBar);
      RGPTLogger.logToFile("Creatting Thread Request: " + requestData.toString());
      threadWorker = new RGPTThreadWorker(Thread.MIN_PRIORITY, this, 
                                           requestData);
      threadWorker.startThreadInvocation();      
   }
   
   // Processing the Thread Request to load Font Streams
   public void processThreadRequest(HashMap requestData) throws Exception
   {
      String reqType = (String) requestData.get("RequestType");
      RGPTLogger.logToFile("Received Thread Request: " + reqType);
      if (reqType.equals("GetFileUploadData")) this.GetFileUploadData(requestData);
   }
   
   private static final int FILE_UPLOAD_INFO_CHECK_TIME = 1000; 
   public void GetFileUploadData(HashMap requestData) throws Exception
   {
      URL serverURL = null;
      String urlStr = "", servName = ""; 
      FileUploadInfoHolder fileUploadInfo = null;
      ObjectInputStream dataStr = null;
      InputStream fileUpldInfoStream = null;
      RGPTProgressBar progBar = null;
      ProgressInfo progInfo = null;
      try
      {
         HashMap connInfo = (HashMap) requestData.get("ConnInfo");
         int fileUpldId = ((Integer)requestData.get("FileUploadId")).intValue();
         // Response Steam from the server
         RGPTLogger.logToFile("Making Server Call for file upload data for: " + 
                               fileUpldId);
         fileUpldInfoStream = makeFileUpldInfoServerCall(
                              connInfo, "GET_FILE_UPLOAD_INFO", fileUpldId);
         dataStr = new ObjectInputStream(fileUpldInfoStream);
         fileUploadInfo = (FileUploadInfoHolder) dataStr.readObject();
         progBar = (RGPTProgressBar) requestData.get("RGPTProgressBar");
         progInfo = progBar.getProgressInfo();

         progInfo.m_TranferredBytes = fileUploadInfo.m_TotalNumOfBytesUploaded/1000;
         progInfo.m_ProgressValue = (int) fileUploadInfo.m_TotalNumOfBytesUploaded/1000;
         progInfo.m_TimeElapsed = (int) fileUploadInfo.m_TotalTimeTaken;
         progInfo.m_TranferRate = (int) fileUploadInfo.m_TransferRate;
         if (fileUploadInfo.m_TransferRate > 0)
            progInfo.m_TimeLeft = (int) ((progInfo.m_TotalNumOfBytes/
                                        fileUploadInfo.m_TransferRate) - 
                                       fileUploadInfo.m_TotalTimeTaken);
         boolean isFinished = false;
         if (fileUploadInfo.m_FileUploadStatus == FileUploadInfoHolder.STATUS_FINISHED) 
         {
            isFinished = true;
         }
         progInfo.m_IsFinished = isFinished;
         progBar.setProgressValue();
         RGPTLogger.logToFile("File Upload Info Data: " + fileUploadInfo.toString());
         if (isFinished){
            String srcName = this.getClass().getSimpleName();
            progBar.dispose();
            if (m_NotificationHandler != null)
               m_NotificationHandler.notifyProgress(
               srcName, NotificationHandlerInterface.STATUS_FINISHED, new HashMap());
            return;
         }
         try {
            Thread.sleep(FILE_UPLOAD_INFO_CHECK_TIME);
            this.GetFileUploadData(requestData);
         }
         catch(Exception ex) {}
      }
      finally 
      {
         // Once we are done we want to make sure to disconnect from the server.
         if (dataStr != null) dataStr.close();
      }
   }

   private InputStream makeFileUpldInfoServerCall(HashMap connInfo, String reqName, 
                                                  int fileUpldId)
   {
      String urlStr = "", servName = ""; 
      urlStr = (String) connInfo.get("URLCodeBase");
      servName = (String) connInfo.get("FileUpldInfoServletName");
      if (urlStr == null || servName == null)
         throw new RuntimeException("URL Code Base or Servlet Name is not set");
      Vector<String> reqParam = new Vector<String>();
      reqParam.addElement("RequestName=" + reqName);
      if (fileUpldId != -1)
         reqParam.addElement("FileUploadId=" + String.valueOf(fileUpldId));
      URL serverURL = ServerProxy.getURL(urlStr, servName, reqParam);
      return ServerProxy.makeServerRequest(serverURL);
   }
   
   private HttpURLConnection getHttpConnection(URL serverURL) 
                             throws Exception 
   {
      HttpURLConnection conn = null;
      System.out.println("Server URL Specified: " + serverURL.toString());
      URLConnection serverConn = serverURL.openConnection();
      System.out.println("URLConn: " + serverConn.toString());
      System.out.println("URLConn Classname: " + serverConn.getClass().toString());
      conn = (HttpURLConnection) serverConn;
      conn.setRequestMethod("PUT");
      conn.setFollowRedirects(false);
      conn.setRequestProperty("content-type", "application/zip");

      // Most HTTP connections do not have any ouput.  The most common case
      // is to set up a number of parameters and then make a request to the
      // server without any additional data.  We want to send the file data
      // up to the server so we need to explicitely tell the connection that
      // we intend to send output to the server.
      conn.setDoOutput(true);
      return conn;
   }
}