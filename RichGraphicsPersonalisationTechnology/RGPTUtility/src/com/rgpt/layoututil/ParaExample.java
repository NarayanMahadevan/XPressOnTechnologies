package com.rgpt.layoututil;

import java.awt.*;

import javax.swing.*;

public class ParaExample extends JFrame{
	
	private static final long serialVersionUID = 1L;
	
	JToolBar jTbar;
	
	public ParaExample(){
		
		JFrame jf = new JFrame("paraExample");
		jf.setSize(600,500);
		jf.setVisible(true);
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	
			
		JPanel jp = new JPanel();
				
			
		JButton jB1 = new JButton("first");
		JButton jB2 = new JButton("second");
		JButton jB3 = new JButton("third");
		JButton jB4 = new JButton("fourth");
		JButton jB5 = new JButton("fifth");
		JButton jB6 = new JButton("sixth");
		JButton jB7 = new JButton("seventh");
		JButton jB8 = new JButton("eighth");
		JTextField jTf1 = new JTextField(5);
		JTextField jTf2 = new JTextField(10);
		JTextArea jTa = new JTextArea(5,9);
		
		jp.setLayout(new ParagraphLayout());
		
		
		jB2.setFont(new Font("serif", Font.PLAIN, 24));
		jp.add(new JLabel("Some buttons:"), ParagraphLayout.NEW_PARAGRAPH);
		jp.add(jB1);
		jp.add(new JLabel("A long label:"), ParagraphLayout.NEW_PARAGRAPH);
		jp.add(jB2);
		jp.add(jB3);
		jp.add(new JLabel("Short label:"), ParagraphLayout.NEW_PARAGRAPH);
		jp.add(jB4);
		jp.add(jB5, ParagraphLayout.NEW_LINE);
		jp.add(jB6);
		jp.add(jB7);
		jp.add(jB8, ParagraphLayout.NEW_LINE);
		jp.add(new JLabel("Text:"), ParagraphLayout.NEW_PARAGRAPH);
		jp.add(jTf1);
		jp.add(new JLabel("More text:"), ParagraphLayout.NEW_PARAGRAPH);
		jp.add(jTf2);
		jp.add(new JLabel("miles"));
		jp.add(new JLabel("A text area:"), ParagraphLayout.NEW_PARAGRAPH_TOP);
		jp.add(jTa);
		jf.getContentPane().add(jp,BorderLayout.CENTER);
	}
	
	public static void main(String[] args){
		
		new ParaExample();
	}

}
