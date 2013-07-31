// RGPT PACKAGES
package com.rgpt.imageutil;

import java.awt.image.BufferedImage;
import java.io.Serializable;

import com.rgpt.util.RGPTActionListener;

// This interface is used to set and process Image Filter.

public interface ImageFilterHandler extends Serializable {
	// This enum specifies the set and unset options for ImageFilterHandler.
	public static enum SetImageFilter {
		ON, OFF, ACTIVATED
	}

	// Returns the Action Component associated with the ImageFilterHandler
	public String getAction();

	// Returns the value associated with the ImageFilterHandler
	public float getValue();

	// This function is called to set Image Filter Data. The Action Param is
	// composed of
	// ImageFilterActions, MethodName(Increase/Decrease/On/Of) and optional
	// param name
	// e.g. SharpenFilter_On, SharpenFilter_Off, SharpenFilter_Def,
	// SharpenFilter_Cancel
	// ContrastFilter_Inc, ContrastFilter_Dec, ContrastFilter_Def,
	// ContrastFilter_Cancel,
	public SetImageFilter setImageFilterParam(String action, float val);

	public SetImageFilter setImageFilterParam(
			RGPTActionListener.ImageFilterActions action, float val);

	// Apply Filter
	public BufferedImage applyFilter(BufferedImage src);

}