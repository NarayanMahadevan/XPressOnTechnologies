// RGPT PACKAGES
package com.rgpt.viewer;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.border.Border;

import com.rgpt.serverhandler.PDFPageHandler;
import com.rgpt.util.RGPTUIUtil;

public class BasicPDFViewerTopPane extends javax.swing.JPanel implements
		MouseListener {
	private BufferedImage m_BackgroundImage;
	private JLabel m_ProofLabel, m_ApproveLabel;

	public BasicPDFViewerTopPane(BufferedImage backGrdImg) {
		m_BackgroundImage = backGrdImg;
		FlowLayout layout = new FlowLayout(FlowLayout.RIGHT);
		layout.setHgap(20);
		layout.setVgap(10);
		this.setLayout(layout);
		m_ProofLabel = RGPTUIUtil.createLabel("Proof",
				PDFPageHandler.IMAGE_PATH, "proof.gif", JLabel.CENTER,
				this.getClass());
		m_ProofLabel.addMouseListener(this);

		m_ApproveLabel = RGPTUIUtil.createLabel("Approve",
				PDFPageHandler.IMAGE_PATH, "approve.gif", JLabel.CENTER,
				this.getClass());
		m_ApproveLabel.addMouseListener(this);

		this.add(m_ProofLabel);
		this.add(m_ApproveLabel);
	}

	public void paintComponent(Graphics g) {
		if (m_BackgroundImage == null)
			return;
		Graphics2D g2d = (Graphics2D) g;
		Dimension panelSize = this.getSize();
		// System.out.println("PDFViewer TopPane Size: " +
		// this.getSize().toString());
		g2d.drawImage(m_BackgroundImage, 0, 0, panelSize.width,
				panelSize.height, this);
	}

	public void mouseClicked(MouseEvent e) {
		Border borderline = BorderFactory.createLineBorder(Color.BLACK);
		if (e.getSource() == m_ProofLabel) {
			m_ProofLabel.setBorder(borderline);
			PDFViewer.m_PDFPageHandler.showPDFProof();
		} else if (e.getSource() == m_ApproveLabel) {
			m_ApproveLabel.setBorder(borderline);
			PDFViewer.m_PDFPageHandler.approvePDF();
		}
	}

	public void mouseEntered(MouseEvent e) {
		Border borderline = BorderFactory.createLineBorder(Color.BLACK);
		if (e.getSource() == m_ProofLabel) {
			// m_ProofLabel.setBorder(borderline);
			this.setCursor(new Cursor(Cursor.HAND_CURSOR));
		} else if (e.getSource() == m_ApproveLabel) {
			// m_ApproveLabel.setBorder(borderline);
			this.setCursor(new Cursor(Cursor.HAND_CURSOR));
		}
	}

	public void mouseExited(MouseEvent e) {
		if (e.getSource() == m_ProofLabel) {
			m_ProofLabel.setBorder(null);
			this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

		} else if (e.getSource() == m_ApproveLabel) {
			m_ApproveLabel.setBorder(null);
			this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}

}
