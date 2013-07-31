/*
 * Rich Graphics Personalization Technology
 */
package com.rgpt.layoututil;

/*

This layout was born from frustration in trying to lay out dialogs. There's no simple 
way to create the standard two-column dialog layout of labels on the left and components 
on the right without a lot of messing around with GridBagLayout and alignment options. 
Those of you who have read "Java Look and Feel Guidelines" will have noticed that while 
hey tell you a lot about how your dialogs should look, none of the standard layouts will let 
you build them easily. This layout will.
The model for this layout is similar to laying out text with hanging indents. For each 
component you add, you specify a constraint which is NEW_PARAGRAPH or NEW_LINE. 
The first of these specifies the beginning of a paragraph: i.e. a label which will go into the 
left column. Components added after this are placed in the right column from left to right 
until you specify a NEW_LINE or another NEW_PARAGRAPH. A NEW_LINE starts a 
new line within the right column. Labels are centred with the first row of each paragraph,
 unless you specify NEW_PARAGRAPH_TOP which will top-align them.
There are three sets of parameters: hMargin and vMargin specify a space around the 
whole layout. vGapMajor specifies the spacing between paragraphs and vGapMinor the gap 
between lines. vGapMajor is the gap between the two olumns and vGapMinor the spacing
 between components in a line. The defaults are something similar to those for the 
 "Java Look and Feel Guidelines".
Here's some example code and the resulting layout:
	setLayout(new ParagraphLayout());
	JButton b1 = new JButton("One");
	JButton b2 = new JButton("Two");
	JButton b3 = new JButton("Three");
	JButton b4 = new JButton("Four");
	JButton b5 = new JButton("Five");
	JButton b6 = new JButton("Six");
	JButton b7 = new JButton("Seven");
	JButton b8 = new JButton("Eight");
	JTextField t1 = new JTextField(4);
	JTextField t2 = new JTextField(20);
	JTextArea t3 = new JTextArea(5, 30);

	b2.setFont(new Font("serif", Font.PLAIN, 24));
	add(new JLabel("Some buttons:"), ParagraphLayout.NEW_PARAGRAPH);
	add(b1);
	add(new JLabel("A long label:"), ParagraphLayout.NEW_PARAGRAPH);
	add(b2);
	add(b3);
	add(new JLabel("Short label:"), ParagraphLayout.NEW_PARAGRAPH);
	add(b4);
	add(b5, ParagraphLayout.NEW_LINE);
	add(b6);
	add(b7);
	add(b8, ParagraphLayout.NEW_LINE);
	add(new JLabel("Text:"), ParagraphLayout.NEW_PARAGRAPH);
	add(t1);
	add(new JLabel("More text:"), ParagraphLayout.NEW_PARAGRAPH);
	add(t2);
	add(new JLabel("miles"));
	add(new JLabel("A text area:"), ParagraphLayout.NEW_PARAGRAPH_TOP);
	add(t3);

*/

import java.awt.*;

public class ParagraphLayout extends ConstraintLayout {

	public final static int TYPE_MASK = 0x03;
	public final static int STRETCH_H_MASK = 0x04;
	public final static int STRETCH_V_MASK = 0x08;

	public final static int NEW_PARAGRAPH_VALUE = 1;
	public final static int NEW_PARAGRAPH_TOP_VALUE = 2;
	public final static int NEW_LINE_VALUE = 3;

	public final static Integer NEW_PARAGRAPH = new Integer(0x01);
	public final static Integer NEW_PARAGRAPH_TOP = new Integer(0x02);
	public final static Integer NEW_LINE = new Integer(0x03);
	public final static Integer STRETCH_H = new Integer(0x04);
	public final static Integer STRETCH_V = new Integer(0x08);
	public final static Integer STRETCH_HV = new Integer(0x0c);
	public final static Integer NEW_LINE_STRETCH_H = new Integer(0x07);
	public final static Integer NEW_LINE_STRETCH_V = new Integer(0x0b);
	public final static Integer NEW_LINE_STRETCH_HV = new Integer(0x0f);

	protected int hGapMajor, vGapMajor;
	protected int hGapMinor, vGapMinor;
	protected int rows;
	protected int colWidth1;
	protected int colWidth2;

	public ParagraphLayout() {
		this(10, 10, 12, 11, 4, 4);
	}
	
	public ParagraphLayout(int hMargin, int vMargin, int hGapMajor, int vGapMajor, int hGapMinor, int vGapMinor) {
		this.hMargin = hMargin;
		this.vMargin = vMargin;
		this.hGapMajor = hGapMajor;
		this.vGapMajor = vGapMajor;
		this.hGapMinor = hGapMinor;
		this.vGapMinor = vGapMinor;
	}
	
