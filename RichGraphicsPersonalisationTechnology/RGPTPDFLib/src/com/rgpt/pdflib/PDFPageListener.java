package com.rgpt.pdflib;

/**
 * RGPT Library
 * 
 * This interface is the notification event to report the current page change
 * 
 * @author Narayan
 * 
 */
public interface PDFPageListener {
	public void reportCurrentPage(int current_page, int num_pages,
			java.lang.Object data);
}
