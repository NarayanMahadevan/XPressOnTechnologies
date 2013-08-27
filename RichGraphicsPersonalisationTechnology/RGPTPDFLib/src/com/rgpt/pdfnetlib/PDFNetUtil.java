package com.rgpt.pdfnetlib;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import pdftron.Common.Matrix2D;
import pdftron.Common.PDFNetException;
import pdftron.Filters.FilterReader;
import pdftron.Filters.MappedFile;
import pdftron.PDF.Element;
import pdftron.PDF.ElementReader;
import pdftron.PDF.GState;
import pdftron.PDF.PDFDoc;
import pdftron.PDF.PDFNet;
import pdftron.PDF.Page;
import pdftron.PDF.Rect;
import pdftron.SDF.Obj;

import com.rgpt.imageutil.ImageHolder;
import com.rgpt.imageutil.ImageUtils;
import com.rgpt.pdflib.PDFLibException;
import com.rgpt.pdflib.VDPElement;
import com.rgpt.pdflib.VDPElement.SelType;
import com.rgpt.util.RGPTLogger;
import com.rgpt.util.RGPTUtil;

public class PDFNetUtil {

	// public final static String PDFNET_LICENSE =
	// "ZESTA Technology Group (zestatech.com):"
	// +
	// "CPU:1:E:W:AMC(20090613):9DF49411553D4A21E3C846109C92AA132F028A5A800B264C050B76F0FA";

	public final static String PDFNET_LICENSE = null;

	public static void initPDFLib() {
		if (PDFNET_LICENSE != null)
			PDFNet.initialize(PDFNET_LICENSE);
		else
			PDFNet.initialize();
	}

	public static void terminatePDFLib() {
		PDFNet.terminate();
	}

	/**
	 * This function returns the buffer from the pdf file path. This is useful
	 * for applications that work with dynamic PDF documents.
	 * 
	 * @param filePath
	 * @return the memory buffer
	 * @throws PDFLibException
	 */
	public static byte[] getPDFBuffer(String filePath) throws PDFLibException {
		try {
			// Read a PDF document in a memory buffer.
			MappedFile file = new MappedFile(filePath);
			long fileSize = file.fileSize();

			FilterReader file_reader = new FilterReader(file);

			byte[] pdfBuf = new byte[(int) fileSize];

			long bytes_read = file_reader.read(pdfBuf);
			RGPTLogger.logDebugMesg("File Size: " + file + " Bytes Read: "
					+ bytes_read);
			return pdfBuf;
		} catch (PDFNetException ex) {
			throw newPDFLibException(ex);
		}
	}

