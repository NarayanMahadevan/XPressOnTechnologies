package com.rgpt.pdfnetlib;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import pdftron.Common.PDFNetException;
import pdftron.PDF.PDFViewCtrl;
import pdftron.PDF.Page;
import pdftron.PDF.Rect;

import com.rgpt.pdflib.PDFThumbViewHandler;
import com.rgpt.pdflib.PDFViewHandler;

public class PDFThumbview extends PDFViewCtrl implements PDFThumbViewHandler {

	private static final long serialVersionUID = 1L;

	PDFViewHandler m_PDFView;

	public PDFThumbview(int thumbNailWt) {
		this.setMinimumSize(new Dimension(thumbNailWt, 200));
		this.setToolMode(PDFViewCtrl.e_custom);
		setImageSmoothing(false);
		this.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {
			}

			public void mouseEntered(MouseEvent e) {
			}

			public void mouseExited(MouseEvent e) {
			}

			public void mousePressed(MouseEvent e) {
				handleMousePressed(e.getX(), e.getY());
			}

			public void mouseReleased(MouseEvent e) {
			}
		});
	}

	@Override
	public void setPDFView(PDFViewHandler pdfView) {
		this.m_PDFView = pdfView;
		super.setDoc((pdftron.PDF.PDFDoc) m_PDFView.getDoc().getPDFDoc());
	}

	@Override
	public JComponent getViewPanel() {
		return super.getViewPane();
	}

	@Override
	public JPanel setContentPane(JTabbedPane tabPane) {
		tabPane.addTab("Pages", super.getViewPane());
		return super.getViewPane();
	}

	@Override
	public void close() {
		try {
			this.getDoc().close();
		} catch (Exception ex) {
		}
		this.destroy();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (m_PDFView != null) {
			int cur_page = m_PDFView.getCurrentPage();
			if (cur_page < 1)
				return;
			try {
				Page page = getDoc().getPage(cur_page);

				// Draw a red border around the selected page.
				Rect bbox = page.getCropBox();
				bbox.normalize();
				double left = bbox.getX1(), bottom = bbox.getY1(), right = bbox
						.getX2(), top = bbox.getY2(), tmp;

				Point2D.Double bottom_left = convPagePtToScreenPt(left, bottom,
						cur_page);
				left = bottom_left.x;
				bottom = bottom_left.y;
				Point2D.Double top_right = convPagePtToScreenPt(right, top,
						cur_page);
				right = top_right.x;
				top = top_right.y;

				if (top > bottom) {
					tmp = top;
					top = bottom;
					bottom = tmp;
				}
				if (right < left) {
					tmp = right;
					right = left;
					left = tmp;
				}
				g.setPaintMode();
				g.setColor(Color.red);
				g.drawRect((int) (left - 2) - getX(), (int) (top - 1) - getY(),
						(int) (right - left + 4), (int) (bottom - top + 3));
			} catch (PDFNetException e) {
				e.printStackTrace();
			}
		}
	}

	protected void handleMousePressed(int x, int y) {
		if (m_PDFView != null) {
			int cur_page = getPageNumberFromScreenPt(x + (getX()), y + (getY()));
			if (cur_page < 1)
				return;
			else
				m_PDFView.setCurrentPage(cur_page);
			repaint();
		}

	}

}
