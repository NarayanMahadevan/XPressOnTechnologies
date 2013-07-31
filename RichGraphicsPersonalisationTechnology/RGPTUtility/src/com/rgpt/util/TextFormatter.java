// RGPT PACKAGES
package com.rgpt.util;

import java.util.Map; 
import java.util.HashMap; 

public class TextFormatter
{
   public static final int NO_FORMAT = 0;
   public static final int TITLE_CASE = 1;
   public static final int ALL_UPPER_CASE = 2;
   public static final int ALL_LOWER_CASE = 3;
   public static final int SENTENCE_CASE = 4;
   public static final int GENERAL_TEXT_FORMAT = 5;
   //public static final int EMAIL_FORMAT = 5;
   
   public final static Map<Integer, String> SUPPORTED_TEXT_FORMAT;
   
   static
   {
      SUPPORTED_TEXT_FORMAT = new HashMap<Integer, String>();
      SUPPORTED_TEXT_FORMAT.put(NO_FORMAT, "No Format");
      SUPPORTED_TEXT_FORMAT.put(TITLE_CASE, "Title Case");
      SUPPORTED_TEXT_FORMAT.put(ALL_UPPER_CASE, "All Upper Case");
      SUPPORTED_TEXT_FORMAT.put(ALL_LOWER_CASE, "All Lower Case");
      SUPPORTED_TEXT_FORMAT.put(SENTENCE_CASE, "Sentence Case");
      //SUPPORTED_TEXT_FORMAT.put(EMAIL_FORMAT, "Email");
   }
   
   public static String getFormattedText(String vdpText, int formatType, String formatSpec)
   {
      switch (formatType) 
      {
         case NO_FORMAT: 
            return  vdpText;
         case TITLE_CASE: 
            return RGPTUtil.toTitleCase(vdpText);
         case ALL_UPPER_CASE: 
            return  vdpText.toUpperCase();
         case ALL_LOWER_CASE: 
            return vdpText.toLowerCase();
         case SENTENCE_CASE: 
            return RGPTUtil.toSentenceCase(vdpText);
         case GENERAL_TEXT_FORMAT: 
            return getFormattedText(vdpText, formatSpec);
         default:
            return  vdpText;
      }
   }
   
   public static String getFormattedText(String vdpText, String formatSpec)
   {
      StringBuffer newText = new StringBuffer();
      char[] specArray = formatSpec.toCharArray();
      char[] txtArray = vdpText.toCharArray();
      int adjVal = 0, vdpTxtIndex = 0;
      for(int i = 0; i < specArray.length; i++)
      {
         vdpTxtIndex = i-adjVal;
         if (vdpTxtIndex >= txtArray.length) return newText.toString();
         String charVal = String.valueOf(txtArray[vdpTxtIndex]);
         if(specArray[i] == 'N') {
            try {
               int numEnt = Integer.parseInt(charVal);
               newText.append(numEnt);
            }
            catch(Exception ex) {
               System.out.println("The Text Enetered is not a Number: " + charVal); 
               throw new RuntimeException("Please Enter only Numbers");
            }
         }
         else if(specArray[i] == 'T') newText.append(charVal);
         else {
            adjVal++;
            newText.append(String.valueOf(specArray[i]));
         }
      } 
      return newText.toString();
   }
}