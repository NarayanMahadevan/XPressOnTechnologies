// RGPT PACKAGES
package com.rgpt.util;

import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public interface RGPTActionListener extends ActionListener, MouseListener,
		MouseMotionListener, KeyListener {
	// START Variables for action performed in XPressOnDesigner

	// XPressOnDesigner Actions
	public static enum PDFTemplateActions {
		// File Actions
		FILE_OPEN, FILE_CLOSE, FILE_SAVE, FILE_UPLOAD,

		// XON Designer Try Personalization
		TRY_DESIGN,

		// Actions for selecting PDF Element
		SELECT_IMAGE, SELECT_WORD, SELECT_LINE, SELECT_PARA,

		// Actions to Edit and Delete Elements
		EDIT_ELEM, DELETE_ELEM,

		// Action for Inserting Elements into the Graphic Content
		INSERT_TEXT_ON_LINE,
		// INSERT_TEXT, INSERT_IMAGE, INSERT_SHAPE,
		// CREATE_PATH, CREATE_LINE_PATH_MENU, CREATE_QUAD_PATH_MENU,
		// CREATE_CUBIC_PATH_MENU,

		// Page Navigation
		PREV_PAGE, NEXT_PAGE
	}

	// END Variables for action performed in XPressOnDesigner

	// START Variable for XPressOnDesigner Button Activation

	public static String[] PDF_BUTTON_ACTIVATION_IDS = {
			PDFTemplateActions.FILE_CLOSE.toString(),
			PDFTemplateActions.FILE_SAVE.toString(),
			PDFTemplateActions.FILE_UPLOAD.toString(),
			PDFTemplateActions.TRY_DESIGN.toString(),
			PDFTemplateActions.SELECT_IMAGE.toString(),
			PDFTemplateActions.SELECT_WORD.toString(),
			PDFTemplateActions.SELECT_LINE.toString(),
			PDFTemplateActions.SELECT_PARA.toString(),
			PDFTemplateActions.PREV_PAGE.toString(),
			PDFTemplateActions.NEXT_PAGE.toString(),
			PDFTemplateActions.EDIT_ELEM.toString(),
			PDFTemplateActions.DELETE_ELEM.toString(),
			PDFTemplateActions.INSERT_TEXT_ON_LINE.toString() };

	public static String[] ELEM_SEL_BUTTON_ACTIVATION_IDS = {};

	// END Variable for XPressOnDesigner Button Activation

	// START Variables for action performed in XONIMageDesigner App

	public static enum IDAppsActions {
		// File Action Button
		FILE_OPEN, FILE_CLOSE, FILE_NEW, FILE_SAVE, FILE_UPLOAD,
		// New Image, Text, and Shape Elements Actions. MA indicates Main Action
		// Component
		MA_ADD_IMAGE, CREATE_SHAPE, MA_ADD_SHAPE, MA_ADD_TEXT,
		// ToolBar Action Components
		TB_SELECT_ELEMENT, MA_SELECT_BG, TB_EDIT_ELEMENT, TB_TRANSLATE_SHAPE, TB_SCALE_SHAPE, TB_ROTATE_SHAPE, TB_EDIT_SHAPE, TB_ADJUST_IMAGE, TB_SET_ALPHA, TB_SET_ALPHA_VALUE, TB_SHOW_HIDE_ELEM, TB_DELETE_ELEM, TB_PREVIEW_XPRESSON,
		// Zoom the Canvas Area
		INCREASE_ZOOM, DECREASE_ZOOM
	}

	// START Variables for action performed in XONIMageDesigner App

	// START Variables for action performed in XONIMageMaker App

	public static enum ImageActions {
		// File Action Button
		IMAGE_FILE_OPEN, IMAGE_FILE_CLOSE, IMAGE_FILE_SAVE, IMAGE_UPLOAD, IMAGE_COMPRESS,

		// Cutout Actions
		CUTOUT_SHAPES, CUTOUT_IMAGE,

		// Image Selections and Update
		IMAGE_PIXEL_SEL, IMAGE_CROP_SEL, PATCH_SEL, CHANGE_PIXEL_SEL,

		// Image Filter Action
		IMAGE_FILTERS,

		// Zoom Actions
		INCREASE_ZOOM, DECREASE_ZOOM,

		// Workflow Step Actions
		// DRAW_GRAPHIC_PATH_STEP, ADJUST_GRAPHIC_PATH_STEP, PREVIEW_SHAPE,
		// SAVE_SHAPE,
		// PREVIEW_IMAGE_CUTOUT, SAVE_IMAGE_CUTOUT,

		// Menu Action Items
		SHAPE_LINE_PATH_MENU, SHAPE_QUAD_PATH_MENU, SHAPE_CUBIC_PATH_MENU, IMAGE_LINE_PATH_MENU, IMAGE_QUAD_PATH_MENU, IMAGE_CUBIC_PATH_MENU, PATCH_PIXEL_SEL_MENU, PATCH_PIXEL_AREA_SEL_MENU, MAKE_PIXEL_TRANSPERENT_MENU, CHANGE_PIXEL_COLOR_MENU
	}

	// END Variables for action performed in XONIMageMaker App

	// START Variables for action performed for Image Filters

	public static enum ImageFilterActions {
		// Image Enhance Filters
		// Actions for Sharpen Image Filter
		SharpenFilter, SharpenFilter_On, SharpenFilter_Off, SharpenFilter_Def, SharpenFilter_Cancel,
		// Actions for Sharpen Image Filter
		ReduceNoiseFilter, ReduceNoiseFilter_On, ReduceNoiseFilter_Off, ReduceNoiseFilter_Def, ReduceNoiseFilter_Cancel,
		// Actions for LightingFilter to beef up lighting of an Image
		LightingFilter, LightingFilter_Slider, LightingFilter_TextBox,
		// Actions for GlowFilter to Add a glow to an image
		GlowFilter, GlowFilter_Slider, GlowFilter_TextBox,
		// Actions for Subtraction blurred version of the image from the
		// original image.
		RemoveBlurFilter, RemoveBlurFilter_Slider, RemoveBlurFilter_TextBox, RemoveBlurFilter_Inc, RemoveBlurFilter_Dec, RemoveBlurFilter_Def, RemoveBlurFilter_Cancel,
		// Actions for Contrast Image Filter
		ContrastFilter, ContrastFilter_Slider, ContrastFilter_TextBox, ContrastFilter_Inc, ContrastFilter_Dec, ContrastFilter_Def, ContrastFilter_Cancel,
		// Actions for Brightness Image Filter
		GainFilter, GainFilter_Inc, GainFilter_Dec, GainFilter_Def, GainFilter_Cancel,
		// Actions for Brightness Image Filter
		BiasFilter, BiasFilter_Inc, BiasFilter_Dec, BiasFilter_Def, BiasFilter_Cancel,
		// Actions for Brightness Image Filter
		BrightnessFilter, BrightnessFilter_Inc, BrightnessFilter_Dec, BrightnessFilter_Def, BrightnessFilter_Cancel,
		// Actions for Exposure Image Filter
		ExposureFilter, ExposureFilter_Inc, ExposureFilter_Dec, ExposureFilter_Def, ExposureFilter_Cancel,

		// Color Filters
		// Actions to adjust Hue in the Image
		HueFilter, HueFilter_Next, HueFilter_Prev, HueFilter_Inc, HueFilter_Dec, HueFilter_Def, HueFilter_Cancel,
		// Actions to adjust Saturation in the Image
		SaturationFilter, SaturationFilter_Inc, SaturationFilter_Dec, SaturationFilter_Def, SaturationFilter_Cancel,
		// Actions to adjust Brightness in the Image
		ColorBrightnessFilter, ColorBrightnessFilter_Inc, ColorBrightnessFilter_Dec, ColorBrightnessFilter_Def, ColorBrightnessFilter_Cancel,
		// Actions to apply different Color Effects on the Image
		ColorEffectFilter, ColorEffectFilter_Inc, ColorEffectFilter_Dec, ColorEffectFilter_Def, ColorEffectFilter_Cancel,
		// Actions to adjust Red in the Image
		AdjustRedFilter, AdjustRedFilter_Inc, AdjustRedFilter_Dec, AdjustRedFilter_Def, AdjustRedFilter_Cancel,
		// Actions to adjust Green in the Image
		AdjustGreenFilter, AdjustGreenFilter_Inc, AdjustGreenFilter_Dec, AdjustGreenFilter_Def, AdjustGreenFilter_Cancel,
		// Actions to adjust Blue in the Image
		AdjustBlueFilter, AdjustBlueFilter_Inc, AdjustBlueFilter_Dec, AdjustBlueFilter_Def, AdjustBlueFilter_Cancel,

		// Image Effects Filters
		CircleFilter, CircleFilter_On, CircleFilter_Active, CircleFilter_Off, PinchFilter, PinchFilter_On, PinchFilter_Off, BulgeFilter, BulgeFilter_On, BulgeFilter_Off, SphereFilter, SphereFilter_On, SphereFilter_Off, WaterFilter, WaterFilter_On, WaterFilter_Off, WaterRippleFilter, WaterRippleFilter_On, WaterRippleFilter_Off, TwirlFilter, TwirlFilter_On, TwirlFilter_Off, CircleLightFilter, CircleLightFilter_On, CircleLightFilter_Off, SpotLightFilter, SpotLightFilter_On, SpotLightFilter_Off, PointLightFilter, PointLightFilter_On, PointLightFilter_Off, FlareLightFilter, FlareLightFilter_On, FlareLightFilter_Off, RingLightFilter, RingLightFilter_On, RingLightFilter_Off, SparkleFilter, SparkleFilter_On, SparkleFilter_Off,

		// Fade Effects
		SplitFadeFilter, SplitFadeFilter_On, SplitFadeFilter_Off, UniformFadeFilter, UniformFadeFilter_On, UniformFadeFilter_Off, BoxFadeFilter, BoxFadeFilter_On, BoxFadeFilter_Off, LinearFadeFilter, LinearFadeFilter_On, LinearFadeFilter_Off, HorizontalFadeFilter, HorizontalFadeFilter_On, HorizontalFadeFilter_Off, VerticalFadeFilter, VerticalFadeFilter_On, VerticalFadeFilter_Off, NoiseFadeFilter, NoiseFadeFilter_On, NoiseFadeFilter_Off, BlurFadeFilter, BlurFadeFilter_On, BlurFadeFilter_Off, DotColorFadeFilter, DotColorFadeFilter_On, DotColorFadeFilter_Off, CrystallizeFadeFilter, CrystallizeFadeFilter_On, CrystallizeFadeFilter_Off, PointillizeFadeFilter, PointillizeFadeFilter_On, PointillizeFadeFilter_Off,

		// Border Effects Filter
		DotColorBorderFilter, DotColorBorderFilter_On, DotColorBorderFilter_Off, CrystallizeBorderFilter, CrystallizeBorderFilter_On, CrystallizeBorderFilter_Off, PointillizeBorderFilter, PointillizeBorderFilter_On, PointillizeBorderFilter_Off,

		// Simple Image Effects Filters
		GrayScaleFilter, GrayScaleFilter_On, GrayScaleFilter_Off, BWFilter, BWFilter_On, BWFilter_Off, SpectrumColorFilter, SpectrumColorFilter_On, SpectrumColorFilter_Off, InvertFilter, InvertFilter_On, InvertFilter_Off, SoloarizeFilter, SoloarizeFilter_On, SoloarizeFilter_Off, HalftoneFilter, HalftoneFilter_On, HalftoneFilter_Off, BurstedImageFilter, BurstedImageFilter_On, BurstedImageFilter_Off, EmbossFilter, EmbossFilter_On, EmbossFilter_Off, GrayOutFilter, GrayOutFilter_On, GrayOutFilter_Off, ComicFilter, ComicFilter_On, ComicFilter_Off, BWComicFilter, BWComicFilter_On, BWComicFilter_Off, SketchedFilter, SketchedFilter_On, SketchedFilter_Off, ComicSketchedFilter, ComicSketchedFilter_On, ComicSketchedFilter_Off, ColorSketchFilter, ColorSketchFilter_On, ColorSketchFilter_Off, NegativeFilter, NegativeFilter_On, NegativeFilter_Off, SkeletonFilter, SkeletonFilter_On, SkeletonFilter_Off, MirrorFilter, MirrorFilter_On, MirrorFilter_Off, MultipleImageFilter, MultipleImageFilter_On, MultipleImageFilter_Off, KaleidoscopeFilter, KaleidoscopeFilter_On, KaleidoscopeFilter_Off, LightUpFilter, LightUpFilter_On, LightUpFilter_Off, TemperatureFilter, TemperatureFilter_On, TemperatureFilter_Off, GammaFilter, GammaFilter_On, GammaFilter_Off, PosterizeFilter, PosterizeFilter_On, PosterizeFilter_Off, ImageOutlineFilter, ImageOutlineFilter_On, ImageOutlineFilter_Off, BWSketchedFilter, BWSketchedFilter_On, BWSketchedFilter_Off, WarpFilter, WarpFilter_On, WarpFilter_Off, SwizzleFilter, SwizzleFilter_On, SwizzleFilter_Off,

		// Distortion Filters
		CrystallizeFilter, CrystallizeFilter_On, CrystallizeFilter_Off, PointillizeFilter, PointillizeFilter_On, PointillizeFilter_Off, SpotColorFilters, SpotColorFilters_On, SpotColorFilters_Off, NoiseFilter, NoiseFilter_On, NoiseFilter_Off, DissolveFilter, DissolveFilter_On, DissolveFilter_Off, OilFilter, OilFilter_On, OilFilter_Off, MarbleFilter, MarbleFilter_On, MarbleFilter_Off, SpotColorFilters_Next, SpotColorFilters_Prev, SpotColorFilters_Cancel, DiffuseFilter, DiffuseFilter_On, DiffuseFilter_Off, DiffuseFilter_Slider, DiffuseFilter_TextBox,

	}

	// Exceptions to Points Adjustments
	public static ImageFilterActions[] RADIAL_ADJ_FILTER_EXCEPTIONS = {
			ImageFilterActions.PosterizeFilter,
			ImageFilterActions.MultipleImageFilter,
			ImageFilterActions.CrystallizeFilter,
			ImageFilterActions.PointillizeFilter, };

	public static ImageFilterActions[] CENTER_ADJ_FILTER_EXCEPTIONS = {
			ImageFilterActions.PosterizeFilter,
			ImageFilterActions.KaleidoscopeFilter,
			ImageFilterActions.CrystallizeFilter,
			ImageFilterActions.PointillizeFilter, };

	// Image Filters which uses Points to control the effects of Different
	// Filters
	public static ImageFilterActions[] CENTER_SLIDER_RADIAL_PTS_FILTERS = {
			ImageFilterActions.PinchFilter, ImageFilterActions.SparkleFilter,
			ImageFilterActions.BulgeFilter, ImageFilterActions.SphereFilter,
			ImageFilterActions.WaterFilter,
			ImageFilterActions.WaterRippleFilter,
			ImageFilterActions.CircleLightFilter,
			ImageFilterActions.FlareLightFilter,
			ImageFilterActions.PosterizeFilter,
			ImageFilterActions.RingLightFilter };

	public static ImageFilterActions[] CENTER_RADIAL_PTS_FILTERS = {
			ImageFilterActions.InvertFilter,
			ImageFilterActions.GrayScaleFilter,
			ImageFilterActions.SpectrumColorFilter,
			ImageFilterActions.NegativeFilter,
			ImageFilterActions.SoloarizeFilter,
			ImageFilterActions.NegativeFilter, };

	public static ImageFilterActions[] ALL_CONTROL_PTS_FILTERS = {
			ImageFilterActions.SpotLightFilter,
			ImageFilterActions.PointLightFilter,
			ImageFilterActions.MultipleImageFilter,
			ImageFilterActions.CrystallizeFilter,
			ImageFilterActions.PointillizeFilter, };

	public static ImageFilterActions[] CENTER_SLIDER_ANGLE_PTS_FILTERS = {
			ImageFilterActions.KaleidoscopeFilter,
			ImageFilterActions.SpotColorFilters,
			ImageFilterActions.SplitFadeFilter,
			ImageFilterActions.UniformFadeFilter,
			ImageFilterActions.BoxFadeFilter,
			ImageFilterActions.LinearFadeFilter,
			ImageFilterActions.NoiseFadeFilter,
			ImageFilterActions.BlurFadeFilter,
			ImageFilterActions.HorizontalFadeFilter,
			ImageFilterActions.VerticalFadeFilter,
			ImageFilterActions.DotColorBorderFilter,
			ImageFilterActions.DotColorFadeFilter,
			ImageFilterActions.CrystallizeFadeFilter,
			ImageFilterActions.CrystallizeBorderFilter,
			ImageFilterActions.PointillizeFadeFilter,
			ImageFilterActions.PointillizeBorderFilter, };

	public static ImageFilterActions[] CENTER_ANGLE_RADIAL_PTS_FILTERS = { ImageFilterActions.TwirlFilter };

	public static ImageFilterActions[] CENTER_SLIDER_PTS_FILTERS = {
			ImageFilterActions.ColorSketchFilter,
			ImageFilterActions.BurstedImageFilter, ImageFilterActions.BWFilter,
			ImageFilterActions.SkeletonFilter,
			ImageFilterActions.TemperatureFilter,
			ImageFilterActions.GammaFilter,
			ImageFilterActions.ImageOutlineFilter,
			ImageFilterActions.MirrorFilter, ImageFilterActions.LightUpFilter,
			ImageFilterActions.NoiseFilter, ImageFilterActions.DissolveFilter,
			ImageFilterActions.OilFilter, ImageFilterActions.DiffuseFilter,
			ImageFilterActions.MarbleFilter, ImageFilterActions.SwizzleFilter };

	// Image Filters which uses OnOffParamControl to produce Different Effects
	public static ImageFilterActions[] ON_OFF_PARAM_CONTROL_IMAGE_FILTERS = {
			ImageFilterActions.CircleFilter, ImageFilterActions.PinchFilter,
			ImageFilterActions.BulgeFilter, ImageFilterActions.SphereFilter,
			ImageFilterActions.WaterFilter,
			ImageFilterActions.WaterRippleFilter,
			ImageFilterActions.CircleLightFilter,
			ImageFilterActions.TwirlFilter, ImageFilterActions.SpotLightFilter,
			ImageFilterActions.PointLightFilter,
			ImageFilterActions.FlareLightFilter,
			ImageFilterActions.RingLightFilter,
			ImageFilterActions.SparkleFilter,
			ImageFilterActions.ColorSketchFilter,
			ImageFilterActions.BurstedImageFilter, ImageFilterActions.BWFilter,
			ImageFilterActions.SkeletonFilter, ImageFilterActions.MirrorFilter,
			ImageFilterActions.MultipleImageFilter,
			ImageFilterActions.KaleidoscopeFilter,
			ImageFilterActions.LightUpFilter,
			ImageFilterActions.TemperatureFilter,
			ImageFilterActions.GammaFilter, ImageFilterActions.PosterizeFilter,
			ImageFilterActions.ImageOutlineFilter,
			ImageFilterActions.SpotColorFilters,
			ImageFilterActions.NoiseFilter, ImageFilterActions.DissolveFilter,
			ImageFilterActions.DiffuseFilter, ImageFilterActions.MarbleFilter,
			ImageFilterActions.OilFilter, ImageFilterActions.WarpFilter,
			ImageFilterActions.SwizzleFilter,
			ImageFilterActions.SplitFadeFilter,
			ImageFilterActions.UniformFadeFilter,
			ImageFilterActions.BoxFadeFilter,
			ImageFilterActions.LinearFadeFilter,
			ImageFilterActions.HorizontalFadeFilter,
			ImageFilterActions.VerticalFadeFilter,
			ImageFilterActions.NoiseFadeFilter,
			ImageFilterActions.BlurFadeFilter,
			ImageFilterActions.DotColorBorderFilter,
			ImageFilterActions.DotColorFadeFilter,
			ImageFilterActions.InvertFilter, ImageFilterActions.GrayOutFilter,
			ImageFilterActions.GrayScaleFilter,
			ImageFilterActions.NegativeFilter,
			ImageFilterActions.SpectrumColorFilter,
			ImageFilterActions.BWComicFilter,
			ImageFilterActions.ComicSketchedFilter,
			ImageFilterActions.EmbossFilter,
			ImageFilterActions.BWSketchedFilter,
			ImageFilterActions.SoloarizeFilter,
			ImageFilterActions.CrystallizeFilter,
			ImageFilterActions.CrystallizeFadeFilter,
			ImageFilterActions.CrystallizeBorderFilter,
			ImageFilterActions.PointillizeFilter,
			ImageFilterActions.PointillizeFadeFilter,
			ImageFilterActions.PointillizeBorderFilter, };

	// END Variables for action performed for Image Filters

	// START Variables for action performed in UI Workflow Navigation Steps

	public static enum WFActions {
		// Workflow Step For Shape Cutout
		DRAW_SHAPE_PATH_STEP, ADJUST_SHAPE_PATH_STEP, PREVIEW_SHAPE, SAVE_SHAPE,
		// Workflow Step For Image Cutout
		DRAW_GRAPHIC_PATH_STEP, ADJUST_GRAPHIC_PATH_STEP, PREVIEW_IMAGE_CUTOUT, SAVE_IMAGE_CUTOUT, SAVE_IMAGE_TO_FILE, SAVE_IMAGE_IN_MEM,
		// Workflow Step For Patch Pixels
		SELECT_PATCH_PIXEL_STEP, APPLY_PATCH_PIXEL_STEP, PREVIEW_PATCH_IMAGE, SAVE_PATCH_IMAGE, SAVE_PATCH_IMAGE_TO_FILE, SAVE_PATCH_IMAGE_IN_MEM,
		// Workflow Step For Changing Pixels
		SELECT_IMAGE_LOCATION_STEP, SELECT_CHANGE_PIXEL_STEP, SEL_IMAGE_PIXEL_COLOR, SEL_CUTOUT_SHAPE, SET_COLOR_RANGE, MODIFY_PIXEL_STEP, ADD_IMAGE_PIXEL_COLOR, REM_IMAGE_PIXEL_COLOR, ADD_PATCH_PIXEL, REM_PATCH_PIXEL, SET_BRUSH_SIZE, PREVIEW_CHANGE_IMAGE, SAVE_CHANGE_IMAGE, SAVE_CHANGE_IMAGE_TO_FILE, SAVE_CHANGE_IMAGE_IN_MEM,
		// WF Step to Add Image in XON Image Designer
		LAUNCH_IMAGE_MAKEOVER_STEP, IMAGE_ENHANCE_FILTERS, IMAGE_EFFECTS_FILTERS, SET_IMAGE_FILTERS, NEW_WF_IMAGE_FILTERS_STEP, SET_IMAGE_SHAPE_STEP, NEW_WF_DRAW_SHAPE_MENU, NEW_WF_SELECT_IMAGE_SHAPE_MENU, NEW_WF_SET_IMAGE_TEXT_STEP,
		// Image Effect Filters
		FUNNY_EFFECT_FILTERS, COLOR_EFFECT_FILTERS, LIGHT_EFFECT_FILTERS, FADE_EFFECT_FILTERS,
		// WF Steps for Image Filters
		BORDER_EFFECT_FILTERS, DISTORTION_FILTERS, BLUR_FILTERS, COLOR_FILTERS, STYLE_FILTERS, END_IMAGE_FILTER_SETTINGS,
		// WF for Draw Shape Menu for Image
		Draw_IMAGE_PATH_STEP, ADJUST_IMAGE_PATH_STEP, SAVE_IMAGE_SHAPE_STEP,
		// WF to Add Shape in XON Image Designer
		NEW_WF_SET_SHAPE_STEP, NEW_WF_SET_SHAPE_TEXT_STEP, SET_SHAPE_NEW_PANEL_STEP,
		// WF to Add Text in XON Image Designer
		SET_TEXT_PATH_STEP, SET_TEXT_NEW_PANEL_STEP,
		// This Step is common to all Workflow and cancels the current operation
		END_SETTINGS, END_CREATE_SHAPE_WF, RESET_WF;

		public static boolean isShapePreviewAction(String action) {
			if (PREVIEW_SHAPE.toString().equals(action))
				return true;
			return false;
		}
	}

	// END Variables for action performed in XONIMageMaker App

	// START Variables for action performed in a Dialog Box

	public static enum DialogActions {
		// Dialog Box Actions for taking information from User

		// Actions Performed in Save Shape Dialog Box
		SHAPE_TYPE_COMBO_ACTION, SHAPE_TYPE_FIELD_ACTION, SHAPE_NAME_FIELD_ACTION, SAVE_SHAPE_OK_ACTION, SAVE_SHAPE_CANCEL_ACTION,

		// Dialog Box to Set Color Range
		SET_COLOR_RANGE_FIELD, SET_COLOR_RANGE_OK, SET_COLOR_RANGE_CANCEL,

		// Dialog Box to Set The Brush Size
		SET_BRUSH_SIZE_FIELD, SET_BRUSH_SIZE_OK, SET_BRUSH_SIZE_CANCEL
	}

	// END Variables for action performed in XONIMageMaker App

	// START Variable for XONIMageMaker Button Activation

	// Image Filters shown in the Preview remains Active for Actions defined
	// below and Image Filter Actions
	public static String[] SHOW_IMAGE_FILTER_ACTIONS = { WFActions.IMAGE_ENHANCE_FILTERS
			.toString() };

	// The Set Cursor to Busy Cursor will not work if the Paint Function is
	// taking time, it has to be called from Paint function
	// itself, as Paint is a asynch operation.
	public static String[] BUSY_CURSOR_ACTIONS = {
			// Image Designer Actions
			IDAppsActions.FILE_OPEN.toString(),
			IDAppsActions.FILE_CLOSE.toString(),
			IDAppsActions.FILE_NEW.toString(),
			IDAppsActions.MA_ADD_IMAGE.toString(),
			IDAppsActions.FILE_SAVE.toString(),
			IDAppsActions.FILE_UPLOAD.toString(),

			// Image Maker Actions
			WFActions.SAVE_IMAGE_TO_FILE.toString(),
			WFActions.SAVE_CHANGE_IMAGE_TO_FILE.toString(),
			WFActions.SAVE_IMAGE_IN_MEM.toString(),
			WFActions.SAVE_CHANGE_IMAGE_IN_MEM.toString(),
			WFActions.SAVE_PATCH_IMAGE_TO_FILE.toString(),
			ImageActions.IMAGE_FILE_OPEN.toString()

	};

	public static String[] POINT_SELECTION_ACTIONS = {
			WFActions.SEL_IMAGE_PIXEL_COLOR.toString(),
			WFActions.SEL_CUTOUT_SHAPE.toString(),
			ImageActions.CHANGE_PIXEL_COLOR_MENU.toString() };

	// public static String[] DISPLAY_SELECTION_ACTIONS = {
	// ImageActions.MAKE_PIXEL_TRANSPERENT_MENU.toString(),
	// WFActions.MODIFY_PIXEL_STEP.toString(),
	// WFActions.SELECT_IMAGE_LOCATION_STEP.toString(),
	// WFActions.SELECT_CHANGE_PIXEL_STEP.toString(),
	// WFActions.SEL_IMAGE_PIXEL_COLOR.toString(),
	// WFActions.SEL_CUTOUT_SHAPE.toString(),
	// WFActions.ADD_IMAGE_PIXEL_COLOR.toString(),
	// WFActions.REM_IMAGE_PIXEL_COLOR.toString(),
	// ImageActions.CHANGE_PIXEL_COLOR_MENU.toString()
	// };

	public static String[] MODIFY_PIXEL_ACTIONS = {
			WFActions.ADD_IMAGE_PIXEL_COLOR.toString(),
			WFActions.REM_IMAGE_PIXEL_COLOR.toString() };

	public static String[] PATCH_PIXEL_ACTIONS = {
			WFActions.ADD_PATCH_PIXEL.toString(),
			WFActions.REM_PATCH_PIXEL.toString() };

	public static String[] WF_PREVIEW_ACTIONS = {
			WFActions.PREVIEW_SHAPE.toString(),
			WFActions.PREVIEW_IMAGE_CUTOUT.toString(),
			WFActions.PREVIEW_CHANGE_IMAGE.toString() };

	public static String[] ZOOM_ACTIONS = {
			ImageActions.INCREASE_ZOOM.toString(),
			ImageActions.DECREASE_ZOOM.toString() };

	public static String[] IMAGE_MAKER_ACTION_ACTIVATION_IDS = {
			ImageActions.IMAGE_FILE_CLOSE.toString(),
			ImageActions.IMAGE_FILE_SAVE.toString(),
			ImageActions.IMAGE_COMPRESS.toString(),
			ImageActions.CUTOUT_SHAPES.toString(),
			ImageActions.CUTOUT_IMAGE.toString(),
			ImageActions.IMAGE_PIXEL_SEL.toString(),
			ImageActions.IMAGE_CROP_SEL.toString(),
			ImageActions.PATCH_SEL.toString(),
			ImageActions.CHANGE_PIXEL_SEL.toString(),
			ImageActions.IMAGE_FILTERS.toString(),
			ImageActions.DECREASE_ZOOM.toString(),
			ImageActions.INCREASE_ZOOM.toString() };

	public static String[] WF_PROCESS_IDS = {
			IDAppsActions.MA_ADD_IMAGE.toString(),
			IDAppsActions.MA_ADD_TEXT.toString(),
			IDAppsActions.CREATE_SHAPE.toString(),
			IDAppsActions.MA_ADD_SHAPE.toString() };

	public static String[] FILE_CLOSED_ACTIVATED_IDS = {
			ImageActions.IMAGE_FILE_OPEN.toString(),
			IDAppsActions.FILE_OPEN.toString(),
			IDAppsActions.FILE_NEW.toString(),
			IDAppsActions.CREATE_SHAPE.toString() };

	public static String[] CREATE_SHAPE_ACTIVATED_IDS = {
			IDAppsActions.FILE_CLOSE.toString(),
			IDAppsActions.INCREASE_ZOOM.toString(),
			IDAppsActions.DECREASE_ZOOM.toString(),
			IDAppsActions.TB_SELECT_ELEMENT.toString() };

	public static String[] FILE_OPEN_ACTIVATED_IDS = {
			ImageActions.IMAGE_FILE_OPEN.toString(),
			ImageActions.IMAGE_FILE_CLOSE.toString(),
			ImageActions.DECREASE_ZOOM.toString(),
			ImageActions.INCREASE_ZOOM.toString(),
			IDAppsActions.FILE_CLOSE.toString(),
			IDAppsActions.FILE_SAVE.toString(),
			IDAppsActions.FILE_UPLOAD.toString(),
			IDAppsActions.INCREASE_ZOOM.toString(),
			IDAppsActions.DECREASE_ZOOM.toString(),
			IDAppsActions.TB_SELECT_ELEMENT.toString() };

	public static String[] DRAW_GRAPHIC_PATH_IDS = {
			ImageActions.SHAPE_LINE_PATH_MENU.toString(),
			ImageActions.SHAPE_QUAD_PATH_MENU.toString(),
			ImageActions.SHAPE_CUBIC_PATH_MENU.toString(),
			ImageActions.IMAGE_LINE_PATH_MENU.toString(),
			ImageActions.IMAGE_QUAD_PATH_MENU.toString(),
			ImageActions.IMAGE_CUBIC_PATH_MENU.toString(),
			WFActions.SELECT_IMAGE_LOCATION_STEP.toString() };

	public static String[] MENU_ACTION_IDS = {
			ImageActions.SHAPE_LINE_PATH_MENU.toString(),
			ImageActions.SHAPE_QUAD_PATH_MENU.toString(),
			ImageActions.SHAPE_CUBIC_PATH_MENU.toString(),
			ImageActions.IMAGE_LINE_PATH_MENU.toString(),
			ImageActions.IMAGE_QUAD_PATH_MENU.toString(),
			ImageActions.IMAGE_CUBIC_PATH_MENU.toString(),
			ImageActions.PATCH_PIXEL_SEL_MENU.toString(),
			ImageActions.PATCH_PIXEL_AREA_SEL_MENU.toString(),
			ImageActions.MAKE_PIXEL_TRANSPERENT_MENU.toString(),
			ImageActions.CHANGE_PIXEL_COLOR_MENU.toString(),
			// Zoom Btn is added as no deactivation of other button should be
			// done when clicked,
			// Just behave similar to Menu Actions
			ImageActions.DECREASE_ZOOM.toString(),
			ImageActions.INCREASE_ZOOM.toString() };

	// public static String[] CUTOUT_SHAPE_WF_PROCESS_IDS = {
	// ImageActions.CUTOUT_SHAPES.toString(),
	// ImageActions.SHAPE_LINE_PATH_MENU.toString(),
	// ImageActions.SHAPE_QUAD_PATH_MENU.toString(),
	// ImageActions.SHAPE_CUBIC_PATH_MENU.toString(),
	// WFActions.DRAW_GRAPHIC_PATH_STEP.toString(),
	// WFActions.ADJUST_GRAPHIC_PATH_STEP.toString(),
	// WFActions.PREVIEW_SHAPE.toString(), WFActions.SAVE_SHAPE.toString()
	// };

	// public static String[] CUTOUT_IMAGE_WF_PROCESS_IDS = {
	// ImageActions.CUTOUT_IMAGE.toString(),
	// ImageActions.IMAGE_LINE_PATH_MENU.toString(),
	// ImageActions.IMAGE_QUAD_PATH_MENU.toString(),
	// ImageActions.IMAGE_CUBIC_PATH_MENU.toString(),
	// WFActions.DRAW_GRAPHIC_PATH_STEP.toString(),
	// WFActions.ADJUST_GRAPHIC_PATH_STEP.toString(),
	// WFActions.PREVIEW_SHAPE.toString(), WFActions.SAVE_SHAPE.toString()
	// };

	// public static String[] PATCH_PIXEL_WF_PROCESS_IDS = {
	// ImageActions.PATCH_SEL.toString(),
	// WFActions.SELECT_PATCH_PIXEL_STEP.toString(),
	// WFActions.APPLY_PATCH_PIXEL_STEP.toString(),
	// WFActions.PREVIEW_PATCH_IMAGE.toString(),
	// WFActions.SAVE_PATCH_IMAGE.toString()
	// };

	// public static String[] CHANGE_PIXEL_WF_PROCESS_IDS = {
	// ImageActions.CHANGE_PIXEL_SEL.toString(),
	// ImageActions.MAKE_PIXEL_TRANSPERENT_MENU.toString(),
	// ImageActions.CHANGE_PIXEL_COLOR_MENU.toString(),
	// WFActions.SELECT_CHANGE_PIXEL_STEP.toString(),
	// WFActions.PATCH_PIXEL_STEP.toString(),
	// WFActions.PREVIEW_CHANGE_IMAGE.toString(),
	// WFActions.SAVE_CHANGE_IMAGE.toString()
	// };

	// END Variable for XONIMageMaker Button Activation

	// Methods

	// This method returns the mneomonic key for the action id else null
	public String getMnemonicKey(String action);

	// This method is called by MneomonicKeyListener when user presses ctrl O,
	// etc or by
	// the actions performed in the UI
	public void performRGPTAction(String action);

	public void performRGPTWFAction(String action);

	public void performRGPTDialogAction(String action);

	public void performImageFilterAction(String action);

	public void setCursor(String action);

	public void resetCursor();

	public void setComponentData(String action, String compName,
			javax.swing.JComponent comp);
}