// RGPT PACKAGES
package com.rgpt.util;

import java.util.*;

public class LocalizationUtil
{
   private static ResourceBundle m_ResourceBundle;
   
   private static void createResourceBundle()
   {
      Locale locale;
      String language="en", country="US";
      Properties prop = RGPTParams.m_RGPTProperties;
      boolean useDefault = true;
      if (prop != null) {
         useDefault = false;
         language = prop.getProperty("LANGUAGE");
         if (language != null && language.length() > 0) language = new String(language);
         else useDefault = true;
         country = new String(prop.getProperty("COUNTRY"));
         if (country != null && country.length() > 0) country = new String(country);
         else useDefault = true;
      }
      if (useDefault) {
         language = new String("en");
         country = new String("US");
      }
      locale = new Locale(language, country);
      m_ResourceBundle = ResourceBundle.getBundle("MessagesBundle", locale);
      // RGPTLogger.logToFile("ResourceBundle: "+ m_ResourceBundle.keySet());
      // RGPTLogger.logToFile(m_ResourceBundle.getString("localeInfo") + 
                           // "(" + locale.getDisplayLanguage() + 
                           // "," + locale.getDisplayCountry() + ").\n");   
   }
   
   public static String getText(String key)
   {
      if (m_ResourceBundle == null) createResourceBundle();
      try {
         return m_ResourceBundle.getString(key);
      } catch(Exception ex) {
         RGPTLogger.logToFile("No Resource found for key: "+key, ex);
      }
      return "";
   }
   
   public static void main(String[] args)
   {
      String language;
      String country;
      Locale locale;
      ResourceBundle rb;

      if (args.length != 2){
         language = new String("en");
         country = new String("US");
      }
      else{
         language = new String(args[0]);
         country = new String(args[1]);
      }
      locale = new Locale(language, country);
      rb = ResourceBundle.getBundle("MessagesBundle", locale);
      System.out.println("ResourceBundle: "+ rb.keySet());
      System.out.println(rb.getString("localeInfo") + "(" + locale.getDisplayLanguage() + "," + locale.getDisplayCountry() + ").\n");
      System.out.println(rb.getString("welcome"));
      System.out.println(rb.getString("sayThanks"));
   }
}
