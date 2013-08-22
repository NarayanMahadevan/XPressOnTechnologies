// RGPT PACKAGES 
package com.rgpt.imageutil;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import com.rgpt.imagefilters.BlockFilter;
import com.rgpt.imagefilters.ChromeFilter;
import com.rgpt.imagefilters.CircleFilter;
import com.rgpt.imagefilters.ColorHalftoneFilter;
import com.rgpt.imagefilters.ContrastFilter;
import com.rgpt.imagefilters.CrystallizeFilter;
import com.rgpt.imagefilters.DiffuseFilter;
import com.rgpt.imagefilters.DissolveFilter;
import com.rgpt.imagefilters.DoGFilter;
import com.rgpt.imagefilters.ExposureFilter;
import com.rgpt.imagefilters.FadeFilter;
import com.rgpt.imagefilters.FeedbackFilter;
import com.rgpt.imagefilters.FlareFilter;
import com.rgpt.imagefilters.GainFilter;
import com.rgpt.imagefilters.GammaFilter;
import com.rgpt.imagefilters.GlowFilter;
import com.rgpt.imagefilters.GrayFilter;
import com.rgpt.imagefilters.GrayscaleColormap;
import com.rgpt.imagefilters.HSBAdjustFilter;
import com.rgpt.imagefilters.InvertFilter;
import com.rgpt.imagefilters.KaleidoscopeFilter;
import com.rgpt.imagefilters.LightFilter;
import com.rgpt.imagefilters.LookupFilter;
import com.rgpt.imagefilters.MarbleFilter;
import com.rgpt.imagefilters.MirrorFilter;
import com.rgpt.imagefilters.NoiseFilter;
import com.rgpt.imagefilters.OilFilter;
import com.rgpt.imagefilters.PinchFilter;
import com.rgpt.imagefilters.PointillizeFilter;
import com.rgpt.imagefilters.PosterizeFilter;
import com.rgpt.imagefilters.RGBAdjustFilter;
import com.rgpt.imagefilters.ReduceNoiseFilter;
import com.rgpt.imagefilters.RescaleFilter;
import com.rgpt.imagefilters.ShapeFilter;
import com.rgpt.imagefilters.SharpenFilter;
import com.rgpt.imagefilters.SkeletonFilter;
import com.rgpt.imagefilters.SolarizeFilter;
import com.rgpt.imagefilters.SparkleFilter;
import com.rgpt.imagefilters.SpectrumColormap;
import com.rgpt.imagefilters.SphereFilter;
import com.rgpt.imagefilters.SwizzleFilter;
import com.rgpt.imagefilters.TemperatureFilter;
import com.rgpt.imagefilters.ThresholdFilter;
import com.rgpt.imagefilters.TwirlFilter;
import com.rgpt.imagefilters.UnsharpFilter;
import com.rgpt.imagefilters.WarpFilter;
import com.rgpt.imagefilters.WarpGrid;
import com.rgpt.imagefilters.WaterFilter;
import com.rgpt.util.RGPTParams;
import com.rgpt.util.RGPTActionListener;
import com.rgpt.util.RGPTLogger;
import com.rgpt.util.RGPTUtil;

// Contrast Filter handles both Contrast and Brightness
class ContrastNBrightFilterHdlr implements ImageFilterHandler {
	private static final long serialVersionUID = 1L;

	public static String m_Action;
	public static int m_Scale = 10;
	public static float m_Increment = RGPTParams
			.getFloatVal("StdFilterIncrement");
	public float m_ContrastValue = 1.0f;
	public float m_BrightnessValue = 1.0f;

	public ContrastNBrightFilterHdlr(String action) {
		m_Action = action;
	}

	public String getAction() {
		return m_Action;
	}

	// This method is call to set or unset this filter
	public SetImageFilter setImageFilterParam(String action, float val) {
		return setImageFilterParam(
				RGPTActionListener.ImageFilterActions.valueOf(action), val);
	}

	public SetImageFilter setImageFilterParam(
			RGPTActionListener.ImageFilterActions action, float val) {
		boolean isContrastFilter = true;
		if (m_Action
				.equals(RGPTActionListener.ImageFilterActions.BrightnessFilter
						.toString()))
			isContrastFilter = false;
		String[] filterParams = action.toString().split("_");
		String paramSetting = filterParams[1];
		if (paramSetting.equals("Cancel") || paramSetting.equals("Def"))
			return SetImageFilter.OFF;
		if (paramSetting.equals("Slider") || paramSetting.equals("TextBox")) {
			if (val == 0)
				return SetImageFilter.OFF;
			m_ContrastValue = (float) val / (float) m_Scale;
			return SetImageFilter.ON;
		}
		if (paramSetting.equals("Inc")) {
			if (isContrastFilter) {
				m_ContrastValue += m_Increment;
				if (m_ContrastValue > 2.0f)
					m_ContrastValue = 2.0f;
			} else {
				m_BrightnessValue += m_Increment;
				if (m_BrightnessValue > 2.0f)
					m_BrightnessValue = 2.0f;
			}
		} else if (paramSetting.equals("Dec")) {
			if (isContrastFilter) {
				m_ContrastValue -= m_Increment;
				if (m_ContrastValue < 0.0f) {
					m_ContrastValue = 0.0f;
					return SetImageFilter.OFF;
				}
			} else {
				m_BrightnessValue -= m_Increment;
				if (m_BrightnessValue < 0.0f) {
					m_BrightnessValue = 0.0f;
					return SetImageFilter.OFF;
				}
			}
		}
		return SetImageFilter.ON;
	}

	// Returns the value associated with the ImageFilterHandler
	public float getValue() {
		boolean isContrastFilter = true;
		if (m_Action
				.equals(RGPTActionListener.ImageFilterActions.BrightnessFilter
						.toString()))
			isContrastFilter = false;
		if (isContrastFilter)
			return RGPTUtil.roundDecimals(m_ContrastValue * m_Scale, 1);
		else
			return RGPTUtil.roundDecimals(m_BrightnessValue * m_Scale, 1);
	}

	// Apply Filter
	public BufferedImage applyFilter(BufferedImage srcImg) {
		boolean isContrastFilter = true;
		if (m_Action
				.equals(RGPTActionListener.ImageFilterActions.BrightnessFilter
						.toString()))
			isContrastFilter = false;
		BufferedImage resImg = null;
		ContrastFilter contrastFilter = new ContrastFilter();
		if (isContrastFilter) {
			contrastFilter.setContrast(m_ContrastValue);
			RGPTLogger.logToFile("Contrast Value: " + m_ContrastValue
					+ " And Set Value: " + contrastFilter.getContrast());
		} else {
			contrastFilter.setBrightness(m_BrightnessValue);
			RGPTLogger.logToFile("Brightness Value: " + m_BrightnessValue
					+ " And Set Value: " + contrastFilter.getBrightness());
		}
		resImg = contrastFilter.filter(srcImg, null);
		return resImg;
	}

	public String toString() {
		return "ImageFilterHandler for " + m_Action;
	}
}

// This FIlter handlers handles the Slider Value to be set appropriatly on the
// Filter
class SliderControlFilterHdlr implements ImageFilterHandler {
	public RGPTActionListener.ImageFilterActions m_Action;
	public float m_Scale;
	public float m_Value, m_DefValue, m_MinValue, m_MaxValue;
	public SetImageFilter m_Status;

	public SliderControlFilterHdlr(RGPTActionListener.ImageFilterActions action) {
		m_Action = action;
		switch (m_Action) {
		case RemoveBlurFilter:
			m_DefValue = 0.5f;
			m_MinValue = 0.0f;
			m_MaxValue = 100.0f;
			m_Scale = 10.0f;
			m_Value = m_DefValue;
			break;
		case GlowFilter:
			m_DefValue = 0.0f;
			m_MinValue = 0.0f;
			m_MaxValue = 1.0f;
			m_Scale = 0.1f;
			m_Value = m_DefValue;
			break;
		case LightingFilter:
			m_DefValue = 1.0f;
			m_MinValue = 0.0f;
			m_MaxValue = 5.0f;
			m_Scale = 0.5f;
			m_Value = m_DefValue;
			break;
		case DiffuseFilter:
			m_DefValue = 4.0f;
			m_MinValue = 1.0f;
			m_MaxValue = 100.0f;
			m_Scale = 1.0f;
			m_Value = m_DefValue;
			break;
		}
	}

	public String getAction() {
		return m_Action.toString();
	}

	// This method is call to set or unset this filter
	public SetImageFilter setImageFilterParam(String action, float val) {
		return setImageFilterParam(
				RGPTActionListener.ImageFilterActions.valueOf(action), val);
	}

	public SetImageFilter setImageFilterParam(
			RGPTActionListener.ImageFilterActions action, float val) {
		m_Status = SetImageFilter.ON;
		if (val == m_MinValue) {
			m_Status = SetImageFilter.OFF;
			return m_Status;
		}
		m_Value = (float) val * (float) m_Scale;
		if (m_Value > m_MaxValue)
			m_Value = m_MaxValue;
		else if (m_Value < m_MinValue)
			m_Value = m_MinValue;
		return m_Status;
	}

	// Returns the value associated with the ImageFilterHandler
	public float getValue() {
		return RGPTUtil.roundDecimals(m_Value / m_Scale, 1);
	}

	// Apply Filter
	public BufferedImage applyFilter(BufferedImage srcImg) {
		if (m_Status == SetImageFilter.OFF)
			return srcImg;
		RGPTLogger.logToFile(m_Action + " Filter Value: " + m_Value);
		switch (m_Action) {
		case RemoveBlurFilter:
			UnsharpFilter unsharpFilter = new UnsharpFilter();
			// Setting Default Filter Parameter Values - Radius can be 0 - 100,
			// threshold - 0 - 255
			unsharpFilter.setRadius(5.0f);
			unsharpFilter.setThreshold(10);
			// Amount is the variable field
			unsharpFilter.setAmount(m_Value);
			return unsharpFilter.filter(srcImg, null);
		case GlowFilter:
			GlowFilter glowFilter = new GlowFilter();
			glowFilter.setAmount(m_Value);
			return glowFilter.filter(srcImg, null);
		case DiffuseFilter:
			DiffuseFilter difFilter = new DiffuseFilter();
			difFilter.setScale(m_Value);
			return difFilter.filter(srcImg, null);
		}
		return srcImg;
	}

	public String toString() {
		return "ImageFilterHandler for " + m_Action.toString();
	}
}

// This Filter is used when multiple options can be applied to produce Image
// Effects
class OptionControlFilterHdlr implements ImageFilterHandler {
	public RGPTActionListener.ImageFilterActions m_Action;
	public int m_Index = -1;
	public String[] m_FilterOptions;
	public SetImageFilter m_Status;

	public OptionControlFilterHdlr(RGPTActionListener.ImageFilterActions action) {
		m_Action = action;
		switch (m_Action) {
		case SpotColorFilters:
			m_FilterOptions = RGPTParams.getVal(m_Action.toString())
					.split("::");
			break;
		}
	}

	public String getAction() {
		return m_Action.toString();
	}

