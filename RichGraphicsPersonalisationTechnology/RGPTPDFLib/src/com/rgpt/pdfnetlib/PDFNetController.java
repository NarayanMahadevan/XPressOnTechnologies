// RGPT PACKAGES
package com.rgpt.pdfnetlib;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import pdftron.Common.PDFNetException;
//import pdftron.Filters.FilterWriter;
//import pdftron.Filters.StdFile;
import pdftron.PDF.PDFDoc;
import pdftron.PDF.PDFDraw;
import pdftron.PDF.Page;
import pdftron.PDF.PageIterator;

import com.rgpt.serverhandler.PDFViewInterface;
import com.rgpt.serverutil.PDFPageInfoManager;
import com.rgpt.templateutil.PDFPageInfo;
import com.rgpt.templateutil.PDFPersonalizationUtil;
import com.rgpt.templateutil.TemplateInfo;
import com.rgpt.templateutil.UserPageData;
import com.rgpt.util.RGPTLogger;
import com.rgpt.util.RGPTRectangle;
import com.rgpt.util.RGPTUtil;
import com.rgpt.util.ScalingFactor;

public abstract class PDFNetController implements PDFViewInterface {
	// This holds all the PDF Page Information in a Document
	protected PDFPageInfoManager m_PDFPageInfoManager;

	// PDFDoc Object
	public PDFDoc m_PDFDocument;

	// This field is set to true if Personalized PDF is generated
	public boolean m_IsPersonalizedPDF;

	public PDFNetController() {
		m_IsPersonalizedPDF = false;
	}

	// This is for Clean-up Activity. When called this releases the Memory of
	// the
	// PDF Document to be Garbage Collected and Calls the PDF Info Manager to
	// cleanup Memory
	public void cleanUpMemory() {
		this.cleanUpMemory(false);
	}

	public void cleanUpMemory(boolean destroyPDFDraw) {
		if (destroyPDFDraw) {
			try {
				m_PDFDraw.destroy();
			} catch (Exception ex) {
			}
			m_PDFDraw = null;
		}
		if (m_PDFDocument != null) {
			try {
				m_PDFDocument.close();
			} catch (Exception ex) {
			}
		}
		m_PDFDocument = null;
		if (m_PDFPageInfoManager == null)
			return;
		m_PDFPageInfoManager.cleanUpMemory();
		m_PDFPageInfoManager = null;
	}

	public void createPDFDocument(HashMap userPageDataSet, boolean createPDF)
			throws Exception {
		BufferedImage persPageImg = null;
		Vector vdpTextFields = null, vdpImageFields = null;
		Map<RGPTRectangle, Vector> multiWordSel = null;
		Map<Integer, PDFPageInfo> pdfPgInfoList = new HashMap<Integer, PDFPageInfo>();
		Map<Integer, BufferedImage> persPDFPages = new HashMap<Integer, BufferedImage>();
		PDFPersonalizationUtil persUtil = new PDFPersonalizationUtil(false);
		for (int i = 0; i < m_PDFPageInfoManager.m_PageCount; i++) {
			int pgNum = i + 1;
			PDFPageInfo pdfPage = m_PDFPageInfoManager.getPDFPage(pgNum);
			UserPageData userPageData = (UserPageData) userPageDataSet
					.get(pgNum);
			RGPTLogger.logToFile("Page Data for Page Num: " + pgNum + " is "
					+ userPageData.toString());
			// Create PDF is true, then Image is constructed from Java Graphics
			// and
			// used in the Personalized PDF. Else the User Page Data is properly
			// constructed which are finally used to create PDF Elements.
			multiWordSel = persUtil.createMultiWordSelLine(pdfPage,
					userPageData);
			persPageImg = persUtil.getPersonalizedPDFPage(pdfPage,
					userPageData, multiWordSel, true);
			if (createPDF) {
				persPDFPages.put(pgNum, persPageImg);
				pdfPgInfoList.put(pgNum, pdfPage);
			} else {
				Dimension panelSize = new Dimension((int) pdfPage.m_PageWidth,
						(int) pdfPage.m_PageHeight);
				ScalingFactor scaleFactor = ScalingFactor.ZOOM_IN_OUT;
				scaleFactor.setZoom(100);
				pdfPage.deriveDeviceCTM(scaleFactor, panelSize, 0,
						pdfPage.m_PageWidth, pdfPage.m_PageHeight);
				vdpTextFields = pdfPage.m_VDPTextFieldInfo;
				vdpImageFields = pdfPage.m_VDPImageFieldInfo;
				persUtil.processVDPTextFields(pdfPage, vdpTextFields,
						userPageData, multiWordSel);
				persUtil.processVDPImageFields(vdpImageFields, userPageData);
			}
		}

		// This specifies if the PDF Document needs to be generated from
		// Personalized
		// PDF Page Image
		boolean genPDFFromImage = false;
		if (createPDF && genPDFFromImage) {
			m_PDFDocument.close();
			m_PDFDocument = PDFUtil.createPDFDocument(pdfPgInfoList,
					persPDFPages);
		}
	}

