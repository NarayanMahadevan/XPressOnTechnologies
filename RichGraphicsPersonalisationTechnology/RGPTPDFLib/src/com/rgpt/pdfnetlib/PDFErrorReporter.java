package com.rgpt.pdfnetlib;

import pdftron.PDF.ErrorReportProc;

import com.rgpt.pdflib.PDFErrorReportListener;

/**
 * RGPT Library
 * 
 * This class listens to the error reported in PDFNet Library and invokes the
 * PDFErrorReportListener to correspondingly notify the error
 * 
 * @author Narayan
 * 
 */

public class PDFErrorReporter implements ErrorReportProc {

	PDFErrorReportListener m_PDFErrorReportListener;

	public PDFErrorReporter(PDFErrorReportListener listener) {
		m_PDFErrorReportListener = listener;
	}

	@Override
	public void reportError(String message, Object data) {
		m_PDFErrorReportListener.reportError(message, data);
	}

}