	// This method is call to set or unset this filter
	public SetImageFilter setImageFilterParam(String action, float val) {
		return setImageFilterParam(
				RGPTActionListener.ImageFilterActions.valueOf(action), val);
	}

	public SetImageFilter setImageFilterParam(
			RGPTActionListener.ImageFilterActions action, float val) {
		m_Status = SetImageFilter.ON;
		String[] filterParams = action.toString().split("_");
		String paramSetting = filterParams[1];
		if (paramSetting.equals("Cancel") || paramSetting.equals("Def")) {
			m_Status = SetImageFilter.OFF;
			return m_Status;
		}
		if (paramSetting.equals("Next")) {
			m_Index++;
			if (m_Index > m_FilterOptions.length - 1)
				m_Index = 0;
		} else if (paramSetting.equals("Prev")) {
			m_Index--;
			if (m_Index < 0)
				m_Index = m_FilterOptions.length - 1;
		}
		return SetImageFilter.ON;
	}

	// Returns the value associated with the ImageFilterHandler
	public float getValue() {
		return m_Index;
	}

	// Apply Filter
	public BufferedImage applyFilter(BufferedImage srcImg) {
		return applyFilter(m_Action, srcImg);
	}

	private BufferedImage applyFilter(String action, BufferedImage srcImg) {
		return applyFilter(
				RGPTActionListener.ImageFilterActions.valueOf(action), srcImg);
	}

	private BufferedImage applyFilter(
			RGPTActionListener.ImageFilterActions action, BufferedImage srcImg) {
		String[] presetValArr = null;
		String presetVal = RGPTParams.getVal(action + "_SetValues");
		if (presetVal != null && presetVal.length() > 0)
			presetValArr = presetVal.split("::");
		if (m_Status == SetImageFilter.OFF)
			return srcImg;
		switch (action) {
		case SpotColorFilters:
			String filter = m_FilterOptions[m_Index];
			RGPTLogger.logToFile(action + " Filter Executed: " + filter);
			presetVal = RGPTParams.getVal(filter + "_SetValues");
			if (presetVal != null && presetVal.length() > 0)
				presetValArr = presetVal.split("::");
			if (filter.equals("PointillizeFilter")) {
				PointillizeFilter ptFilters = new PointillizeFilter();
				ptFilters.setScale(getFloatValue(presetValArr[0]));
				ptFilters.setEdgeThickness(getFloatValue(presetValArr[1]));
				ptFilters.setGridType(getIntValue(presetValArr[2]));
				return ptFilters.filter(srcImg, null);
			} else if (filter.equals("CrystallizeFilter")) {
				CrystallizeFilter crystalFilter = new CrystallizeFilter();
				crystalFilter.setScale(getFloatValue(presetValArr[0]));
				crystalFilter.setEdgeThickness(getFloatValue(presetValArr[1]));
				crystalFilter.setGridType(getIntValue(presetValArr[2]));
				return crystalFilter.filter(srcImg, null);
			} else if (filter.equals("MosiacFilter")) {
				return (new BlockFilter(getIntValue(presetValArr[0]))).filter(
						srcImg, null);
			} else if (filter.equals("ColorHalftoneFilter")) {
				ColorHalftoneFilter halftoneFilter = new ColorHalftoneFilter();
				halftoneFilter.setdotRadius(getFloatValue(presetValArr[0]));
				return halftoneFilter.filter(srcImg, null);
			} else if (filter.equals("NoiseFilter")) {
				NoiseFilter noiseFilter = new NoiseFilter();
				noiseFilter.setAmount(getIntValue(presetValArr[0]));
				return noiseFilter.filter(srcImg, null);
			} else if (filter.equals("DissolveFilter")) {
				DissolveFilter dissolveFilter = new DissolveFilter();
				dissolveFilter.setDensity(getFloatValue(presetValArr[0]));
				dissolveFilter.setSoftness(getFloatValue(presetValArr[1]));
				return dissolveFilter.filter(srcImg, null);
			}
			break;
		}
		return srcImg;
	}

	private float getFloatValue(String nvPair) {
		String[] nvPairVal = nvPair.split(":=");
		return (Float.valueOf(nvPairVal[1])).floatValue();
	}

	private int getIntValue(String nvPair) {
		String[] nvPairVal = nvPair.split(":=");
		return (Integer.valueOf(nvPairVal[1])).intValue();
	}

	public String toString() {
		return "ImageFilterHandler for " + m_Action.toString();
	}
}

// This Filter is used if Increment and Decrement Buttons are used to set the
// Value for the Filter
class IncDecControlFilterHdlr implements ImageFilterHandler {
	public RGPTActionListener.ImageFilterActions m_Action;
	public int m_Scale;
	public float m_Factor, m_DefValue, m_MinValue, m_MaxValue;
	public float m_Increment, m_Value;
	public SetImageFilter m_Status;

	public IncDecControlFilterHdlr(RGPTActionListener.ImageFilterActions action) {
		m_Action = action;
		switch (m_Action) {
		case ExposureFilter:
			m_Factor = 20.0f;
			m_DefValue = 2.0f;
			m_MinValue = 0.0f;
			m_MaxValue = 5.0f;
			m_Increment = (m_MaxValue - m_MinValue) / m_Factor;
			m_Value = m_DefValue;
			break;
		case RemoveBlurFilter:
			m_Factor = 10.0f;
			m_DefValue = 0.1f;
			m_MinValue = 0.0f;
			m_MaxValue = 1.0f;
			m_Increment = (m_MaxValue - m_MinValue) / m_Factor;
			m_Value = m_DefValue;
			break;
		case GainFilter:
		case BiasFilter:
			m_Factor = 10.0f;
			m_DefValue = 0.0f;
			m_MinValue = 0.0f;
			m_MaxValue = 1.0f;
			m_Increment = (m_MaxValue - m_MinValue) / m_Factor;
			m_Value = m_DefValue;
			break;
		case HueFilter:
		case SaturationFilter:
		case ColorBrightnessFilter:
		case AdjustRedFilter:
		case AdjustGreenFilter:
		case AdjustBlueFilter:
			m_Factor = 20.0f;
			m_DefValue = 0.0f;
			m_MinValue = -1.0f;
			m_MaxValue = 1.0f;
			m_Increment = (m_MaxValue - m_MinValue) / m_Factor;
			m_Value = m_DefValue;
			break;
		}
		RGPTLogger.logToFile(m_Action + " Factor: " + m_Factor + " Inc: "
				+ m_Increment);
	}

	public String getAction() {
		return m_Action.toString();
	}

	// This method is call to set or unset this filter
	public SetImageFilter setImageFilterParam(String action, float val) {
		return setImageFilterParam(
				RGPTActionListener.ImageFilterActions.valueOf(action), val);
	}

	public SetImageFilter setImageFilterParam(
			RGPTActionListener.ImageFilterActions action, float val) {
		m_Status = SetImageFilter.ON;
		String[] filterParams = action.toString().split("_");
		String paramSetting = filterParams[1];
		if (paramSetting.equals("Cancel")) {
			m_Status = SetImageFilter.OFF;
			return m_Status;
		}
		if (paramSetting.equals("Def")) {
			if (m_DefValue > 0.0f) {
				m_Value = m_DefValue;
				return m_Status;
			} else {
				m_Status = SetImageFilter.OFF;
				return m_Status;
			}
		}
		if (paramSetting.equals("Inc") || paramSetting.equals("Next")) {
			m_Value += m_Increment;
			if (m_Value > m_MaxValue)
				m_Value = m_MaxValue;
		} else if (paramSetting.equals("Dec") || paramSetting.equals("Prev")) {
			m_Value -= m_Increment;
			if (m_Value < m_MinValue)
				m_Value = m_MinValue;
		}
		if (val > 0)
			m_Value = val;
		return m_Status;
	}

	// Returns the value associated with the ImageFilterHandler
	public float getValue() {
		return RGPTUtil.roundDecimals(m_Value, 1);
	}

	// Apply Filter
	public BufferedImage applyFilter(BufferedImage srcImg) {
		return applyFilter(m_Action, srcImg);
	}

	private BufferedImage applyFilter(String action, BufferedImage srcImg) {
		return applyFilter(
				RGPTActionListener.ImageFilterActions.valueOf(action), srcImg);
	}

	private BufferedImage applyFilter(
			RGPTActionListener.ImageFilterActions action, BufferedImage srcImg) {
		String[] presetValArr = null;
		String presetVal = RGPTParams.getVal(action + "_SetValues");
		if (presetVal != null && presetVal.length() > 0)
			presetValArr = presetVal.split("::");
		if (m_Status == SetImageFilter.OFF)
			return srcImg;
		switch (action) {
		case ExposureFilter:
			ExposureFilter expFilter = new ExposureFilter();
			expFilter.setExposure(m_Value);
			RGPTLogger.logToFile("Exposure Value: " + m_Value
					+ " And Set Value: " + expFilter.getExposure());
			return expFilter.filter(srcImg, null);
		case BiasFilter:
			GainFilter biasFilter = new GainFilter();
			biasFilter.setBias(m_Value);
			RGPTLogger.logToFile(action + " Value: " + m_Value
					+ " And Set Value: " + biasFilter.getBias());
			return biasFilter.filter(srcImg, null);
		case GainFilter:
			GainFilter gainFilter = new GainFilter();
			gainFilter.setGain(m_Value);
			RGPTLogger.logToFile(action + " Value: " + m_Value
					+ " And Set Value: " + gainFilter.getGain());
			return gainFilter.filter(srcImg, null);
		case HueFilter:
			HSBAdjustFilter hueFilter = new HSBAdjustFilter();
			hueFilter.hFactor = m_Value;
			RGPTLogger.logToFile(action + " Value: " + m_Value);
			return hueFilter.filter(srcImg, null);
		case SaturationFilter:
			HSBAdjustFilter satFilter = new HSBAdjustFilter();
			satFilter.sFactor = m_Value;
			RGPTLogger.logToFile(action + " Value: " + m_Value);
			return satFilter.filter(srcImg, null);
		case ColorBrightnessFilter:
			HSBAdjustFilter brightFilter = new HSBAdjustFilter();
			brightFilter.bFactor = m_Value;
			RGPTLogger.logToFile(action + " Value: " + m_Value);
			return brightFilter.filter(srcImg, null);
		case AdjustRedFilter:
			RGBAdjustFilter rFilter = new RGBAdjustFilter();
			rFilter.rFactor = m_Value;
			RGPTLogger.logToFile(action + " Value: " + m_Value);
			return rFilter.filter(srcImg, null);
		case AdjustGreenFilter:
			RGBAdjustFilter gFilter = new RGBAdjustFilter();
			gFilter.gFactor = m_Value;
			RGPTLogger.logToFile(action + " Value: " + m_Value);
			return gFilter.filter(srcImg, null);
		case AdjustBlueFilter:
			RGBAdjustFilter bFilter = new RGBAdjustFilter();
			bFilter.bFactor = m_Value;
			RGPTLogger.logToFile(action + " Value: " + m_Value);
			return bFilter.filter(srcImg, null);
		case RemoveBlurFilter:
			UnsharpFilter filter = new UnsharpFilter();
			// Setting Default Filter Parameter Values - Radius can be 0 - 100,
			// threshold - 0 - 255
			filter.setRadius(RGPTUtil.getFloatValue(presetValArr[0]));
			filter.setThreshold((int) RGPTUtil.getFloatValue(presetValArr[1]));
			// Amount is the variable field
			filter.setAmount(m_Value);
			RGPTLogger.logToFile("Unsharp Filter Value: " + m_Value
					+ " And Set Value: " + filter.getAmount());
			return filter.filter(srcImg, null);
		}
		return srcImg;
	}

