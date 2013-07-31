// RGPT PACKAGES
package com.rgpt.serverhandler;

import java.io.Serializable;
import java.util.Vector;

import com.rgpt.util.RGPTRectangle;

// This is to request Asset from the Server. If Asset Id is provided then server 
// returns the Asset corresponding to Id or can return all the asset pertaining 
// to the user from the server
public class CustomerAssetRequest extends ServerRequest implements Serializable
{
   public static final String m_RequestType = "CustomerAssetRequest";
   
   public static int THUMBVIEW_IMAGE = 1;
   public static int REGULAR_IMAGE = 2;
   
   public static String PDF_ASSET_TYPE = "PDF";
   public static String IMAGE_ASSET_TYPE = "IMAGE";
   public static String FONT_ASSET_TYPE = "FONT";
   
   // This is used to retrieve Customer Uploaded Assets
   public int m_CustomerId;
   
   // This is to retrieve Theme Assets uploaded by Service Provider and used 
   // by the Customer
   public int m_ThemeId = -1;
   
   // This specifies the Pages requested from the Server. The PDF Page is always
   // returned as Image
   public Vector m_PDFPages;
   
   // Default Asset Id is set to -1
   public int m_AssetId = -1;
   
   // Default Template  Id is set to -1
   public int m_TemplateId = -1;
   
   // Either PDF or IMAGE
   public String m_AssetType;
   
   // This shows the Image BBox of the Regular Image
   public RGPTRectangle m_ImageBBox;
   
   // This takes the value THUMBVIEW_IMAGE or REGULAR_IMAGE if the 
   // Asset Type is IMAGE
   public int m_ImageSizeType;
   
   public CustomerAssetRequest() 
   {
      // This is just the non populated value used for Image Asset Type
      m_PDFPages = new Vector();
   }
   
   public String toString()
   {
      StringBuffer mesg = new StringBuffer();
      mesg.append(" REQUEST TYPE: " + m_RequestType);
      mesg.append(" Customer ID: " + m_CustomerId);
      mesg.append(" TemplateId: " + m_TemplateId);
      mesg.append(" ThemeId: " + m_ThemeId);
      mesg.append(" AssetId: " + m_AssetId);
      mesg.append(" Asset Type: " + m_AssetType);
      if (m_AssetType.equals(IMAGE_ASSET_TYPE))
      {
         mesg.append(" Asset Type: Image");
         // Default is Thunbview Image
         String imgSizeType = "Thumbview Image";
         if (m_ImageSizeType == REGULAR_IMAGE)
            imgSizeType = "Regular Image";
         mesg.append(" Image Size Type: " + imgSizeType);
         if (m_ImageBBox != null)
            mesg.append(" Image BBox: " + m_ImageBBox.getRectangle2D().toString());
      }
      else if (m_PDFPages != null)
         mesg.append(" PDF Pages: " + m_PDFPages.toString());
      return mesg.toString();
   }
}
