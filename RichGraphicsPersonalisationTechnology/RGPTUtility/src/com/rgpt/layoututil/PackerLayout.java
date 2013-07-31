package com.rgpt.layoututil;

/*

This layout is a bit tricky to describe. As you add components to the layout, it keeps 
the bounding box of all components added so far. You specify for each component
 where it goes in relation to the current bounding box: left, right, above or below 
 as well as the fill and alignment in its cell.
	setLayout(new PackerLayout());
	JButton b1 = new JButton("One");
	JButton b2 = new JButton("Two");
	JButton b3 = new JButton("Three");
	JButton b4 = new JButton("Four");
	JButton b5 = new JButton("Five");
	JButton b6 = new JButton("Six");

	b2.setFont(new Font("serif", Font.PLAIN, 24));
	add(b1);
	add(b2, PackerLayout.LEFT_CENTER);
	add(b3, PackerLayout.BOTTOM_CENTER_FILL);
	add(b4, PackerLayout.TOP_CENTER_FILL);
	add(b5, PackerLayout.TOP_LEFT);
	add(b6, PackerLayout.RIGHT_CENTER);
The main use for PackerLayout is with fairly simple panels with just three or four components.

*/

import java.awt.*;

public class PackerLayout extends ConstraintLayout implements Direction {

	public final static Integer LEFT_TOP = new Integer((Direction.LEFT << 8) | Alignment.TOP);
	public final static Integer LEFT_CENTER = new Integer((Direction.LEFT << 8) | Alignment.CENTER);
	public final static Integer LEFT_BOTTOM = new Integer((Direction.LEFT << 8) | Alignment.BOTTOM);
	public final static Integer RIGHT_TOP = new Integer((Direction.RIGHT << 8) | Alignment.TOP);
	public final static Integer RIGHT_CENTER = new Integer((Direction.RIGHT << 8) | Alignment.CENTER);
	public final static Integer RIGHT_BOTTOM = new Integer((Direction.RIGHT << 8) | Alignment.BOTTOM);

	public final static Integer TOP_LEFT = new Integer((Direction.TOP << 8) | Alignment.LEFT);
	public final static Integer TOP_CENTER = new Integer((Direction.TOP << 8) | Alignment.CENTER);
	public final static Integer TOP_RIGHT = new Integer((Direction.TOP << 8) | Alignment.RIGHT);
	public final static Integer BOTTOM_LEFT = new Integer((Direction.BOTTOM << 8) | Alignment.LEFT);
	public final static Integer BOTTOM_CENTER = new Integer((Direction.BOTTOM << 8) | Alignment.CENTER);
	public final static Integer BOTTOM_RIGHT = new Integer((Direction.BOTTOM << 8) | Alignment.RIGHT);

	public final static Integer LEFT_TOP_FILL = new Integer((Direction.LEFT << 8) | Alignment.TOP | 0x80);
	public final static Integer LEFT_CENTER_FILL = new Integer((Direction.LEFT << 8) | Alignment.CENTER | 0x80);
	public final static Integer LEFT_BOTTOM_FILL = new Integer((Direction.LEFT << 8) | Alignment.BOTTOM | 0x80);
	public final static Integer RIGHT_TOP_FILL = new Integer((Direction.RIGHT << 8) | Alignment.TOP | 0x80);
	public final static Integer RIGHT_CENTER_FILL = new Integer((Direction.RIGHT << 8) | Alignment.CENTER | 0x80);
	public final static Integer RIGHT_BOTTOM_FILL = new Integer((Direction.RIGHT << 8) | Alignment.BOTTOM | 0x80);

	public final static Integer TOP_LEFT_FILL = new Integer((Direction.TOP << 8) | Alignment.LEFT | 0x80);
	public final static Integer TOP_CENTER_FILL = new Integer((Direction.TOP << 8) | Alignment.CENTER | 0x80);
	public final static Integer TOP_RIGHT_FILL = new Integer((Direction.TOP << 8) | Alignment.RIGHT | 0x80);
	public final static Integer BOTTOM_LEFT_FILL = new Integer((Direction.BOTTOM << 8) | Alignment.LEFT | 0x80);
	public final static Integer BOTTOM_CENTER_FILL = new Integer((Direction.BOTTOM << 8) | Alignment.CENTER | 0x80);
	public final static Integer BOTTOM_RIGHT_FILL = new Integer((Direction.BOTTOM << 8) | Alignment.RIGHT | 0x80);