	// This function is call to generate PDF in memory. The PDF Document is
	// updated
	// with User Data
	public boolean createPersonalizedDocument(TemplateInfo tempInfo,
			HashMap userPageDataSet, int pdfSaveMode, String filePath)
			throws Exception {
		PDFPageInfo pdfPageInfo = null;
		PDFPersonalizationUtil pdfPersUtil = new PDFPersonalizationUtil();
		Vector pdfPageInfoList = pdfPersUtil.getAllPDFPageInfo(tempInfo);
		for (int i = 0; i < m_PDFPageInfoManager.m_PageCount; i++) {
			pdfPageInfo = (PDFPageInfo) pdfPageInfoList.elementAt(i);
			// Merging the Initial UserPageData Set retrieved from PDFPageInfo
			// with
			// User Supplied UserPageData
			UserPageData userSpecPageData = (UserPageData) userPageDataSet
					.get(pdfPageInfo.m_PageNum);
			UserPageData usrPageData = pdfPageInfo.populateUserPageData();
			usrPageData.mergeVDPDataFields(userSpecPageData);
			userPageDataSet.put(pdfPageInfo.m_PageNum, usrPageData);
			m_PDFPageInfoManager.m_PDFPageInfo.put(pdfPageInfo.m_PageNum,
					pdfPageInfo);
		}
		this.createPersonalizedDocument(userPageDataSet, pdfSaveMode, filePath);
		return true;
	}

	// This function is call to generate PDF in memory. The PDF Document is
	// updated
	// with User Data.
	public boolean createPersonalizedDocument(HashMap userPageData,
			int pdfSaveMode, String filePath) throws Exception {
		return this.createPersonalizedDocument(userPageData, pdfSaveMode,
				filePath, false);
	}

	// This function is call to generate PDF in memory. The PDF Document is
	// updated
	// with User Data
	// createPDFPersDoc - If true indicates if the VDP Data is modified in the
	// PDF to
	// generate Personalized Document. If false the VDP Data is manipulated in
	// the
	// Image to generate the Personalized Document.
	public boolean createPersonalizedDocument(HashMap userPageData,
			int pdfSaveMode, String filePath, boolean createPDFPersDoc)
			throws Exception {
		return this.createPersonalizedDocument(userPageData, pdfSaveMode,
				filePath, createPDFPersDoc, false, "");
	}

	public boolean createPersonalizedDocument(HashMap userPageData,
			int pdfSaveMode, String filePath, boolean createPDFPersDoc,
			boolean genMemFile, String outputFileName) throws Exception {
		RGPTLogger.logToFile("USER PAGA DATA: " + userPageData.toString());
		if (createPDFPersDoc)
			createPDFDocument(userPageData, false);
		else {
			createPDFDocument(userPageData, true);
			// return true;
		}
		(new PDFUtil()).createPDFDocument(m_PDFDocument, userPageData);
		// Saving the document to a memory buffer
		if (pdfSaveMode == SAVE_PDF_IN_MEMORY) {
			byte[] memBuf = m_PDFDocument.save(
					pdftron.SDF.SDFDoc.e_remove_unused, null);
			if (genMemFile) {
				String outDir = "../PDF_Files/";
				if (!(new File(outDir)).exists())
					outDir = System.getProperty("java.io.tmpdir");
				String tempFilePath = outDir + outputFileName;
				System.out.println("TEMPLATE SAVED PATH: " + tempFilePath);
				java.io.FileOutputStream fileStream = null;
				fileStream = new java.io.FileOutputStream(tempFilePath);
				fileStream.write(memBuf);
				fileStream.flush();
				fileStream.close();
			}
		} else if (pdfSaveMode == SAVE_PDF_IN_FILE)
			m_PDFDocument.save(filePath, pdftron.SDF.SDFDoc.e_remove_unused,
					null);
		else
			throw new RuntimeException("Wrong PDF Save Mode");
		m_IsPersonalizedPDF = true;
		return true;
	}

