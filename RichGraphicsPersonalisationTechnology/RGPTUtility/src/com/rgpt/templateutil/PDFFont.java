// RGPT PACKAGES
package com.rgpt.templateutil;

import java.util.Hashtable;

public class PDFFont
{
   public int m_FontType;
   public String m_FontName;
   public String m_BaseFontName;
   public boolean m_IsBold;
   public boolean m_IsItalic;
   
   public PDFFont(int fontType, String fontName, String baseFontName,
                  boolean isBold, boolean isItalic)
   {
      m_FontType = fontType;
      m_FontName = fontName;
      m_BaseFontName = baseFontName;
      m_IsBold = isBold;
      m_IsItalic = isItalic;
   }
   
   public String toString()
   {
      StringBuffer logMesg = new StringBuffer();
      logMesg.append("PDF Font Type: " + m_FontType);
      logMesg.append("\nBase Font Name: " + m_BaseFontName);
      logMesg.append("\nPDF Font Name: " + m_FontName);
      logMesg.append("\nIs Bold: " + m_IsBold);
      logMesg.append("\nIs Oblique/Italic: " + m_IsItalic);
      return logMesg.toString();
   }
}


