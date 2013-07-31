// RGPT PACKAGES
package com.rgpt.util;

// Using Log4J for Logging. The Log4JProperties are set at the .classes Location
// PRINT_INFO - This logger is used to Print PDF Dumps which are mearly informational.
//                             This logged to File with name pdfdump.log. The Append is set to false.
import org.apache.log4j.*;

import java.util.Properties;
import java.util.Enumeration;
import java.util.Map;
import java.util.HashMap;

public class RGPTLogger
{
   public static final int DEBUG_LOG = 0;
   public static final int INFO_LOG = 1;
   public static final int WARNING_LOG = 2;
   public static final int ERROR_LOG = 3;
      
   public static final String XON_ID_LOG = "ImgDsgnLog";
   public static final String XON_IM_LOG = "ImgMakerLog";
   public static final String TEMPLATE_MAKER_LOG = "TempMakerLog";
   public static final String MESG_SERVER_LOG = "MesgServerLog";
   public static final String PDF_SERVER_LOG = "PDFServerLog";
   public static final String BATCH_SERVER_LOG = "BatchServerLog";
   public static final String NOT_SERVER_LOG = "NotServerLog";
   public static final String SEARCH_ENGINE_LOG = "SearchEngLog";
   
   // Log Level to Log Indicator Mapping
   private static Map<String, Integer> m_LogLevelInd;
   
   public static String m_LogMode;
   public static String m_Logger;
   public static String m_LogLevel = "";
   public static int m_LogInd = -1;
   public static int m_DefLogInd = INFO_LOG; // This indicator is used if Log Level is not set
   public static int m_DefLog = DEBUG_LOG; // This is set if in the method call log level is not set
   
   public static String m_LogDir = "";
   public static String m_LogFile = "";
   public static boolean m_AppendLog = true;
   
   // Initialize the logger.  
   //  m_ConsoleLog is the logger for this class. This inherits all the propoerties 
   // of the Root Category. This is set to print in the console in the property file
   // in Dev env and in server log in Production Env
   private static Logger m_ConsoleLog;
   
   // This is the Information Logger. This retrieves the PRINT_INFO logger set 
   // through the Properties file. This is used to dump the pdf elements.
   private static Logger m_InfoLog;
   
   public static String m_LogFileName;
   
   static {
      m_LogLevelInd = new HashMap<String, Integer>();
      m_LogLevelInd.put("DEBUG", DEBUG_LOG);
      m_LogLevelInd.put("INFO", INFO_LOG);
      m_LogLevelInd.put("WARNING", WARNING_LOG);
      m_LogLevelInd.put("ERROR", ERROR_LOG);
   }
   
   private static void populateLogger()
   {
      Properties prop = AppletParameters.m_ServerProperties;
      if (prop == null) return;
      m_LogMode = prop.getProperty("LOG_MODE");
      m_Logger = prop.getProperty("LOGGER");
      m_LogLevel = prop.getProperty("LOG_LEVEL");
      m_LogDir = prop.getProperty("LogDir");
      String appendLog = prop.getProperty("AppendLog");
      if (appendLog == null) return;
      if (appendLog.equals("true")) m_AppendLog = true;
      else m_AppendLog = false;
      if (m_Logger == null || m_Logger.length() == 0 || m_Logger == "CONSOLE") return;
      if (m_Logger.equals("LOG4J"))
      {
         try {
            m_InfoLog = Logger.getLogger("PRINT_INFO");
         } catch(Throwable ex) { 
            m_LogMode = "CONSOLE"; 
            m_Logger = "RGPT_LOGGER"; 
            if (m_LogLevel.length() > 0) {
               Integer logIndObj = m_LogLevelInd.get(m_LogLevel);
               if (logIndObj != null) m_LogInd = logIndObj.intValue();
               else m_LogInd = m_DefLogInd;
            }
            else m_LogInd = m_DefLogInd;
            return; 
         }    
         Integer logIndObj = m_LogLevelInd.get(m_LogLevel);
         if (logIndObj != null) m_LogInd = logIndObj.intValue();
         else m_LogInd = m_DefLogInd;
         @SuppressWarnings("rawtypes")
		Enumeration appendars = m_InfoLog.getAllAppenders();
         Appender appendar = null;
         while (appendars.hasMoreElements()) 
         {
            appendar = (Appender) appendars.nextElement();
            String className = appendar.getClass().getName();
            System.out.println(className);
            if (className.equals("org.apache.log4j.FileAppender"))
            {
               FileAppender fAppendar = (FileAppender) appendar;
               String logFile = RGPTFileFilter.getFileName(fAppendar.getFile());
               System.out.println("Log File Set: " + m_LogFile);
               if (m_LogFile != null && !m_LogFile.isEmpty()) logFile = m_LogFile;
               System.out.println("Old: " + logFile);
               if (AppletParameters.m_RequestParamValues != null) {
                  m_LogDir = AppletParameters.getVal("LogDir", m_LogDir);
                  m_AppendLog = AppletParameters.getBoolVal("AppendLog", m_AppendLog);
               }
               if (m_LogDir != null && !m_LogDir.isEmpty()) {
                  logFile = m_LogDir+logFile;
               }
               fAppendar.setAppend(m_AppendLog);
               String currDate = RGPTUtil.getCurrentDate("dd-MM-yyyy");
               System.out.println("Current Date Time : " + currDate);
               m_LogFileName = logFile+"-"+currDate+".log";
               System.out.println("NEW: " + m_LogFileName);
               fAppendar.setFile(m_LogFileName);
               fAppendar.activateOptions();
            }
         }
         m_ConsoleLog = Logger.getLogger(RGPTLogger.class.getName());
         // By default the Looger inherits all the property of the Root Logger. 
         // This is  done to disable inheriting of appenders
         m_InfoLog.setAdditivity(false);
         m_InfoLog.info("READY TO LOG AT LOG LEVEL: " + m_LogLevel + 
                        " LOG IND: " + m_LogInd);
      }
      System.out.println("Log Mode: " + m_LogMode + " :Logger : " + m_Logger);
   }
   
