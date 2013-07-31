// RGPT PACKAGES
package com.rgpt.util;

import java.awt.*;
import javax.swing.*;

public class RGPTLookAndFeel
{
   public static Color getBackgroundColor()
   {
      LookAndFeel laf = UIManager.getLookAndFeel();
      UIDefaults uid = laf.getDefaults();
      Color col = uid.getColor("EaSynth.internalframe.main.color");
      return col;
   }
   
}