	public String toString() {
		return "ImageFilterHandler for " + m_Action.toString();
	}
}

class OnOffControlFilterHdlr implements ImageFilterHandler {
	public RGPTActionListener.ImageFilterActions m_Action;
	public boolean m_SetFilter = false;
	public ImageFilterController m_ImageFilterController;

	public OnOffControlFilterHdlr(RGPTActionListener.ImageFilterActions action,
			ImageFilterController imgFlrCtrl) {
		m_Action = action;
		m_ImageFilterController = imgFlrCtrl;
	}

	public String getAction() {
		return m_Action.toString();
	}

	// This method is call to set or unset this filter
	public SetImageFilter setImageFilterParam(String action, float val) {
		return setImageFilterParam(
				RGPTActionListener.ImageFilterActions.valueOf(action), val);
	}

	public SetImageFilter setImageFilterParam(
			RGPTActionListener.ImageFilterActions action, float val) {
		String[] filterParams = action.toString().split("_");
		if (filterParams[1].equals("On")) {
			m_SetFilter = true;
			return SetImageFilter.ON;
		} else
			m_SetFilter = false;
		return SetImageFilter.OFF;
	}

	// Returns the value associated with the ImageFilterHandler
	public float getValue() {
		if (m_SetFilter)
			return 1;
		else
			return 0;
	}

	// Apply Filter
	public BufferedImage applyFilter(BufferedImage srcImg) {
		return applyFilter(m_Action, srcImg);
	}

	private BufferedImage applyFilter(String action, BufferedImage srcImg) {
		return applyFilter(
				RGPTActionListener.ImageFilterActions.valueOf(action), srcImg);
	}

	private BufferedImage applyFilter(
			RGPTActionListener.ImageFilterActions action, BufferedImage srcImg) {
		if (!m_SetFilter)
			return srcImg;
		BufferedImage resImg = null;
		String[] presetValArr = null;
		String presetVal = RGPTParams.getVal(action + "_SetValues");
		if (presetVal != null && presetVal.length() > 0)
			presetValArr = presetVal.split("::");
		ImageFilterController imgFltCtrl = m_ImageFilterController;
		switch (action) {
		case SharpenFilter:
			SharpenFilter sharpenFilter = new SharpenFilter();
			RGPTLogger.logToFile("Applying Filter: " + sharpenFilter);
			// sharpImg = ImageUtils.createImageCopy(srcImg);
			// sharpenFilter.filter(srcImg, sharpImg);
			resImg = sharpenFilter.createCompatibleDestImage(srcImg, null);
			// ImageUtils.displayImage(sharpImg, sharpenFilter.toString());
			return sharpenFilter.filter(srcImg, resImg);
		case ReduceNoiseFilter:
			ReduceNoiseFilter filter = new ReduceNoiseFilter();
			return filter.filter(srcImg, null);
		case GrayOutFilter:
			return (new GrayFilter()).filter(srcImg, null);
		case GrayScaleFilter:
			return (new LookupFilter(new GrayscaleColormap())).filter(srcImg,
					null);
		case NegativeFilter:
			resImg = this.applyFilter("InvertFilter", srcImg);
			ShapeFilter burstedFilter = new ShapeFilter();
			burstedFilter.setUseAlpha(false);
			burstedFilter.setType(ShapeFilter.LINEAR);
			burstedFilter.setFactor(0.0f);
			// burstedFilter.setColormap(new GrayscaleColormap());
			return burstedFilter.filter(resImg, null);
		case SpectrumColorFilter:
			return (new LookupFilter(new SpectrumColormap())).filter(srcImg,
					null);
		case InvertFilter:
			return (new InvertFilter()).filter(srcImg, null);
		case SoloarizeFilter:
			return (new SolarizeFilter()).filter(srcImg, null);
		case ComicFilter:
			resImg = (new SolarizeFilter()).filter(srcImg, null);
			return this.applyFilter("InvertFilter", resImg);
			// return (new InvertFilter()).filter(resImg, null);
		case BWComicFilter:
			resImg = this.applyFilter("GrayScaleFilter", srcImg);
			return this.applyFilter("ComicFilter", resImg);
		case ComicSketchedFilter:
			resImg = this.applyFilter("BWComicFilter", srcImg);
			return this.applyFilter("SoloarizeFilter", resImg);
		case SketchedFilter:
			resImg = imgFltCtrl.applyFilter(srcImg, "BWFilter", "On", -1);
			ChromeFilter chromeFilter = new ChromeFilter();
			chromeFilter.setBumpSoftness(getFloatValue(presetValArr[0]));
			chromeFilter.setBumpHeight(getFloatValue(presetValArr[1]));
			chromeFilter.setAmount(getFloatValue(presetValArr[2]));
			// chromeFilter.setDiffuseColor(getColorValue(presetValArr[3]));
			// chromeFilter.setColorSource(LightFilter.COLORS_FROM_IMAGE);
			resImg = chromeFilter.filter(resImg, null);
			resImg = this.applyFilter("InvertFilter", resImg);
			return imgFltCtrl.applyFilter(resImg, "BWFilter", "On", 1);
		case EmbossFilter:
			LightFilter embossFilter = new LightFilter();
			Vector embLights = embossFilter.getLights();
			embLights.removeAllElements();
			LightFilter.DistantLight embLight = embossFilter.new DistantLight();
			embossFilter.addLight(embLight);
			embossFilter.setBumpHeight(5.0f);
			embossFilter.setBumpSoftness(5.0f);
			return embossFilter.filter(srcImg, null);
		case BWSketchedFilter:
			resImg = imgFltCtrl.applyFilter(srcImg, "ImageOutlineFilter", "On",
					0.2F);
			resImg = imgFltCtrl.applyFilter(resImg, "GammaFilter", "On", 0.1F);
			resImg = imgFltCtrl.applyFilter(resImg, "ColorSketchFilter", "On",
					-1.0F);
			return imgFltCtrl.applyFilter(resImg, "GammaFilter", "On", 0.1F);
		}
		return srcImg;
	}

	private float getFloatValue(String nvPair) {
		String[] nvPairVal = nvPair.split(":=");
		return (Float.valueOf(nvPairVal[1])).floatValue();
	}

	private int getIntValue(String nvPair) {
		String[] nvPairVal = nvPair.split(":=");
		String val = nvPairVal[0];
		if (nvPairVal.length == 2)
			val = nvPairVal[1];
		return (Integer.valueOf(val)).intValue();
	}

	private int getColorValue(String nvPair) {
		String[] nvPairVal = nvPair.split(":=");
		String[] rgbVal = nvPairVal[1].split(":");
		Color color = new Color(getIntValue(rgbVal[0]), getIntValue(rgbVal[1]),
				getIntValue(rgbVal[2]));
		return color.getRGB();
	}

	public String toString() {
		return "ImageFilterHandler for " + m_Action.toString();
	}
}

class OnOffParamControlFilterHdlr implements ImageFilterHandler {
	public RGPTActionListener.ImageFilterActions m_Action;
	public SetImageFilter m_ImageFilterStatus = SetImageFilter.OFF;
	public Map<String, String> m_ParamSetValues = null;
	float m_Value = -1.0F;
	public Map<String, Object> m_FilterParamValues = null;

	public ImageFilterController m_ImageFilterController;

	public OnOffParamControlFilterHdlr(
			RGPTActionListener.ImageFilterActions action,
			ImageFilterController imgFlrCtrl) {
		m_Action = action;
		m_ImageFilterController = imgFlrCtrl;
		m_ParamSetValues = new HashMap<String, String>();
		m_FilterParamValues = new HashMap<String, Object>();
		setDefaultParamValues();
	}

	public String getAction() {
		return m_Action.toString();
	}

	// This method is call to set or unset this filter
	public SetImageFilter setImageFilterParam(String action, float val) {
		return setImageFilterParam(
				RGPTActionListener.ImageFilterActions.valueOf(action), val);
	}

	public SetImageFilter setImageFilterParam(
			RGPTActionListener.ImageFilterActions action, float val) {
		m_Value = val;
		String[] filterParams = action.toString().split("_");
		if (filterParams[1].equals("On"))
			m_ImageFilterStatus = SetImageFilter.ON;
		else if (filterParams[1].equals("Active"))
			m_ImageFilterStatus = SetImageFilter.ACTIVATED;
		else
			m_ImageFilterStatus = SetImageFilter.OFF;
		return m_ImageFilterStatus;
	}

	// Returns the value associated with the ImageFilterHandler
	public float getValue() {
		if (m_ImageFilterStatus == SetImageFilter.ON)
			return 1.0f;
		if (m_ImageFilterStatus == SetImageFilter.ACTIVATED)
			return 2.0f;
		else
			return 0.0f;
	}

	// Apply Filter
	public BufferedImage applyFilter(BufferedImage srcImg) {
		try {
			return applyFilter(m_Action, srcImg);
		} catch (Exception ex) {
			RGPTLogger.logToFile("", ex);
			return srcImg;
		}

	}

	private BufferedImage applyFilter(String action, BufferedImage srcImg) {
		return applyFilter(
				RGPTActionListener.ImageFilterActions.valueOf(action), srcImg);
	}