	public void measureLayout(Container target, Dimension dimension, int type)  {
		int count = target.getComponentCount();
		if (count > 0) {
			Insets insets = target.getInsets();
			Dimension size = target.getSize();
			int x = 0;
			int y = 0;
			int rowHeight = 0;
			int colWidth = 0;
			int numRows = 0;
			boolean lastWasParagraph = false;

			Dimension[] sizes = new Dimension[count];

			colWidth1 = colWidth2 = 0;

			// First pass: work out the column widths and row heights
			for (int i = 0; i < count; i++) {
				Component c = target.getComponent(i);
				if (includeComponent(c)) {
					Dimension d = getComponentSize(c, type);
					int w = d.width;
					int h = d.height;
					sizes[i] = d;
					Integer n = (Integer)getConstraint(c);

					if (i == 0 || n == NEW_PARAGRAPH || n == NEW_PARAGRAPH_TOP) {
						if (i != 0)
							y += rowHeight+vGapMajor;
						colWidth1 = Math.max(colWidth1, w);
						colWidth = 0;
						rowHeight = 0;
						lastWasParagraph = true;
					} else if (n == NEW_LINE || lastWasParagraph) {
						x = 0;
						if (!lastWasParagraph && i != 0)
							y += rowHeight+vGapMinor;
						colWidth = w;
						colWidth2 = Math.max(colWidth2, colWidth);
						if (!lastWasParagraph)
							rowHeight = 0;
						lastWasParagraph = false;
					} else {
						colWidth += w+hGapMinor;
						colWidth2 = Math.max(colWidth2, colWidth);
						lastWasParagraph = false;
					}
					rowHeight = Math.max(h, rowHeight);
				}
			}

			// Second pass: actually lay out the components
			if (dimension != null) {
				dimension.width = colWidth1 + hGapMajor + colWidth2;
				dimension.height = y + rowHeight;
			} else {
				int spareHeight = size.height-(y+rowHeight)-insets.top-insets.bottom-2*vMargin;
				x = 0;
				y = 0;
				lastWasParagraph = false;
				int start = 0;
				int rowWidth = 0;
				Integer paragraphType = NEW_PARAGRAPH;
				boolean stretchV = false;
				
				boolean firstLine = true;
				for (int i = 0; i < count; i++) {
					Component c = target.getComponent(i);
					if (includeComponent(c)) {
						Dimension d = sizes[i];
						int w = d.width;
						int h = d.height;
						Integer n = (Integer)getConstraint(c);
						int nv = n != null ? n.intValue() : 0;

						if (i == 0 || n == NEW_PARAGRAPH || n == NEW_PARAGRAPH_TOP) {
							if (i != 0)
								layoutRow(target, sizes, start, i-1, y, rowWidth, rowHeight, firstLine, type, paragraphType);
							stretchV = false;
							paragraphType = n;
							start = i;
							firstLine = true;
							if (i != 0)
								y += rowHeight+vGapMajor;
							rowHeight = 0;
							rowWidth = colWidth1+hGapMajor-hGapMinor;
							lastWasParagraph = true;
						} else if (n == NEW_LINE || lastWasParagraph) {
							if (!lastWasParagraph) {
								layoutRow(target, sizes, start, count-1, y, rowWidth, rowHeight, firstLine, type, paragraphType);
								stretchV = false;
								start = i;
								firstLine = false;
								y += rowHeight+vGapMinor;
								rowHeight = 0;
							}
							rowWidth += sizes[i].width+hGapMinor;
							lastWasParagraph = false;
						} else {
							rowWidth += sizes[i].width+hGapMinor;
							lastWasParagraph = false;
						}
						if ((nv & STRETCH_V_MASK) != 0 && !stretchV) {
							stretchV = true;
							h += spareHeight;
						}
						rowHeight = Math.max(h, rowHeight);
					}
				}
				layoutRow(target, sizes, start, count-1, y, rowWidth, rowHeight, firstLine, type, paragraphType);
			}
		}

	}

	protected void layoutRow(Container target, Dimension[] sizes, int start, int end, int y, int rowWidth, int rowHeight, boolean paragraph, int type, Integer paragraphType) {
		int x = 0;
		Insets insets = target.getInsets();
		Dimension size = target.getSize();
		int spareWidth = size.width-rowWidth-insets.left-insets.right-2*hMargin;

		for (int i = start; i <= end; i++) {
			Component c = target.getComponent(i);
			if (includeComponent(c)) {
				Integer n = (Integer)getConstraint(c);
				int nv = n != null ? n.intValue() : 0;
				Dimension d = sizes[i];
				int w = d.width;
				int h = d.height;

				if ((nv & STRETCH_H_MASK) != 0) {
					w += spareWidth;
					Dimension max = getComponentSize(c, MAXIMUM);
					Dimension min = getComponentSize(c, MINIMUM);
					w = Math.max(min.width, Math.min(max.width, w));
				}
				if ((nv & STRETCH_V_MASK) != 0) {
					h = rowHeight;
					Dimension max = getComponentSize(c, MAXIMUM);
					Dimension min = getComponentSize(c, MINIMUM);
					h = Math.max(min.height, Math.min(max.height, h));
				}

				if (i == start) {
					if (paragraph)
						x = colWidth1-w;
					else
						x = colWidth1 + hGapMajor;
				} else if (paragraph && i == start+1) {
					x = colWidth1 + hGapMajor;
				}
				int yOffset = paragraphType == NEW_PARAGRAPH_TOP ? 0 : (rowHeight-h)/2;
				if (target.getComponentOrientation().isLeftToRight())
					c.setBounds(insets.left+hMargin+x, insets.top+vMargin+y+yOffset, w, h);
				else
					c.setBounds(size.width-insets.right-insets.left-hMargin-x-w, insets.top+vMargin+y+yOffset, w, h);
				x += w + hGapMinor;
			}
		}
	}

}
