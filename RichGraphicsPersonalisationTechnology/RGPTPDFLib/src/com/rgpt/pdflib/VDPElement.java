package com.rgpt.pdflib;

import java.util.HashMap;
import java.util.Map;

/**
 * This holds the PDF Element Selection. This object is compatible to JSON
 * 
 * @author Narayan
 * 
 */

public class VDPElement {

	public int page_num;

	public enum SelType {
		TEXT, IMAGE, PATH
	}

	public enum ImageAttr {
		Name, Length, Value, Type, ThemeId, AllowUploadWithTheme, ImageHeight,
		ImageWidth, ColorComponents, RenderingIntent, BitsPerComponent,
		ImageXObjectRef, UsePictureFrame, OpaquePictureFrame, FrameFilePath,
		ElementCTM, ClipSegmentCount, SetImageMask, ImageMask,
		IsBackgoundImage, IsVDPPrepopulated
	}

	public SelType sel_type;

	// Rectangular BBox indicating the position of the PDF Element in the Page
	// in the PDF Doc and Screen Coordinates
	public PDFRect rect;
	public PDFRect rect_scr;

	// Status of the PDF Element Selection
	public int status;

	// The attributes of PDF Element in Name Value pair...
	public Map<String, Object> namevalue;

	public VDPElement(int pageNum, SelType selType, PDFRect bbox) {
		page_num = pageNum;
		sel_type = selType;
		rect = bbox;
		status = 0;
		namevalue = new HashMap<String, Object>();
	}
}
