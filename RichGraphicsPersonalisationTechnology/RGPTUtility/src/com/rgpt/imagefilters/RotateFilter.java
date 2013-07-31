package com.rgpt.imagefilters;

import java.awt.*;
import java.awt.image.*;

/**
 * A filter which rotates an image. These days this is easier done with Java2D, but this filter remains.
 */
public class RotateFilter extends TransformFilter {
	
	private float angle;
	private float cos, sin;
	private boolean resize = true;

	/**
     * Construct a RotateFilter.
     */
    public RotateFilter() {
		this(ImageMath.PI);
	}

	/**
     * Construct a RotateFilter.
     * @param angle the angle to rotate
     */
	public RotateFilter(float angle) {
		this(angle, true);
	}

	/**
     * Construct a RotateFilter.
     * @param angle the angle to rotate
     * @param resize true if the output image should be resized
     */
	public RotateFilter(float angle, boolean resize) {
		setAngle(angle);
		this.resize = resize;
	}

	/**
     * Specifies the angle of rotation.
     * @param angle the angle of rotation.
     * @angle
     * @see #getAngle
     */
	public void setAngle(float angle) {
		this.angle = angle;
		cos = (float)Math.cos(this.angle);
		sin = (float)Math.sin(this.angle);
	}

	/**
     * Returns the angle of rotation.
     * @return the angle of rotation.
     * @see #setAngle
     */
	public float getAngle() {
		return angle;
	}

    public void transform(double x, double y, double[] retcoord) {
        // Remember that the coordinate system is upside down so apply
        // the transform as if the angle were negated.
        // cos(-angle) =  cos(angle)
        // sin(-angle) = -sin(angle)
        retcoord[0] = cos * x + sin * y;
        retcoord[1] = cos * y - sin * x;
        //retcoord[0] = cos * x - sin * y;
        //retcoord[1] = cos * y + sin * x;
    }

    public void itransform(double x, double y, double[] retcoord) {
        // Remember that the coordinate system is upside down so apply
        // the transform as if the angle were negated.  Since inverting
        // the transform is also the same as negating the angle, itransform
        // is calculated the way you would expect to calculate transform.
        retcoord[0] = cos * x - sin * y;
        retcoord[1] = cos * y + sin * x;
    }

    private double coord[] = new double[2];
    protected void transformSpace1(Rectangle rect) {
      if (!resize)return;
        double minx = Double.POSITIVE_INFINITY;
        double miny = Double.POSITIVE_INFINITY;
        double maxx = Double.NEGATIVE_INFINITY;
        double maxy = Double.NEGATIVE_INFINITY;
        for (int y = 0; y <= 1; y++) {
            for (int x = 0; x <= 1; x++) {
                transform(rect.x + x * rect.width,
                          rect.y + y * rect.height,
                          coord);
                minx = Math.min(minx, coord[0]);
                miny = Math.min(miny, coord[1]);
                maxx = Math.max(maxx, coord[0]);
                maxy = Math.max(maxy, coord[1]);
            }
        }
        rect.x = (int) Math.floor(minx);
        rect.y = (int) Math.floor(miny);
        rect.width = (int) Math.ceil(maxx) - rect.x + 1;
        rect.height = (int) Math.ceil(maxy) - rect.y + 1;
    }

   
	protected void transformSpace(Rectangle rect) {
		if (resize) {
			Point out = new Point(0, 0);
			int minx = Integer.MAX_VALUE;
			int miny = Integer.MAX_VALUE;
			int maxx = Integer.MIN_VALUE;
			int maxy = Integer.MIN_VALUE;
			int w = rect.width;
			int h = rect.height;
			int x = rect.x;
			int y = rect.y;

			for (int i = 0; i < 4; i++)  {
				switch (i) {
				case 0: transform(x, y, out); break;
				case 1: transform(x + w, y, out); break;
				case 2: transform(x, y + h, out); break;
				case 3: transform(x + w, y + h, out); break;
				}
				minx = Math.min(minx, out.x);
				miny = Math.min(miny, out.y);
				maxx = Math.max(maxx, out.x);
				maxy = Math.max(maxy, out.y);
			}

			rect.x = minx;
			rect.y = miny;
			rect.width = maxx - rect.x;
			rect.height = maxy - rect.y;
		}
	}

	private void transform(int x, int y, Point out) {
		out.x = (int)((x * cos) + (y * sin));
		out.y = (int)((y * cos) - (x * sin));
	}

	protected void transformInverse(int x, int y, float[] out) {
		out[0] = (x * cos) - (y * sin);
		out[1] = (y * cos) + (x * sin);
	}

	public String toString() {
		return "Rotate "+(int)(angle * 180 / Math.PI);
	}

}
