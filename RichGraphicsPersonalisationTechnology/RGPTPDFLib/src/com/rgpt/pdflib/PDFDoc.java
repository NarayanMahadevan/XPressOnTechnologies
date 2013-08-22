package com.rgpt.pdflib;

/**
 * 
 * This is an abstract class for the PDF Document. The implementation class will
 * correspond to the PDF Library and will implement all the abstract methods and
 * will maintain reference to the actual PDFDoc object
 * 
 * @author Narayan
 * 
 */

public abstract class PDFDoc {

	/**
	 * Save Options used while saving PDF Document. The Save Options are
	 * IncrementalSave - Saves the document using incremental mode. FullSave -
	 * Saves the document using full mode. SaveToRemoveUnusedElem - Remove
	 * unused objects. QuickSave - Save the document in linearized (fast
	 * web-view) format.
	 * 
	 */
	public enum SaveOptions {
		IncrementalSave, FullSave, SaveToRemoveUnusedElem, QuickSave
	}

	/**
	 * Gets the PDF Doc Object as per PDF Library
	 */
	public abstract Object getPDFDoc();

	/**
	 * Checks if any authorization is required to view the document. This also
	 * initializes the Security Handler of the actual PDF Doc
	 * 
	 * @return returns true if the security handler of the document is
	 *         initialized or else valid password is needed to check if the user
	 *         is authorized to view.
	 * @throws PDFLibException
	 */
	public abstract boolean initSecurityHandler() throws PDFLibException;

	/**
	 * Checks with PDF Document Security Handler if the password is valid and
	 * the user is authorized to view the document
	 * 
	 * @param password
	 *            password for authorization
	 * @return
	 * @throws PDFLibException
	 */
	public abstract boolean isAuthorizedToView(String password)
			throws PDFLibException;

	/**
	 * Get the page object from given page number
	 * 
	 * @param pageNum
	 *            the page number in document's page sequence.
	 * @return a Page corresponding to a given page number, or null
	 * @throws PDFLibException
	 */
	public abstract PDFPage getPDFPage(int pageNum) throws PDFLibException;

	/**
	 * Saves the document to a file. If a full save is requested to the original
	 * path, the file is saved to a file system-determined temporary file, the
	 * old file is deleted, and the temporary file is renamed to path. A full
	 * save with remove unused or linearization option may re-arrange object in
	 * the cross reference table.
	 * 
	 * @param path
	 *            The full path name to which the file is saved.
	 * @param options
	 *            Options used for Saving. Each options leads to multiple
	 *            operation while saving.
	 * @param progress
	 *            The Progress notified by PDF Lib to be shown to end user
	 * @throws PDFLibException
	 */
	public abstract void save(String path, SaveOptions options,
			ProgressInfoListener progress) throws PDFLibException;
}
