// RGPT PACKAGES
package com.rgpt.util;

public class CommonUtil
{
   public static String getCurrentDate(String format)
   {
      if (format == null || format.isEmpty())
         format = "yyyy.MM.dd'-'HH:mm:ss";
      java.text.DateFormat dateFormat = new java.text.SimpleDateFormat(format);
      java.util.Date date = new java.util.Date();
      String currDate = dateFormat.format(date);
      return currDate;
   }
}
