package com.rgpt.util;

import java.awt.*;
import javax.swing.*;
import java.util.*;


public class RGPTListCellRenderer extends JLabel implements ListCellRenderer 
{
   private static final long serialVersionUID = 1L;
   public String[] m_Labels = null;
   public Map<String, String> m_LabelImageIconMap = null;
   int m_CellWidth = 0, m_CellHeight = 0;
   
   public RGPTListCellRenderer(Map<String, String> labelImagesIconMap, int wt, int ht) 
   {
      this(labelImagesIconMap.keySet().toArray(new String[0]), wt, ht);
      m_LabelImageIconMap = labelImagesIconMap;
   }
   
   public RGPTListCellRenderer(String[] labels, int wt, int ht) 
   {
      m_Labels = labels; m_CellWidth = wt; m_CellHeight = ht;
   }
   
   // This method finds the image and text corresponding to the selected
   // value and returns the label, set up to display the text and image.    
   public Component 
      getListCellRendererComponent(JList list, Object value, int index, 
                                   boolean isSelected, boolean cellHasFocus) 
   {
      // Get the selected index. (The index param isn't
      // always valid, so just use the value.)
      String selItem = (String) value;

      // Set the icon and text. If icon was null, say so.
      String imageFile = null;
      if (m_LabelImageIconMap != null && m_LabelImageIconMap.size() > 0) {
         imageFile = m_LabelImageIconMap.get(selItem);
         if (imageFile != null) {
            ImageIcon icon = RGPTUIUtil.getImageIcon(imageFile);
            setIcon(icon);
         }
      }
      RGPTUIUtil.setLabelText(this, "  "+selItem, m_CellWidth, "LabelFontSize");
      // setBorder(BorderFactory.createEmptyBorder(1,5,1,1));
      setBorder(BorderFactory.createMatteBorder(1, 5, 1, 1, RGPTUIManager.BG_COLOR));
      setOpaque(true);
      setHorizontalAlignment(LEFT);
      setVerticalAlignment(CENTER);
      return this;
   }
}