	protected int hGap, vGap;

	public PackerLayout() {
		this(0, 0, 0, 0);
	}

	public PackerLayout(int hGap, int vGap, int hMargin, int vMargin) {
		this.hGap = hGap;
		this.vGap = vGap;
		this.hMargin = hMargin;
		this.vMargin = vMargin;
	}

	public void measureLayout(Container target, Dimension dimension, int type)  {
		int count = target.getComponentCount();
		if (count > 0) {
			Insets insets = target.getInsets();
//			Dimension size = target.getSize();

			int minX = 0;
			int maxX = 0;
			int minY = 0;
			int maxY = 0;
			int position = Direction.RIGHT;
			int alignment = Alignment.CENTER;
			Rectangle[] sizes = new Rectangle[count];

			for (int i = 0; i < count; i++) {
				Component c = target.getComponent(i);
				if (includeComponent(c)) {
					Dimension d = getComponentSize(c, type);
					int x = 0;
					int y = 0;
					int w = d.width;
					int h = d.height;
					int cellX = 0;
					int cellY = 0;
					int cellW = w;
					int cellH = h;
					int fill = Alignment.FILL_NONE;
					
					if (i == 0) {
						maxX = w;
						maxY = h;
					} else {
						Integer n = (Integer)getConstraint(c);
						int v = 0;

						if (n != null)
							v = n.intValue();
						position = (v >> 8) & 0x7f;
						alignment = v & 0xff;
						if ((v & 0x80) != 0)
							fill = Alignment.FILL_BOTH;

						switch (position) {
						case Direction.LEFT:
						case Direction.RIGHT:
							switch (position) {
							case Direction.LEFT:
								x = minX - w - hGap;
								break;
							case Direction.RIGHT:
								x = maxX + hGap;
								break;
							}
							if (alignment == Alignment.TOP)
								y = minY;
							else if (alignment == Alignment.BOTTOM)
								y = maxY-h;
							else if (alignment == Alignment.CENTER)
								y = (minY+maxY-h)/2;
							break;
						case Direction.TOP:
						case Direction.BOTTOM:
							switch (position) {
							case Direction.TOP:
								y = minY - h - vGap;
								break;
							case Direction.BOTTOM:
								y = maxY + vGap;
								break;
							}
							if (alignment == Alignment.LEFT)
								x = minX;
							else if (alignment == Alignment.RIGHT)
								x = maxX-w;
							else if (alignment == Alignment.CENTER)
								x = (minX+maxX-w)/2;
							break;
						}
						minX = Math.min(minX, x);
						maxX = Math.max(maxX, x+w);
						minY = Math.min(minY, y);
						maxY = Math.max(maxY, y+h);
						switch (position) {
						case Direction.LEFT:
						case Direction.RIGHT:
							cellX = x;
							cellY = minY;
							cellH = maxY-minY;
							break;
						case Direction.TOP:
						case Direction.BOTTOM:
							cellX = minX;
							cellY = y;
							cellW = maxX-minX;
							break;
						}
					}

					sizes[i] = new Rectangle(x, y, w, h);
					Rectangle cell = new Rectangle(cellX, cellY, cellW, cellH);
					Alignment.alignInCell(sizes[i], cell, alignment, fill);
				}
			}

			if (dimension != null) {
				dimension.width = maxX-minX;
				dimension.height = maxY-minY;
			} else {
				for (int i = 0; i < count; i++) {
					Component c = target.getComponent(i);
					if (includeComponent(c)) {
						Rectangle r = sizes[i];
						c.setBounds(insets.left+hMargin-minX+r.x, insets.top+vMargin-minY+r.y, r.width, r.height);
					}
				}
			}
		}

	}

}
