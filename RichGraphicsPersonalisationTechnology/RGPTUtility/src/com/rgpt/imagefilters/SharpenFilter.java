package com.rgpt.imagefilters;

//import java.awt.image.*;

/**
 * A filter which performs a simple 3x3 sharpening operation.
 */
public class SharpenFilter extends ConvolveFilter {
	
	private static float[] sharpenMatrix = {
		 0.0f, -0.2f,  0.0f,
		-0.2f,  1.8f, -0.2f,
		 0.0f, -0.2f,  0.0f
	};

	public SharpenFilter() {
		super(sharpenMatrix);
	}

	public String toString() {
		return "Blur/Sharpen";
	}
}
