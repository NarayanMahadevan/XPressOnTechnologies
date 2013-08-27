package com.rgpt.pdfnetlib;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JPanel;

import pdftron.PDF.PDFViewCtrl;

import com.rgpt.pdflib.PDFElemSelHandler;
import com.rgpt.pdflib.PDFView;
import com.rgpt.pdflib.PDFViewHandler;
import com.rgpt.util.RGPTLogger;

public class PDFNetViewer extends PDFViewCtrl implements PDFView,
		MouseListener, MouseMotionListener {

	private static final long serialVersionUID = 1L;

	VDPMode m_VDPMode;
	PDFNetViewController m_PDFViewHandler;

	// This holds Element Selection Handler for the page
	PDFElemSelHandler m_PDFElemSelHandler;

	public PDFNetViewer() {
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
	}

	@Override
	public void setPPFHandler(PDFViewHandler hdlr) {
		m_PDFViewHandler = (PDFNetViewController) hdlr;
		m_PDFViewHandler.setPDFViewCtrl(this);
	}

	@Override
	public void setPDFElemSelHandler(PDFElemSelHandler selHdlr) {
		m_PDFElemSelHandler = selHdlr;
	}

	@Override
	public JPanel getViewPanel() {
		return this.getViewPane();
	}

	@Override
	public void setVDPMode(VDPMode vdpMode) {
		m_VDPMode = vdpMode;
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseClicked(MouseEvent event) {
		int x = event.getX();
		int y = event.getY();
		RGPTLogger.logDebugMesg("x: " + x + " y: " + y);
		if (m_VDPMode.equals(VDPMode.MARK_IMAGE))
			m_PDFElemSelHandler.selectImageElements(x, y);
		// displayallselection(pgno);
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void close() {
		m_PDFViewHandler = null;
		m_PDFElemSelHandler = null;
	}

}
