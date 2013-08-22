// RGPT PACKAGES
package com.rgpt.pdfnetlib;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import pdftron.Common.Matrix2D;
import pdftron.Common.PDFNetException;
import pdftron.Filters.FilterReader;
import pdftron.Filters.FilterWriter;
import pdftron.Filters.MemoryFilter;
import pdftron.PDF.CharData;
import pdftron.PDF.CharIterator;
import pdftron.PDF.ColorPt;
import pdftron.PDF.ColorSpace;
import pdftron.PDF.Element;
import pdftron.PDF.ElementBuilder;
import pdftron.PDF.ElementReader;
import pdftron.PDF.ElementWriter;
import pdftron.PDF.Font;
import pdftron.PDF.GState;
import pdftron.PDF.PDFDoc;
import pdftron.PDF.PDFDraw;
import pdftron.PDF.PDFNet;
import pdftron.PDF.Page;
import pdftron.PDF.PageIterator;
import pdftron.PDF.PathData;
import pdftron.PDF.Rect;
import pdftron.SDF.DictIterator;
import pdftron.SDF.Obj;
import pdftron.SDF.ObjSet;
import pdftron.SDF.SDFDoc;

import com.rgpt.imageutil.ImageHolder;
import com.rgpt.imageutil.ImageUtils;
import com.rgpt.serverhandler.ImageHandlerInterface;
import com.rgpt.serverhandler.PDFViewInterface;
import com.rgpt.templateutil.PDFPageInfo;
import com.rgpt.templateutil.UserPageData;
import com.rgpt.templateutil.VDPImageFieldInfo;
import com.rgpt.templateutil.VDPTextFieldInfo;
import com.rgpt.util.ClipPath;
import com.rgpt.util.RGPTFileFilter;
import com.rgpt.util.RGPTLogger;
import com.rgpt.util.RGPTRectangle;
import com.rgpt.util.RGPTUtil;
import com.rgpt.util.StaticFieldInfo;
import com.rgpt.viewer.PDFPageHandler;

// Error Code 1 Seriers 0-9 - Common PDF Errors like RGPT Private Objects
// Error Code 10 Seriers 10-20 - Common Error for Text and Image Elements
// Error Code 10 Seriers 10-20 - Text Elements
// Error Code 30 Seriers 20-30 - Image Elements

public class PDFUtil {

	public final static String PDFNET_LICENSE = "ZESTA Technology Group (zestatech.com):"
			+ "CPU:1:E:W:AMC(20090613):9DF49411553D4A21E3C846109C92AA132F028A5A800B264C050B76F0FA";

	public PDFUtil() {
	}

	public static void initalizePDFNET(String resPath) throws Exception {
		try {
			System.out.println("Initializing PDFNet.");

			// Initializes PDFNet library and called once, during process
			// initialization
			// PDFNet.initialize("ZESTA Technology Group (zestatech.com):CPU:1:E:W:AMC(20090613):9DF49411553D4A21E3C846109C92AA132F028A5A800B264C050B76F0FA");
			PDFNet.initialize(PDFNET_LICENSE);

			// Sets the location of PDFNet resource file to process PDF
			// documents that
			// use CJKV encodings or standard fonts.
			System.out.println("Setting Resource Path: " + resPath);
			PDFNet.setResourcesPath(resPath);
		} catch (Exception ex) {
			System.out.println("Exception in initializing PDFNet: "
					+ ex.getMessage());
			throw ex;
		}
	}

	public static BufferedImage getRGBImage(Obj imageobj, int cstype)
			throws PDFNetException, Exception {
		System.out.println("Entering CheckforSpotColorSpace....");
		System.out.println("Colorspace type " + cstype);
		// Obj imageobj = imgElement.getXObject();
		pdftron.PDF.Image pdfImage = new pdftron.PDF.Image(imageobj);
		// Checking For Image Mask and 1 BPC . Is true then no further color
		// conversion is done.
		if (pdfImage.isImageMask() || pdfImage.getComponentNum() == 1)
			return null;
		long imgStrLen = imageobj.getRawStreamLength();
		MemoryFilter memFil = new MemoryFilter(imgStrLen, false);
		if (cstype == ColorSpace.e_device_rgb || cstype == ColorSpace.e_icc
				|| cstype == ColorSpace.e_cal_gray
				|| cstype == ColorSpace.e_indexed) {
			System.out.println("Processing RGB Color Space with imgStr Len: "
					+ imgStrLen);
			try {
				BufferedImage buffImg = ImageUtils.ScaleToSize(
						pdfImage.getBitmap(), -1, -1);
				return buffImg;
			} catch (Exception ex) {
				System.out.println("Unable to export image of ColorSpace : "
						+ cstype);
				ex.printStackTrace();
				pdfImage.export(new FilterWriter(memFil));
				System.out.println("Image Data Out = "
						+ memFil.getBuffer().length);
				return ImageUtils.getBufferedImage(memFil.getBuffer());
			}
		}
		if (cstype == ColorSpace.e_device_cmyk) {
			System.out.println("Processing CMYK Space");
			pdfImage.exportAsPng(new FilterWriter(memFil));
			System.out.println("Image Data Out = " + memFil.getBuffer().length);
			return ImageUtils.getBufferedImage(memFil.getBuffer());
		}
		System.out.println("Exiting CheckforSpotColorSpace....");
		return null;
	}

