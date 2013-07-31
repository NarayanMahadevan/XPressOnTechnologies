// RGPT PACKAGES
package com.rgpt.viewer;

import java.awt.*;
import java.lang.Thread;

import javax.swing.*; 
import javax.swing.border.Border;

import com.rgpt.util.ProgressInfo;

public class RGPTProgressBar extends JDialog 
{
   public JProgressBar m_ProgressBarUI;
   JTextArea out;
   ProgressInfo m_ProgressInfoHldr;
   JLabel m_TransferredByteLabel, m_TimeElapsedLabel, m_TimeLeftLabel, 
          m_ProgressRateLabel;  

   public RGPTProgressBar(ProgressInfo progInfo, String title) 
   {
      super(new JFrame(title));
      this.m_ProgressInfoHldr = progInfo;

      JPanel pane = new JPanel();
      m_ProgressBarUI = new JProgressBar(0, (int)m_ProgressInfoHldr.m_TotalNumOfBytes);
      m_ProgressBarUI.setValue(0);
      m_ProgressBarUI.setStringPainted(true);
      m_ProgressBarUI.setPreferredSize(new Dimension(400, 20));
      JPanel infoPane = createInfoPanel();
      infoPane.setLayout(new GridLayout(1,2,20,20));
      pane.add(m_ProgressBarUI);
      pane.add(infoPane);
      this.add(pane);
      this.setSize(450,150);
      this.setLocation(200,150);
   }
    
   public ProgressInfo getProgressInfo()
   {
      return m_ProgressInfoHldr;
   }
    
   public JPanel createInfoPanel()
   {
      JPanel panel = new JPanel();
      panel.setLayout(new GridLayout(1,2));
      panel.add(createLeftPane());
      panel.add(createRightPane());    	
      return panel;
   }

   private JPanel createLeftPane() 
   {
      JLabel text_transferred, timeelapsed, speed;  
      JPanel panel1 = new JPanel();
      panel1.setLayout(new GridLayout(3, 2, 3, 3));

      text_transferred = new JLabel("Transferred : ");
      m_TransferredByteLabel = new JLabel();
      m_TransferredByteLabel.setBackground(getBackground());
      m_TimeElapsedLabel = new JLabel();
      m_TimeElapsedLabel.setBackground(getBackground());

      timeelapsed = new JLabel("Time elapsed : ");
      speed = new JLabel("Speed : ");
      m_ProgressRateLabel = new JLabel(m_ProgressInfoHldr.m_TranferRate + " KB/sec   ");
      m_ProgressRateLabel.setBackground(getBackground());
      
      panel1.add(text_transferred);
      panel1.add(m_TransferredByteLabel);
      panel1.add(timeelapsed);
      panel1.add(m_TimeElapsedLabel);
      panel1.add(speed);
      panel1.add(m_ProgressRateLabel);
      
      return panel1;
	}
    
	public JPanel createRightPane()
	{
      JLabel text_total, text_total_value, timeleft;  
    	JPanel panel1 = new JPanel();
    	
    	panel1.setLayout(new GridLayout(3, 2, 3, 3));
    	text_total = new JLabel("Total :");
    	text_total.setHorizontalTextPosition(SwingUtilities.RIGHT);
    	
    	text_total_value = new JLabel();
    	text_total_value = new JLabel(m_ProgressInfoHldr.m_TotalNumOfBytes+" KB ");
    	text_total_value.setBackground(getBackground());

    	m_TimeLeftLabel = new JLabel();
    	m_TimeLeftLabel.setBackground(getBackground());
    	m_TimeLeftLabel.setHorizontalTextPosition(SwingUtilities.RIGHT);

    	timeleft = new JLabel("Time left : ");
    	timeleft.setBackground(getBackground());

    	panel1.add(text_total);
    	panel1.add(text_total_value);
    	panel1.add(timeleft);
    	panel1.add(m_TimeLeftLabel);
		
		return panel1;
	}

	public void setProgressValue()
   {
//		System.out.println(" ##### INSIDE SETPROGRESSVALUE #####");
      System.out.println("p_info.currentValue : "+m_ProgressInfoHldr.m_ProgressValue);
      m_ProgressBarUI.setValue(m_ProgressInfoHldr.m_ProgressValue);
      m_TransferredByteLabel.setText(m_ProgressInfoHldr.m_TranferredBytes +" KB    ");
      m_TimeElapsedLabel.setText(m_ProgressInfoHldr.m_TimeElapsed +" sec  ");
      m_TimeLeftLabel.setText(m_ProgressInfoHldr.m_TimeLeft +" sec  ");
      m_ProgressRateLabel.setText(m_ProgressInfoHldr.m_TranferRate + " KB/sec   ");
      this.repaint();
   }

} 