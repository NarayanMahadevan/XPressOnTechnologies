/*
Copyright 2006 Jerry Huxtable

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.rgpt.imagefilters;

import java.awt.image.*;

/**
 * A filter which solarizes an image.
 */
public class SolarizeFilter extends TransferFilter {

	protected float transferFunction( float v ) {
		return v > 0.5f ? 2*(v-0.5f) : 2*(0.5f-v);
	}

	public double margin = 0.0;
   public int imgWt = 0, imgHt = 0;
   public void setDimensions(int width, int height) { imgWt = width; imgHt = height; }
	
   public int filterRGB(int x, int y, int rgb) {
      if (!ImageFilterUtils.filterRGB(margin, imgWt, imgHt, x, y)) return rgb; 
      return super.filterRGB(x, y, rgb);
   }
   
   public String toString() {
		return "Colors/Solarize";
	}
}