	private BufferedImage applyFilter(
			RGPTActionListener.ImageFilterActions action, BufferedImage srcImg) {
		if (m_ImageFilterStatus == SetImageFilter.OFF)
			return srcImg;
		BufferedImage resImg = null;
		String[] presetValArr = null;
		float amt = 0.0F, angle = 0.0F, cntrX = 0.0F, cntrY = 0.0F;
		int index = 0;
		float percBorder = RGPTParams.getFloatVal("FadeBorderPercent");
		String actionParam = RGPTParams.getVal(action.toString()
				+ "_UseValues", action.toString());
		// if (actionParam.contains("NEW")) { String[] actionParams =
		// actionParam.split("NEW");
		// actionParam = actionParams[0]; }
		String presetVal = RGPTParams.getVal(actionParam + "_SetValues");
		if (presetVal != null && presetVal.length() > 0)
			presetValArr = presetVal.split("::");
		ImageFilterController imgFltCtrl = m_ImageFilterController;
		// RGPTLogger.logToFile(action+" Param Values Are: "+m_ParamSetValues);
		switch (action) {
		case GrayOutFilter:
			return (new GrayFilter()).filter(srcImg, null);
		case GrayScaleFilter:
			LookupFilter grayScFltr = new LookupFilter(new GrayscaleColormap());
			amt = getFloatValue(getParamValue("Amount", null));
			if (m_ParamSetValues.get("SetFadeMargin").equals("true"))
				grayScFltr.margin = amt / 2;
			else
				grayScFltr.margin = 1.0F - getFloatValue(getParamValue(
						"RadialFactor", null));
			return grayScFltr.filter(srcImg, null);
		case NegativeFilter:
			resImg = this.applyFilter("InvertFilter", srcImg);
			ShapeFilter negFilter = new ShapeFilter();
			amt = getFloatValue(getParamValue("Amount", null));
			if (m_ParamSetValues.get("SetFadeMargin").equals("true"))
				negFilter.margin = amt / 2;
			else
				negFilter.margin = 1.0F - getFloatValue(getParamValue(
						"RadialFactor", null));
			negFilter.setUseAlpha(false);
			negFilter.setType(ShapeFilter.LINEAR);
			negFilter.setFactor(0.0f);
			// burstedFilter.setColormap(new GrayscaleColormap());
			return negFilter.filter(resImg, null);
		case SpectrumColorFilter:
			LookupFilter specClrFltr = new LookupFilter(new SpectrumColormap());
			amt = getFloatValue(getParamValue("Amount", null));
			if (m_ParamSetValues.get("SetFadeMargin").equals("true"))
				specClrFltr.margin = amt / 2;
			else
				specClrFltr.margin = 1.0F - getFloatValue(getParamValue(
						"RadialFactor", null));
			// RGPTLogger.logToFile(action+" is executed by: "+specClrFltr);
			return specClrFltr.filter(srcImg, null);
		case SoloarizeFilter:
			SolarizeFilter solFltr = new SolarizeFilter();
			amt = getFloatValue(getParamValue("Amount", null));
			if (m_ParamSetValues.get("SetFadeMargin").equals("true"))
				solFltr.margin = amt / 2;
			else
				solFltr.margin = 1.0F - getFloatValue(getParamValue(
						"RadialFactor", null));
			return solFltr.filter(srcImg, null);
		case ComicFilter:
			resImg = applyFilter("SoloarizeFilter", srcImg);
			return this.applyFilter("InvertFilter", resImg);
		case BWComicFilter:
			resImg = this.applyFilter("GrayScaleFilter", srcImg);
			return this.applyFilter("ComicFilter", resImg);
		case ComicSketchedFilter:
			resImg = this.applyFilter("BWComicFilter", srcImg);
			return this.applyFilter("SoloarizeFilter", resImg);
		case SketchedFilter:
			resImg = imgFltCtrl.applyFilter(srcImg, "BWFilter", "On", -1);
			ChromeFilter chromeFilter = new ChromeFilter();
			chromeFilter.setBumpSoftness(getFloatValue(presetValArr[0]));
			chromeFilter.setBumpHeight(getFloatValue(presetValArr[1]));
			chromeFilter.setAmount(getFloatValue(presetValArr[2]));
			// chromeFilter.setDiffuseColor(getColorValue(presetValArr[3]));
			// chromeFilter.setColorSource(LightFilter.COLORS_FROM_IMAGE);
			resImg = chromeFilter.filter(resImg, null);
			resImg = this.applyFilter("InvertFilter", resImg);
			return imgFltCtrl.applyFilter(resImg, "BWFilter", "On", 1);
		case EmbossFilter:
			LightFilter embossFilter = new LightFilter();
			Vector embLights = embossFilter.getLights();
			embLights.removeAllElements();
			LightFilter.DistantLight embLight = embossFilter.new DistantLight();
			embossFilter.addLight(embLight);
			embossFilter.setBumpHeight(getFloatValue(presetValArr[0]));
			embossFilter.setBumpSoftness(getFloatValue(presetValArr[1]));
			return embossFilter.filter(srcImg, null);
		case BWSketchedFilter:
			resImg = imgFltCtrl.applyFilter(srcImg, "ImageOutlineFilter", "On",
					0.2F);
			resImg = imgFltCtrl.applyFilter(resImg, "GammaFilter", "On", 0.1F);
			resImg = imgFltCtrl.applyFilter(resImg, "ColorSketchFilter", "On",
					-1.0F);
			return imgFltCtrl.applyFilter(resImg, "GammaFilter", "On", 0.1F);
		case CircleFilter:
			CircleFilter cirFilter = new CircleFilter();
			cirFilter.setRadius(0.0f);
			cirFilter.setHeight(getFloatValue(getParamValue("Height",
					presetValArr[0])));
			cirFilter.setSpreadAngle(getFloatValue(getParamValue("SpreadAngle",
					presetValArr[1])));
			angle = (float) Math.toRadians(getFloatValue(getParamValue(
					"RotAngle", presetValArr[2])));
			cirFilter.setAngle(angle);
			// cirFilter.setAngle(getFloatValue(getParamValue("RotAngle",
			// presetValArr[2])));
			return cirFilter.filter(srcImg, null);
		case WarpFilter:
			int w = srcImg.getWidth(),
			h = srcImg.getHeight();
			int numGrid = RGPTParams.getIntVal("NumOfGrid");
			WarpGrid srcGrid = new WarpGrid(numGrid, numGrid, w, h);
			WarpGrid desGrid = new WarpGrid(numGrid, numGrid, w, h);
			// RGPTLogger.logToFile("ScrGrid X: "+RGPTUtil.createVector(srcGrid.xGrid)+
			// "\nY: "+RGPTUtil.createVector(srcGrid.yGrid));
			setGridPts("SrcGirdPts", srcGrid);
			setGridPts("DesGirdPts", desGrid);
			// RGPTLogger.logToFile("After ScrGrid X: "+RGPTUtil.createVector(srcGrid.xGrid)+
			// "\nY: "+RGPTUtil.createVector(srcGrid.yGrid));
			// RGPTLogger.logToFile("After DesGrid X: "+RGPTUtil.createVector(desGrid.xGrid)+
			// "\nY: "+RGPTUtil.createVector(desGrid.yGrid));
			return (new WarpFilter(srcGrid, desGrid)).filter(srcImg, null);
		case PinchFilter:
		case BulgeFilter:
			PinchFilter pinFilter = new PinchFilter();
			pinFilter.setAngle(getFloatValue(getParamValue("Angle",
					presetValArr[0])));
			pinFilter.setCentreX(getFloatValue(getParamValue("CenterX",
					presetValArr[1])));
			pinFilter.setCentreY(getFloatValue(getParamValue("CenterY",
					presetValArr[2])));
			pinFilter.setRadius(getFloatValue(getParamValue("Radius",
					presetValArr[3])));
			amt = getFloatValue(getParamValue("Amount", presetValArr[4]));
			if (action.toString().equals(
					(RGPTActionListener.ImageFilterActions.BulgeFilter
							.toString())))
				amt = -amt;
			pinFilter.setAmount(amt);
			return pinFilter.filter(srcImg, null);
		case SphereFilter:
			SphereFilter sphereFilter = new SphereFilter();
			sphereFilter.setCentreX(getFloatValue(getParamValue("CenterX",
					presetValArr[0])));
			sphereFilter.setCentreY(getFloatValue(getParamValue("CenterY",
					presetValArr[1])));
			sphereFilter.setRadius(getFloatValue(getParamValue("Radius",
					presetValArr[2])));
			amt = getFloatValue(getParamValue("Amount", presetValArr[3])) * 3.0f;
			sphereFilter.setRefractionIndex(amt);
			sphereFilter.setEdgeAction(getIntValue(getParamValue("EdgeAction",
					presetValArr[4])));
			return sphereFilter.filter(srcImg, null);
		case WaterFilter:
		case WaterRippleFilter:
			WaterFilter waterFilter = new WaterFilter();
			waterFilter.setCentreX(getFloatValue(getParamValue("CenterX",
					presetValArr[0])));
			waterFilter.setCentreY(getFloatValue(getParamValue("CenterY",
					presetValArr[1])));
			waterFilter.setRadius(getFloatValue(getParamValue("Radius",
					presetValArr[2])));
			amt = getFloatValue(getParamValue("Amount", presetValArr[3]));
			String waterFltr = RGPTActionListener.ImageFilterActions.WaterFilter
					.toString();
			String rippleFltr = RGPTActionListener.ImageFilterActions.WaterRippleFilter
					.toString();
			if (action.toString().equals(waterFltr)) {
				amt = 200.0f * amt;
				waterFilter.setWavelength(amt);
				float amp = getFloatValue(getParamValue("Amplitude",
						presetValArr[4]));
				waterFilter.setAmplitude(amp);
			} else {
				waterFilter.setAmplitude(amt);
				float waveLth = getFloatValue(getParamValue("Wavelength",
						presetValArr[4]));
				waterFilter.setWavelength(waveLth);
			}
			waterFilter.setPhase(getFloatValue(getParamValue("Phase",
					presetValArr[5])));
			return waterFilter.filter(srcImg, null);
		case TwirlFilter:
			TwirlFilter twirlFilter = new TwirlFilter();
			twirlFilter.setCentreX(getFloatValue(getParamValue("CenterX",
					presetValArr[0])));
			twirlFilter.setCentreY(getFloatValue(getParamValue("CenterY",
					presetValArr[1])));
			twirlFilter.setRadius(getFloatValue(getParamValue("Radius",
					presetValArr[2])));
			angle = (float) Math.toRadians(getFloatValue(getParamValue("Angle",
					presetValArr[3])));
			twirlFilter.setAngle(angle);
			return twirlFilter.filter(srcImg, null);
		case CircleLightFilter:
			LightFilter crclLightFilter = new LightFilter();
			Vector crclLights = crclLightFilter.getLights();
			crclLights.removeAllElements();
			LightFilter.SpotLight crclSpotLight = crclLightFilter.new SpotLight();
			crclLightFilter.addLight(crclSpotLight);
			crclSpotLight.setElevation(1.0f);
			crclSpotLight.setCentreX(getFloatValue(getParamValue("CenterX",
					presetValArr[0])));
			crclSpotLight.setCentreY(getFloatValue(getParamValue("CenterY",
					presetValArr[1])));
			crclSpotLight.setDistance(getFloatValue(getParamValue("Radius",
					presetValArr[2])));
			crclSpotLight.setFocus(getFloatValue(getParamValue("Amount",
					presetValArr[3])));
			crclSpotLight.setConeAngle(getFloatValue(getParamValue("ConeAngle",
					presetValArr[4])));
			return crclLightFilter.filter(srcImg, null);
		case SpotLightFilter:
			LightFilter spotLightFilter = new LightFilter();
			Vector spotLights = spotLightFilter.getLights();
			spotLights.removeAllElements();
			LightFilter.SpotLight spotLight = spotLightFilter.new SpotLight();
			spotLightFilter.addLight(spotLight);
			spotLight.setCentreX(getFloatValue(getParamValue("CenterX",
					presetValArr[0])));
			spotLight.setCentreY(getFloatValue(getParamValue("CenterY",
					presetValArr[1])));
			spotLight.setDistance(getFloatValue(getParamValue("Radius",
					presetValArr[2])));
			spotLight.setElevation(getFloatValue(getParamValue("Amount",
					presetValArr[3])));
			angle = (float) Math.toRadians(getFloatValue(getParamValue("Angle",
					presetValArr[4])));
			spotLight.setAzimuth(angle);
			spotLight.setFocus(getFloatValue(getParamValue("Focus",
					presetValArr[5])));
			spotLight.setConeAngle(getFloatValue(getParamValue("ConeAngle",
					presetValArr[6])));
			return spotLightFilter.filter(srcImg, null);
		case PointLightFilter:
			LightFilter ptLightFilter = new LightFilter();
			Vector ptLights = ptLightFilter.getLights();
			ptLights.removeAllElements();
			LightFilter.PointLight ptLight = ptLightFilter.new PointLight();
			ptLightFilter.addLight(ptLight);
			ptLight.setCentreX(getFloatValue(getParamValue("CenterX",
					presetValArr[0])));
			ptLight.setCentreY(getFloatValue(getParamValue("CenterY",
					presetValArr[1])));
			ptLight.setDistance(getFloatValue(getParamValue("Radius",
					presetValArr[2])));
			ptLight.setElevation(getFloatValue(getParamValue("Amount",
					presetValArr[3])));
			angle = (float) Math.toRadians(getFloatValue(getParamValue("Angle",
					presetValArr[4])));
			ptLight.setAzimuth(angle);
			return ptLightFilter.filter(srcImg, null);
		case FlareLightFilter:
			FlareFilter flareFilter = new FlareFilter();
			cntrX = getFloatValue(getParamValue("CenterX", presetValArr[0]));
			cntrY = getFloatValue(getParamValue("CenterY", presetValArr[1]));
			flareFilter
					.setCentre(new java.awt.geom.Point2D.Float(cntrX, cntrY));
			flareFilter.setRadius(getFloatValue(getParamValue("Radius",
					presetValArr[2])));
			flareFilter.setRayAmount(getFloatValue(getParamValue("Amount",
					presetValArr[3])));
			flareFilter.setBaseAmount(getFloatValue(getParamValue("BaseAmount",
					presetValArr[4])));
			flareFilter.setRingAmount(getFloatValue(getParamValue("RingAmount",
					presetValArr[5])));
			flareFilter.setRingWidth(getFloatValue(getParamValue("RingWidth",
					presetValArr[6])));
			return flareFilter.filter(srcImg, null);
		case RingLightFilter:
			FlareFilter ringFilter = new FlareFilter();
			cntrX = getFloatValue(getParamValue("CenterX", presetValArr[0]));
			cntrY = getFloatValue(getParamValue("CenterY", presetValArr[1]));
			ringFilter.setCentre(new java.awt.geom.Point2D.Float(cntrX, cntrY));
			ringFilter.setRadius(getFloatValue(getParamValue("Radius",
					presetValArr[2])));
			ringFilter.setRingAmount(getFloatValue(getParamValue("Amount",
					presetValArr[3])));
			ringFilter.setRayAmount(getFloatValue(getParamValue("RayAmount",
					presetValArr[4])));
			ringFilter.setBaseAmount(getFloatValue(getParamValue("BaseAmount",
					presetValArr[5])));
			ringFilter.setRingWidth(getFloatValue(getParamValue("RingWidth",
					presetValArr[6])));
			return ringFilter.filter(srcImg, null);
		case SparkleFilter:
			SparkleFilter sparkleFilter = new SparkleFilter();
			sparkleFilter.setPercCentreX(getFloatValue(getParamValue("CenterX",
					presetValArr[0])));
			sparkleFilter.setPercCentreY(getFloatValue(getParamValue("CenterY",
					presetValArr[1])));
			sparkleFilter.setRadius((int) getFloatValue(getParamValue("Radius",
					presetValArr[2])));
			sparkleFilter.setRays((int) (getFloatValue(getParamValue("Amount",
					presetValArr[3])) * 300));
			sparkleFilter.setRandomness(getIntValue(getParamValue("Randomness",
					presetValArr[4])));
			return sparkleFilter.filter(srcImg, null);
		case ColorSketchFilter:
			LightFilter clrSketchFilter = new LightFilter();
			Vector clrSketchLights = clrSketchFilter.getLights();
			clrSketchLights.removeAllElements();
			LightFilter.DistantLight clrSketchLight = clrSketchFilter.new DistantLight();
			clrSketchLight.setElevation(1.0f);
			clrSketchFilter.addLight(clrSketchLight);
			clrSketchFilter.setBumpHeight(getFloatValue(getParamValue("Amount",
					presetValArr[2])) * 5);
			clrSketchFilter.setBumpSoftness(getFloatValue(getParamValue(
					"BumpSoftness", presetValArr[3])));
			return clrSketchFilter.filter(srcImg, null);
		case BurstedImageFilter:
			ShapeFilter burstedFilter = new ShapeFilter();
			burstedFilter.setMerge(true);
			burstedFilter.setUseAlpha(false);
			burstedFilter.setType(ShapeFilter.LINEAR);
			burstedFilter.setFactor(getFloatValue(getParamValue("Amount",
					presetValArr[2])) * 3);
			// burstedFilter.setColormap(new GrayscaleColormap());
			return burstedFilter.filter(srcImg, null);
		case MirrorFilter:
			MirrorFilter mirrorFilter = new MirrorFilter();
			mirrorFilter.setCentreY(getFloatValue(getParamValue("Amount",
					presetValArr[2])));
			float radang = (float) Math.toRadians(getFloatValue(getParamValue(
					"MirAngle", presetValArr[3])));
			// mirrorFilter.setRotation(getFloatValue(getParamValue("MirAngle",
			// presetValArr[3])));
			// mirrorFilter.setAngle(radang);
			return mirrorFilter.filter(srcImg, null);
		case BWFilter:
			ThresholdFilter thresholdFilter = new ThresholdFilter();
			if (m_Value > 0.0F)
				thresholdFilter.setLowerThreshold((int) (m_Value * 127));
			else
				thresholdFilter
						.setLowerThreshold((int) (getFloatValue(getParamValue(
								"Amount", presetValArr[2])) * 127));
			return thresholdFilter.filter(srcImg, null);
		case SkeletonFilter:
			SkeletonFilter skeletonFilter = new SkeletonFilter();
			skeletonFilter.setIterations((int) (getFloatValue(getParamValue(
					"Amount", presetValArr[2])) * 100));
			return skeletonFilter.filter(srcImg, null);
		case LightUpFilter:
			RescaleFilter lightupFilter = new RescaleFilter();
			lightupFilter.setScale(getFloatValue(getParamValue("Amount",
					presetValArr[2])) * 5);
			return lightupFilter.filter(srcImg, null);
		case TemperatureFilter:
			TemperatureFilter tempFilter = new TemperatureFilter();
			float temp = getFloatValue(getParamValue("Amount", presetValArr[2])) * 10000;
			if (temp <= 1000)
				temp = 1000.0F;
			tempFilter.setTemperature(temp);
			return tempFilter.filter(srcImg, null);
		case GammaFilter:
			GammaFilter gammaFilter = new GammaFilter();
			if (m_Value > 0.0F)
				amt = m_Value;
			else
				amt = getFloatValue(getParamValue("Amount", presetValArr[2]));
			gammaFilter.setGamma(amt * 3);
			return gammaFilter.filter(srcImg, null);
		case PosterizeFilter:
			PosterizeFilter posterizeFilter = new PosterizeFilter();
			amt = getFloatValue(getParamValue("Amount", presetValArr[2])) * 10;
			int level = Math.round(amt);
			if (level < 2)
				level = 2;
			posterizeFilter.setNumLevels(level);
			posterizeFilter.margin = 1.0F - getFloatValue(getParamValue(
					"RadialFactor", null));
			return posterizeFilter.filter(srcImg, null);
		case ImageOutlineFilter:
			DoGFilter imgOutlineFilter = new DoGFilter();
			if (m_Value > 0.0F)
				amt = m_Value;
			else
				amt = getFloatValue(getParamValue("Amount", presetValArr[2]));
			// RGPTLogger.logToFile(action+" amt: "+amt);
			imgOutlineFilter.setRadius2(amt * 10);
			imgOutlineFilter.setNormalize(true);
			imgOutlineFilter.setInvert(true);
			resImg = imgOutlineFilter.filter(srcImg, null);
			// ImageUtils.displayImage(resImg, action.toString());
			return resImg;
		case KaleidoscopeFilter:
			KaleidoscopeFilter kFilter = new KaleidoscopeFilter();
			kFilter.setCentreX(getFloatValue(getParamValue("CenterX",
					presetValArr[0])));
			kFilter.setCentreY(getFloatValue(getParamValue("CenterY",
					presetValArr[1])));
			int sides = Math.round((getFloatValue(getParamValue("Amount",
					presetValArr[2]))) * 20);
			angle = (float) Math.toRadians(getFloatValue(getParamValue("Angle",
					presetValArr[3])));
			kFilter.setSides(sides);
			kFilter.setAngle2(angle);
			return kFilter.filter(srcImg, null);
		case MultipleImageFilter:
			FeedbackFilter feedbackFilter = new FeedbackFilter();
			feedbackFilter.setCentreX(getFloatValue(getParamValue("CenterX",
					presetValArr[0])));
			feedbackFilter.setCentreY(getFloatValue(getParamValue("CenterY",
					presetValArr[1])));
			feedbackFilter.setZoom(getFloatValue(getParamValue("RadialFactor",
					presetValArr[2])) * -1.0F);
			feedbackFilter.setIterations((int) (getFloatValue(getParamValue(
					"Amount", presetValArr[3])) * 20.0F));
			angle = (float) Math.toRadians(getFloatValue(getParamValue("Angle",
					presetValArr[4])));
			feedbackFilter.setRotation(angle);
			return feedbackFilter.filter(srcImg, null);
		case NoiseFilter:
			NoiseFilter noiseFilter = new NoiseFilter();
			amt = getFloatValue(getParamValue("Amount", presetValArr[2]));
			if (m_ParamSetValues.get("SetFadeMargin").equals("true")) {
				noiseFilter.margin = amt * percBorder;
				amt = getFloatValue(presetValArr[4]);
			}
			noiseFilter.setAmount(Math.round(amt * 100));
			noiseFilter.setDistribution(getIntValue(presetValArr[3]));
			return noiseFilter.filter(srcImg, null);
		case DissolveFilter:
			DissolveFilter dissolveFilter = new DissolveFilter();
			amt = getFloatValue(getParamValue("Amount", presetValArr[2]));
			if (m_ParamSetValues.get("SetFadeMargin").equals("true")) {
				dissolveFilter.margin = amt * percBorder;
				amt = getFloatValue(presetValArr[4]);
			}
			dissolveFilter.setDensity(amt);
			dissolveFilter.setSoftness(getFloatValue(presetValArr[3]));
			return dissolveFilter.filter(srcImg, null);
		case DiffuseFilter:
			DiffuseFilter difFilter = new DiffuseFilter();
			difFilter.setScale(getFloatValue(getParamValue("Amount",
					presetValArr[2])) * 100);
			difFilter.setEdgeAction(getIntValue(presetValArr[3]));
			difFilter.setInterpolation(getIntValue(presetValArr[4]));
			return difFilter.filter(srcImg, null);
		case SplitFadeFilter:
			angle = (float) Math.toRadians(getFloatValue(getParamValue("Angle",
					presetValArr[3])));
			FadeFilter fadeFilter = new FadeFilter();
			fadeFilter.setSides(4);
			fadeFilter.setFadeWidth(Math
					.round(getFloatValue(presetValArr[4]) * 100));
			fadeFilter.setFadeStart(0.0F);
			fadeFilter.setAngle(angle);
			resImg = fadeFilter.filter(srcImg, null);
			return applyFilter("UniformFadeFilter", resImg);
		case UniformFadeFilter:
		case BoxFadeFilter:
			amt = getFloatValue(getParamValue("Amount", presetValArr[2]));
			if (m_ParamSetValues.get("SetFadeMargin").equals("true"))
				angle = 0.0F;
			else
				angle = getFloatValue(getParamValue("Angle", presetValArr[3]));
			int shapeTypeInd = (int) Math.round((angle / 360.0F) * 100);
			// RGPTLogger.logToFile("Exec fade action: "+action);
			return ImageUtils.fadeImage(srcImg, amt, false, action.toString(),
					shapeTypeInd);
		case LinearFadeFilter:
			amt = getFloatValue(getParamValue("Amount", presetValArr[2]));
			angle = getFloatValue(getParamValue("Angle", presetValArr[3]));
			int fadeTypeInd = (int) Math.round((angle / 360.0F) * 100);
			return ImageUtils.linearFadeImage(srcImg, amt, fadeTypeInd);
		case NoiseFadeFilter:
			m_ParamSetValues.put("SetFadeMargin", Boolean.toString(true));
			resImg = applyFilter("UniformFadeFilter", srcImg);
			return applyFilter("NoiseFilter", resImg);
		case BlurFadeFilter:
			m_ParamSetValues.put("SetFadeMargin", Boolean.toString(true));
			angle = getFloatValue(getParamValue("Angle", presetValArr[3]));
			int imgFltrInd = (int) Math.round((angle / 360.0F) * 100);
			String[] imgFltrs = (RGPTParams.getVal(action.toString()))
					.split("::");
			String imgFltr = imgFltrs[0];
			if (imgFltrInd >= 0) {
				int mod = imgFltrInd % imgFltrs.length;
				imgFltr = imgFltrs[mod];
			}
			resImg = applyFilter(imgFltr, srcImg);
			return applyFilter("UniformFadeFilter", resImg);
		case HorizontalFadeFilter:
		case VerticalFadeFilter:
			resImg = ImageUtils
					.linearFadeImage(srcImg, 1.0f, action.toString());
			return applyFilter("UniformFadeFilter", resImg);
		case InvertFilter:
			InvertFilter invFltr = new InvertFilter();
			amt = getFloatValue(getParamValue("Amount", null));
			if (m_ParamSetValues.get("SetFadeMargin").equals("true"))
				invFltr.margin = amt / 2;
			else
				invFltr.margin = 1.0F - getFloatValue(getParamValue(
						"RadialFactor", null));
			return invFltr.filter(srcImg, null);
		case OilFilter:
			OilFilter oilFilter = new OilFilter();
			amt = getFloatValue(getParamValue("Amount", presetValArr[2])) * 5;
			oilFilter.setRange(Math.round(amt));
			return oilFilter.filter(srcImg, null);
		case MarbleFilter:
			MarbleFilter marbleFilter = new MarbleFilter();
			marbleFilter.setXScale(getFloatValue(getParamValue("Amount",
					presetValArr[2])) * 100);
			marbleFilter.setYScale(getFloatValue(presetValArr[3]));
			marbleFilter.setTurbulence(getFloatValue(presetValArr[4]));
			return marbleFilter.filter(srcImg, null);
		case SwizzleFilter:
			String[] matrixOpts = RGPTParams.getVal(action.toString())
					.split("::");
			amt = getFloatValue(getParamValue("Amount", presetValArr[2]))
					* matrixOpts.length;
			index = Math.round(amt);
			if (index < 0)
				index = 0;
			if (index >= matrixOpts.length)
				index = matrixOpts.length - 1;
			// RGPTLogger.logToFile(action+" Amt: "+amt+" Index: "+index+
			// " tot Opt: "+matrixOpts.length);
			SwizzleFilter swizzleFilter = new SwizzleFilter();
			int[] matrix = swizzleFilter.getMatrix();
			String[] mIndex = matrixOpts[index].split(",");
			for (int i = 0; i < mIndex.length; i++)
				matrix[getIntValue(mIndex[i])] = 1;
			return swizzleFilter.filter(srcImg, null);
		case DotColorBorderFilter:
		case DotColorFadeFilter:
			angle = getFloatValue(getParamValue("Angle", presetValArr[3]));
			amt = getFloatValue(getParamValue("Amount", presetValArr[2]));
			if (action.toString().equals("DotColorBorderFilter")
					&& m_ParamSetValues.get("SetFadeMargin").equals("true")) {
				angle = getFloatValue(presetValArr[4]);
				amt = amt * percBorder;
			}
			if (action.toString().equals("DotColorFadeFilter"))
				amt = amt * percBorder;
			ColorHalftoneFilter dotClrBorderFltr = new ColorHalftoneFilter();
			float dotClrRad = angle % 30;
			dotClrBorderFltr.margin = amt;
			if (dotClrRad == 0)
				dotClrRad = getFloatValue(presetValArr[3]);
			dotClrBorderFltr.setdotRadius(dotClrRad);
			resImg = dotClrBorderFltr.filter(srcImg, null);
			if (action.toString().equals("DotColorFadeFilter")) {
				m_ParamSetValues.put("SetFadeMargin", Boolean.toString(true));
				return applyFilter("UniformFadeFilter", resImg);
			}
			return resImg;
		case CrystallizeFilter:
		case CrystallizeFadeFilter:
		case CrystallizeBorderFilter:
			RGPTLogger.logToFile(action + " is executed for: " + actionParam);
			String[] crystalOpts = RGPTParams.getVal(
					actionParam.toString()).split("::");
			amt = getFloatValue(getParamValue("Amount", presetValArr[2]));
			angle = getFloatValue(getParamValue("Angle", presetValArr[3]));
			int crystalCol = Math.round(0.0F);
			double cryMargin = amt;
			if (action.toString().equals("CrystallizeFilter")) {
				amt = amt * crystalOpts.length;
				index = Math.round(amt) - 1;
				if (index < 0)
					index = 0;
				crystalCol = Math.round(angle);
				cryMargin = 1.0F - getFloatValue(getParamValue("RadialFactor",
						null));
			} else {
				index = Math.round(angle) % crystalOpts.length;
				if (action.toString().equals("CrystallizeFadeFilter"))
					cryMargin = amt * percBorder;
			}
			String crystalFltrOpt = crystalOpts[index];
			String[] crystalInfo = crystalFltrOpt.split("_");
			CrystallizeFilter crystalFltr = new CrystallizeFilter();
			crystalFltr.margin = cryMargin;
			crystalFltr.setScale(getFloatValue(presetValArr[4]));
			crystalFltr.setEdgeThickness(getFloatValue(presetValArr[5]));
			Vector<String> crystalSettings = new Vector<String>();
			setSpotColorSettings(crystalInfo[1], crystalSettings);
			crystalFltr.setGridType(getIntValue(crystalSettings.elementAt(0)));
			crystalFltr
					.setFadeEdges(getBoolValue(crystalSettings.elementAt(1)));
			crystalFltr.setEdgeColor(RGPTUtil.getRGBColor(360, crystalCol));
			resImg = crystalFltr.filter(srcImg, null);
			if (action.toString().equals("CrystallizeFadeFilter")) {
				m_ParamSetValues.put("SetFadeMargin", Boolean.toString(true));
				return applyFilter("UniformFadeFilter", resImg);
			}
			return resImg;
		case PointillizeFilter:
		case PointillizeFadeFilter:
		case PointillizeBorderFilter:
			RGPTLogger.logToFile(action + " is executed for: " + actionParam);
			String[] spotOpts = RGPTParams.getVal(actionParam.toString())
					.split("::");
			amt = getFloatValue(getParamValue("Amount", presetValArr[2]));
			angle = getFloatValue(getParamValue("Angle", presetValArr[3]));
			int spotCol = Math.round(0.0F);
			double spotMargin = amt;
			if (action.toString().equals("PointillizeFilter")) {
				amt = amt * spotOpts.length;
				index = Math.round(amt) - 1;
				if (index < 0)
					index = 0;
				spotCol = Math.round(angle);
				spotMargin = 1.0F - getFloatValue(getParamValue("RadialFactor",
						null));
			} else {
				index = Math.round(angle) % spotOpts.length;
				if (action.toString().equals("PointillizeFadeFilter"))
					spotMargin = amt * percBorder;
			}
			String spotFltrOpt = spotOpts[index];
			String[] spotInfo = spotFltrOpt.split("_");
			PointillizeFilter spotFltr = new PointillizeFilter();
			spotFltr.margin = spotMargin;
			spotFltr.setScale(getFloatValue(presetValArr[4]));
			spotFltr.setEdgeThickness(getFloatValue(presetValArr[5]));
			Vector<String> spotSettings = new Vector<String>();
			setSpotColorSettings(spotInfo[1], spotSettings);
			spotFltr.setGridType(getIntValue(spotSettings.elementAt(0)));
			spotFltr.setFadeEdges(getBoolValue(spotSettings.elementAt(1)));
			spotFltr.setEdgeColor(RGPTUtil.getRGBColor(360, spotCol));
			resImg = spotFltr.filter(srcImg, null);
			if (action.toString().equals("PointillizeFadeFilter")) {
				m_ParamSetValues.put("SetFadeMargin", Boolean.toString(true));
				return applyFilter("UniformFadeFilter", resImg);
			}
			return resImg;
		case SpotColorFilters:
			angle = getFloatValue(getParamValue("Angle", presetValArr[3]));
			int colIndex = Math.round(angle);
			String[] filterOpts = RGPTParams.getVal(action.toString())
					.split("::");
			amt = getFloatValue(getParamValue("Amount", presetValArr[2]))
					* filterOpts.length;
			index = Math.round(amt) - 1;
			if (index < 0)
				index = 0;
			String filter = filterOpts[index];
			String spotFltrSetVal = "";
			String[] spotFltrValues = null;
			String[] fltrInfo = filter.split("_");
			String fltrParam = "";
			spotFltrSetVal = RGPTParams
					.getVal(fltrInfo[0] + "_SetValues");
			// RGPTLogger.logToFile(action+" Filter Executed: "+filter+" Props: "+
			// spotFltrSetVal);
			if (spotFltrSetVal != null && spotFltrSetVal.length() > 0)
				spotFltrValues = spotFltrSetVal.split("::");
			if (filter.equals("ColorHalftoneFilter")) {
				ColorHalftoneFilter halftoneFilter = new ColorHalftoneFilter();
				float dotRad = angle % 30;
				if (dotRad < getFloatValue(spotFltrValues[0]))
					dotRad = getFloatValue(spotFltrValues[0]);
				halftoneFilter.setdotRadius(dotRad);
				return halftoneFilter.filter(srcImg, null);
			} else if (filter.equals("MosiacFilter")) {
				int blockSize = Math.round(angle % 100);
				if (blockSize < getIntValue(spotFltrValues[0]))
					blockSize = getIntValue(spotFltrValues[0]);
				return (new BlockFilter(blockSize)).filter(srcImg, null);
			} else if (filter.startsWith("PointillizeFilter")) {
				PointillizeFilter ptFilters = new PointillizeFilter();
				ptFilters.setScale(getFloatValue(spotFltrValues[0]));
				ptFilters.setEdgeThickness(getFloatValue(spotFltrValues[1]));
				Vector<String> fltrSettings = new Vector<String>();
				fltrParam = fltrInfo[1];
				setSpotColorSettings(fltrParam, fltrSettings);
				ptFilters.setGridType(getIntValue(fltrSettings.elementAt(0)));
				ptFilters.setFadeEdges(getBoolValue(fltrSettings.elementAt(1)));
				ptFilters.setEdgeColor(RGPTUtil.getRGBColor(360, colIndex));
				return ptFilters.filter(srcImg, null);
			} else if (filter.startsWith("CrystallizeFilter")) {
				CrystallizeFilter crystalFilter = new CrystallizeFilter();
				crystalFilter.setScale(getFloatValue(spotFltrValues[0]));
				crystalFilter
						.setEdgeThickness(getFloatValue(spotFltrValues[1]));
				fltrParam = fltrInfo[1];
				Vector<String> fltrSettings = new Vector<String>();
				setSpotColorSettings(fltrParam, fltrSettings);
				crystalFilter
						.setGridType(getIntValue(fltrSettings.elementAt(0)));
				crystalFilter.setFadeEdges(getBoolValue(fltrSettings
						.elementAt(1)));
				crystalFilter.setEdgeColor(RGPTUtil.getRGBColor(360, colIndex));
				return crystalFilter.filter(srcImg, null);
			}
			break;
		}
		return srcImg;
	}

