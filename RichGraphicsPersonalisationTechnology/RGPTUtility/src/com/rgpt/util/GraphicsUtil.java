package com.rgpt.util;
import java.awt.*;
import java.awt.image.*;
import javax.swing.*;

public class GraphicsUtil
{
   public static void drawImage(Graphics2D g2d, BufferedImage image, Rectangle dispArea)
   {
      int wt = image.getWidth();
      int ht = image.getHeight();
      int x = dispArea.x+(int)((dispArea.width-wt)/2);
      int y = dispArea.y+(int)((dispArea.height-ht)/2);
      g2d.drawImage(image, x, y, wt, ht, null);
   }
   
}
