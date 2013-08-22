package com.rgpt.templatemaker;

import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Map;

import javax.swing.JPanel;

import com.rgpt.pdflib.PDFDrawHandler;
import com.rgpt.pdflib.PDFElemSelHandler;
import com.rgpt.pdflib.PDFViewHandler;
import com.rgpt.util.StaticFieldInfo;

public class PDFViewer extends JPanel implements MouseListener,
		MouseMotionListener {

	private static final long serialVersionUID = 1L;

	// public JPanel m_PDFViewPane;
	// public PDFViewHandler m_PDFViewHandler;
	// public PDFLibConnector m_PDFLibConnector;

	public PDFDrawHandler m_PrintRenderer = null;
	public Map<Integer, Boolean> m_UpdatePageGraphics;

	// This holds Element Selection Handler for the page
	PDFElemSelHandler m_PDFElemSelHandler;

	PDFViewController m_PDFViewController;

	public PDFViewer(PDFViewController vwCntr) {
		m_PDFViewController = vwCntr;
		addMouseListener(this);
		addMouseMotionListener(this);
		this.setLayout(new BorderLayout(10, 10));
	}

	public boolean setPDFViewHandler(PDFViewHandler viewHdlr) {
		// m_PDFViewHandler = viewHdlr;
		// m_PDFViewPane = viewHdlr.getViewPanel();
		this.add(viewHdlr.getViewPanel(), BorderLayout.CENTER);
		this.validate();
		return true;
	}

	public void setPDFElemSelHandler(PDFElemSelHandler selHdlr) {
		m_PDFElemSelHandler = selHdlr;
	}

	public void close() {
		m_PDFElemSelHandler = null;
		cleanUpMemory();
		// m_PDFViewHandler = null;
	}

	private void cleanUpMemory() {

	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	/**
	 * This draws a rubberband rectangle, from the location where the mouse was
	 * first clicked to the location where the mouse is dragged.
	 */
	@Override
	public void mouseDragged(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	/**
	 * When the user presses the mouse, record the location of the top-left
	 * corner of rectangle.
	 */
	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	/**
	 * Erase the last rectangle when the user releases the mouse.
	 */
	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseClicked(MouseEvent event) {
		int x = event.getX();
		int y = event.getY();
		if (this.m_PDFViewController.m_VDPMode == StaticFieldInfo.MARK_IMAGE)
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

}
