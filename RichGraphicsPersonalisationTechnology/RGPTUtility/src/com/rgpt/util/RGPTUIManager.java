// RGPT PACKAGES
package com.rgpt.util;

import java.awt.Color;

import javax.swing.*;


public class RGPTUIManager 
{
   static Color basecolor=Color.WHITE ;
   public static Color PANEL_COLOR;
   public static Color BG_COLOR;
   public static Color DARK_COLOR;
   
   public static void setLookAndFeel()
   {
      
      try 
      {
         UIManager.getCrossPlatformLookAndFeelClassName();  
         UIManager.put("activeCaption", new javax.swing.plaf.ColorUIResource(basecolor));   
         JFrame.setDefaultLookAndFeelDecorated(true);
         UIManager.put("activeCaption", new javax.swing.plaf.ColorUIResource(basecolor));   
         JDialog.setDefaultLookAndFeelDecorated(true);	
         //UIManager.getSystemLookAndFeelClassName();
      } 
      catch (Exception ex) {
         System.out.println("UI Manager Abort");
      }
   }
   
	public static void setUIDefaults(Color colorpanel, Color colorbg, Color colordark)
	{
      PANEL_COLOR = colorpanel;
      DARK_COLOR = colordark;
      BG_COLOR = colorbg;
      Color textColor = Color.BLACK;
      UIManager.put("OptionPane.background",colorpanel);  

      UIManager.put("MenuBar.borderColor",colordark);  
      UIManager.put("MenuBar.background",colorpanel);  
      UIManager.put("Menu.selectionBackground",colorbg);  
      UIManager.put("Menu.background",colorpanel);  
      UIManager.put("MenuItem.selectionBackground",colorbg);  
      UIManager.put("MenuItem.background",colorpanel);  

      UIManager.put("Panel.background",colorpanel);  
      UIManager.put("Panel.foreground",textColor);  
      
      UIManager.put("Button.background",colorbg);  
      UIManager.put("Button.foreground",textColor);  
      UIManager.put("Button.select",colorpanel);  

      UIManager.put("ToggleButton.select",colorpanel);  

      UIManager.put("ScrollBar.background", colorpanel);	 
      UIManager.put("ScrollBar.track", colordark);
      UIManager.put("ScrollBar.trackHighlight", colorbg);
      UIManager.put("ScrollBar.foreground", textColor);
      UIManager.put("ScrollBar.darkShadow", colorpanel);
      UIManager.put("ScrollBar.highlight", colordark);
      UIManager.put("ScrollBar.shadow", colorbg);
      UIManager.put("ScrollBar.thumb", colorbg);
      UIManager.put("ScrollBar.thumbDarkShadow", colordark);
      UIManager.put("ScrollBar.thumbHighlight", colordark);
      UIManager.put("ScrollBar.thumbShadow", colordark);
      UIManager.put("ScrollBar.width", AppletParameters.getIntVal("ScrollBarWidth"));
      UIManager.put("ScrollBar.hright", AppletParameters.getIntVal("ScrollBarHeight"));

      UIManager.put("ScrollPane.background", colorpanel);
      UIManager.put("ScrollPane.foreground", textColor);

      UIManager.put("ToolBar.borderColor", colorbg);
      UIManager.put("ToolBar.background", colorpanel);

      UIManager.put("TextField.selectionBackground", colorbg);
      UIManager.put("TextField.background", colorbg);
      UIManager.put("TextField.highlight", colorbg);

      UIManager.put("Tree.selectionForeground", textColor);
      UIManager.put("Tree.selectionBackground", colorbg);
      UIManager.put("Tree.Background", colorpanel);

      UIManager.put("List.selectionBackground", colorbg);

      UIManager.put("TabbedPane.selectHighlight", colorbg);
      UIManager.put("TabbedPane.selected", colorbg);
      UIManager.put("TabbedPane.shadow", colorbg);
      UIManager.put("TabbedPane.tabAreaColor", colorpanel);
      UIManager.put("TabbedPane.borderHighlightColor", colordark);

      UIManager.put("ComboBox.buttonBackground", colorbg);
      UIManager.put("ComboBox.selectionBackground", colorbg);
      UIManager.put("ComboBox.background", colorbg);
      UIManager.put("ComboBox.foreground", textColor);
      UIManager.put("ComboBox.selectionForeground", textColor);

      UIManager.put("Spinner.background", colorpanel);
      UIManager.put("Spinner.foreground", textColor);
      
      UIManager.put("Table.background", colorpanel);
      UIManager.put("Table.foreground", textColor);
      
      UIManager.put("RadioButton.background", colorpanel);
      UIManager.put("RadioButton.foreground", textColor);

      UIManager.put("ProgressBar.background", colorpanel);
      UIManager.put("ProgressBar.foreground", textColor);

      UIManager.put("ToolTip.background", colorbg);

      UIManager.put("RadioButtonMenuItem.background", colorpanel);

      UIManager.put("CheckBoxMenuItem.background", colorpanel);

      UIManager.put("ComboBox.buttonBackground", colorbg);

      UIManager.put("Slider.background", colorbg);
      UIManager.put("Slider.foreground", colorbg);
      UIManager.put("Slider.altTrackColor", colordark);

      UIManager.put("CheckBox.background", colorbg);
      UIManager.put("CheckBoxMenuItem.background", colorpanel);

      UIManager.put("ColorChooser.background", colorbg);
      UIManager.put("ColorChooser.foreground", textColor);
      UIManager.put("ColorChooser.swatchesDefaultRecentColor", Color.GRAY);

      UIManager.put("Separator.background", colorpanel);
      UIManager.put("Separator.foreground", textColor);

      UIManager.put("PopupMenu.background", colorbg);
      UIManager.put("PopupMenu.foreground", textColor);

      UIManager.put("SplitPane.dividerFocusColor", colordark);
      UIManager.put("SplitPaneDivider.draggingColor", colorbg);
      UIManager.put("SplitPane.background", colorpanel);
      UIManager.put("Dialog.background", colorpanel);
      UIManager.put("Dialog.foreground", textColor);
      
      // Setting Fonts on UI
      // Font defaultFont = new Font("Arial", Font.BOLD, 10);
      // UIManager.put("Tooltip.font", defaultFont);
      
//		 SwingUtilities.updateComponentTreeUI(this);
   }

}