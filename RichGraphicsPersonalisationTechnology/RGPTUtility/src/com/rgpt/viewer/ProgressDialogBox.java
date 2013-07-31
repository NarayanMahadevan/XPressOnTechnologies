// RGPT PACKAGES
package com.rgpt.viewer;

//import javax.swing.JInternalFrame;
import java.awt.*;
import javax.swing.*;
import java.net.URL;
import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractAction;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ProgressDialogBox extends JDialog implements Runnable
{
   // To send Approval Request to Server
   String m_ProgressMessage; 
   //BufferedImage m_ProgressBarImage;
   //private JDialog m_ProgressMonitorBox; 
   
   public ProgressDialogBox(String mesg)
   {
      super(new java.awt.Frame(), mesg, true);
      m_ProgressMessage = mesg;
		this.createProgressMonitor();
   }
   
   URL m_ResourcePath;
   private void createProgressMonitor()
   {
      String imgLocation = "/res/progress_bar.gif";
      //String imgLocation = "/res/Clock.gif";
      int locx = 200, locy = 200, sizex = 300, sizey = 300;
      Dimension screenSize = null;
      int res = this.getScreenInfo(screenSize);
      if (screenSize != null)
      {
         locx = (int) (screenSize.getWidth()/2);
         locy = (int) (screenSize.getHeight()/2);
      }
      try
      {
         m_ResourcePath = this.getClass().getResource(imgLocation);
         System.out.println("Resource Path: " + m_ResourcePath.toString()); 
         this.displayImage(locx, locy, sizex, sizey);
      }
      catch (Exception ex)
      {
         ex.printStackTrace();
      }
   }

   ImageIcon m_DisplayImage;
   JLabel m_DisplayLabel;
   private void displayImage(int locx, int locy, int sizex, int sizey)
   {
      JPanel imgPanel = new JPanel(new BorderLayout());
      this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
      this.setLocation(locx,locy);
      this.setSize(sizex,sizey);
      this.setContentPane(imgPanel);
      m_DisplayImage = new ImageIcon(m_ResourcePath, m_ProgressMessage);
      m_DisplayLabel = new JLabel(m_DisplayImage);
      this.add(m_DisplayLabel, BorderLayout.CENTER);
   }
   
   public int getScreenInfo(Dimension screenSize)
   {
      int res = 0;
      try {
         Toolkit toolkit = Toolkit.getDefaultToolkit();
         screenSize = toolkit.getScreenSize();
         res = toolkit.getScreenResolution();
         System.out.println("Res: " + res + " Screen Size: " + screenSize.toString());
      }
      catch (Exception ex)
      {
         ex.printStackTrace();
      }
      return res;
   }
   
   
   public void startProgressMonitor() 
   {
      /* Construct an instance of Thread, passing the current class (i.e. the Runnable) as an argument. */
      Thread t = new Thread(this);
      t.setPriority(Thread.MAX_PRIORITY);
      t.start(); // Calls back to run method in "this"
      //m_DisplayLabel.setIcon(m_DisplayImage);
      //this.setVisible(true);
      //repaint();
   }    
   
   public void stopProgressMonitor() 
   {
      this.setVisible(false);
   }    
   
   public void run() 
   {
      //this.createProgressMonitor();
      this.setVisible(true);
/*      
      Graphics g = this.getGraphics();
      //m_ProgressMonitorBox.update(g);
      Dimension visibleRect = this.getSize();
      g.clearRect(0,0, (int)visibleRect.getWidth(), 
                  (int)visibleRect.getHeight());
      m_DisplayLabel.setIcon(m_DisplayImage);
      repaint();
*/      
   }
   
   public static void main(String[] args)
   {
      ProgressDialogBox progressMonitorBox;
      String mesg = "Creating Personalized PDF";
      progressMonitorBox = new ProgressDialogBox(mesg);
      progressMonitorBox.startProgressMonitor();
   }
}
