// RGPT PACKAGES
package com.rgpt.templateutil;

import java.io.Serializable;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.util.Vector;
import java.util.HashMap;

public class TemplateInfo implements Serializable
{
   static final long serialVersionUID = 1785169276972210137L;
   
   // This indicates the version of the Template Data. This is assigned from 
   // PDF Page Info Object.
   public long m_PDFPageInfoVersion;
   
   // This values are populated by the Web Server when the Template is uploaded
   // to Server
   public int m_TemplateId;
   public int m_ServiceIdentifier;
   public int m_ProductId;
   public String m_TemplateName;
   public String m_TemplateDesc;
   public String m_TemplateFile; 
   public String m_TemplateSourceDir; 
   
   // The following data are populated in database by the Server Controller during 
   // the saving the template
   public int m_PageCount;
   public String m_ThumbImageFile;
   public String m_EnlargedImageFile;
   public String m_CSVFile;
   public String m_SerializedDataFile;
   public String m_SerializedFontFile;
   public boolean m_IsZipUploadReq;
   
   // This fields identify number of VDP Text and Image Fields defined for this 
   // Template
   public int m_VDPTextFields = 0;
   public int m_VDPImageFields = 0;
   
   // The m_TemplatePageData and m_TemplateFontData are serialized into 
   // m_SerializedDataFile
   // The Template Page Data holds the data for every page like the VDP Data, Image, etc 
   public HashMap m_TemplatePageData;
   // The Template Font Data holds all the Embedded font in the PDF.
   //HashMap m_TemplateFontData;

   public Vector<Integer> getPageWithNoVDPField()
   {
      if (m_TemplatePageData == null) 
         throw new RuntimeException("Template Page Not Specified");
      TemplatePageData tempPageData = null;
      Vector<Integer> pageWithNoVDPData = new Vector<Integer>();
      for (int i = 0; i < m_TemplatePageData.size(); i++)
      {
         int pgNum = i+1;
         tempPageData = (TemplatePageData) m_TemplatePageData.get(pgNum);
         if (tempPageData.m_VDPTextFieldData.size() > 0 || 
             tempPageData.m_VDPImageFieldData.size() > 0) continue;
         
         // No VDP Field is defined for this page hence adding to Vector;
         pageWithNoVDPData.addElement(pgNum);
      }
      return pageWithNoVDPData;
   }
   
   //Serialization
   public void save(ObjectOutputStream objstream) throws IOException 
   {
      objstream.writeObject(this);
   }
   
   //De-Serialization
   public static TemplateInfo load(ObjectInputStream objstream) 
   {
      TemplateInfo templateInfo = null;
      try
      {
         System.out.println("In Load Method: " + objstream.available()); 
         templateInfo = (TemplateInfo) objstream.readObject();
         StringBuffer errMsg = new StringBuffer("Need to regenerate Template. ");
         if (!templateInfo.checkTemplateValid(errMsg)) 
            throw new RuntimeException(errMsg.toString());
         System.out.println("Retrieved TemplateInfo " + templateInfo);
         //objstream.close();
      }
      catch(Exception ex)
      {
         System.out.println("Unable to Load Template Info: " + ex.getMessage());
         ex.printStackTrace();
         throw new RuntimeException("Unable to Load Template Info.");
      }
      finally
      {
         try {
            objstream.close();
         } catch(Exception ex) {} 
      }
      return templateInfo;
   }
   
   public boolean checkTemplateValid(StringBuffer errMsg)
   {
      boolean isTempValid = true;
      if (m_PDFPageInfoVersion != PDFPageInfo.serialVersionUID) {
         errMsg.append("PDFPageInfo Versions not matching.");
         return false;
      }
      for (int pgNum = 1; pgNum <= m_PageCount; pgNum++) 
      {
         Object obj = m_TemplatePageData.get(new Integer(pgNum)); 
         if (obj == null) {
            errMsg.append("TemplatePageData not populated.");
            isTempValid = false;
            break;
         }
         TemplatePageData pageData = (TemplatePageData) obj;
         System.out.println("TEMPLATE Page INFO: " + pageData.toString());
         if (!pageData.isVDPFieldPopulated()) {
            errMsg.append("TemplatePageData not fully populated.");
            isTempValid = false;
            break;
         }
      }
      return isTempValid;
   }
   
   public String toString()
   {
      StringBuffer mesg = new StringBuffer("\nTemplate Metadata");
      mesg.append("\n Template Id: " + m_TemplateId);
      mesg.append(" PSP Id: " + m_ServiceIdentifier);
      mesg.append(" Product Id: " + m_ProductId);
      mesg.append(" Template Name: " + m_TemplateName);
      mesg.append(" Template Desc: " + m_TemplateDesc);
      mesg.append(" Num VDP Text: " + m_VDPTextFields);
      mesg.append(" Num VDP Image: " + m_VDPImageFields);
      mesg.append(" Page Count: " + m_PageCount);
      mesg.append("\nTemplate Source Dir: " + m_TemplateSourceDir);
      mesg.append(" Template File Name: " + m_TemplateFile);
      mesg.append(" Thumb Image File Name: " + m_ThumbImageFile);
      mesg.append(" Enlarged Image File Name: " + m_EnlargedImageFile);
      mesg.append(" Seriaized File Name: " + m_SerializedDataFile);
      mesg.append(" Seriaized Font File Name: " + m_SerializedFontFile);
      if (m_TemplatePageData != null) 
         mesg.append("\nTemplate Page Data: " + m_TemplatePageData);
      else mesg.append(" Template Page Data is undefined");
      return mesg.toString();
   }
}