	// This function returns the Page as Image based on the Quality Mode
	public BufferedImage getPDFPage(int qualityMode, int pageNum)
			throws Exception {
		return this.getPDFPage(qualityMode, pageNum, null);
	}

	// This function saves the PDF Page as Image based on the Quality Mode to
	// the File
	public void savePDFPage(int qualityMode, int pageNum, String fileName)
			throws Exception {
		this.getPDFPage(qualityMode, pageNum, fileName);
	}

	// This function returns the Page as Image based on the supplied Image Width
	// and Height
	public BufferedImage getPDFPage(int pageNum, int width, int height)
			throws Exception {
		return this.getPDFPage(pageNum, width, height, null);
	}

	// This function saves the PDF Page as Image based on the Size to the File
	public void savePDFPage(int pageNum, int width, int height, String fileName)
			throws Exception {
		this.getPDFPage(pageNum, width, height, fileName);
	}

	// This function is called to retrieve Thumbview Images of Pages
	// corresponding
	// to the Page Numbers
	public HashMap getPDFPageThumbviews(Vector pageNums) throws Exception {
		BufferedImage thumbImg = null;
		// Map<Integer, BufferedImage> pdfPageThumbviews =
		// new HashMap<Integer, BufferedImage>();
		HashMap pdfPageThumbviews = new HashMap();
		if (pageNums == null || pageNums.size() == 0) {
			int pgCnt = m_PDFDocument.getPageCount();
			pageNums = new Vector();
			for (int i = 0; i < pgCnt; i++)
				pageNums.addElement(i + 1);
		}
		for (int i = 0; i < pageNums.size(); i++) {
			int pageNum = ((Integer) pageNums.elementAt(i)).intValue();
			thumbImg = this.getPDFPage(pageNum,
					PDFViewInterface.THUMBVIEW_WIDTH,
					PDFViewInterface.THUMBVIEW_HEIGHT, null);
			pdfPageThumbviews.put(pageNum, thumbImg);
		}
		return pdfPageThumbviews;
	}

	// If the fileName is specified, this method will save the image of the page
	// to the file. This means the Image that is returned is null. And also no
	// update
	// is done to PDFPage
	protected PDFDraw m_PDFDraw = null;

	private BufferedImage getPDFPage(int pageNum, int reqPgWt, int reqPgHt,
			String fileName) throws Exception {
		BufferedImage img = null;
		if (m_PDFDraw == null)
			m_PDFDraw = new PDFDraw();
		Page currPage = m_PDFDocument.getPage(pageNum);
		Map<String, Integer> scaledPgSize = null;
		double pgWt = currPage.getPageWidth(), pgHt = currPage.getPageHeight();
		scaledPgSize = RGPTUtil.calcScaledImageSize(pgWt, pgHt,
				(double) reqPgWt, (double) reqPgHt);
		boolean preserveAspectRatio = true;
		m_PDFDraw.setAntiAliasing(true);
		m_PDFDraw.setImageSize(scaledPgSize.get("Width").intValue(),
				scaledPgSize.get("Height").intValue(), preserveAspectRatio);
		// If Image is Exported to File than no Bitmap Image is returned
		if (fileName != null) {
			m_PDFDraw.export(currPage, fileName, "PNG");
			return null;
		}
		img = m_PDFDraw.getBitmap(currPage);
		return img;
	}

