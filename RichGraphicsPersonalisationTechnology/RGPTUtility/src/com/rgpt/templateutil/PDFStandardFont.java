// RGPT PACKAGES
package com.rgpt.templateutil;

import java.util.Hashtable;

// This class accomodates the 14 PDF Fonts specified in the PDF Reference 1.6
// They are: Times-Roman, Times-Bold, Times-Italic, Times-BoldItalic, 
//                  Helvetica, Helvetica-Bold, Helvetica-Oblique, Helvetica-BoldOblique, 
//                  Courier, Courier-Bold, Courier-Oblique, Courier-BoldOblique, 
//                  Symbol, ZapfDingbats

public class PDFStandardFont
{
   // Courier, Courier-Bold, Courier-Oblique, Courier-BoldOblique, Symbol, ZapfDingbats
   public static final Hashtable<Integer, PDFFont> m_PDFFontTable;
   
   // Times Roman Font Family
   public static final PDFFont TIMES_NEW_ROMAN;
   public static final PDFFont TIMES_BOLD;
   public static final PDFFont TIMES_ITALIC;
   public static final PDFFont TIMES_BOLD_ITALIC;

   // Helvetica Font Family
   public static final PDFFont HELVETICA;
   public static final PDFFont HELVETICA_BOLD;
   public static final PDFFont HELVETICA_OBLIQUE;
   public static final PDFFont HELVETICA_BOLD_OBLIQUE;
   
   // Courier Font Family
   public static final PDFFont COURIER;
   public static final PDFFont COURIER_BOLD;
   public static final PDFFont COURIER_OBLIQUE;
   public static final PDFFont COURIER_BOLD_OBLIQUE;
   
   // Symbol, ZapfDingbats Fonts
   public static final PDFFont SYMBOL;
   public static final PDFFont ZAPF_DINGBOTS;
   
   static
   {
      m_PDFFontTable = new Hashtable<Integer, PDFFont>();
      
      // Times Roman Font Family
      TIMES_NEW_ROMAN = new PDFFont(0, "Times-Roman", "Times-Roman", false, false);
      m_PDFFontTable.put(new Integer(0), TIMES_NEW_ROMAN);
      
      TIMES_BOLD = new PDFFont(1, "Times-Bold", "Times-Roman", true, false);
      m_PDFFontTable.put(new Integer(1), TIMES_BOLD);
      
      TIMES_ITALIC = new PDFFont(2, "Times-Italic", "Times-Roman", false, true);
      m_PDFFontTable.put(new Integer(2), TIMES_ITALIC);
      
      TIMES_BOLD_ITALIC = new PDFFont(3, "Times-BoldItalic", "Times-Roman", true, true);
      m_PDFFontTable.put(new Integer(3), TIMES_BOLD_ITALIC);
      
      // Helvetica Font Family
      HELVETICA = new PDFFont(4, "Helvetica", "Helvetica", false, false);
      m_PDFFontTable.put(new Integer(4), HELVETICA);
      
      HELVETICA_BOLD = new PDFFont(5, "Helvetica-Bold", "Helvetica", true, false);
      m_PDFFontTable.put(new Integer(5), HELVETICA_BOLD);
      
      HELVETICA_OBLIQUE = new PDFFont(6, "Helvetica-Oblique", "Helvetica", false, true);
      m_PDFFontTable.put(new Integer(6), HELVETICA_OBLIQUE);
      
      HELVETICA_BOLD_OBLIQUE = new PDFFont(7, "Helvetica-BoldOblique", "Helvetica", true, true);
      m_PDFFontTable.put(new Integer(7), HELVETICA_BOLD_OBLIQUE);

      // Courier Font Family
      COURIER = new PDFFont(8, "Courier", "Courier", false, false);
      m_PDFFontTable.put(new Integer(8), COURIER);
      
      COURIER_BOLD = new PDFFont(9, "Courier-Bold", "Courier", true, false);
      m_PDFFontTable.put(new Integer(9), COURIER_BOLD);
      
      COURIER_OBLIQUE = new PDFFont(10, "Courier-Oblique", "Courier", false, true);
      m_PDFFontTable.put(new Integer(10), COURIER_OBLIQUE);
      
      COURIER_BOLD_OBLIQUE = new PDFFont(11, "Courier-BoldOblique", "Courier", true, true);
      m_PDFFontTable.put(new Integer(11), COURIER_BOLD_OBLIQUE);

      // Symbol, ZapfDingbats Fonts
      SYMBOL = new PDFFont(12, "Symbol", "Symbol", false, false);
      m_PDFFontTable.put(new Integer(12), SYMBOL);
      
      ZAPF_DINGBOTS = new PDFFont(13, "ZapfDingbats", "ZapfDingbats", false, false);
      m_PDFFontTable.put(new Integer(13), ZAPF_DINGBOTS);
   }
   
   public static PDFFont getPDFStandardFont(int fontType)
   {
      return m_PDFFontTable.get(new Integer(fontType));
   }
   
   public static String getPDFFontTable()
   {
      return m_PDFFontTable.toString();
   }
   
}

