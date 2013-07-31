package com.rgpt.serverutil;

// This classes are needed to make URL Connection and read the Stream
import java.util.Vector;
import java.net.URL;
import java.net.URLConnection;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.util.Properties;

import com.rgpt.serverhandler.ServerInterface;
import com.rgpt.util.RGPTLogger;

public class ServerProxy
{
   // This returns a HTTP URL Object based on the URL CodeBase and Servlet Name.
   // Mainly used by the Applet. Here the reqParam is in the form name=value
   public static URL getURL(String codeBaseURL, String servletName, Vector<String> reqParam)
   {
      try
      {
         return getURL(codeBaseURL + servletName, reqParam);
      }
      catch (Exception ex)
      {
         ex.printStackTrace();
         throw new RuntimeException("Unable to create the Server URL");
      }
   }
   
   // This returns a HTTP URL Object based on the fully qualified url stream.
   // Mainly used by JNLP Application
   public static URL getURL(String url, Vector<String> reqParam)
   {
      URL serverURL = null;
      StringBuffer servURLStrBuf = null;
      try
      {
         servURLStrBuf = new StringBuffer(url);
         if (reqParam == null || reqParam.size() == 0) {
            serverURL = new URL(servURLStrBuf.toString());
            return serverURL;
         }
         for (int i = 0; i < reqParam.size(); i++) {
            if (i == 0) servURLStrBuf.append("?" + reqParam.elementAt(i));
            else servURLStrBuf.append("&" + reqParam.elementAt(i));
         }
         serverURL = new URL(servURLStrBuf.toString());
         return serverURL;
      }
      catch (Exception ex)
      {
         ex.printStackTrace();
         throw new RuntimeException("Unable to create the Server URL");
      }
   }
   
   // This returns a HTTP URL Object based on the URL Object and Servlet Name.
   // Mainly used by Applet
   public static URL getURL(URL url, String servletName)
   {
      URL serverURL = null;
      try
      {
         serverURL = new URL(url, servletName);
         return serverURL;
      }
      catch (Exception ex)
      {
         ex.printStackTrace();
         throw new RuntimeException("Unable to create the Server URL");
      }
   }
   
   public static InputStream makeServerRequest(URL url, String servletName, ServerInterface serverReq)
   {
      URL serverURL = null;
      try
      {
         serverURL = new URL(url, servletName);
      }
      catch (Exception ex)
      {
         ex.printStackTrace();
         throw new RuntimeException("Unable to create to the Server URL");
      }
      RGPTLogger.logToFile("Server URL Stream: " + serverURL);
      return makeServerRequest(serverURL, serverReq);
   }
   
   public static InputStream makeServerRequest(String url, ServerInterface serverReq)
   {
      URL serverURL = null;
      try
      {
         serverURL = new URL(url);
      }
      catch (Exception ex)
      {
         ex.printStackTrace();
         throw new RuntimeException("Unable to create to the Server URL");
      }
      RGPTLogger.logToFile("Server URL Stream: " + serverURL);
      return makeServerRequest(serverURL, serverReq);
   }
   
   public static InputStream makeServerRequest(String url, String servletName, ServerInterface serverReq)
   {
      URL serverURL = null, urlContext = null;
      try
      {
         urlContext = new URL(url);
         serverURL = new URL(urlContext, servletName);
      }
      catch (Exception ex)
      {
         ex.printStackTrace();
         throw new RuntimeException("Unable to create to the Server URL");
      }
      //RGPTLogger.logToFile("Server URL Stream: " + serverURL);
      return makeServerRequest(serverURL, serverReq);
   }
   
   // This is to Retry Server Calls once an exception is thrown from Server. 
   private static boolean m_RetryServerCall = false;
   // This is the Wait Time in Lilliseconds before Retry 
   private static int m_RetryWaitTime = 5000;
   
   public static InputStream makeServerRequest(URL serverURL, ServerInterface serverReq)
   {
      try
      {
         RGPTLogger.logToFile("Server Request: " + serverReq.toString(), true);
         RGPTLogger.logToFile("Server URL Derived: " + serverURL.toString(), true);
         URLConnection serverConn = serverURL.openConnection();
         serverConn.setDoInput(true);
         serverConn.setDoOutput(true);
         serverConn.setConnectTimeout(0);
         serverConn.setUseCaches(false);
         serverConn.setReadTimeout(0);
   		serverConn.setRequestProperty(
   			"Content-Type",
   			"application/x-java-serialized-object");
         
         // Serialize Serrver Request and write it to the Server Connection Stream
         ObjectOutputStream objStream = null;
         objStream = new ObjectOutputStream(serverConn.getOutputStream()); 
         serverReq.save(objStream);
         objStream.close();
         
         // Response Steam from the server
         InputStream serverRespStream = serverConn.getInputStream();
         
         // Resetting the Server Call
         m_RetryServerCall = false;
         
         // Read the Respose from the server connection stream
         RGPTLogger.logToFile("Server Response Received.", true);
         return serverRespStream;
      }
      catch (Exception ex)
      {
         ex.printStackTrace();
         if (!m_RetryServerCall)
         {
            m_RetryServerCall = true;
            try {
               Thread.sleep(m_RetryWaitTime);
               return makeServerRequest(serverURL, serverReq);
            }
            catch (Exception e) {}
         }
         else m_RetryServerCall = false;
         throw new RuntimeException("Unable to retrieve response from Server " +
                                    ex.getMessage()); 
      }
   }

   public static InputStream makeServerRequest(String url)
   {
      URL serverURL = null;
      try
      {
         serverURL = new URL(url);
      }
      catch (Exception ex)
      {
         ex.printStackTrace();
         throw new RuntimeException("Unable to create to the Server URL");
      }
      RGPTLogger.logToFile("Server URL Stream: " + serverURL);
      return makeServerRequest(serverURL);
   }
   
   // This method is invoked to invoke a URL with req param
   public static InputStream makeServerRequest(String url, Vector<String> reqParam)
   {
      URL serverURL = null;
      try
      {
         serverURL = getURL(url, reqParam);
      }
      catch (Exception ex)
      {
         ex.printStackTrace();
         throw new RuntimeException("Unable to create to the Server URL");
      }
      RGPTLogger.logToFile("Server URL Stream: " + serverURL);
      return makeServerRequest(serverURL);
   }
   
   public static InputStream makeServerRequest(URL serverURL)
   {
      try
      {
         RGPTLogger.logToFile("Server Call Received: " + serverURL);
			URLConnection serverConn = serverURL.openConnection();
			serverConn.connect();
         // Response Steam from the server
         InputStream serverRespStream = serverConn.getInputStream();
         // Resetting the Server Call
         m_RetryServerCall = false;
         // Read the Respose from the server connection stream
         RGPTLogger.logToFile("Server Response Received.");
         return serverRespStream;
      }
      catch (Exception ex)
      {
         ex.printStackTrace();
         if (!m_RetryServerCall)
         {
            m_RetryServerCall = true;
            try {
               Thread.sleep(m_RetryWaitTime);
               return makeServerRequest(serverURL);
            }
            catch (Exception e) {}
         }
         else m_RetryServerCall = false;
         throw new RuntimeException("Unable to retrieve response from Server " +
                                    ex.getMessage()); 
      }
	}
   
}