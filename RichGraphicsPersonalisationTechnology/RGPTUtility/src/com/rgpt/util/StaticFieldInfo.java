// RGPT PACKAGES
package com.rgpt.util;

public class StaticFieldInfo {
	public static String ALLIGN_LEFT = "Left Allign";
	public static String ALLIGN_CENTER = "Center Allign";
	public static String ALLIGN_RIGHT = "Right Allign";
	public static String ALLIGN_PAGE_LEFT = "Page Left Allign";
	public static String ALLIGN_PAGE_CENTER = "Page Center Allign";
	public static String ALLIGN_PAGE_RIGHT = "Page Right Allign";

	public static String IMAGE_PATH = "res/pdfviewer/";

	public final static String HORZ_LEFT_ALLIGN = "LEFT";
	public final static String HORZ_RIGHT_ALLIGN = "RIGHT";
	public final static String HORZ_CENTER_ALLIGN = "CENTER";

	public final static String VERT_TOP_ALLIGN = "TOP";
	public final static String VERT_BOTTOM_ALLIGN = "BOTTOM";
	public final static String VERT_CENTER_ALLIGN = "CENTER";

	// Used by PDFViewMain to identify User Action for Text Selectopn
	// vdpTextMode
	public final static int WORD = 0;
	public final static int LINE = 1;
	public final static int PARA = 2;

	// Used by PDFViewMain to identify User Action vdpmode
	public final static int MARK_TEXT = 1;
	public final static int MARK_IMAGE = 2;
	public final static int INSERT_TEXT_ALONG_PATH = 3;
	public final static int INSERT_TEXT = 4;

	// This are Used to Modify Selection
	public final static int DEFAULT_EDIT_SEL = 0;
	public final static int EDIT_SEL_ELEM = 1;
	public final static int DEL_SEL_ELEM = 2;
	public final static int MASK_SEL_ELEM = 3;

	// Used by PDFViewer to identify Selection Type zelement.seltype
	public final static int TEXT_SEL = 1;
	public final static int IMAGE_SEL = 2;
	public final static int PATH_SEL = 3;
	public final static int TEXT_ON_GRAPHICS_SEL = 4;

	// Used for VDP Text Sel to specify eitther Field Lenght is Fixed, or BBox
	// is
	// Fixed or Auto Fit Text in the BBox
	public final static String FIELD_LENGTH_FIXED = "Field Length Fixed";
	public final static String TEXT_WIDTH_FIXED = "Text Width Fixed";

	public final static String AUTO_FIT_TEXT = "Auto Fit Text";
	public final static String KEEP_FONT_SIZE = "Keep Font Size";

	public final static String VARIABLE_TEXT_ENTRY = "Variable Text";
	public final static String FIXED_TEXT_ENTRY = "Fixed Text";
	public final static String OPTIONAL_TEXT_ENTRY = "Optional Text";

	// Font Weight
	public final static String WEIGHT_LIGHT = "Light";
	public final static String WEIGHT_REGULAR = "Regular";
	public final static String WEIGHT_MEDIUM = "Medium";
	public final static String WEIGHT_BOLD = "Bold";
	public final static String WEIGHT_HEAVY = "Heavy";
	public final static String WEIGHT_ULTRABOLD = "Ultra Bold";

	// Fill Shape Logics
	public final static String NO_FILL = "No Fill";
	public final static String COLOR_FILL = "Fill with Color";
	public final static String IMAGE_FILL = "Fill with Image";
	public final static String USER_IMAGE_FILL = "Fill with User IMAGE";

	public static int LINE_PATH = 0;
	public static int CIRCLE_PATH = 1;
	public static int ELLIPSE_PATH = 2;
	public static int SQUARE_PATH = 3;
	public static int RECTANGLE_PATH = 4;
	public static int GRAPHIC_PATH = 5;

	// This are indicaters to view Image along Width or Height.
	public final transient static int SCALED_ALONG_HEIGHT = 0;
	public final transient static int SCALED_ALONG_WIDTH = 1;

	public static String[] TEXT_ALONG_SHAPES = { "LINE", "CIRCLE", "ELLIPSE",
			"SQUARE", "RECTANGLE" };

	// START Variables for Enums

	public static enum VDPFieldType {
		Text, Image, Shape
	}

	public static enum GraphicsPaths {
		LINE_PATH, QUAD_PATH, CUBIC_PATH;

		public static boolean isLinePath(String action) {
			if (LINE_PATH.toString().equals(action))
				return true;
			return false;
		}

		public static boolean isQuadPath(String action) {
			if (QUAD_PATH.toString().equals(action))
				return true;
			return false;
		}

		public static boolean isCubicPath(String action) {
			if (CUBIC_PATH.toString().equals(action))
				return true;
			return false;
		}
	}

	public static GraphicsPaths DEF_SHAPE_PATH = GraphicsPaths.CUBIC_PATH;

	public static enum XONDesignType {
		IMAGE_DESIGNS, PDF_DESIGNS, SHAPE_DESIGNS
	}

	// Mouse Actions, No UI Events are registered. It acts as indicator to Mouse
	// Motion
	public static enum MouseActions {
		TRANSLATED, SCALED, ROTATED, PATH_ADJUSTED, CIRCLE_FILTER_ADJUSTED, RADIAL_FILTER_ADJUSTED, FILTER_CIRCLE_ADJUSTED, GRID_POINT_ADJUSTED
	}

	// END Variables for action performed in XONIMageMaker App
}