	// If the fileName is specified, this method will save the image of the page
	// to the file. This means the Image that is returned is null. And also no
	// update
	// is done to PDFPage
	private BufferedImage getPDFPage(int qualityMode, int pageNum,
			String fileName) throws Exception {
		BufferedImage img = null;
		int dpi = PDFViewInterface.REGULAR_QUALITY_DPI;
		if (m_PDFDraw == null)
			m_PDFDraw = new PDFDraw();
		Page currPage = m_PDFDocument.getPage(pageNum);
		PDFPageInfo pdfPageInfo = m_PDFPageInfoManager.getPDFPage(pageNum);
		switch (qualityMode) {
		case PDFViewInterface.PAGE_QUALITY_PDF: {
			dpi = PDFViewInterface.PAGE_QUALITY_DPI;
		}
			break;
		case PDFViewInterface.MEDIUM_QUALITY_PDF: {
			dpi = PDFViewInterface.MEDIUM_QUALITY_DPI;
		}
			break;
		case PDFViewInterface.REGULAR_QUALITY_PDF: {
			dpi = PDFViewInterface.REGULAR_QUALITY_DPI;
		}
			break;
		case PDFViewInterface.MAX_QUALITY_PDF: {
			dpi = PDFViewInterface.MAX_QUALITY_DPI;
		}
			break;
		// Calling this method by setting the Quality Mode to Regular
		default:
			dpi = PDFViewInterface.REGULAR_QUALITY_DPI;
		}

		m_PDFDraw.setDPI(dpi);
		if (fileName != null) {
			m_PDFDraw.export(currPage, fileName,
					PDFViewInterface.SAVED_PDF_IMAGE_FORMAT);
			return null;
		}
		if (pdfPageInfo != null && pdfPageInfo.m_BufferedImage != null)
			return pdfPageInfo.m_BufferedImage;
		img = m_PDFDraw.getBitmap(currPage);
		// Setting the Image back in the PDFPageInfo Object
		if (pdfPageInfo != null)
			pdfPageInfo.m_BufferedImage = img;
		return img;
	}

	protected void populateAllPDFPages(PDFDoc doc, boolean usePrivateObj)
			throws Exception {
		this.populateAllPDFPages(doc, usePrivateObj, -1);
	}

	protected void populateAllPDFPages(PDFDoc doc, boolean usePrivateObj,
			int qualityMode) throws Exception {
		m_PDFDocument = doc;
		PDFPageInfo pdfPageInfo = null;
		try {
			RGPTLogger.logToFile("TOTAL PAGE COUNT IN THIS DOCUMENT: "
					+ doc.getPageCount());

			PageIterator page_begin = doc.getPageIterator();
			PageIterator itr;
			StringBuffer mesg = new StringBuffer();
			int totalPgCount = doc.getPageCount();

			// Traversing through the Iterator to Read every page of the
			// Document.
			// This is achieved using the hasNext() which moves the iterator to
			// the
			// next page and returns a boolean to indicate if there is any page.
			for (itr = page_begin; itr.hasNext();) {
				// The next() returns the page object. This does not take the
				// iterator to
				// the next page.
				Page currPage = (Page) (itr.next());

				mesg.append("\nCURRENT PAGE BEING PROCESSED: "
						+ currPage.getIndex()
						+ " ----------------------------------------\n");

				// The crop box is the region of the page to display and print
				// and is
				// specified in User Space coords
				mesg.append("\nCROP BOX : "
						+ currPage.getCropBox().getRectangle().toString());

				// The media box defines the boundaries of the physical medium
				// on which
				// the page is to be printed. It may include any extended area
				// surrounding
				// the finished page for bleed, printing marks, or other such
				// purposes. This
				// is specified in User Space Coords
				mesg.append("\nMEDIA BOX : "
						+ currPage.getMediaBox().getRectangle().toString());
				pdfPageInfo = this.populatePDFPage(currPage, usePrivateObj);

				RGPTLogger.logToFile(mesg.toString());
				mesg.setLength(0);
			}
			boolean result = false;
			if (usePrivateObj)
				result = (new PDFUtil()).saveTemplate(doc,
						m_PDFPageInfoManager.m_PDFPageInfo);
			// Drawing the PDFPage and Building Properfields
			this.buildProperFields(qualityMode);
			// m_PDFDocument.save("test.pdf", pdftron.SDF.SDFDoc.e_remove_unused
			// , null);
		} catch (PDFNetException pdfNetEx) {
			RGPTLogger.logToFile("EXCEPTION WHILE POPULATING PAGE.\n"
					+ pdfPageInfo.toString());
			StringBuffer msg = new StringBuffer("EXCEPTION THROWN - "
					+ pdfNetEx.getMessage());
			msg.append("\nFile Name = " + pdfNetEx.getFileName());
			msg.append("\nConditional Expression = " + pdfNetEx.getCondExpr());
			msg.append("\nLine Number = " + pdfNetEx.getLineNumber());
			RGPTLogger.logToFile("\n_______________________________\n\n");
			RGPTLogger.logToFile(msg.toString());
			RGPTLogger.logToFile("\n\n_____________________________\n");
			throw pdfNetEx;
		}
	}