   // This Logs to File only when the LogMode enables Logging to File else 
   // logs to Console. Further it logs to File only When Log4J is enabled.
   // RGPTLogger.logToFile
   public static void logToFile(String mesg)
   {
      logToFile(mesg, true, DEBUG_LOG);
   }
   
   public static void logToFile(String mesg, int logLevel)
   {
      logToFile(mesg, true, logLevel);
   }
   
   public static void logToFile(String logMesg, Throwable ex)
   {
      StringBuffer mesg = new StringBuffer(logMesg+" Exception Mesg: "+ex.getMessage());
      mesg.append("\n"+RGPTUtil.StackTraceToString(ex));
      logToFile(mesg.toString(), true, ERROR_LOG);
   }
   
   public static void logToFile(String logMesg, boolean showTime)
   {
      logToFile(logMesg, showTime, DEBUG_LOG);
   }
   
   public static void logWarningMesg(String logMesg)
   {
      logToFile(logMesg, true, WARNING_LOG);
   }
   
   public static void logErrorMesg(String logMesg)
   {
      logToFile(logMesg, true, ERROR_LOG);
   }
   
   public static void logInfoMesg(String logMesg)
   {
      logToFile(logMesg, true, INFO_LOG);
   }
   
   public static void logDebugMesg(String logMesg)
   {
      logToFile(logMesg, true, DEBUG_LOG);
   }
   
   public static void logToFile(String logMesg, boolean showTime, int logLevel)
   {
      if (m_LogInd > logLevel) return;
      String timeMesg = "";
      if (showTime && m_LogMode == "File") {
         String format = "EEE, HH:mm:ss ': '";
         timeMesg = RGPTUtil.getCurrentDate(format);
      }
      final Throwable t = new Throwable();
      final StackTraceElement[] methodCallers = t.getStackTrace();      
      String fname = methodCallers[1].getFileName(); 
      String mthName = methodCallers[1].getMethodName();
      if (fname == "RGPTLogger.java") {
         fname = methodCallers[2].getFileName();
         mthName = methodCallers[2].getMethodName();
      }
      int index = fname.lastIndexOf('.');
      fname = fname.substring(0,index);      
      String mesg = timeMesg + fname + "." + mthName + ": " + logMesg;
      
      if (m_Logger == null || m_LogMode == null)
         populateLogger();
      if (m_Logger != null && m_Logger.equals("LOG4J") && m_LogMode == "File") 
         m_InfoLog.info(mesg);
      else System.out.println(mesg);
   }
   
   // This Logs to File only when the LogMode enables Logging to File else 
   // logs to Console. Further it logs to File only When Log4J is enabled.
   public static void logToConsole(String mesg)
   {
      logToConsole(mesg, DEBUG_LOG);
   }
   
   public static void logToConsole(String mesg, int logLevel)
   {
      if (m_LogInd > logLevel) return;
      final Throwable t = new Throwable();
      final StackTraceElement[] methodCallers = t.getStackTrace();      
      String fname = methodCallers[1].getFileName(); 
      String mthName = methodCallers[1].getMethodName();
      if (fname == "RGPTLogger.java") {
         fname = methodCallers[2].getFileName();
         mthName = methodCallers[2].getMethodName();
      }
      int index = fname.lastIndexOf('.');
      fname = fname.substring(0,index);       
      String logMesg = fname+"."+mthName+": " + mesg;
      
      if (m_Logger == null || m_LogMode == null)
         populateLogger();
      // if (m_Logger != null && m_Logger.equals("LOG4J")) 
         // m_ConsoleLog.debug(mesg);
      System.out.println(logMesg);
   }
   
        
   public String toString()
   {
      StringBuffer logMesg = new StringBuffer();
      logMesg.append("RGPT Logger Log Mode: " + m_LogMode);
      logMesg.append(" Logger: " + m_Logger);
      return logMesg.toString();
   }
}


