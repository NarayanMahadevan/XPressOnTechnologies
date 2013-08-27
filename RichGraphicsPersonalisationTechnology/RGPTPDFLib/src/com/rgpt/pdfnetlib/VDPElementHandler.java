package com.rgpt.pdfnetlib;

import pdftron.Common.Matrix2D;
import pdftron.Common.PDFNetException;
import pdftron.PDF.Element;
import pdftron.PDF.Rect;

import com.rgpt.pdflib.PDFLibException;
import com.rgpt.pdflib.VDPElement;
import com.rgpt.pdflib.VDPElement.ImageAttr;
import com.rgpt.pdflib.VDPElement.SelType;

public class VDPElementHandler {

	public static VDPElement createVDPElement(int pgNum, SelType selType,
			Element elem, int selIndex) throws PDFNetException, PDFLibException {
		Rect rect = elem.getBBox();
		PDFNetRect pdfRect = new PDFNetRect(rect);
		VDPElement vdpElement = new VDPElement(pgNum, selType, pdfRect);
		// Image Selection
		if (selType.equals(SelType.IMAGE)) {
			loadImageAttributes(vdpElement, elem, selIndex);
		}

		return vdpElement;
	}

	public static void loadImageAttributes(VDPElement vdpElement,
			Element element, int index) throws PDFNetException {
		// Refer line 1697
		String text = "Not Applicable";
		int length = element.getImageDataSize();
		vdpElement.namevalue.put(ImageAttr.Name.toString(), "VdpImg" + index);
		vdpElement.namevalue.put(ImageAttr.Length.toString(),
				String.valueOf(length));
		vdpElement.namevalue.put(ImageAttr.Value.toString(), text);
		vdpElement.namevalue.put(ImageAttr.Type.toString(), "Image");
		vdpElement.namevalue.put(ImageAttr.ThemeId.toString(), -1);
		vdpElement.namevalue.put(ImageAttr.AllowUploadWithTheme.toString(),
				false);
		vdpElement.namevalue.put(ImageAttr.ImageHeight.toString(),
				element.getImageHeight());
		vdpElement.namevalue.put(ImageAttr.ImageWidth.toString(),
				element.getImageWidth());
		vdpElement.namevalue.put(ImageAttr.ColorComponents.toString(),
				element.getComponentNum());
		vdpElement.namevalue.put(ImageAttr.RenderingIntent.toString(),
				element.getImageRenderingIntent());
		vdpElement.namevalue.put(ImageAttr.BitsPerComponent.toString(),
				element.getBitsPerComponent());
		vdpElement.namevalue.put(ImageAttr.ImageXObjectRef.toString(), element
				.getXObject().getObjNum());
		vdpElement.namevalue.put(ImageAttr.UsePictureFrame.toString(), false);
		vdpElement.namevalue
				.put(ImageAttr.OpaquePictureFrame.toString(), false);
		vdpElement.namevalue.put(ImageAttr.FrameFilePath.toString(), "");

		// NARAYAN: Added Element CTM to the Selection Object
		Matrix2D elemCTM = element.getCTM();
		System.out.println("ELEMENT CTM " + elemCTM.getA() + " "
				+ elemCTM.getB() + " " + elemCTM.getC() + " " + elemCTM.getD()
				+ " " + elemCTM.getH() + " " + elemCTM.getV());
		vdpElement.namevalue.put(ImageAttr.ElementCTM.toString(), elemCTM);
		vdpElement.namevalue.put(ImageAttr.ClipSegmentCount.toString(),
				new Integer(0));

		// Setting default values for Image Mask
		vdpElement.namevalue.put(ImageAttr.SetImageMask.toString(), false);
		vdpElement.namevalue.put(ImageAttr.ImageMask.toString(), "");
		vdpElement.namevalue.put(ImageAttr.IsBackgoundImage.toString(), false);
		vdpElement.namevalue.put(ImageAttr.IsVDPPrepopulated.toString(), false);

	}
}
