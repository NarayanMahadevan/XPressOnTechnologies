package com.rgpt.templatemaker;

import java.awt.BorderLayout;
import java.util.Map;

import javax.swing.JPanel;

import com.rgpt.pdflib.PDFDrawHandler;
import com.rgpt.pdflib.PDFViewHandler;

public class PDFViewer extends JPanel {

	private static final long serialVersionUID = 1L;

	// public JPanel m_PDFViewPane;
	// public PDFViewHandler m_PDFViewHandler;
	// public PDFLibConnector m_PDFLibConnector;

	public PDFDrawHandler m_PrintRenderer = null;
	public Map<Integer, Boolean> m_UpdatePageGraphics;

	PDFViewController m_PDFViewController;

	public PDFViewer(PDFViewController vwCntr) {
		m_PDFViewController = vwCntr;
		// addMouseListener(this);
		// addMouseMotionListener(this);
		this.setLayout(new BorderLayout(10, 10));
	}

	public boolean setPDFViewHandler(PDFViewHandler viewHdlr) {
		// m_PDFViewHandler = viewHdlr;
		// m_PDFViewPane = viewHdlr.getViewPanel();
		this.add(viewHdlr.getViewPanel(), BorderLayout.CENTER);
		this.validate();
		return true;
	}

	public void close() {
		cleanUpMemory();
		// m_PDFViewHandler = null;
	}

	private void cleanUpMemory() {

	}
}
