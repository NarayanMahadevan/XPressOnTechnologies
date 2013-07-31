// RGPT PACKAGES
package com.rgpt.util;

// This files are added to support serialization
import java.io.Serializable;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

// Since Rectangle2D is not Serializable, this class is temporarily introduced
public class RGPTRectangle implements Serializable
{
   public static final long serialVersionUID = -7430480942626079901L;
   public double x, y, width, height;
   
   public RGPTRectangle() {}
   
   public RGPTRectangle(RGPTRectangle rect) 
   {
      x = rect.x;
      y = rect.y;
      width = rect.width;
      height = rect.height;
   }
   
   public static RGPTRectangle getReactangle(Rectangle2D.Double rect2D)
   {
      RGPTRectangle zRect = new RGPTRectangle();
      zRect.x = rect2D.getX();
      zRect.y = rect2D.getY();
      zRect.width = rect2D.getWidth();
      zRect.height = rect2D.getHeight();
      return zRect;
   }
   
   public boolean contains(double x, double y)
   {
      return getRectangle2D().contains(x, y);
   }
   
   public boolean equals(RGPTRectangle bbox)
   {
      boolean res = getRectangle2D().equals(bbox.getRectangle2D());
      System.out.println("Is The 2 BBox Equal " + res);
      return res;
   }
   
   public boolean contains(RGPTRectangle bbox)
   {
      System.out.println("\n\n****************\n\n");
      System.out.println("This BBox: " + this.toString());
      System.out.println("Contained BBox: " + bbox.toString());
      boolean res = getRectangle2D().contains(bbox.getRectangle2D());
      System.out.println("Result is : " + res);
      System.out.println("\n\n****************\n");
      return res;
   }
   
   public Rectangle getRectangle()
   {
      return RGPTUtil.getRectangle(getRectangle2D()); 
   }
   
   public Rectangle2D.Double getRectangle2D()
   {
      return new Rectangle2D.Double(x, y, width, height); 
   }
   
   //Serialization
   public void save(ObjectOutputStream objstream) throws IOException 
   {
      objstream.writeObject(this);
   }
   
   //De-Serialization
   public static Object load(ObjectInputStream objstream ) throws Exception 
   {
      RGPTRectangle zRect = (RGPTRectangle) objstream.readObject();
      return zRect.getRectangle2D();
   }
   
   public String toString()
   {
      return this.getRectangle2D().toString();
   }
   
}