	// This method is used by Warp Filter to get the Grid Pts
	private void setGridPts(String gridPtKey, WarpGrid grid) {
		Vector<Point2D.Double> gridPts = null;
		Point2D.Double gridPt = null;
		gridPts = (Vector<Point2D.Double>) m_FilterParamValues.get(gridPtKey);
		for (int i = 0; i < gridPts.size(); i++) {
			gridPt = gridPts.elementAt(i);
			grid.xGrid[i] = (float) gridPt.x;
			grid.yGrid[i] = (float) gridPt.y;
		}
	}

	// First Element is Grid Type and 2nd element is Fade
	private void setSpotColorSettings(String fltrParam,
			Vector<String> fltrSettings) {
		int gridType = 0;
		boolean fade = false;
		if (fltrParam.equals("FadeRandom"))
			fade = true;
		else if (fltrParam.equals("Sq"))
			gridType = 1;
		else if (fltrParam.equals("Hex"))
			gridType = 2;
		else if (fltrParam.equals("SqOct"))
			gridType = 3;
		else if (fltrParam.equals("Triangle"))
			gridType = 4;
		else if (fltrParam.equals("FadeHex")) {
			fade = true;
			gridType = 2;
		} else if (fltrParam.equals("FadeSqOct")) {
			fade = true;
			gridType = 3;
		}
		fltrSettings.addElement(Integer.toString(gridType));
		fltrSettings.addElement(Boolean.toString(fade));
	}

