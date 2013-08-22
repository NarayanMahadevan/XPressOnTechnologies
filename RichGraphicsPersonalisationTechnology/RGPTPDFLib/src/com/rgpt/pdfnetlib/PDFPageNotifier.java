package com.rgpt.pdfnetlib;

import pdftron.PDF.CurrentPageProc;

import com.rgpt.pdflib.PDFPageListener;

public class PDFPageNotifier implements CurrentPageProc {

	PDFPageListener m_PDFPageListener;

	public PDFPageNotifier(PDFPageListener listener) {
		m_PDFPageListener = listener;
	}

	@Override
	public void reportCurrentPage(int current_page, int num_pages, Object data) {
		m_PDFPageListener.reportCurrentPage(current_page, num_pages, data);
	}

}
