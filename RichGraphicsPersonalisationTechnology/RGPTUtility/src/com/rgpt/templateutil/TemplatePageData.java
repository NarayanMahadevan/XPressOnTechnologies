// RGPT PACKAGES
package com.rgpt.templateutil;

import java.util.Map;
import java.util.HashMap;
import java.util.Vector;
import java.io.Serializable;

public class TemplatePageData implements Serializable
{
   static final long serialVersionUID = -4377852477070217628L;
   
   public int m_PageNum;
   public String m_ThumbViewFile;
   public String m_StdQualityImageFile;
   public String m_MaxQualityImageFile;
   
   // Maintains the file path of serialized PDFPageInfo object for this  PageNumber
   public String m_SerializedDataFile;
   
   // This maintains the list of VDP Text Fields and Image Fields for this page.
   // This vector maintains the sequence and Each element of the Vector is the 
   // HashMap containg VDP Field Metadata.
   public Vector<Map> m_VDPTextFieldData;
   public Vector<Map> m_VDPImageFieldData;
  
   //public String m_HighQualityImageFile;
   //public String m_BestQualityImageFile;
   
   public TemplatePageData()
   {
      m_VDPTextFieldData = new Vector<Map>();
      m_VDPImageFieldData = new Vector<Map>();
   }
   
   public boolean isVDPFieldPopulated()
   {
      if (m_VDPTextFieldData == null || m_VDPImageFieldData == null ||
          m_MaxQualityImageFile == null || m_MaxQualityImageFile.length() == 0) 
         return false;
      // if (m_VDPTextFieldData != null && m_VDPTextFieldData.size() > 0) return true;
      // if (m_VDPImageFieldData != null && m_VDPImageFieldData.size() > 0) return true;
      return true;
   }
   
   public String toString()
   {
      StringBuffer mesg = new StringBuffer("\nTemplate PageData");
      mesg.append("\nPage Num: " + m_PageNum);
      if (m_VDPTextFieldData != null) 
         mesg.append(" Num Text Flds: " + m_VDPTextFieldData.size());
      if (m_VDPImageFieldData != null) 
         mesg.append(" Num Image Flds: " + m_VDPImageFieldData.size());
      mesg.append("\nThumbViewFile: " + m_ThumbViewFile);
      mesg.append("\nSerializedDataFile: " + m_SerializedDataFile);
      mesg.append("\nStdQualityImageFile: " + m_StdQualityImageFile);   
      mesg.append("\nMaxQualityImageFile: " + m_MaxQualityImageFile);   
      //mesg.append(" HighQualityImageFile: " + m_HighQualityImageFile);
      //mesg.append(" BestQualityImageFile: " + m_BestQualityImageFile);
      return mesg.toString();
   }
   
} 