	private void setDefaultParamValues() {
		String[] presetValArr = null;
		String actionParam = RGPTParams.getVal(m_Action.toString()
				+ "_UseValues", m_Action.toString());
		RGPTLogger.logToFile(m_Action + " is executed for: " + actionParam);
		// if (actionParam.contains("NEW")) { String[] actionParams =
		// actionParam.split("NEW");
		// actionParam = actionParams[0]; }
		String presetVal = RGPTParams.getVal(actionParam + "_SetValues");
		float radFactor = RGPTParams
				.getFloatVal("ImageFilterRadialFactor");
		if (presetVal != null && presetVal.length() > 0)
			presetValArr = presetVal.split("::");
		m_ParamSetValues.put("RadialFactor", Float.toString(radFactor));
		m_ParamSetValues.put("SetFadeMargin", Boolean.toString(false));
		m_ParamSetValues.put("CenterX", Float.toString(0.5F));
		m_ParamSetValues.put("CenterY", Float.toString(0.5F));
		switch (m_Action) {
		case CircleFilter:
			m_ParamSetValues.put("Height", getDefParamValue(presetValArr[0]));
			m_ParamSetValues.put("SpreadAngle",
					getDefParamValue(presetValArr[1]));
			m_ParamSetValues.put("RotAngle", getDefParamValue(presetValArr[2]));
			break;
		case WarpFilter:
			m_FilterParamValues.put("SrcGirdPts", new Vector<Point2D.Double>());
			m_FilterParamValues.put("DesGirdPts", new Vector<Point2D.Double>());
			break;
		case PinchFilter:
		case BulgeFilter:
			m_ParamSetValues.put("Angle", getDefParamValue(presetValArr[0]));
			m_ParamSetValues.put("CenterX", getDefParamValue(presetValArr[1]));
			m_ParamSetValues.put("CenterY", getDefParamValue(presetValArr[2]));
			m_ParamSetValues.put("Radius", getDefParamValue(presetValArr[3]));
			m_ParamSetValues.put("Amount", getDefParamValue(presetValArr[4]));
			break;
		case SphereFilter:
			m_ParamSetValues.put("CenterX", getDefParamValue(presetValArr[0]));
			m_ParamSetValues.put("CenterY", getDefParamValue(presetValArr[1]));
			m_ParamSetValues.put("Radius", getDefParamValue(presetValArr[2]));
			m_ParamSetValues.put("Amount", getDefParamValue(presetValArr[3]));
			m_ParamSetValues.put("EdgeAction",
					getDefParamValue(presetValArr[4]));
			break;
		case WaterFilter:
		case WaterRippleFilter:
			String waterFltr = RGPTActionListener.ImageFilterActions.WaterFilter
					.toString();
			String rippleFltr = RGPTActionListener.ImageFilterActions.WaterRippleFilter
					.toString();
			m_ParamSetValues.put("CenterX", getDefParamValue(presetValArr[0]));
			m_ParamSetValues.put("CenterY", getDefParamValue(presetValArr[1]));
			m_ParamSetValues.put("Radius", getDefParamValue(presetValArr[2]));
			m_ParamSetValues.put("Amount", getDefParamValue(presetValArr[3]));
			if (m_Action.toString().equals(waterFltr))
				m_ParamSetValues.put("Amplitude",
						getDefParamValue(presetValArr[4]));
			else
				m_ParamSetValues.put("Wavelength",
						getDefParamValue(presetValArr[4]));
			m_ParamSetValues.put("Phase", getDefParamValue(presetValArr[5]));
			break;
		case TwirlFilter:
			m_ParamSetValues.put("CenterX", getDefParamValue(presetValArr[0]));
			m_ParamSetValues.put("CenterY", getDefParamValue(presetValArr[1]));
			m_ParamSetValues.put("Radius", getDefParamValue(presetValArr[2]));
			m_ParamSetValues.put("Angle", getDefParamValue(presetValArr[3]));
			break;
		case CircleLightFilter:
			m_ParamSetValues.put("CenterX", getDefParamValue(presetValArr[0]));
			m_ParamSetValues.put("CenterY", getDefParamValue(presetValArr[1]));
			m_ParamSetValues.put("Radius", getDefParamValue(presetValArr[2]));
			m_ParamSetValues.put("Amount", getDefParamValue(presetValArr[3]));
			m_ParamSetValues
					.put("ConeAngle", getDefParamValue(presetValArr[4]));
			break;
		case SpotLightFilter:
			m_ParamSetValues.put("CenterX", getDefParamValue(presetValArr[0]));
			m_ParamSetValues.put("CenterY", getDefParamValue(presetValArr[1]));
			m_ParamSetValues.put("Radius", getDefParamValue(presetValArr[2]));
			m_ParamSetValues.put("Amount", getDefParamValue(presetValArr[3]));
			m_ParamSetValues.put("Angle", getDefParamValue(presetValArr[4]));
			m_ParamSetValues.put("Focus", getDefParamValue(presetValArr[5]));
			m_ParamSetValues
					.put("ConeAngle", getDefParamValue(presetValArr[6]));
			break;
		case PointLightFilter:
		case MultipleImageFilter:
			m_ParamSetValues.put("CenterX", getDefParamValue(presetValArr[0]));
			m_ParamSetValues.put("CenterY", getDefParamValue(presetValArr[1]));
			m_ParamSetValues.put("Radius", getDefParamValue(presetValArr[2]));
			m_ParamSetValues.put("Amount", getDefParamValue(presetValArr[3]));
			m_ParamSetValues.put("Angle", getDefParamValue(presetValArr[4]));
			break;
		case FlareLightFilter:
			m_ParamSetValues.put("CenterX", getDefParamValue(presetValArr[0]));
			m_ParamSetValues.put("CenterY", getDefParamValue(presetValArr[1]));
			m_ParamSetValues.put("Radius", getDefParamValue(presetValArr[2]));
			m_ParamSetValues.put("Amount", getDefParamValue(presetValArr[3]));
			m_ParamSetValues.put("BaseAmount",
					getDefParamValue(presetValArr[4]));
			m_ParamSetValues.put("RingAmount",
					getDefParamValue(presetValArr[5]));
			m_ParamSetValues
					.put("RingWidth", getDefParamValue(presetValArr[6]));
			break;
		case RingLightFilter:
			m_ParamSetValues.put("CenterX", getDefParamValue(presetValArr[0]));
			m_ParamSetValues.put("CenterY", getDefParamValue(presetValArr[1]));
			m_ParamSetValues.put("Radius", getDefParamValue(presetValArr[2]));
			m_ParamSetValues.put("Amount", getDefParamValue(presetValArr[3]));
			m_ParamSetValues
					.put("RayAmount", getDefParamValue(presetValArr[4]));
			m_ParamSetValues.put("BaseAmount",
					getDefParamValue(presetValArr[5]));
			m_ParamSetValues
					.put("RingWidth", getDefParamValue(presetValArr[6]));
			break;
		case SparkleFilter:
			m_ParamSetValues.put("CenterX", getDefParamValue(presetValArr[0]));
			m_ParamSetValues.put("CenterY", getDefParamValue(presetValArr[1]));
			m_ParamSetValues.put("Radius", getDefParamValue(presetValArr[2]));
			m_ParamSetValues.put("Amount", getDefParamValue(presetValArr[3]));
			m_ParamSetValues.put("Randomness",
					getDefParamValue(presetValArr[4]));
			break;
		case ColorSketchFilter:
			m_ParamSetValues.put("CenterX", getDefParamValue(presetValArr[0]));
			m_ParamSetValues.put("CenterY", getDefParamValue(presetValArr[1]));
			m_ParamSetValues.put("Amount", getDefParamValue(presetValArr[2]));
			m_ParamSetValues.put("BumpSoftness",
					getDefParamValue(presetValArr[3]));
			break;
		case BurstedImageFilter:
		case BWFilter:
		case SkeletonFilter:
		case LightUpFilter:
		case TemperatureFilter:
		case GammaFilter:
		case PosterizeFilter:
		case ImageOutlineFilter:
		case NoiseFilter:
		case DissolveFilter:
		case DiffuseFilter:
		case MarbleFilter:
		case OilFilter:
		case SwizzleFilter:
			m_ParamSetValues.put("CenterX", getDefParamValue(presetValArr[0]));
			m_ParamSetValues.put("CenterY", getDefParamValue(presetValArr[1]));
			m_ParamSetValues.put("Amount", getDefParamValue(presetValArr[2]));
			break;
		case MirrorFilter:
			m_ParamSetValues.put("CenterX", getDefParamValue(presetValArr[0]));
			m_ParamSetValues.put("CenterY", getDefParamValue(presetValArr[1]));
			m_ParamSetValues.put("Amount", getDefParamValue(presetValArr[2]));
			m_ParamSetValues.put("MirAngle", getDefParamValue(presetValArr[3]));
			break;
		case KaleidoscopeFilter:
		case SpotColorFilters:
		case SplitFadeFilter:
		case UniformFadeFilter:
		case BoxFadeFilter:
		case LinearFadeFilter:
		case HorizontalFadeFilter:
		case VerticalFadeFilter:
		case NoiseFadeFilter:
		case BlurFadeFilter:
		case DotColorBorderFilter:
		case CrystallizeFilter:
		case CrystallizeFadeFilter:
		case CrystallizeBorderFilter:
		case PointillizeFilter:
		case PointillizeFadeFilter:
		case PointillizeBorderFilter:
		case DotColorFadeFilter:
			m_ParamSetValues.put("CenterX", getDefParamValue(presetValArr[0]));
			m_ParamSetValues.put("CenterY", getDefParamValue(presetValArr[1]));
			m_ParamSetValues.put("Amount", getDefParamValue(presetValArr[2]));
			m_ParamSetValues.put("Angle", getDefParamValue(presetValArr[3]));
			break;
		}
		// RGPTLogger.logToFile(m_Action+" Def Param Values: "+m_ParamSetValues);
	}