	public static PDFLibException newPDFLibException(PDFNetException ex) {
		return new PDFLibException(ex.getCondExpr(), ex.getLineNumber(),
				ex.getFileName(), ex.getFunction(), ex.getMessage());
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

	/**
	 * This functions retrieves all the Image Elements along with its Graphic
	 * State for the given Page
	 * 
	 * @param pdfPage
	 * @param elemReader
	 * @return Vector of Image Elements in the Page
	 * @throws PDFNetException
	 */
	public static Vector<Map<String, Object>> createPageImageElements(
			Page pdfPage, ElementReader elemReader) throws PDFNetException {
		Rect rect;
		Element element;
		Map<String, Object> imgElement = null;
		Vector<Map<String, Object>> pageImageElements = new Vector<Map<String, Object>>();
		while ((element = elemReader.next()) != null) {
			switch (element.getType()) {
			case Element.e_image:
			case Element.e_inline_image: {
				RGPTLogger
						.logDebugMesg("Entered Element for e_image no - Element Type "
								+ element.getType());
				rect = element.getBBox();
				if (rect == null)
					break;

				RGPTLogger.logDebugMesg("Image Rect BBox: "
						+ (rect.getRectangle()).toString());
				RGPTLogger.logDebugMesg("Image Object Id: "
						+ element.getXObject().getObjNum());

				// Adding Image Elements and the BBox
				imgElement = new HashMap<String, Object>();
				imgElement.put("PageNum", pdfPage.getIndex());
				imgElement.put("ElementBBox", rect.getRectangle());
				imgElement
						.put("CSType", element.getImageColorSpace().getType());
				imgElement.put("ImageXObjectRef", element.getXObject()
						.getObjNum());
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
					imgElement.put("FillCSType", gs.getFillColorSpace()
							.getType());
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				try {
					imgElement.put("FillCSCompNum", gs.getFillColorSpace()
							.getComponentNum());
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				pageImageElements.addElement(imgElement);
			}
				break;
			case Element.e_form: {
				RGPTLogger.logDebugMesg("Procession Form Elements: "
						+ element.getType());
				elemReader.formBegin();
				Vector<Map<String, Object>> formImgElems = createPageImageElements(
						pdfPage, elemReader);
				if (formImgElems != null && formImgElems.size() > 0)
					pageImageElements.addAll(formImgElems);
				elemReader.end();
			}
				break;
			} // switch(element.getType())
		} // while ((element = reader.next()) != null)
		return pageImageElements;
	}

	/**
	 * This method searches through the VDP Elements Selected and retrieves the
	 * VDP Element at the selElemRect. This can be any PDF Element i.e. Image,
	 * Text or Path indicated by SelType
	 * 
	 * @param vdpElements
	 * @param pageno
	 * @param selElemRect
	 * @param seltype
	 * @return the index of the VDP Element at the selElemRect
	 * @throws PDFNetException
	 */
	public static int searchVDPElement(Vector<VDPElement> vdpElements,
			int pageno, Rect selElemRect, SelType seltype)
			throws PDFNetException {
		VDPElement vdpElem = null;
		for (int i1 = 0; i1 < vdpElements.size(); ++i1) {
			vdpElem = (VDPElement) vdpElements.elementAt(i1);
			if (vdpElem.page_num != pageno || vdpElem.sel_type != seltype)
				continue;
			Rectangle2D vdprect = ((Rect) vdpElem.rect.getPDFRect())
					.getRectangle();
			Rectangle2D selrect = selElemRect.getRectangle();
			Rectangle2D intersectRect = selrect.createIntersection(vdprect);

			// Reducing the Precision from Double to Float
			Rectangle2D.Float vdprectF = new Rectangle2D.Float();
			vdprectF.setRect((float) vdprect.getX(), (float) vdprect.getY(),
					(float) vdprect.getWidth(), (float) vdprect.getHeight());
			Rectangle2D.Float selrectF = new Rectangle2D.Float();
			selrectF.setRect((float) selrect.getX(), (float) selrect.getY(),
					(float) selrect.getWidth(), (float) selrect.getHeight());
			Rectangle2D intersectRectF = selrectF.createIntersection(vdprectF);

			// Logging the Rectangles...
			RGPTLogger.logDebugMesg("ZSelection Rect: " + vdprect.toString());
			RGPTLogger.logDebugMesg("ZSelection Rect Float: "
					+ vdprect.toString());
			RGPTLogger.logDebugMesg("ElemBBox : " + selrect.toString());
			RGPTLogger.logDebugMesg("ElemBBox Float: " + selrectF.toString());
			RGPTLogger.logDebugMesg("Intersected Rect: "
					+ intersectRect.toString());
			RGPTLogger.logDebugMesg("Intersected Rect Float: "
					+ intersectRectF.toString());

			// This checks if the Selection Type is Image Selection
			if (seltype == SelType.IMAGE) {
				if (selrect.equals(vdprect) || selrectF.equals(vdprectF)) {
					RGPTLogger.logDebugMesg("Found IMAGE Selection: "
							+ vdpElem.namevalue.get("Name"));
					return i1;
				}
				continue;
			}

			// This part of the logic is for Text Selection
			if (selrectF.equals(vdprectF) || selrectF.contains(vdprectF)
					|| vdprectF.contains(selrectF)
					|| intersectRect.equals(vdprectF)) {
				RGPTLogger.logDebugMesg("Found TEXT Selection: "
						+ vdpElem.namevalue.get("Name"));
				return i1;
			}

			// TBD
			// This case is Selection Rectangle for Text Contains another
			// selection. This is the case when a Line Selection is already done
			// and a new Para Selection is done and it contains this Line
			// Selection. In this case the Line Selection is removed and Para
			// Selection is added.

			if (!vdprect.contains(intersectRect))
				continue;

			// This calculates how much percentage of intersect area is
			// contained inside the BBox. This calculation assumes the Word is
			// smaller then the text.
			double intersectWidth = intersectRect.getWidth();
			double intersectHt = intersectRect.getHeight();
			double percentInside = (intersectWidth * intersectHt)
					/ (vdprect.getHeight() * vdprect.getWidth());

			// If the intersection Rectangle Occupies 98% of the area of the
			// Word or Elem BBox, then the case 5 is satisfied
			System.out.println("% Intersection Rect : " + percentInside);
			if (percentInside > 0.95)
				return i1;
		}
		return -1;
	}

	/**
	 * This method retrieves the list of Image Elements at the point indicated
	 * by x and y for the pdf page. This methods actually scans through the List
	 * of PDF Image Elements. If the PDF Element at x, y is already selected as
	 * VDP Element then the image element is not considered.
	 * 
	 * @param pgNo
	 * @param pageImageElements
	 * @param vdpElements
	 * @param x
	 * @param y
	 * @return the VDP Image Elements at point x, y
	 * @throws PDFNetException
	 */
	public static Vector<Map<String, Object>> getImageElements(int pgNo,
			Vector<Map<String, Object>> pageImageElements,
			Vector<VDPElement> vdpElements, double x, double y)
			throws PDFNetException {
		Rectangle2D.Double imageBBox = null;
		Map<String, Object> imageElem = null, contImgElem = null;
		Vector<Map<String, Object>> selectedImages = new Vector<Map<String, Object>>();
		for (int i = 0; i < pageImageElements.size(); i++) {
			imageElem = pageImageElements.elementAt(i);
			imageBBox = (Rectangle2D.Double) imageElem.get("ElementBBox");
			if (!imageBBox.contains(x, y))
				continue;
			RGPTLogger.logDebugMesg("Selected Image: " + imageElem.toString());

			// This check indicates the image element is not in VDP Selection
			if (searchVDPElement(vdpElements, pgNo, new Rect(imageBBox),
					SelType.IMAGE) == -1)
				selectedImages.addElement(imageElem);
		}
		if (selectedImages.size() > 1) {
			contImgElem = getContainedImageElem(selectedImages);
			if (contImgElem == null)
				return selectedImages;
			// Found Exact Match
			selectedImages.clear();
			selectedImages.addElement(contImgElem);
		}
		return selectedImages;
	}

	public static Map<String, Object> getContainedImageElem(
			Vector<Map<String, Object>> vdpDataList) {
		Map<String, Object> vdpData = null;
		Map<String, Object> selVDPData = vdpDataList.elementAt(0);
		Rectangle2D.Double vdpBBox = null;
		Rectangle selRect = null, vdpRect = null;
		Rectangle2D.Double selBBox = (Rectangle2D.Double) selVDPData
				.get("ElementBBox");
		selRect = RGPTUtil.getRectangle(selBBox);
		System.out
				.println("Init Contained Elem BBox is: " + selBBox.toString());
		for (int i = 1; i < vdpDataList.size(); i++) {
			vdpData = vdpDataList.elementAt(i);
			vdpBBox = (Rectangle2D.Double) vdpData.get("ElementBBox");
			vdpRect = RGPTUtil.getRectangle(vdpBBox);
			RGPTLogger.logDebugMesg("Checking Next Contained Elem BBox is: "
					+ vdpBBox.toString());
			if (vdpBBox.contains(selBBox) || vdpRect.contains(selRect))
				continue;
			if (selBBox.contains(vdpBBox) || selRect.contains(vdpRect)) {
				selVDPData = vdpData;
				RGPTLogger.logDebugMesg("New Contained Elem BBox is: "
						+ vdpBBox.toString());
				selBBox = vdpBBox;
				selRect = vdpRect;
				continue;
			}
			return null;
		}
		return selVDPData;
	}

	/**
	 * Retrieves the PDF Image Element from the Image Element Map
	 * 
	 * @param page
	 * @param imgElemMap
	 * @return PDF Image Element
	 * @throws PDFNetException
	 */
	public static Element getMappedImageElement(Page page,
			Map<String, Object> imgElemMap) throws PDFNetException {
		ElementReader reader = new ElementReader();
		reader.begin(page);
		Element selElem = getSelectedImageElement(reader, imgElemMap);
		reader.end();
		if (selElem != null)
			return selElem;
		throw new RuntimeException(
				"Unable to get Element Object for selected Image");
	}

	private static Element getSelectedImageElement(ElementReader reader,
			Map<String, Object> imgElemMap) throws PDFNetException {
		Rect rect;
		Element element;
		Rectangle2D imageBBox = (Rectangle2D) imgElemMap.get("ElementBBox");
		while ((element = reader.next()) != null) {
			switch (element.getType()) {
			case Element.e_image:
			case Element.e_inline_image: {
				System.out
						.println("Entered Element for e_image no - Element Type "
								+ element.getType());
				rect = element.getBBox();
				if (rect == null)
					break;
				if (!(rect.getRectangle()).equals(imageBBox))
					continue;
				// if (selImgObjnum == imgObjNum)
				return element;
			}

			case Element.e_form: {
				reader.formBegin();
				Element formElem = getSelectedImageElement(reader, imgElemMap);
				reader.end();
				if (formElem != null)
					return formElem;
			}
				break;
			} // switch(element.getType())
		} // while ((element = reader.next()) != null)
		return null;
	}

	public static ImageHolder extractImage(PDFDoc doc,
			Map<String, Object> mappedImageElement) {
		int thumbWidth = 50, thumbHt = 50;
		ImageHolder imgHldr = null;
		BufferedImage bigImage = null, thumbImage = null;
		try {
			long imageObjnum = ((Long) mappedImageElement
					.get("ImageXObjectRef")).longValue();
			Obj imageobj = doc.getSDFDoc().getObj(imageObjnum);
			int cstype = ((Integer) mappedImageElement.get("CSType"))
					.intValue();
			bigImage = PDFUtil.getRGBImage(imageobj, cstype);
			if (bigImage == null)
				return null;
		} catch (Exception ex) {
			ex.printStackTrace();
			// continue;
			throw new RuntimeException(
					"Unable to convert PDF Image to Buffered Image");
		}
		// ImageUtils.displayImage(bigImage, "Image Viewer");
		thumbImage = ImageUtils.ScaleToSize(bigImage, thumbWidth, thumbHt);
		// ImageUtils.displayImage(thumbImage, "Image Viewer");
		imgHldr = new ImageHolder(-1, thumbImage, bigImage);
		imgHldr.m_UseCachedImages = true;
		return imgHldr;
	}

}
