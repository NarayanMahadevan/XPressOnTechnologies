// RGPT PACKAGES
package com.rgpt.viewer;

import javax.swing.*;

import java.util.Map;
import java.util.HashMap;

import javax.swing.JPanel;

import com.rgpt.viewer.BasicPDFViewerTopPane;

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;

public class BasicPDFViewer extends PDFViewer 
{
   //public static String TOP_PANE_BACKGROUND_IMAGE = "classic.jpg";
   public static String TOP_PANE_BACKGROUND_IMAGE = "";
   //public static String CENTER_PANE_BACKGROUND_IMAGE = "classic.jpg";
   public static String CENTER_PANE_BACKGROUND_IMAGE = "";
   
   protected BasicPDFViewer(PDFPageHandler pdfPgHdlr)
	{
      super(pdfPgHdlr, PDFPageHandler.THUMBVIEW_IMAGE_PANE, 
            CENTER_PANE_BACKGROUND_IMAGE);
      this.createPDFViewUI();
      this.revalidate();
      this.setVisible(true);
      repaint();
      final BufferedImage progBarImg = getPDFViewerImage(PROGRESS_BAR_IMAGE);
      m_PDFPageHandler.setProgBarImage(progBarImg);
      SwingUtilities.invokeLater(new Runnable() 
      {
         public void run() {
            initPageHandler(PDFPageHandler.THUMBVIEW_IMAGE_PANE, 
                            CENTER_PANE_BACKGROUND_IMAGE, progBarImg);
         }
      });
   }
   
   private void createPDFViewUI()
   {
      // Settting the Layout of PDFView to BorderLayout
      this.setLayout(new BorderLayout());

      // Get the Background Image for Top Pane
      BufferedImage backGrdImg = getPDFViewerImage(TOP_PANE_BACKGROUND_IMAGE);
      this.add(new BasicPDFViewerTopPane(backGrdImg), BorderLayout.NORTH);
      
      backGrdImg = getPDFViewerImage(CENTER_PANE_BACKGROUND_IMAGE);
      JPanel contentPane = m_PDFPageHandler.createContentPane();
		this.add(contentPane,BorderLayout.CENTER);
   }
}