	private String getParamValue(String key, String nvPair) {
		String val = m_ParamSetValues.get(key);
		if (val != null)
			return val;
		if (nvPair == null)
			return null;
		return getDefParamValue(nvPair);
	}

	private String getDefParamValue(String nvPair) {
		String[] nvPairVal = nvPair.split(":=");
		return nvPairVal[1];
	}

	private float getFloatValue(String nvPair) {
		if (nvPair == null)
			return 0.0F;
		String[] nvPairVal = nvPair.split(":=");
		String val = nvPairVal[0];
		if (nvPairVal.length == 2)
			val = nvPairVal[1];
		return (Float.valueOf(val)).floatValue();
	}

	private int getIntValue(String nvPair) {
		if (nvPair == null)
			return -1;
		String[] nvPairVal = nvPair.split(":=");
		String val = nvPairVal[0];
		if (nvPairVal.length == 2)
			val = nvPairVal[1];
		return (Integer.valueOf(val)).intValue();
	}

	private boolean getBoolValue(String nvPair) {
		String[] nvPairVal = nvPair.split(":=");
		String val = nvPairVal[0];
		if (nvPairVal.length == 2)
			val = nvPairVal[1];
		return (Boolean.valueOf(val)).booleanValue();
	}

	private int getColorValue(String nvPair) {
		String[] nvPairVal = nvPair.split(":=");
		String[] rgbVal = nvPairVal[1].split(":");
		Color color = new Color(getIntValue(rgbVal[0]), getIntValue(rgbVal[1]),
				getIntValue(rgbVal[2]));
		return color.getRGB();
	}