	protected int getPDFPageQuality(int pageNum, boolean retQualMode)
			throws Exception {
		Page page = m_PDFDocument.getPage(pageNum);
		double ht = page.getPageHeight() / 72;
		double wd = page.getPageWidth() / 72;
		return getPDFPageQuality(wd, ht, retQualMode);
	}

	protected int getPDFPageQuality(double wd, double ht, boolean retQualMode) {
		if (wd < 8.0 && ht < 8.0) {
			if (retQualMode)
				return PDFViewInterface.REGULAR_QUALITY_PDF;
			return PDFViewInterface.REGULAR_QUALITY_DPI;
		}
		if (wd > 15.0 && ht > 8.0) {
			if (retQualMode)
				return PDFViewInterface.PAGE_QUALITY_PDF;
			return PDFViewInterface.PAGE_QUALITY_DPI;
		}
		if ((wd > 8.0 && wd < 15.0) && ht > 8.0) {
			if (retQualMode)
				return PDFViewInterface.MEDIUM_QUALITY_PDF;
			return PDFViewInterface.MEDIUM_QUALITY_DPI;
		}
		if (retQualMode)
			return PDFViewInterface.MEDIUM_QUALITY_PDF;
		return PDFViewInterface.MEDIUM_QUALITY_DPI;
	}

	private void buildProperFields() throws Exception {
		buildProperFields(-1);
	}

	private void buildProperFields(int qualityMode) throws Exception {
		PDFPageInfo pdfPageInfo = null;
		HashMap pdfPageInfoList = m_PDFPageInfoManager.m_PDFPageInfo;
		int pgNum = 0;
		Object[] keys = pdfPageInfoList.keySet().toArray();
		for (int i = 0; i < keys.length; ++i) {
			pgNum = ((Integer) keys[i]).intValue();
			pdfPageInfo = (PDFPageInfo) pdfPageInfoList.get(new Integer(pgNum));
			// Getting the Buffered Image for the set Image Size
			BufferedImage img = null;
			if (qualityMode == -1)
				img = getPDFPage(getPDFPageQuality(pgNum, true),
						pdfPageInfo.m_PageNum);
			else
				img = getPDFPage(qualityMode, pdfPageInfo.m_PageNum);
			pdfPageInfo.m_BufferedImage = img;
			// byte[] outputStr = ImageUtils.getImageStream(img, "PNG");
			// System.out.println("Get Image Size for DPI: " +
			// getPDFPageQuality(pgNum, false) + " is: " +
			// outputStr.length);

			// Calling this method is must after all the Fields are Populated.
			// This
			// will use flags, do necessary transformation to create proper
			// fields.
			pdfPageInfo.buildProperFields();
			RGPTLogger.logToFile("\nSUCCESSFULLY CREATED PAGE: "
					+ pdfPageInfo.m_PageNum + " Num Of VDP Text Flds: "
					+ pdfPageInfo.m_VDPTextFieldInfo.size());
			RGPTLogger.logToFile("\n" + pdfPageInfo.toString());
		}
	}

	private PDFPageInfo populatePDFPage(Page currPage, boolean usePrivateObj)
			throws PDFNetException {
		PDFPageInfo pdfPageInfo = null;
		try {
			// Retrieving the Current Page
			try {
				pdfPageInfo = VDPFieldsExtractor.getInstance()
						.extractVDPFIelds(currPage, usePrivateObj);
			} catch (PDFNetException ex) {
				RGPTLogger.logToFile(
						"PDFNetException while extracting VDP Fields: ", ex);
				// ex.printStackTrace();
				pdfPageInfo = new PDFPageInfo();
			} catch (Throwable ex) {
				RGPTLogger.logToFile("Exception while extracting VDP Fields: ",
						ex);
				// ex.printStackTrace();
				pdfPageInfo = new PDFPageInfo();
			}

			pdfPageInfo.m_PageNum = currPage.getIndex();
			pdfPageInfo.m_PageWidth = new Double(currPage.getPageWidth())
					.intValue();
			pdfPageInfo.m_PageHeight = new Double(currPage.getPageHeight())
					.intValue();
			m_PDFPageInfoManager.m_PDFPageInfo.put(new Integer(
					pdfPageInfo.m_PageNum), pdfPageInfo);

			return pdfPageInfo;
		} catch (Throwable th) {
			th.printStackTrace();
		}
		return null;
	}

}
