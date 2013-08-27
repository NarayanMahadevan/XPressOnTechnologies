package com.rgpt.pdflib;

import javax.swing.JPanel;

public interface PDFView {

	public enum VDPMode {
		NULL, MARK_TEXT, MARK_IMAGE, INSERT_TEXT_ALONG_PATH, INSERT_TEXT
	}

	/**
	 * This method returns the view pane of the PDF to be displayed on the
	 * Template Maker
	 * 
	 * @return The Component for display
	 */
	public JPanel getViewPanel();

	/**
	 * Sets the PDF Handler. This is mainly to register the PDF View with the
	 * handler
	 * 
	 * @param hdlr
	 */
	public void setPPFHandler(PDFViewHandler hdlr);

	/**
	 * Setting the PDF Element Selection Hnadler...
	 * 
	 * @param selHdlr
	 */
	public void setPDFElemSelHandler(PDFElemSelHandler selHdlr);

	/**
	 * Sets the VDP Mode Selected
	 * 
	 * @param vdpMode
	 */
	public void setVDPMode(VDPMode vdpMode);

	public void close();

}