	public String toString() {
		return "ImageFilterHandler for " + m_Action.toString();
	}
}

public class ImageFilterController implements Serializable {
	// This maintains the set of Image Filters that is applied on the Image
	public Vector<ImageFilterHandler> m_SetImageFilterHdlr;

	public ImageFilterController() {
		m_SetImageFilterHdlr = new Vector<ImageFilterHandler>();
	}

	public BufferedImage applyFilter(BufferedImage srcImg, String filterAction,
			String paramAction, float val) {
		return applyFilter(srcImg, filterAction, paramAction, val, false);
	}

	public BufferedImage applyFilter(BufferedImage srcImg, String filterAction,
			String paramAction, float val, boolean usePixelRGB) {
		ImageFilterController imgFilterCtrl = new ImageFilterController();
		ImageFilterHandler imgFilter = imgFilterCtrl
				.createImageFilterHandler(filterAction);
		if (imgFilter == null) {
			RGPTLogger.logToFile("No Image Filter Hdlr for: " + filterAction);
			return srcImg;
		}
		ImageFilterHandler.SetImageFilter status = imgFilter
				.setImageFilterParam(filterAction + "_" + paramAction, val);
		// RGPTLogger.logToFile(filterAction+" Status: "+status);
		if (usePixelRGB)
			return applyFilter(srcImg, imgFilter);
		BufferedImage finImg = imgFilter.applyFilter(srcImg);
		boolean isTranslucent = false;
		if (imgFilter.getAction().toString().contains("FadeFilter"))
			isTranslucent = true;
		return ImageUtils.createImageCopy(finImg, isTranslucent);
	}

	public ImageFilterHandler createImageFilterHandler(String action) {
		return createImageFilterHandler(RGPTActionListener.ImageFilterActions
				.valueOf(action));
	}

	public ImageFilterHandler createImageFilterHandler(
			RGPTActionListener.ImageFilterActions action) {
		// RGPTLogger.logToFile("ImageFilterHandler for: "+action.toString());
		ImageFilterHandler imgFilterHdlr = getImageFilterHandler(action
				.toString());
		if (imgFilterHdlr != null)
			return imgFilterHdlr;
		RGPTActionListener.ImageFilterActions[] onOffImgFltrActions = null;
		onOffImgFltrActions = RGPTActionListener.ON_OFF_PARAM_CONTROL_IMAGE_FILTERS;
		String[] onOffImgFltrs = RGPTUtil
				.enumToStringArray(onOffImgFltrActions);
		if (RGPTUtil.contains(onOffImgFltrs, action.toString()))
			return new OnOffParamControlFilterHdlr(action, this);
		switch (action) {
		// case SharpenFilter : case ReduceNoiseFilter : case GrayScaleFilter :
		// case SpectrumColorFilter : case InvertFilter :
		// case SoloarizeFilter : case GrayOutFilter : case NegativeFilter :
		// case ComicFilter : case BWComicFilter : case SketchedFilter :
		// case ComicSketchedFilter : case EmbossFilter : case BWSketchedFilter
		// :
		// return new OnOffControlFilterHdlr(action, this);
		case ContrastFilter:
		case BrightnessFilter:
			return new ContrastNBrightFilterHdlr(action.toString());
		case ExposureFilter:
		case RemoveBlurFilter:
		case GainFilter:
		case BiasFilter:
		case HueFilter:
		case SaturationFilter:
		case ColorBrightnessFilter:
		case AdjustRedFilter:
		case AdjustGreenFilter:
		case AdjustBlueFilter:
			return new IncDecControlFilterHdlr(action);
		case GlowFilter:
		case LightingFilter:
			return new SliderControlFilterHdlr(action);
		}
		return null;
	}

	public ImageFilterHandler getImageFilterHandler(String action) {
		ImageFilterHandler imgFilterHdlr = null;
		for (int i = 0; i < m_SetImageFilterHdlr.size(); i++) {
			imgFilterHdlr = m_SetImageFilterHdlr.elementAt(i);
			if (imgFilterHdlr.getAction().equals(action))
				return imgFilterHdlr;
		}
		return null;
	}

	public ImageFilterHandler setImageFilterHandler(String action) {
		ImageFilterHandler imgFilterHdlr = getImageFilterHandler(action);
		if (imgFilterHdlr == null) {
			imgFilterHdlr = createImageFilterHandler(RGPTActionListener.ImageFilterActions
					.valueOf(action));
			m_SetImageFilterHdlr.addElement(imgFilterHdlr);
		}
		return imgFilterHdlr;
	}

	public ImageFilterHandler setImageFilterParam(String action, float val) {
		return setImageFilterParam(
				RGPTActionListener.ImageFilterActions.valueOf(action), val);
	}

	public ImageFilterHandler setImageFilterParam(
			RGPTActionListener.ImageFilterActions imgFilterAction, float val) {
		ImageFilterHandler.SetImageFilter status = ImageFilterHandler.SetImageFilter.OFF;
		String[] filterParams = imgFilterAction.toString().split("_");
		if (filterParams.length == 0)
			RGPTLogger.logToFile("Issue with Action Param: " + imgFilterAction);
		String action = filterParams[0], paramSetting = filterParams[1];
		ImageFilterHandler imgFilterHdlr = setImageFilterHandler(action);
		// RGPTLogger.logToFile("Image Filter Handler for action: "+imgFilterHdlr.getAction());
		if (paramSetting.equals("Cancel")) {
			m_SetImageFilterHdlr.remove(imgFilterHdlr);
			return null;
		}
		status = imgFilterHdlr.setImageFilterParam(imgFilterAction, val);
		if (status.toString().equals(
				ImageFilterHandler.SetImageFilter.OFF.toString())) {
			m_SetImageFilterHdlr.remove(imgFilterHdlr);
			return null;
		}
		return imgFilterHdlr;
	}

	// Apply all the applicable Filters
	public BufferedImage applyFilter(BufferedImage srcImg) {
		return applyFilter(srcImg, null);
	}

	public BufferedImage applyFilter(BufferedImage srcImg,
			ImageFilterHandler imgFilterHdlr) {
		BufferedImage resImg = ImageUtils.createImageCopy(srcImg);
		int[][][] pixel_rgb = ImageUtils.BufferedImagetoPixel3DRGB(resImg);
		resImg = ImageUtils.Pixel3DRGBtoBufferedImage(pixel_rgb);
		boolean isTranslucent = false;
		if (imgFilterHdlr != null) {
			resImg = imgFilterHdlr.applyFilter(resImg);
		} else {
			if (m_SetImageFilterHdlr.size() == 0)
				return srcImg;
			for (int i = 0; i < m_SetImageFilterHdlr.size(); i++) {
				imgFilterHdlr = m_SetImageFilterHdlr.elementAt(i);
				if (imgFilterHdlr.getAction().toString().contains("FadeFilter"))
					isTranslucent = true;
				// RGPTLogger.logToFile("Applying Filter using: "+imgFilterHdlr);
				resImg = imgFilterHdlr.applyFilter(resImg);
				// ImageUtils.displayImage(resImg,
				// "In Controller: "+imgFilterHdlr);
			}
		}
		BufferedImage finImg = ImageUtils
				.createImageCopy(resImg, isTranslucent);
		return finImg;
	}

	// Apply Last Filters
	public BufferedImage applyLastFilter(BufferedImage srcImg) {
		BufferedImage resImg = srcImg;
		ImageFilterHandler imgFilterHdlr = m_SetImageFilterHdlr.lastElement();
		resImg = imgFilterHdlr.applyFilter(resImg);
		BufferedImage finImg = ImageUtils.createImageCopy(resImg);
		return finImg;
	}

	public String toString() {
		return "Set ImageFilterHandler are " + m_SetImageFilterHdlr;
	}

}