	// This function return true if the Template is Valid otherwise false
	public boolean isTemplateValid(PDFDoc doc, HashMap errorMap) {
		boolean isError = false;
		boolean isVDPFieldDefined = false;
		try {
			Obj rgptPageVDPFieldDict;
			doc.initSecurityHandler();
			int pgnum = doc.getPageCount();
			PageIterator page_begin = doc.getPageIterator();
			PageIterator itr;
			for (itr = page_begin; itr.hasNext();) // Read every page
			{
				Page page = (Page) itr.next();
				int pgno = page.getIndex();
				StringBuffer pageError = new StringBuffer();
				errorMap.put(pgnum, pageError);

				// Retriving the rgptPageVDPFieldDict Private Object. If null
				// then the page does
				// not contain RGPT Template. If all Plages do not contain RGPT
				// Object
				// then this Template is not generated by RGPT Template Maker
				rgptPageVDPFieldDict = searchRGPTPageVDP(page);
				if (rgptPageVDPFieldDict == null) {
					pageError.append("Error # 1: No RGPT VDP Object Defined");
					continue;
				}

				// This Page contans VDP Elements.
				isVDPFieldDefined = true;
				if (!this.checkFormat(rgptPageVDPFieldDict, pgno, pageError))
					isError = true;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			isError = true;
		}
		// If Error or none of the page have VDP Field Defined then
		// the Template is not valid
		if (isError || !isVDPFieldDefined)
			return false;

		// Template is valid
		return true;
	}

	public Obj searchRGPTPageVDP(Page page) throws PDFNetException {
		Obj page_sdfobj = (Obj) page.getSDFObj();

		DictIterator pieceinfo_itr = page_sdfobj.find("PieceInfo");
		if (pieceinfo_itr.hasNext()) {
			Obj pieceinfo_dict_obj = (Obj) pieceinfo_itr.value();

			// Retriving ZViewer SDF Obj from PieceInfo Object
			Obj rgptPageVDP_obj = pieceinfo_dict_obj.findObj("RGPTPageVDP");
			if (rgptPageVDP_obj == null)
				return null;

			// Retriving Private SDF Obj from ZViewer Obj
			Obj prvt_obj = rgptPageVDP_obj.findObj("Private");
			if (prvt_obj == null)
				return null;

			// Retrieving rgptPageVDPFieldDict SDF Obj from the Private Object
			Obj rgptvdp_dict = prvt_obj.findObj("RGPTPageVDPFields");
			if (rgptPageVDP_obj == null)
				return null;
			return rgptPageVDP_obj;
		}
		return null;
	}

	// This function returns false if this function reports error
	private boolean checkFormat(Obj rgptPageVDPFieldDict, int pgno,
			StringBuffer pageError) throws PDFNetException {
		int seltype = 0;
		String type = "";
		Rect rect;
		boolean isError = false;

		DictIterator itr = rgptPageVDPFieldDict.getDictIterator();
		if (!itr.hasNext()) {
			pageError.append("\n:Error # 2: Dictionary has no entries");
			isError = true;
			return false;
		}

		// Checking the VDP Entries in the Dictionary
		while (itr.hasNext()) {
			// This is used to check error for the VDP Field. If the VDP field
			// has
			// any error isError is to true and the methods returns the error.
			isError = false;
			Obj field_key = (Obj) itr.key();
			Obj fld = (Obj) itr.value();

			Obj fldvalue = fld.findObj("Type");
			if (fldvalue == null)
				pageError.append("\n:Error # 10: No Type Field");

			type = fldvalue.getAsPDFText();
			if (type.equals("Text"))
				seltype = 1;
			else if (type.equals("Image"))
				seltype = 2;
			else {
				pageError
						.append("\n:Error # 11: Type Field is not Image or Text");
				isError = true;
			}
			// System.out.println("Type " + type);
			fldvalue = fld.findObj("RectPAGE");
			if (fldvalue == null) {
				pageError
						.append("\n:Error # 12: No Page Rectangle Points Defined for VDP Element");
				isError = true;
			}

			double x1 = fldvalue.getAt(0).getNumber();
			double y1 = fldvalue.getAt(1).getNumber();
			double x2 = fldvalue.getAt(2).getNumber();
			double y2 = fldvalue.getAt(3).getNumber();
			rect = new Rect(x1, y1, x2, y2);
			if (rect == null) {
				pageError
						.append("\n:Error # 13: Incorrect Page Rectangle points");
				isError = true;
			}

			fldvalue = fld.findObj("Name");
			if (fldvalue == null) {
				pageError.append("\n:Error # 14: No information for Name");
				isError = true;
			}

			fldvalue = fld.findObj("Value");
			if (fldvalue == null) {
				pageError.append("\n:Error # 15: No information for Value");
				isError = true;
			}

			fldvalue = fld.findObj("Length");
			if (fldvalue == null) {
				pageError.append("\n:Error # 16: No information for Length");
				isError = true;
			}
			// System.out.println("Before seltype Name ");

			if (seltype == 1) {
				// System.out.println("Entering text color / font attributes");
				fldvalue = fld.findObj("FillColor");
				if (fldvalue == null) {
					pageError
							.append("\n:Error # 20: No information for FillColor");
					isError = true;
				}

				fldvalue = fld.findObj("FillColorSpace");
				if (fldvalue == null) {
					pageError
							.append("\n:Error # 21: No information for FillColorSpace");
					isError = true;
				}

				fldvalue = fld.findObj("FontName");
				if (fldvalue == null) {
					pageError
							.append("\n:Error # 22: No information for FontName");
					isError = true;
				}

				fldvalue = fld.findObj("FontSize");
				if (fldvalue == null) {
					pageError
							.append("\n:Error # 23: No information for FontSize");
					isError = true;
				}
			}

			if (seltype == 2) {
				// System.out.println("Entering Image attributes");
				fldvalue = fld.findObj("ImageWidth");
				if (fldvalue == null) {
					pageError
							.append("\n:Error # 30: No information for ImageWidth");
					isError = true;
				}

				fldvalue = fld.findObj("ImageHeight");
				if (fldvalue == null) {
					pageError
							.append("\n:Error # 31: No information for ImageHeight");
					isError = true;
				}

				fldvalue = fld.findObj("ColorComponents");
				if (fldvalue == null) {
					pageError
							.append("\n:Error # 32: No information for ColorComponents");
					isError = true;
				}

				fldvalue = fld.findObj("RenderingIntent");
				if (fldvalue == null) {
					pageError
							.append("\n:Error # 33: No information for RenderingIntent");
					isError = true;
				}

				fldvalue = fld.findObj("BitsPerComponent");
				if (fldvalue == null) {
					pageError
							.append("\n:Error # 34: No information for BitsPerComponent");
					isError = true;
				}
				if (isError = true)
					return false;
			}
			itr.next();
		}

		return (true);
	}

	// Note this function uses import pages to copy Page from the One Document
	// to
	// another. This is done because PageImport will not import duplicate copies
	// of
	// resources that are shared across pages (such as fonts, images,
	// colorspaces etc).
	// Page Import is very useful when importing list of pages that consists of
	// several
	// pages that share the same resources.
	public static void copyPDFPages(PDFDoc fromDoc, PDFDoc toDoc)
			throws Exception {
		// Create a list of pages to copy.
		Page[] copyPages = new Page[fromDoc.getPageCount()];
		int j = 0;
		for (PageIterator itr = fromDoc.getPageIterator(); itr.hasNext(); j++)
			copyPages[j] = (Page) (itr.next());

		// Import all the pages from 'copyPages' list
		Page[] importedPages = toDoc.importPages(copyPages);

		// Note that pages in 'imported_pages' list are not placed in documen't
		// page sequence. This is done in the following step.
		for (int i = 0; i < importedPages.length; ++i)
			toDoc.pagePushBack(importedPages[i]);
	}

	public static int getRotIndex(int angle) {
		// int angle = Math.abs(rotAngle);
		int rotIndex = Page.e_0;
		if (angle == 0)
			rotIndex = Page.e_0;
		else if (angle == 90 || angle == -270)
			rotIndex = Page.e_90;
		else if (angle == 180 || angle == -180)
			rotIndex = Page.e_180;
		else if (angle == 270 || angle == -90)
			rotIndex = Page.e_270;
		return rotIndex;
	}

	public static void saveAsImagePDF(PDFDoc origDoc, int docRotAng,
			String fullPDFPath) throws Exception {
		Element element = null;
		BufferedImage pdfImg = null;
		int num_pages = origDoc.getPageCount();
		System.out.println("The Num of Pages in this doc is: " + num_pages);
		ElementWriter writer = new ElementWriter();
		ElementReader reader = new ElementReader();
		PDFDraw draw = new PDFDraw();
		int rotIndex = getRotIndex(docRotAng);
		System.out.println("Doc Rotation: " + docRotAng + " Rot Index: "
				+ rotIndex);
		draw.setRotate(rotIndex);
		int pgno = 0;
		PDFDoc doc = new PDFDoc();
		copyPDFPages(origDoc, doc);
		for (int i = 1; i <= num_pages; ++i) {
			PageIterator itr = doc.getPageIterator(i);
			Page page = (Page) (itr.next());
			pgno = page.getIndex();
			pdfImg = PDFUtil.drawPDFPage(draw, doc, 300, pgno, null);
			double pgWt = page.getPageWidth(), pgHt = page.getPageHeight();
			double newPgWt = pgWt, newPgHt = pgHt;
			Rect cropBox = page.getCropBox(), mediaBox = page.getMediaBox();
			Rect newCropBox = cropBox, newMediaBox = mediaBox;
			if (rotIndex == Page.e_90 || rotIndex == Page.e_270) {
				newPgWt = pgHt;
				newPgHt = pgWt;
				Rectangle2D.Double pgBox = cropBox.getRectangle();
				pgBox.width = cropBox.getHeight();
				pgBox.height = cropBox.getWidth();
				newCropBox.set(pgBox);
				pgBox = mediaBox.getRectangle();
				pgBox.width = mediaBox.getHeight();
				pgBox.height = mediaBox.getWidth();
				newMediaBox.set(pgBox);
			}
			System.out.println("In SaveTemplate To Process Element: "
					+ page.getIndex());
			System.out.println("Page Crop Box: " + cropBox.getRectangle());
			System.out.println("Page Media Box: " + mediaBox.getRectangle());
			System.out.println("New Page Crop Box: "
					+ newCropBox.getRectangle());
			System.out.println("New Page Media Box: "
					+ newMediaBox.getRectangle());
			System.out.println("Page Width: " + pgWt + " :Ht: " + pgHt);
			System.out.println("New Page Width: " + newPgWt + " :Ht: "
					+ newPgHt);
			Page new_page = doc.pageCreate();
			PageIterator next_page = itr;
			doc.pageInsert(next_page, new_page);
			writer.begin(new_page);
			pgno = new_page.getIndex();
			element = addImageElement(doc, pgno, pdfImg, newPgWt, newPgHt, 0.0,
					0.0);
			writer.writePlacedElement(element);
			writer.end(); // save changes to the current page
			new_page.setCropBox(newCropBox);
			new_page.setMediaBox(newMediaBox);
			new_page.setRotation(page.getRotation());
			doc.pageRemove(doc.getPageIterator(i));
		}

		// Closing the PDF Draw Object
		draw.destroy();
		// Saving the Temp PDF to the File System
		doc.save(fullPDFPath, pdftron.SDF.SDFDoc.e_remove_unused, null);
		doc.close();
		System.out.println("Saved PDF File: " + fullPDFPath);
	}

	public static BufferedImage drawPDFPage(PDFDraw draw, PDFDoc doc, int dpi,
			int pageNum, String fileName) throws Exception {
		BufferedImage img = null;
		boolean closePDFDraw = false;
		if (draw == null) {
			draw = new PDFDraw();
			closePDFDraw = true;
		}
		Page currPage = doc.getPage(pageNum);
		if (dpi != -1) {
			draw.setDPI((double) dpi);
			// return null;
		} else {
			int w = (int) (currPage.getPageWidth());
			int h = (int) (currPage.getPageHeight());
			boolean preserveAspectRatio = true;
			draw.setImageSize(w, h, preserveAspectRatio);
		}
		if (fileName != null) {
			draw.export(currPage, fileName, "JPEG");
			return null;
		}
		img = draw.getBitmap(currPage);
		if (closePDFDraw)
			draw.destroy();
		return img;
	}

	public static PDFDoc createPDFWithTextData(Vector<String> textData,
			String srcDir, String pdfFileName) throws Exception {
		try {
			PDFDoc doc = new PDFDoc();
			// ElementBuilder is used to build new Element objects
			ElementBuilder eb = new ElementBuilder();
			// ElementWriter is used to write Elements to the page
			ElementWriter writer = new ElementWriter();

			Element element;
			GState gstate;

			// Start a new page ------------------------------------
			int pgWt = 612, pgHt = 794;
			Page page = doc.pageCreate(new Rect(0, 0, pgWt, pgHt));

			writer.begin(page); // begin writing to the page
			eb.reset(); // Reset the GState to default

			for (int i = 0; i < textData.size(); i++) {
				if (i == 0) {
					element = eb.createTextBegin(
							Font.create(doc, Font.e_times_roman), 8);
					writer.writeElement(element);
					element = eb.createTextRun(textData.elementAt(i));
					element.setTextMatrix(1, 0, 0, 1, 50, pgHt - 50);
					gstate = element.getGState();
					gstate.setCharSpacing(1);
					gstate.setWordSpacing(0);
					gstate.setLineWidth(3);
					gstate.setLeading(15); // Set the spacing between lines
					writer.writeElement(element);
					continue;
				}
				writer.writeElement(eb.createTextNewLine());
				element = eb.createTextRun(textData.elementAt(i));
				writer.writeElement(element);
			}
			writer.writeElement(eb.createTextEnd());
			doc.pagePushBack(page);
			writer.end(); // save changes to the current page
			String filePath = srcDir + pdfFileName;
			System.out.println("Saving the PDF in: " + filePath);
			doc.save(filePath, SDFDoc.e_remove_unused, null);
			System.out.println("Done. Text saved in: " + filePath);
			return doc;
		} catch (Exception ex) {
			System.out.println("Exception in createPDFWithTextData: "
					+ ex.getMessage());
			throw ex;
		}
	}

	public static File genMultipleFiles(PDFDoc doc, int pages2Read,
			String srcDir, String fileName) throws Exception {
		int pgCnt = doc.getPageCount();
		if (pgCnt <= pages2Read)
			return null;
		String pdfFileName = RGPTFileFilter.getFileName(fileName);
		String tempDirPath = srcDir + "temp_" + pdfFileName;
		File tempDir = new File(tempDirPath);
		if (!tempDir.exists()) {
			boolean result = tempDir.mkdir();
			if (!result)
				throw new RuntimeException(
						"Unable to Create New PDF Directory: " + tempDirPath);
		}
		int startPg = 0, endPg = 0, splitDocNum = 0;
		int numOfRuns = (int) (pgCnt / pages2Read);
		for (int i = 0; i < numOfRuns; i++) {
			splitDocNum = i + 1;
			if (i == 0) {
				startPg = 1;
				endPg = pages2Read;
			} else {
				startPg = i * pages2Read + 1;
				endPg = startPg + pages2Read - 1;
			}
			String fullPDFPath = tempDirPath + "/" + pdfFileName + "_"
					+ splitDocNum + ".pdf";
			importPDFPages(doc, fullPDFPath, startPg, endPg, pages2Read);
		}
		if (numOfRuns * pages2Read == pgCnt)
			return tempDir;
		startPg = numOfRuns * pages2Read + 1;
		endPg = pgCnt;
		splitDocNum = numOfRuns + 1;
		String fullPDFPath = tempDirPath + "/" + pdfFileName + "_"
				+ splitDocNum + ".pdf";
		importPDFPages(doc, fullPDFPath, startPg, endPg, pages2Read);
		return tempDir;
	}

	public static void importPDFPages(PDFDoc doc, String fullPDFPath)
			throws Exception {
		int pgCnt = doc.getPageCount();
		importPDFPages(doc, fullPDFPath, 1, pgCnt, pgCnt);
	}

	public static void importPDFPages(PDFDoc doc, String fullPDFPath,
			int startPg, int endPg, int pages2Read) throws Exception {
		PDFDoc tempDoc = new PDFDoc();
		importPDFPages(doc, tempDoc, startPg, endPg, pages2Read);
		// Saving the Temp PDF to the File System
		tempDoc.save(fullPDFPath, pdftron.SDF.SDFDoc.e_remove_unused, null);
		tempDoc.close();
		System.out.println("Saved PDF File: " + fullPDFPath);
	}

	public static void importPDFPages(PDFDoc fromDoc, PDFDoc toDoc,
			int startPg, int endPg, int pages2Read) throws Exception {
		// Create a list of pages to copy.
		System.out.println("Reading Pages from: " + startPg + " to: " + endPg);
		Page[] copyPages = new Page[endPg - startPg + 1];
		int j = 0;
		StringBuffer mesg = new StringBuffer("Copied Page#: ");
		for (int i = startPg; i <= endPg; i++) {
			copyPages[j] = fromDoc.getPage(i);
			mesg.append(i + ":");
			j++;
		}
		System.out.println(mesg.toString());

		// Import all the pages from 'copyPages' list
		Page[] importedPages = toDoc.importPages(copyPages);

		System.out.println("Imported Pages: " + importedPages.length);
		// Note that pages in 'imported_pages' list are not placed in documen't
		// page sequence. This is done in the following step.
		for (int i = 0; i < importedPages.length; ++i)
			toDoc.pagePushBack(importedPages[i]);
	}

	public static Map<Integer, String> createApprovedPDFDoc(String srcDir,
			String fileName, PDFDoc origDoc, int pdfPgCnt,
			HashMap pdfPageApprovals) throws Exception {
		// Create a list of pages to copy.
		Vector disApprPages = new Vector();
		boolean isPDFPgApproved = true;
		for (int i = 0; i < pdfPageApprovals.size(); i++) {
			int pgNum = i + 1;
			isPDFPgApproved = ((Boolean) pdfPageApprovals.get(pgNum))
					.booleanValue();
			if (isPDFPgApproved)
				continue;
			disApprPages.addElement(pgNum);
		}

		int totPgCnt = origDoc.getPageCount();
		int pgCntr = 1, pdfCntr = 0;
		PageIterator pgIter = origDoc.getPageIterator();
		PDFDoc apprPDFDoc = null;
		Map<Integer, String> apprPDFFiles = new HashMap<Integer, String>();
		String apprPDFFile = "";
		while (pgCntr <= totPgCnt) {
			pdfCntr++;
			for (int i = 0; i < pdfPgCnt; i++) {
				Page pdfPage = (Page) pgIter.next();
				boolean isDelPage = check4DelPage(disApprPages, pgCntr);
				System.out.println("Is Page: " + pgCntr + " to be Deleted "
						+ isDelPage);
				pgCntr++;
				if (isDelPage)
					continue;
				if (i == 0)
					apprPDFDoc = new PDFDoc();
				apprPDFDoc.pagePushBack(pdfPage);
				if (i == pdfPgCnt - 1) {
					apprPDFFile = RGPTFileFilter.getFileName(fileName) + "_"
							+ pdfCntr + ".pdf";
					apprPDFDoc.save(srcDir + apprPDFFile, 0, null);
					apprPDFFiles.put(pdfCntr, apprPDFFile);
					apprPDFDoc = null;
				}
			}
		}

		pgCntr = totPgCnt;
		while (pgCntr > 0) {
			pgIter = origDoc.getPageIterator(pgCntr);
			boolean isDelPage = check4DelPage(disApprPages, pgCntr);
			System.out.println("Page to be Deleted: " + pgCntr);
			pgCntr--;
			if (!isDelPage)
				continue;
			origDoc.pageRemove(pgIter);
		}
		return apprPDFFiles;
	}

	public void createApprovedPDFDoc(PDFDoc origDoc, HashMap pdfPageApprovals)
			throws Exception {
		// Create a list of pages to copy.
		Vector disApprPages = new Vector();
		boolean isPDFPgApproved = true;
		for (int i = 0; i < pdfPageApprovals.size(); i++) {
			int pgNum = i + 1;
			isPDFPgApproved = ((Boolean) pdfPageApprovals.get(pgNum))
					.booleanValue();
			if (isPDFPgApproved)
				continue;
			disApprPages.addElement(pgNum);
		}

		int pageCnt = origDoc.getPageCount();
		int pgCounter = pageCnt;
		while (pgCounter > 0) {
			PageIterator pgIter = origDoc.getPageIterator(pgCounter);
			boolean isDelPage = check4DelPage(disApprPages, pgCounter);
			System.out.println("Page to be Deleted: " + pgCounter);
			pgCounter--;
			if (!isDelPage)
				continue;
			origDoc.pageRemove(pgIter);
		}
	}

	private static boolean check4DelPage(Vector disApprPages, int pageNum) {
		for (int j = 0; j < disApprPages.size(); j++) {
			int pgNum = ((Integer) disApprPages.elementAt(j)).intValue();
			if (pageNum == pgNum)
				return true;
		}
		return false;
	}

	public static PDFDoc createPDFDocument(
			Map<Integer, PDFPageInfo> pdfPgInfoList,
			Map<Integer, BufferedImage> persPDFPages) throws Exception {
		return createPDFDocument(pdfPgInfoList, persPDFPages, 1000);
	}

	public static PDFDoc createPDFDocument(
			Map<Integer, PDFPageInfo> pdfPgInfoList,
			Map<Integer, BufferedImage> persPDFPages, int tempFileId)
			throws Exception {

		Element element = null;
		GState gstate = null;
		PDFDoc doc = new PDFDoc();
		// ElementBuilder is used to build new
		ElementBuilder eb = new ElementBuilder();
		// Element objects
		ElementWriter writer = new ElementWriter(); // ElementWriter is used to
													// write

		int numPages = pdfPgInfoList.size();
		for (int i = 0; i < numPages; i++) {
			int pgNum = i + 1;
			PDFPageInfo pdfPage = pdfPgInfoList.get(pgNum);
			BufferedImage persPDFPage = persPDFPages.get(pgNum);

			// Start a new page ------------------------------------
			Page page = doc.pageCreate(new Rect(0, 0,
					(int) pdfPage.m_PageWidth, (int) pdfPage.m_PageHeight));
			writer.begin(page); // begin writing to the page
			Element elem = createImageElement(doc, persPDFPage,
					pdfPage.m_PageWidth, pdfPage.m_PageHeight, 0, 0, tempFileId);
			writer.writePlacedElement(elem);
			writer.end(); // save changes to the current page
			doc.pagePushBack(page);
		}
		return doc;
	}

	public static Element createImageElement(PDFDoc doc, BufferedImage buffImg,
			double wt, double ht, double tx, double ty, int tempFileId)
			throws Exception {
		String outDir = System.getProperty("java.io.tmpdir");
		RGPTLogger.logToFile("Get Output Dir: " + outDir);
		String tempFilePath = outDir + "img_" + tempFileId + ".png";
		byte[] imgBytes = ImageUtils.getImageStream(buffImg, "PNG");
		RGPTUtil.writeToFile(tempFilePath, imgBytes);
		// ImageUtils.SaveImage(textImg, tempFilePath, "PNG");
		ObjSet objset = new ObjSet();
		Obj flate_hint = objset.createArray();
		flate_hint.pushBackName("Flate");
		flate_hint.pushBackName("Level");
		flate_hint.pushBackNumber(9); // Maximum compression

		pdftron.PDF.Image img = pdftron.PDF.Image.create(doc, tempFilePath,
				flate_hint);
		ElementBuilder eb = new ElementBuilder();
		// Matrix2D elemMtx = new Matrix2D(wt, 0, 0, ht, tx, ty);
		// mtx = mtx.multiply(elemMtx);
		// element.GetGState().SetTransform(mtx);
		// Element element = eb.createImage(img, elemMat);
		// Element element = eb.createImage(img, new Matrix2D(wt, 0, 0, ht, tx,
		// ty);
		Element element = eb.createImage(img, tx, ty, wt, ht);
		RGPTLogger.logToFile("Element Matrix: "
				+ getAffineMatrix(element.getCTM()).toString());
		RGPTLogger.logToFile("Element BBox: "
				+ element.getBBox().getRectangle().toString());
		RGPTLogger.logToFile("Element Image Wt: " + element.getImageWidth()
				+ " And Ht: " + element.getImageHeight());
		return element;
	}

	// This Method is used to create Personalized PDF Document
	public PDFDoc createPDFDocument(PDFDoc doc, HashMap uservdpdata)
			throws Exception {
		try {
			// if (!PasswordDialog.CheckDocForSecurity(null, doc))
			// return null;
			int num_pages = doc.getPageCount();
			RGPTLogger.logToFile("The Num of Pages in this doc is: "
					+ num_pages);
			ElementWriter writer = new ElementWriter();
			ElementReader reader = new ElementReader();
			Element element;
			int pgno = 0;
			for (int i = 1; i <= num_pages; ++i) {
				PageIterator itr = doc.getPageIterator(i);
				Page page = (Page) (itr.next());
				pgno = page.getIndex();
				UserPageData updi = (UserPageData) uservdpdata.get(pgno);
				if (updi == null)
					continue;
				RGPTLogger.logToFile("The Num Fields: " + updi.getNumVDPFlds());
				reader.begin(page);
				Page new_page = doc.pageCreate();
				PageIterator next_page = itr;
				doc.pageInsert(next_page, new_page);
				writer.begin(new_page);
				ProcessElements(reader, writer, pgno, doc, updi);
				writer.end();
				reader.end();
				new_page.setMediaBox(page.getCropBox());
				new_page.setRotation(page.getRotation());
				doc.pageRemove(doc.getPageIterator(i));
			}
			// byte[] memBuf = doc.save(SDFDoc.e_remove_unused, null);

		} catch (Exception e) {
			e.printStackTrace();
			RGPTLogger.logToFile("Exception at Creating Personalized PDF Doc",
					e);
			System.out.println("NewPDFWithVDP-rewritefile");
			return null;
		}
		return (doc);
	}

	public static String printPageProps(PDFDoc doc, int page)
			throws PDFNetException {
		Page currPage = doc.getPage(page);
		int rot = currPage.getRotation();
		int angle = 0;
		if (rot == Page.e_90)
			angle = 90;
		else if (rot == Page.e_180)
			angle = 180;
		else if (rot == Page.e_270)
			angle = 270;
		double ht = currPage.getPageHeight();
		double wd = currPage.getPageWidth();

		StringBuffer mesg = new StringBuffer("\nPAGE PROPERTIES FOR PAGE NO: "
				+ page + "\n");
		mesg.append("Rotation Angle: " + angle + " Pg Wt: " + wd + " Pg Ht: "
				+ ht);
		mesg.append("\nPage Matrix: "
				+ getAffineMatrix(currPage.getDefaultMatrix()).toString());
		mesg.append("\nRotation Matrix: "
				+ getAffineMatrix(getPageRotationMatrix(currPage)).toString());
		mesg.append("\nMedia BBox: "
				+ currPage.getBox(Page.e_media).getRectangle().toString());
		mesg.append("\nCorp BBox: "
				+ currPage.getBox(Page.e_crop).getRectangle().toString());
		mesg.append("\nBleed BBox: "
				+ currPage.getBox(Page.e_bleed).getRectangle().toString());
		mesg.append("\nTrim BBox: "
				+ currPage.getBox(Page.e_trim).getRectangle().toString());
		mesg.append("\nArt BBox: "
				+ currPage.getBox(Page.e_art).getRectangle().toString());
		return mesg.toString();
	}

	// If the fileName is specified, this method will save the image of the page
	// to the file. This means the Image that is returned is null. And also no
	// update
	// is done to PDFPage
	public static BufferedImage getPDFPage(PDFDoc doc, int pageNum, int dpi,
			String fileName) throws Exception {
		BufferedImage img = null;
		PDFDraw pdfDraw = new PDFDraw();
		Page currPage = doc.getPage(pageNum);
		pdfDraw.setDPI(dpi);
		if (fileName != null) {
			pdfDraw.export(currPage, fileName,
					PDFViewInterface.SAVED_PDF_IMAGE_FORMAT);
		}
		img = pdfDraw.getBitmap(currPage);
		pdfDraw.destroy();
		return img;
	}

	public static byte[] addTextImageElement(BufferedImage textImg, PDFDoc doc,
			int page, double wt, double ht, double tx, double ty)
			throws Exception {
		Page currPage = doc.getPage(page);
		ElementWriter writer = new ElementWriter();
		writer.begin(currPage);
		Matrix2D elemMat = new Matrix2D();
		// Matrix2D rotMat = Matrix2D.rotationMatrix(Math.toRadians(rotAng));
		// elemMat = elemMat.multiply(rotMat);
		// elemMat = getImageRotateMatrix(wt, ht, tx, ty, rotAng);
		// Matrix2D elemMat = new Matrix2D();
		Element element = addTextImageElement(doc, page, textImg, wt, ht, tx,
				ty);
		System.out.println("Element Matrix: "
				+ getAffineMatrix(element.getCTM()).toString());
		System.out.println("Element Matrix Inv: "
				+ getAffineMatrix(element.getCTM().inverse()).toString());
		System.out.println("Element BBox: "
				+ element.getBBox().getRectangle().toString());
		System.out.println("Element Image Wt: " + element.getImageWidth()
				+ " And Ht: " + element.getImageHeight());
		writer.writePlacedElement(element);
		writer.end(); // save changes to the current page
		byte[] memBuf = doc.save(SDFDoc.e_remove_unused, null);
		return memBuf;
	}

	public static Matrix2D getImageRotateMatrix(double width, double height,
			double tx, double ty, double rotAng) throws Exception {
		Matrix2D rotMat = null;
		if (rotAng == 0.0)
			rotMat = new Matrix2D();
		else {
			rotMat = Matrix2D.rotationMatrix(Math.toRadians(rotAng));
			rotMat.translate(1, 0); // Translate the unit image in first
									// quadrant.
		}

		// Concatentate scaling and translation with the rotation matrix.
		Matrix2D mtx = (new Matrix2D(width, 0, 0, height, tx, ty))
				.multiply(rotMat);
		return mtx;
	}

	public static Element addTextImageElement(PDFDoc doc, int page,
			BufferedImage textImg, double wt, double ht, double tx, double ty)
			throws Exception {
		return addImageElement(doc, page, textImg, wt, ht, tx, ty);
	}

	public static Element addImageElement(PDFDoc doc, int page,
			BufferedImage buffImg, double wt, double ht, double tx, double ty)
			throws Exception {
		Page currPage = doc.getPage(page);
		String outDir = System.getProperty("java.io.tmpdir");
		RGPTLogger.logToFile("Get Output Dir: " + outDir);
		String tempFilePath = outDir + "TextImage2.png";
		byte[] imgBytes = ImageUtils.getImageStream(buffImg, "PNG");
		RGPTUtil.writeToFile(tempFilePath, imgBytes);
		// ImageUtils.SaveImage(textImg, tempFilePath, "PNG");
		ObjSet objset = new ObjSet();
		Obj flate_hint = objset.createArray();
		flate_hint.pushBackName("Flate");
		flate_hint.pushBackName("Level");
		flate_hint.pushBackNumber(9); // Maximum compression

		pdftron.PDF.Image img = pdftron.PDF.Image.create(doc, tempFilePath,
				flate_hint);
		// pdftron.PDF.Image img = pdftron.PDF.Image.create(doc,
		// (java.awt.Image)textImg);
		ElementBuilder eb = new ElementBuilder();
		// Matrix2D elemMtx = new Matrix2D(wt, 0, 0, ht, tx, ty);
		// mtx = mtx.multiply(elemMtx);
		// element.GetGState().SetTransform(mtx);
		// Element element = eb.createImage(img, elemMat);
		// Element element = eb.createImage(img, new Matrix2D(wt, 0, 0, ht, tx,
		// ty);
		// Element element = eb.createImage(img, tx, ty, wt, ht);
		Element element = eb.createImage(img);
		// element.getGState().setTransform(1, 0, 0, -1, 0,
		// currPage.getPageHeight());
		Matrix2D mtx = getPageRotationMatrix(currPage);
		// mtx = new Matrix2D(1, 0, 0, -1, 0, currPage.getPageHeight());
		// mtx = new Matrix2D(0, -1, 0, 1, 0, 0);
		// mtx = getMatrix2D(new AffineTransform(1.0, 0.0, 0.0, -1.0, 1, ht));
		// mtx = currPage.getDefaultMatrix().inverse();
		// element.getGState().setTransform(mtx);
		// element.getGState().setTransform(Matrix2D.identityMatrix().inverse());
		RGPTLogger.logToFile("Rot Matrix: " + getAffineMatrix(mtx).toString());
		// mtx.translate(1, 0);
		// System.out.println("Translate (1,0): " +
		// getAffineMatrix(mtx).toString());
		mtx.scale(wt, ht);
		RGPTLogger.logToFile("After Scale: " + getAffineMatrix(mtx).toString());
		mtx.translate(tx, ty);
		RGPTLogger.logToFile("After Trans Tx, Ty Final: "
				+ getAffineMatrix(mtx).toString());
		// element.getGState().setTransform(elemMat);
		// mtx = (new Matrix2D(ht, 0, 0, wt, tx, ty)).multiply(mtx);
		// Element element = eb.createImage(img, mtx);
		// Matrix2D pgMat = currPage.getDefaultMatrix().inverse();
		// mtx.concat(wt, 0, 0, ht, tx, ty);
		element.getGState().setTransform(mtx);
		// Element element = eb.createImage(img, mtx);
		// writer.writePlacedElement(element);
		// writer.writeElement(element);
		// writer.end(); // save changes to the current page
		RGPTLogger.logToFile("Element Matrix: "
				+ getAffineMatrix(element.getCTM()).toString());
		RGPTLogger.logToFile("Element BBox: "
				+ element.getBBox().getRectangle().toString());
		RGPTLogger.logToFile("Element Image Wt: " + element.getImageWidth()
				+ " And Ht: " + element.getImageHeight());
		return element;
	}

	public static Matrix2D getPageRotationMatrix(Page page)
			throws PDFNetException {
		double angle = 0;
		int rot = page.getRotation();
		if (rot == Page.e_90)
			angle = 90;
		else if (rot == Page.e_180)
			angle = 180;
		else if (rot == Page.e_270)
			angle = 270;
		else
			return new Matrix2D();
		double rad = Math.toRadians(angle);
		// System.out.println("Rotation Angle: " + angle + " Rad: " + rad +
		// "cos theta: " + Math.cos(rad) + " sin thata: " +
		// Math.sin(rad));

		// Create a transformation matrix for this rotation...
		double deg2rad = 3.1415926535 / 180.0;
		Matrix2D mtx = Matrix2D.rotationMatrix(angle * deg2rad);
		return mtx;
	}

	public static byte[] addTextImageElement(byte[] textImage, PDFDoc doc,
			int page, double wt, double ht, double tx, double ty)
			throws Exception {
		Page currPage = doc.getPage(page);
		ElementWriter writer = new ElementWriter();
		writer.begin(currPage);
		MemoryFilter memFil = new MemoryFilter(textImage.length, false);
		FilterWriter fw = new FilterWriter(memFil);
		fw.writeBuffer(textImage);
		FilterReader fr = new FilterReader(memFil);
		pdftron.PDF.Image img = pdftron.PDF.Image.create(doc, fr, (int) wt,
				(int) ht, 8, ColorSpace.createDeviceRGB());
		ElementBuilder eb = new ElementBuilder();
		Element element = eb.createImage(img,
				new Matrix2D(wt, 0, 0, ht, tx, ty));
		// element.getGState().setFillOpacity(0.1);
		writer.writePlacedElement(element);
		writer.end(); // save changes to the current page
		byte[] memBuf = doc.save(SDFDoc.e_remove_unused, null);
		return memBuf;
	}

	private void ProcessElements(ElementReader reader, ElementWriter writer,
			int pageno, PDFDoc doc, UserPageData updi) throws Exception {
		Element element, elem;
		System.out.println("Entering ProcessElements");
		ElementBuilder eb = new ElementBuilder();

		while ((element = reader.next()) != null) {
			switch (element.getType()) {
			case Element.e_image:
			case Element.e_inline_image: {
				Rect rect = element.getBBox();
				Rectangle2D.Double bbox = rect.getRectangle();
				elem = SearchNReplaceImageElement(updi, bbox, element, doc);
				if (elem != null) {
					element = elem;
					writer.writePlacedElement(element);
					continue;
				}
			}
				break;

			case Element.e_text: {
				Rect rect = element.getBBox();
				if (rect == null)
					break;
				Rectangle2D.Double bbox = rect.getRectangle();
				elem = SearchNUpdateTextElement(updi, bbox, element, writer);
				if (elem != null)
					continue;
			}
				break;

			case Element.e_form: // Process form XObjects
				reader.formBegin();
				ProcessElements(reader, writer, pageno, doc, updi);
				reader.end();
				break;
			}
			writer.writeElement(element);
		}

		// Writing Text In Rect Element as Image Elements into PDF
		Vector textInRectVect = updi.getAllVDPData("TextOnGraphics");
		for (int i = 0; i < textInRectVect.size(); i++) {
			HashMap vdpData = (HashMap) textInRectVect.elementAt(i);
			BufferedImage textImg = (BufferedImage) vdpData
					.get("UserSetFinalImage");
			RGPTRectangle rect = (RGPTRectangle) vdpData.get("NewBBox");
			double tx = 0.0, ty = 0.0;
			tx = rect.x;
			ty = rect.y; // Working fine if the Page is non Rotated
			element = addTextImageElement(doc, pageno, textImg, rect.width,
					rect.height, tx, ty);
			writer.writePlacedElement(element);
		}

		// System.out.println("Exiting ProcessElements");
	}

	private Element SearchNUpdateTextElement(UserPageData updi,
			Rectangle2D.Double bbox, Element element, ElementWriter writer)
			throws Exception {
		// This returns the vdpTextData for Word, Line or Para
		RGPTLogger.logToFile("Processing Text Element: "
				+ element.getTextString());
		HashMap vdpTextData = (HashMap) updi.getVDPData("Text", bbox);
		if (vdpTextData == null)
			return null;
		boolean pdfElemCreated = ((Boolean) vdpTextData
				.get("PDFElementCreated")).booleanValue();
		if (pdfElemCreated)
			return element;
		RGPTLogger.logToFile("In SearchNUpdateTextElement for Text Field: "
				+ vdpTextData.get("VDPFieldName"));
		// Checking for Para. If VDP Text Mode is of type PARA then appropriate
		// line is retrieved.
		int vdpTxtMode = ((Integer) vdpTextData.get("VDPTextMode")).intValue();
		if (vdpTxtMode == StaticFieldInfo.PARA) {
			Vector vdpTextLines = (Vector) vdpTextData.get("LineAttributes");
			vdpTextData = updi.getLineSel(vdpTextLines, bbox);
		}

		RGPTLogger.logToFile("TEXT DATA FOUND: " + vdpTextData.toString());
		String vdpText = (String) vdpTextData.get("UserSetValue");
		if (vdpText == null)
			return null;
		System.out.println("User Set Value: " + vdpText);
		String textAllignment = (String) vdpTextData.get("TextAllignment");
		// if (textAllignment != null) {
		// RGPTLogger.logToFile("Text Allignment: " + textAllignment);
		// if (!textAllignment.equals("LEFT"))
		// vdpText = getAdjustedVDPText(vdpTextData);
		// }
		// System.out.println("TEXT DATA FOUND: " + vdpTextData.toString());
		if (vdpTxtMode != StaticFieldInfo.PARA) {
			// boolean useTitleCase = ((Boolean)
			// vdpTextData.get("UseTitleCase")).
			// booleanValue();
			// if (useTitleCase) vdpText = RGPTUtil.toTitleCase(vdpText);
			// vdpText = (String) vdpTextData.get("PrefixValue") + vdpText;
			vdpText = (String) vdpTextData.get("UserSetFinalValue");
		}
		ElementBuilder eb = new ElementBuilder();
		GState gs = element.getGState();
		RGPTLogger.logToFile("Font Name: " + gs.getFont().getName());
		// gs.setFont(gs.getFont(), gs.getFontSize());
		eb.reset(gs);
		Element elem = eb.createTextRun(vdpText);
		double startx = 0.0, starty = 0.0;
		Double startxObj = (Double) vdpTextData.get("StartPtX");
		AffineTransform textCTM = (AffineTransform) vdpTextData
				.get("TextMatrix");
		Matrix2D elemMat = this.getMatrix2D(textCTM);
		// Matrix2D elemMat = element.getTextMatrix();
		System.out.println("Elem BBox: " + bbox.toString());
		System.out.println("Element Text Matrix Set: "
				+ getAffineMatrix(element.getTextMatrix()).toString());
		System.out.println("Element Text Matrix Stored BFOR: "
				+ getAffineMatrix(elemMat).toString());

		if (textAllignment != null && vdpTxtMode != StaticFieldInfo.PARA) {
			Rectangle2D.Double origRect = ((RGPTRectangle) vdpTextData
					.get("BBox")).getRectangle2D();
			Rectangle2D.Double rect = ((RGPTRectangle) vdpTextData
					.get("NewBBox")).getRectangle2D();
			System.out.println("Stored BBox: " + origRect.toString()
					+ " New BBox: " + rect.toString());
			System.out.println("Text Allignment: " + textAllignment);
			if (textAllignment.equals(PDFPageHandler.ALLIGN_CENTER)) {
				if (startxObj != null) {
					startx = startxObj.doubleValue();
					System.out.println("Setting StartX: " + startx
							+ " StartY: " + starty);
					// elemMat.setH(startx);
					startx += rect.getX();
					elemMat.setH(startx);
					System.out.println("Setting H: " + startx);
					if (origRect.getY() != rect.getY()) {
						starty += rect.getY();
						elemMat.setV(starty);
						System.out.println("Setting V: " + starty);
					}
				}
			} else if (textAllignment.equals(PDFPageHandler.ALLIGN_RIGHT)) {
				if (startxObj != null) {
					startx = startxObj.doubleValue();
					// elemMat.setH(startx);
					System.out.println("Setting StartX: " + startx
							+ " StartY: " + starty);
					startx += rect.getX();
					elemMat.setH(startx);
					System.out.println("Setting H: " + startx);
					if (origRect.getY() != rect.getY()) {
						starty += rect.getY();
						elemMat.setV(starty);
						System.out.println("Setting V: " + starty);
					}
				}
			} else if (textAllignment.equals(PDFPageHandler.ALLIGN_LEFT)) {
				if (!origRect.equals(rect)) {
					System.out.println("Orig Rect and New BBox are different");
					if (origRect.getX() != rect.getX()) {
						startx += rect.getX();
						elemMat.setH(startx);
						System.out.println("Setting H: " + startx);
					}
					if (origRect.getY() != rect.getY()) {
						starty += rect.getY();
						elemMat.setV(starty);
						System.out.println("Setting V: " + starty);
					}
				}
				// else elemMat = element.getTextMatrix();
			}
		}

		System.out.println("Element Text Matrix After: "
				+ getAffineMatrix(elemMat).toString());
		elem.setTextMatrix(elemMat);
		elem.setPosAdjustment(element.getPosAdjustment());
		elem.updateTextMetrics();
		writer.writeElement(elem);
		if (vdpTxtMode != StaticFieldInfo.PARA) {
			// boolean useTitleCase = ((Boolean)
			// vdpTextData.get("UseTitleCase")).
			// booleanValue();
			// if (useTitleCase) vdpText = RGPTUtil.toTitleCase(vdpText);
			// vdpText = (String) vdpTextData.get("PrefixValue") + vdpText;
			vdpTextData.put("PDFElementCreated", true);
		}

		return elem;
	}

	private String getAdjustedVDPText(HashMap vdpTextData) {
		String vdpText = (String) vdpTextData.get("UserSetValue");
		String textAllignment = (String) vdpTextData.get("TextAllignment");

		// This is if the Text is Alligned Center or Right
		int totalSpaces = 0;
		int txtLen = new Integer((String) vdpTextData.get("FieldLength"))
				.intValue();
		// Center or Right Allign text horizontally
		if (textAllignment.equals("CENTER"))
			totalSpaces = (int) ((txtLen - vdpText.length()) / 2);
		else if (textAllignment.equals("RIGHT"))
			totalSpaces = txtLen - vdpText.length();
		System.out.println("Txt Len: " + txtLen + " VDP Text Len "
				+ vdpText.length() + " Total Space: " + totalSpaces);
		int sp = '\u0020';
		char[] spaces = new char[totalSpaces];
		for (int i = 0; i < totalSpaces; ++i) { // replace wordtext by spaces
			spaces[i] = (char) sp;
		}
		vdpText = (new String(spaces)) + vdpText;
		System.out.println("VDP Text to write on pdf: " + vdpText);
		return vdpText;
	}

	Element SearchNReplaceImageElement(UserPageData updi,
			Rectangle2D.Double bbox, Element element, PDFDoc doc)
			throws Exception {
		HashMap vdpImagedata = (HashMap) updi.getVDPData("Image", bbox);
		if (vdpImagedata == null)
			return null;
		// System.out.println("IMAGE VDP DATA FOUND: " +
		// vdpImagedata.toString());
		ImageHolder imgHldr = (ImageHolder) vdpImagedata.get("ImageHolder");
		if (imgHldr == null)
			return null;
		RGPTLogger.logToFile("IMAGE HOLDER FOUND: " + imgHldr.toString());
		BufferedImage buffImg = null;
		pdftron.PDF.Image image = null;
		if (imgHldr.m_ImageFromMode == ImageHandlerInterface.DESKTOP_IMAGES
				&& imgHldr.m_UseCachedImages) {
			buffImg = ImageUtils.getBufferedImage(imgHldr.m_ImgStr);
		} else {
			buffImg = ImageUtils.getPrintableImage(imgHldr,
					ImageHandlerInterface.IMAGE_PRINT_DPI);
		}
		try {
			boolean showPictFrame = false, isOpqPictFrame = false;
			showPictFrame = ((Boolean) vdpImagedata.get("ShowPictureFrame"))
					.booleanValue();
			if (showPictFrame) {
				ImageHolder frImgHldr = null;
				frImgHldr = (ImageHolder) vdpImagedata
						.get("PictureFrameHolder");
				isOpqPictFrame = ((Boolean) vdpImagedata
						.get("IsOpaquePictureFrame")).booleanValue();
				BufferedImage framedImg = null, picFrame = null;
				picFrame = ImageUtils.getBufferedImage(frImgHldr.m_ImgStr);
				framedImg = ImageUtils.MaskImageRGB(buffImg, picFrame, 150.0,
						-1, isOpqPictFrame);
				if (framedImg != null)
					buffImg = framedImg;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		image = pdftron.PDF.Image.create(doc, (java.awt.Image) buffImg);
		double alphaValue = ((Double) vdpImagedata.get("ImageAlphaValue"))
				.doubleValue();
		System.out.println("Alpha Value is: " + alphaValue);
		/*
		 * String imageabspath = imgHldr.m_SrcDir + imgHldr.m_FileName; //String
		 * imageabspath = (String) vdpImagedata.get("ImageAbsPath"); boolean
		 * isClipped = imgHldr.m_IsClipped; //boolean isClipped = ((Boolean)
		 * vdpImagedata.get("IsClipped")). // booleanValue(); if (imageabspath
		 * == null) return null ; ImageUtils iu = new ImageUtils(); if
		 * (m_ImgHldr.m_UseCachedImages) // load Image from User_filename to
		 * bufferedImage BufferedImage bimg = iu.LoadImage(imageabspath);
		 * BufferedImage buffImg = null; pdftron.PDF.Image image = null; if
		 * (isClipped) { Rectangle rect = (Rectangle)
		 * vdpImagedata.get("ClipRect"); buffImg = bimg.getSubimage((int)
		 * rect.getX(), (int) rect.getY(), (int) rect.getWidth(), (int)
		 * rect.getHeight()); image = pdftron.PDF.Image.create(doc,
		 * (java.awt.Image)buffImg); } else image =
		 * pdftron.PDF.Image.create(doc, (java.awt.Image)bimg);
		 */
		Matrix2D ctm = element.getCTM();
		ElementBuilder eb = new ElementBuilder();
		GState gs = element.getGState();
		eb.reset(gs);
		Element elem = eb.createImage(image, ctm);
		boolean isFill = element.isFilled();
		if (isFill) {
			double[] pathPts = getPathPoints(element);
			StringBuffer pthPtsBuf = new StringBuffer();
			for (int i = 0; i < pathPts.length; i++)
				pthPtsBuf.append(i + 1 + ":" + pathPts[i] + " ");
			System.out.println("Path Pts: " + pthPtsBuf.toString());
			setPathPoints(element, elem, pathPts);
			elem.setPathFill(true);
		}

		elem.getGState().setFillOpacity(alphaValue);
		return (elem);
	}

	public boolean saveTemplate(PDFDoc doc, HashMap pdfPageInfoList)
			throws PDFNetException {
		PDFPageInfo pdfPageInfo = null;
		try {
			doc.initSecurityHandler();
			int num_pages = doc.getPageCount();
			RGPTLogger.logToFile("No of pages " + num_pages);
			ElementWriter writer = new ElementWriter();
			ElementReader reader = new ElementReader();
			Element element;
			int pgno = 0;
			for (int i = 1; i <= num_pages; ++i) {
				RGPTLogger
						.logToFile("In SaveTemplate Reading Page No. ----------------"
								+ i);
				PageIterator itr = doc.getPageIterator(i);

				Page page = (Page) (itr.next());
				reader.begin(page);
				pgno = page.getIndex();
				pdfPageInfo = (PDFPageInfo) pdfPageInfoList.get(new Integer(
						pgno));
				RGPTLogger.logToFile("Number of VDP Text: "
						+ pdfPageInfo.m_VDPTextFieldInfo.size());
				RGPTLogger.logToFile("Number of VDP Images: "
						+ pdfPageInfo.m_VDPImageFieldInfo.size());
				Page new_page = doc.pageCreate();
				PageIterator next_page = itr;
				doc.pageInsert(next_page, new_page);
				writer.begin(new_page);
				this.saveTemplate(page, reader, writer, doc, pdfPageInfo);
				writer.end();
				reader.end();
				new_page.setMediaBox(page.getCropBox());
				new_page.setRotation(page.getRotation());
				doc.pageRemove(doc.getPageIterator(i));
			}

			// System.out.println("Saving document...");
			// doc.save(SDFDoc.e_remove_unused , null);
		} catch (Exception e) {
			e.printStackTrace();
			// System.out.println("ProcessVDPElements");
		}
		return true;
	}

	private void saveTemplate(Page page, ElementReader reader,
			ElementWriter writer, PDFDoc doc, PDFPageInfo pdfPageInfo)
			throws PDFNetException {
		VDPTextFieldInfo vdpTextFieldInfo = null;
		VDPImageFieldInfo vdpImageFieldInfo = null;
		Element element, elem;
		Vector vect = new Vector();
		ElementBuilder eb = new ElementBuilder();
		Rectangle2D pageRect2D = (page.getCropBox()).getRectangle();
		System.out.println("In SaveTemplate To Process Element: "
				+ page.getIndex());
		System.out.println("Page Crop Box: " + pageRect2D.toString());
		System.out.println("Page Width: " + page.getPageWidth() + " :Ht: "
				+ page.getPageHeight());

		while ((element = reader.next()) != null) {
			Obj shadeObj = getShading(element);
			if (shadeObj != null) {
				try {
					// RGPTLogger.logToFile("Elem Type: " + element.getType());
					// RGPTLogger.logToFile("Shade Obj #: " +
					// shadeObj.getObjNum());
					// RGPTLogger.logToFile("Name: " + shadeObj.getName() +
					// " Type: " + shadeObj.getType());
					// Shading shading = new Shading(shadeObj);
					// RGPTLogger.logToFile("Shading BBox: " +
					// shading.getBBox().getRectangle().toString());
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			switch (element.getType()) {
			case Element.e_image:
			case Element.e_inline_image: {
				if (pdfPageInfo.m_VDPImageFieldInfo.size() == 0)
					break;
				Rect imgRect = element.getBBox();
				if (imgRect == null)
					break;
				vdpImageFieldInfo = isSelectedImage(imgRect, pdfPageInfo, false);
				if (vdpImageFieldInfo == null)
					break;
				System.out.println("VDP Image Field Name Selected: "
						+ vdpImageFieldInfo.m_FieldName);
				vdpImageFieldInfo.m_ImageAlphaValue = element.getGState()
						.getFillOpacity();
				System.out.println("VDP Image Opacity Value: "
						+ element.getGState().getFillOpacity());
				try {
					HashMap imageData = this.getImageData(element);
					System.out.println("\n****Image Data Bfor*****\n"
							+ imageData.toString());
				} catch (Exception ex) {
				}
				// The Selected Image Element is replaced with Dummy VDP Image.
				// This will be done in the Client
				// String imgLocation = "res/" + "markimg" + ".gif";
				// String imgLocation = "res/rgpt_logo_2_big.gif";
				String imgLocation = "res/addImagePtr.gif";
				Obj mask = element.getMask();
				if (mask != null)
					retrieveImageMask(element, vdpImageFieldInfo);
				element = this.WriteImageObj(doc, imgLocation, element);

				try {
					HashMap imageData = this.getImageData(element);
					System.out.println("\n****Image Data After*****\n"
							+ imageData.toString());
				} catch (Exception ex) {
				}
				// If the Mask is set on the old Image element then Writing
				// the Mask again on the new Image Element
				if (mask != null)
					this.WriteImageMaskObj(doc, vdpImageFieldInfo, element);

				Matrix2D elemCTM = element.getCTM();
				// vdpImageFieldInfo.m_ImageAlphaValue = element.
				// getGState().getFillOpacity();
				System.out.println("Image Element Opacity Value: "
						+ element.getGState().getFillOpacity());
				System.out.println("Image Rect: " + imgRect.getRectangle());
				if (imgRect.getRectangle().equals(pageRect2D)
						|| imgRect.getRectangle().contains(pageRect2D)) {
					RGPTLogger.logToFile("Found Background Image: "
							+ vdpImageFieldInfo.m_FieldName);
					vdpImageFieldInfo.m_IsBackgroundImage = true;
				}
				System.out.println("Is Background Img: "
						+ vdpImageFieldInfo.m_IsBackgroundImage);
				if (vdpImageFieldInfo.m_IsBackgroundImage) {
					if (vdpImageFieldInfo.m_ImageAlphaValue == 1.0)
						vdpImageFieldInfo.m_ImageAlphaValue = 0.5;
					element.getGState().setFillOpacity(
							vdpImageFieldInfo.m_ImageAlphaValue);
				}
				vdpImageFieldInfo.m_ElementCTM = getAffineMatrix(elemCTM);
				writer.writePlacedElement(element);
				// writer.writeElement(element);
				RGPTLogger.logToFile("Element CTM: "
						+ vdpImageFieldInfo.m_ElementCTM.toString());
				continue;
			}
			// break;
			case Element.e_path: {
				if (!element.isClippingPath()
						|| pdfPageInfo.m_VDPImageFieldInfo.size() == 0)
					break;

				Rect rect = element.getBBox();
				if (rect == null)
					break;
				vdpImageFieldInfo = isSelectedImage(rect, pdfPageInfo, true);
				if (vdpImageFieldInfo == null)
					break;
				System.out.println("Found Path in ImageBBox");
				// Background Image cannot contain any Path. So no Path
				// Processing
				// is done if this VDP Image is a background Image
				// if (vdpImageFieldInfo.m_IsBackgroundImage) break;
				this.processPath(reader, element, vdpImageFieldInfo);
			}
				break;
			case Element.e_text: {
				if (pdfPageInfo.m_VDPTextFieldInfo.size() == 0) {
					writeOrigElement(writer, element);
					continue;
				}
				Rect rect = element.getBBox();

				if (rect == null)
					break;
				// RGPTLogger.logToFile("Checking Element BBox : " +
				// rect.getRectangle().toString());
				vdpTextFieldInfo = isSelectedText(rect, element, pdfPageInfo);
				if (vdpTextFieldInfo == null) {
					writeOrigElement(writer, element);
					continue;
				}

				// The BBox of the Element is used up by the previous element,
				// hence there is no need to write this element again
				RGPTLogger.logDebugMesg("Found VDP Text PDFUtil: "
						+ vdpTextFieldInfo.m_FieldName + " val: "
						+ vdpTextFieldInfo.m_FieldValue);
				if (isSelectionProcessed(element, vdpTextFieldInfo))
					continue;
				System.out.println("The Selection is not Processed for Text: "
						+ vdpTextFieldInfo.m_FieldValue);
				vdpTextFieldInfo.m_FontSize = element.getGState().getFontSize();
				System.out.println("Font Size: "
						+ element.getGState().getFontSize());
				element = createTextElement(element, vdpTextFieldInfo, doc);
				vdpTextFieldInfo.m_ElementCTM = getAffineMatrix(element
						.getCTM());
				vdpTextFieldInfo.m_TextMatrix = getAffineMatrix(element
						.getTextMatrix());
				System.out.println("The Font Set is: "
						+ element.getGState().getFont().getName());
				// RGPTLogger.logToFile("Set Text is: " +
				// element.getTextString());
				System.out.println("Elem CTM: "
						+ vdpTextFieldInfo.m_ElementCTM.toString()
						+ "Text CTM: "
						+ vdpTextFieldInfo.m_TextMatrix.toString());
				writer.writeElement(element);
				continue;
			}
			// break;
			case Element.e_form: // Process form XObjects
			{
				reader.formBegin();
				saveTemplate(page, reader, writer, doc, pdfPageInfo);
				reader.end();
			}
				break;
			}
			writer.writeElement(element);
		}
		// System.out.println("Exiting ProcessElements");
	}

	private HashMap getImageData(Element element) throws Exception {
		HashMap imgElement = new HashMap();
		imgElement.put("ElementBBox", element.getBBox().getRectangle());
		imgElement.put("CSType", element.getImageColorSpace().getType());
		imgElement.put("ImageXObjectRef", element.getXObject().getObjNum());
		imgElement.put("IsFilled", element.isFilled());
		imgElement.put("IsImageMask", element.isImageMask());
		GState gs = element.getGState();
		try {
			imgElement.put("AISFlag", gs.getAISFlag());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		try {
			imgElement.put("StorkeOpacity", gs.getStrokeOpacity());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		try {
			imgElement.put("FillOpacity", gs.getFillOpacity());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		try {
			imgElement.put("FillCSType", gs.getFillColorSpace().getType());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		try {
			imgElement.put("FillCSCompNum", gs.getFillColorSpace()
					.getComponentNum());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return imgElement;
	}

	private boolean isSelectionProcessed(Element element,
			VDPTextFieldInfo vdpTextFieldInfo) throws PDFNetException {
		int vdpTxtMode = vdpTextFieldInfo.m_VDPTextMode;
		if (vdpTxtMode == StaticFieldInfo.WORD
				|| vdpTxtMode == StaticFieldInfo.LINE) {
			RGPTLogger.logToFile("Check isSelectionProcessed: "
					+ vdpTextFieldInfo.m_SelectionProcessed + " for: "
					+ vdpTextFieldInfo.m_FieldValue);
			return vdpTextFieldInfo.m_SelectionProcessed;
		}
		// Processing for PARA
		if (vdpTxtMode != StaticFieldInfo.PARA)
			throw new RuntimeException("VDP Text Mode is not recognized");
		HashMap lineAttrs = this.getLineSel(element, vdpTextFieldInfo);
		if (lineAttrs == null)
			throw new RuntimeException("No Line Found for Element BBox");
		boolean isLineProc = ((Boolean) lineAttrs.get("RewriteLineSel"))
				.booleanValue();
		return isLineProc;
	}

	private HashMap getLineSel(Element element,
			VDPTextFieldInfo vdpTextFieldInfo) throws PDFNetException {
		HashMap lineAttrs = null;
		Rectangle2D.Double lineBBox = null;
		Rectangle2D.Double elemBBox = element.getBBox().getRectangle();
		for (int i = 0; i < vdpTextFieldInfo.m_VDPLineAttrList.size(); i++) {
			lineAttrs = (HashMap) vdpTextFieldInfo.m_VDPLineAttrList
					.elementAt(i);
			lineBBox = ((RGPTRectangle) lineAttrs.get("LineBBox"))
					.getRectangle2D();
			Rectangle2D intersectRect = lineBBox.createIntersection(elemBBox);
			if (lineBBox.contains(elemBBox) || elemBBox.contains(lineBBox)
					|| CheckCharsinWordBBox(lineBBox, element)
					|| lineBBox.contains(intersectRect)) {
				return lineAttrs;
			}
		}
		return null;
	}

	public void writeOrigElement(ElementWriter writer, Element element)
			throws PDFNetException {
		ElementBuilder eb = new ElementBuilder();
		GState gs = element.getGState();
		eb.reset(gs);
		Element elem = eb.createTextRun(element.getTextString());
		elem.setTextMatrix(element.getTextMatrix());
		elem.setPosAdjustment(element.getPosAdjustment());
		elem.updateTextMetrics();
		writer.writeElement(element);
	}

	// The isSelectPath is set to true if the check is to select the Path which
	// are
	// contained in the Image BBox
	private VDPImageFieldInfo isSelectedImage(Rect rect,
			PDFPageInfo pdfPageInfo, boolean isSelectPath)
			throws PDFNetException {
		Rectangle2D.Double rect2D = null, selrect = null;
		VDPImageFieldInfo vdpImageFieldInfo = null;
		System.out.println("Image Rect: " + rect.getRectangle().toString()
				+ " Number of VDP Images: "
				+ pdfPageInfo.m_VDPImageFieldInfo.size());
		for (int i = 0; i < pdfPageInfo.m_VDPImageFieldInfo.size(); i++) {
			vdpImageFieldInfo = (VDPImageFieldInfo) pdfPageInfo.m_VDPImageFieldInfo
					.elementAt(i);
			System.out.println("Checking the Image Elem with VDP Image: "
					+ vdpImageFieldInfo.m_FieldName);
			// This is either the Path or the Image Element Rectangle
			rect2D = (Rectangle2D.Double) rect.getRectangle();

			if (isSelectPath) {
				// RGPTLogger.logToFile("Image BBox: " +
				// vdpImageFieldInfo.m_PageRectangle.toString());
				// RGPTLogger.logToFile("Path BBox: " + rect2D.toString());
				// No Clip Path is considered for Background Images
				if (vdpImageFieldInfo.m_IsBackgroundImage)
					continue;
				if (vdpImageFieldInfo.m_PageRectangle.contains(rect2D))
					return vdpImageFieldInfo;
				continue;
			}

			// This is the case to check if the Path is contained in the Image
			// BBox
			// RGPTLogger.logToFile("VDP Field Name: " +
			// vdpImageFieldInfo.m_FieldName);
			// RGPTLogger.logToFile("Image BBox: " +
			// vdpImageFieldInfo.m_PageRectangle.toString());
			// RGPTLogger.logToFile("Element BBox: " + rect2D.toString());

			// This condition is to check if the Image from the PDF is the VDP
			// Image
			if (rect2D.equals(vdpImageFieldInfo.m_PageRectangle))
				return vdpImageFieldInfo;

			// If there is minor difference in BBox, type casting to Float will
			// solve the problem
			selrect = vdpImageFieldInfo.m_PageRectangle;
			Rectangle2D.Float elemRectF = new Rectangle2D.Float();
			elemRectF.setRect((float) rect2D.getX(), (float) rect2D.getY(),
					(float) rect2D.getWidth(), (float) rect2D.getHeight());
			Rectangle2D.Float selrectF = new Rectangle2D.Float();
			selrectF.setRect((float) selrect.getX(), (float) selrect.getY(),
					(float) selrect.getWidth(), (float) selrect.getHeight());
			System.out.println("Image BBox in Float: " + selrectF.toString());
			System.out
					.println("Element BBox in Float: " + elemRectF.toString());
			if (selrectF.equals(elemRectF))
				return vdpImageFieldInfo;
			Rectangle elemRect = RGPTUtil.getRectangle(rect2D);
			Rectangle imgRect = RGPTUtil.getRectangle(selrect);

			System.out.println("Image BBox in Int: " + imgRect.toString());
			System.out.println("Element BBox in Int: " + elemRect.toString());
			if (imgRect.equals(elemRect))
				return vdpImageFieldInfo;

			// interRect2D = (Rectangle2D.Double) rect2D.createIntersection(
			// vdpImageFieldInfo.m_PageRectangle);
			// if (interRect2D == null) continue;
			// if (interRect2D.equals(vdpImageFieldInfo.m_PageRectangle))
			// return vdpImageFieldInfo;
		}

		// System.out.println("No Match Found for Text in PDF with BBox: " +
		// rect2D.toString());
		return null;
	}

	private Element WriteImageObj(PDFDoc doc, String imageAbspath,
			Element element) throws PDFNetException {
		if (imageAbspath == null)
			return null;
		ImageUtils iu = new ImageUtils();
		// load Image from User_filename to bufferedImage
		BufferedImage bimg = null;
		try {
			bimg = iu.getBufferedImage(imageAbspath, this.getClass());
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		// BufferedImage bimg = iu.LoadImage(imageAbspath);
		pdftron.PDF.Image image = null;
		try {
			image = pdftron.PDF.Image.create(doc, (java.awt.Image) bimg);
		} catch (Exception ex) {
			System.out.println("Exception while creating VDP Image Object: "
					+ ex.getMessage());
			ex.printStackTrace();
			return null;
		}

		boolean isFill = element.isFilled();
		Matrix2D ctm = element.getCTM();
		ElementBuilder eb = new ElementBuilder();
		GState gs = element.getGState();
		eb.reset(gs);
		Element elem = eb.createImage(image, ctm);
		if (isFill) {
			double[] pathPts = getPathPoints(element);
			StringBuffer pthPtsBuf = new StringBuffer();
			for (int i = 0; i < pathPts.length; i++)
				pthPtsBuf.append(i + 1 + ":" + pathPts[i] + " ");
			System.out.println("Path Pts: " + pthPtsBuf.toString());
			setPathPoints(element, elem, pathPts);
			elem.setPathFill(true);
		}
		return elem;
	}

	private Element WriteImageMaskObj(PDFDoc doc, VDPImageFieldInfo vdpImgFld,
			Element element) throws PDFNetException {
		Element elem;
		int width = element.getImageWidth();
		int height = element.getImageHeight();

		Matrix2D ctm = element.getCTM();
		pdftron.PDF.Image img = new pdftron.PDF.Image(element.getXObject());
		ColorSpace device_gray = ColorSpace.createDeviceGray();

		BufferedImage loadImg = (BufferedImage) vdpImgFld.m_ImageMask;
		int maskImg_height = loadImg.getHeight();
		int maskImg_width = loadImg.getWidth();
		// mask are black/white images - 1 bit per component
		int mask_data_sz = maskImg_width * maskImg_height * 1;
		byte maskr[] = null;

		DataBufferByte dbuf = new DataBufferByte(maskr, mask_data_sz);
		dbuf = (DataBufferByte) loadImg.getData().getDataBuffer();
		maskr = dbuf.getData();
		System.out.println("Dbuf size " + dbuf.getSize() + " type "
				+ dbuf.getDataType());

		pdftron.PDF.Image mask = pdftron.PDF.Image.create(doc, maskr,
				maskImg_width, maskImg_height, 1, device_gray);

		mask.getSDFObj().putBool("ImageMask", true);

		// mask is the explicit mask for the primary (base) image
		img.setMask(mask);
		ElementBuilder eb = new ElementBuilder();
		GState gs = element.getGState();
		eb.reset(gs);
		elem = eb.createImage(img, ctm);
		return elem;
	}

	public void retrieveImageMask(Element element, VDPImageFieldInfo vdpImgFld)
			throws PDFNetException {
		Matrix2D ctm = element.getCTM();
		pdftron.PDF.Image mask = new pdftron.PDF.Image(element.getMask());
		java.awt.Image maskImg = mask.getBitmap();
		RGPTLogger.logToFile("PDFUtil Mask Img Width: "
				+ maskImg.getWidth(null) + " Ht: " + maskImg.getHeight(null));
		vdpImgFld.m_ImageMask = ImageUtils.ScaleToSize(maskImg, -1, -1);
		// return (BufferedImage) vdpImgFld.m_ImageMask;
	}

	public static AffineTransform getAffineMatrix(Matrix2D elemCTM)
			throws PDFNetException {
		AffineTransform affineMat = null;
		affineMat = new AffineTransform(elemCTM.getA(), elemCTM.getB(),
				elemCTM.getC(), elemCTM.getD(), elemCTM.getH(), elemCTM.getV());
		return affineMat;
	}

	public static Matrix2D getMatrix2D(AffineTransform elemCTM)
			throws PDFNetException {
		Matrix2D ctm = null;
		ctm = new Matrix2D(elemCTM.getScaleX(), elemCTM.getShearX(),
				elemCTM.getShearX(), elemCTM.getScaleY(),
				elemCTM.getTranslateX(), elemCTM.getTranslateY());
		return ctm;
	}

	private void processPath(ElementReader reader, Element path,
			VDPImageFieldInfo vdpImgFld) throws PDFNetException {
		ClipPath clipPath = new ClipPath();
		vdpImgFld.m_ClipPath.addElement(clipPath);
		GState gs = path.getGState();

		// Set Path State 0 (stroke, fill, fill-rule)
		// -----------------------------------
		if (path.isStroked()) {
			if (gs.getStrokeColorSpace().getType() != ColorSpace.e_pattern) {
				// Get stroke color (you can use PDFNet color conversion
				// facilities)
				ColorPt rgb;
				rgb = gs.getStrokeColorSpace().convert2RGB(gs.getStrokeColor());
				Color fg = new Color((int) rgb.get(0), (int) rgb.get(1),
						(int) rgb.get(2), (int) rgb.get(3));
				clipPath.m_StrokeColorPt = fg.getRGB();
				clipPath.m_IsStroked = true;
			}
		}

		if (path.isFilled()) {
			if (gs.getFillColorSpace().getType() == ColorSpace.e_pattern) {
				// System.out.println("Path has associated pattern");
			} else {
				ColorPt rgb;
				rgb = gs.getFillColorSpace().convert2RGB(gs.getFillColor());
				Color bg = new Color((int) rgb.get(0), (int) rgb.get(1),
						(int) rgb.get(2), (int) rgb.get(3));
				clipPath.m_FillColorPt = bg.getRGB();
				clipPath.m_IsFilled = true;
			}
		}

		clipPath.m_ElementCTM = this.getAffineMatrix(path.getCTM());
		clipPath.m_PathCTM = this.getAffineMatrix(gs.getTransform());

		// Path X, Y Coordinates and Path Type like Move To, Line To, etc
		clipPath.m_PathPoints = getPathPoints(path);
		clipPath.m_PathTypes = getPathTypes(path);

		// Stroke Attributes
		// Typecasting whereever needed from double to float
		clipPath.m_LineWidth = (float) gs.getLineWidth();
		clipPath.m_LineCap = gs.getLineCap();
		clipPath.m_LineJoin = gs.getLineJoin();
		clipPath.m_MitreLimit = (float) gs.getMiterLimit();
		clipPath.m_Dashes = this.getFloat(gs.getDashes());
		clipPath.m_DashPhase = (float) gs.getPhase();
		clipPath.buildProperFields();
		RGPTLogger.logToFile("The Clip Path " + clipPath.toString());
	}

	private VDPTextFieldInfo isSelectedText(Rect rect, Element element,
			PDFPageInfo pdfPageInfo) throws PDFNetException {
		Rectangle2D.Double elemBBox = null, wordBBox = null, interRect2D = null;
		VDPTextFieldInfo vdpTextFieldInfo = null;
		// System.out.println("Element Text: " + element.getTextString());
		for (int i = 0; i < pdfPageInfo.m_VDPTextFieldInfo.size(); i++) {
			vdpTextFieldInfo = (VDPTextFieldInfo) pdfPageInfo.m_VDPTextFieldInfo
					.elementAt(i);
			elemBBox = (Rectangle2D.Double) rect.getRectangle();
			wordBBox = vdpTextFieldInfo.m_PageRectangle;
			// if
			// (element.getTextString().equals(vdpTextFieldInfo.m_FieldValue)) {
			// System.out.println("VDP Field Value: " +
			// vdpTextFieldInfo.m_FieldValue);
			// System.out.println("Element Text: " + element.getTextString());
			// System.out.println("VDP Word BBox: " + wordBBox.toString());
			// System.out.println("PDF Element BBox: " + elemBBox.toString());
			// }
			if (wordBBox.contains(elemBBox)) {
				RGPTLogger
						.logToFile("Case 1 : Word is broken into multiple elements");
				return vdpTextFieldInfo;
			}

			// Case 2 : Element and Word are same
			if (elemBBox.contains(wordBBox)) {
				RGPTLogger.logToFile("Case 2 : Element and Word are same");
				return vdpTextFieldInfo;
			}

			// Case 3 : Same as Case 1 if each element character pen position is
			// within word bounding box
			if (!element.getTextString().trim().isEmpty()
					&& CheckCharsinWordBBox(wordBBox, element)) {
				RGPTLogger
						.logToFile("Case 3 : Same as Case 1, each element character pen "
								+ "position is within word bounding box");
				return vdpTextFieldInfo;
			}

			// Intersection Logic
			interRect2D = (Rectangle2D.Double) wordBBox
					.createIntersection(elemBBox);
			// System.out.println("Intersection BBox: " +
			// interRect2D.toString());
			if (interRect2D == null)
				continue;
			// Narayan Check if this condition is valid
			if (element.getTextString().trim().isEmpty())
				return null;
			// if (wordBBox.equals(interRect2D) ||
			// wordBBox.contains(interRect2D)) {
			if (wordBBox.equals(interRect2D) || elemBBox.equals(interRect2D)) {
				RGPTLogger
						.logToFile("Case 4 : Intersection Rectangle Rule is Satisfied");
				return vdpTextFieldInfo;
			}

			// Calculating the deviation in intersection only when the Word or
			// the Element contains the Intersection Rect
			if (!wordBBox.contains(interRect2D))
				continue;

			// This calculates howmuch percentage of intersect area is
			// coontained
			// inside the BBox. This calculation assumes the Word is smaller
			// then the text.
			double percentWordInside = (interRect2D.getHeight() * interRect2D
					.getWidth()) / (wordBBox.getHeight() * wordBBox.getWidth());
			// This calculation assumes the Element is smaller then the Word
			// text.
			double percentElemInside = (interRect2D.getHeight() * interRect2D
					.getWidth()) / (elemBBox.getHeight() * elemBBox.getWidth());
			// If the intersection Rectangle Occupies 80% of the area of the
			// Word
			// or Elem BBox, then the case 5 is satisfied
			double percentOccupied = 0.80;
			RGPTLogger
					.logToFile("Case 5: % Intersection Rect inside the Word BBox: "
							+ percentWordInside
							+ " :Elem Inside: "
							+ percentElemInside);
			if (percentWordInside > percentOccupied
					|| percentElemInside > percentOccupied)
				return vdpTextFieldInfo;
		}
		return null;
	}

	private boolean CheckCharsinWordBBox(Rectangle2D.Double wordRect,
			Element element) throws PDFNetException {
		double x, y;
		long char_code;

		Matrix2D textMtx = element.getTextMatrix();
		Matrix2D ctm = element.getCTM();
		Matrix2D mtx = ctm.multiply(textMtx);

		boolean isContaned = false;
		// RGPTLogger.logToFile("Word BBox: " + wordRect.toString());
		// RGPTLogger.logToFile("MinX: " + wordRect.getMinX() + " MinY: " +
		// wordRect.getMinY());
		// RGPTLogger.logToFile("MaxX: " + wordRect.getMaxX() + " MaxY: " +
		// wordRect.getMaxY());
		for (CharIterator itr = element.getCharIterator(); itr.hasNext();) {
			CharData data = (CharData) (itr.next());
			char_code = data.getCharCode();
			x = data.getGlyphX(); // character positioning information
			y = data.getGlyphY();

			java.awt.geom.Point2D.Double t = mtx.multPoint(x, y);
			x = t.x;
			y = t.y;
			// RGPTLogger.logToFile("Char: " + (char) char_code + " X: " + x +
			// " Y: " + y);
			if ((x >= wordRect.getMinX() && x <= wordRect.getMaxX())
					&& (y >= wordRect.getMinY() && y <= wordRect.getMaxY()))
				isContaned = true;
			else
				isContaned = false;
			if (!isContaned)
				return false;

			// if (!wordRect.contains(x,y) || )
			// return (false);
		}
		return (true);
	}

	private Element createTextElement(Element element,
			VDPTextFieldInfo vdpTextFld, PDFDoc doc) throws PDFNetException {
		Font pdfFont = null;
		Element elem;
		String wordtext, elemtext;

		if (vdpTextFld.m_SDFObjNum != -1) {
			Obj obj = doc.getSDFDoc().getObj(vdpTextFld.m_SDFObjNum);
			pdfFont = new Font(obj);
			RGPTLogger.logToFile("Loading Font Name for Element: "
					+ pdfFont.getName());
		}

		// Set the New Font for all the Element within the BBox.
		// If the PDFFont is not set then no need to write the new Element.
		// Remove isMemoryDoc
		int length = 0;
		// Finding the Appropriate Length to replace the Element with Empty
		// Text.
		HashMap lineAttrs = null;
		int vdpTxtMode = vdpTextFld.m_VDPTextMode;
		if (vdpTxtMode == StaticFieldInfo.WORD
				|| vdpTxtMode == StaticFieldInfo.LINE)
			length = new Integer(vdpTextFld.m_FieldLength).intValue();
		else if (vdpTxtMode == StaticFieldInfo.PARA) {
			lineAttrs = this.getLineSel(element, vdpTextFld);
			length = ((String) lineAttrs.get("LineText")).length();
			lineAttrs.put("RewriteLineSel", true);
		}

		// RGPTLogger.logToFile("Fields Length: " + length);
		char[] word = new char[length];
		for (int i = 0; i < length; ++i) { // replace wordtext by spaces
			word[i] = ' ';
		}

		ElementBuilder eb = new ElementBuilder();
		GState gs = element.getGState();
		// ColorPt c = gs.getFillColor();
		// gs.setFillColor(c);
		// Print the Tranform and Element CTM and check if they are same
		// Matrix2D mtx = myctm.multiply(text_mtx);
		// elem.setTextMatrix(mtx);
		// gs.setTransform(Matrix2D mtx)
		// gs.getTransform()

		// if (pdfFont != null)
		// gs.setFont(pdfFont, gs.getFontSize());
		// Setting the Graphic State for the new Text Element
		eb.reset(gs);
		elem = eb.createTextRun(new String(word));
		RGPTLogger.logToFile("Text Elem is created: " + elem.getTextString());
		elem.setTextMatrix(element.getTextMatrix());
		elem.setPosAdjustment(element.getPosAdjustment());
		elem.updateTextMetrics();
		vdpTextFld.m_SelectionProcessed = true;
		return elem;
	}

	public static float[] getFloat(double[] dbl) {
		float[] flt = new float[dbl.length];
		for (int i = 0; i < dbl.length; i++)
			flt[i] = (float) dbl[i];
		return flt;
	}

	//
	// Methods to support Backward Compatibility and current PDFNet Library
	//

	private Obj getShading(Element element) throws PDFNetException {
		// return element.getShading();
		return element.getShading().getSDFObj();
	}

	private byte[] getPathTypes(Element element) throws PDFNetException {
		// element.getPathTypes();
		return element.getPathData().getOperators();
	}

	private double[] getPathPoints(Element element) throws PDFNetException {
		// double[] pathPts = element.getPathPoints();
		PathData pathData = element.getPathData();
		double[] pathPts = pathData.getPoints();
		return pathPts;
	}

	private void setPathPoints(Element oldElem, Element newElem,
			double[] pathPts) throws PDFNetException {
		// element.setPathPoints(pathPts);
		newElem.setPathData(oldElem.getPathData());
	}

}
