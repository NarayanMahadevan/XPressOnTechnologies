// RGPT PACKAGES
package com.rgpt.util;

// This files are added to support serialization
import java.io.Serializable;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;

public class FontStreamHolder implements Serializable
{
   public long m_SDFObjNum;
   public byte[] m_FontStream;
   public String m_FontName;
   
   public FontStreamHolder(long sdfObjNum, byte[] fontStr, String fontName)
   {
      m_SDFObjNum = sdfObjNum;
      m_FontStream = fontStr;
      m_FontName = fontName;
   }

   public String toString()
   {
      StringBuffer mesg = new StringBuffer("FontStreamHolder: ");
      mesg.append("Font Name: " + m_FontName);
      mesg.append(" SDF Obj Num: " + m_SDFObjNum);
      mesg.append(" Font Size: " + m_FontStream.length);
      return mesg.toString();
   }
}
