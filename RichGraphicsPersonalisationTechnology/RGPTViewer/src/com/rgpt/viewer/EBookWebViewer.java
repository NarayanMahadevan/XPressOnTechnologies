// RGPT PACKAGES
package com.rgpt.viewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.HashMap;

import javax.swing.JApplet;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.rgpt.serverutil.EBookServerProxy;
import com.rgpt.util.AppletParameters;
import com.rgpt.util.RGPTUIManager;

public class EBookWebViewer extends JApplet {
	// Hashmap of Applet Parameters
	AppletParameters m_AppletParameters;

	// EBookServer
	EBookServerProxy m_EBookServer;

	// EBook Viewer
	EBookViewer m_EBookViewer;

	public void init() {
		try {
			this.loadAppletParameters();
			// This request is used when only Applet Libraries needs to be
			// loaded.
			// In this case no Applet UI is loaded
			String reqType = m_AppletParameters.m_RequestType;
			System.out.println("In RGPTServices Development. ReqType: "
					+ reqType);
			if (reqType.equals(AppletParameters.LOAD_APPLET_REQUEST)) {
				System.out.println(m_AppletParameters.toString());
				return;
			}
			RGPTUIManager.setLookAndFeel();
			// lightcolor for panel
			Color colorpanel = new Color(51, 255, 153);
			// darkercolor for button
			Color colorbg = new Color(153, 255, 255);
			// darkestcolor for text
			Color colordark = new Color(214, 255, 255);
			String[] rgbCol = new String[0];
			String colorVal = null;
			colorVal = AppletParameters.getVal("ColorPanel");
			if (colorVal != null) {
				rgbCol = colorVal.split(":");
				if (rgbCol.length == 3)
					colorpanel = new Color((new Integer(rgbCol[0]).intValue()),
							(new Integer(rgbCol[1]).intValue()), (new Integer(
									rgbCol[2]).intValue()));
			}
			colorVal = AppletParameters.getVal("ColorBg");
			if (colorVal != null) {
				rgbCol = colorVal.split(":");
				if (rgbCol.length == 3)
					colorbg = new Color((new Integer(rgbCol[0]).intValue()),
							(new Integer(rgbCol[1]).intValue()), (new Integer(
									rgbCol[2]).intValue()));
			}
			colorVal = AppletParameters.getVal("ColorDark");
			if (colorVal != null) {
				rgbCol = colorVal.split(":");
				if (rgbCol.length == 3)
					colordark = new Color((new Integer(rgbCol[0]).intValue()),
							(new Integer(rgbCol[1]).intValue()), (new Integer(
									rgbCol[2]).intValue()));
			}

			RGPTUIManager.setUIDefaults(colorpanel, colorbg, colordark);

			// For Server Mode the PDFPageInfoManager acts as both the
			// PDFViewInterface
			// and the holder of the PDFPageInfo Holder.

			String name = "maxwidth";
			String value = getParameter(name);
			m_AppletParameters.m_FrameWidth = new Integer(value).intValue();
			name = "maxheight";
			value = getParameter(name);
			m_AppletParameters.m_FrameHeight = new Integer(value).intValue();
			System.out.println("Window Width: "
					+ m_AppletParameters.m_FrameWidth + " Window Height: "
					+ m_AppletParameters.m_FrameHeight);
			setSize(m_AppletParameters.m_FrameWidth - 25,
					m_AppletParameters.m_FrameHeight - 25);
			// setSize(800,700);

			JFrame.setDefaultLookAndFeelDecorated(true);
			JDialog.setDefaultLookAndFeelDecorated(true);

			// displayLogoImage(cp);
			setVisible(true);
		} catch (Exception ex) {
			ex.printStackTrace();
			this.showError("Unable to load Applet Parameters",
					"Load Applet Parameter Error");
		}

	}

	public Color definecolor() {
		int rcol = 220, gcol = 208, bcol = 180;
		Color col = new Color(rcol, gcol, bcol);
		return col;
	}

	// Called by init.
	protected void loadAppletParameters() throws Exception {
		// Retriving the Request Type
		String name = "request_type";
		String value = getParameter(name);
		if (value == null)
			throw new RuntimeException(name + " Applet Parameter Undefined");

		// HashMap loads Applet Parameters
		String reqType = value;
		HashMap appletParameters = new HashMap();

		// This Request Type is used to Load Applet Libraries. In this case
		// Applet
		// UI is not loaded.
		System.out.println("URL: " + getCodeBase().toString());
		if (reqType.equals(AppletParameters.LOAD_APPLET_REQUEST)) {
			m_AppletParameters = new AppletParameters(reqType, getCodeBase());
			return;
		} else if (reqType.equals(AppletParameters.VIEW_PDF_REQUEST)) {
			m_AppletParameters = new AppletParameters(reqType, getCodeBase());
		} else
			throw new RuntimeException("Unable to process Applet Request "
					+ reqType);

		// Color Settings
		name = "ColorPanel";
		value = getParameter(name);
		AppletParameters.putVal(name, value);
		name = "ColorBg";
		value = getParameter(name);
		AppletParameters.putVal(name, value);
		name = "ColorDark";
		value = getParameter(name);
		AppletParameters.putVal(name, value);
	}

	private void viewPDFAsset() {
		m_EBookServer = new EBookServerProxy(m_AppletParameters);
		int assetId = m_AppletParameters.m_DigitalAssetId;
		int pageCount = m_AppletParameters.getIntVal("page_count");
		boolean enablePDFAppr = m_AppletParameters
				.getBoolVal("enable_pdf_approval");
		boolean enablePgAnim = m_AppletParameters
				.getBoolVal("enable_page_animation");
		int animSpeed = m_AppletParameters.getIntVal("page_animation_speed");
		int animStep = m_AppletParameters.getIntVal("page_animation_step");
		m_EBookViewer = new EBookViewer(assetId, pageCount, this,
				m_EBookServer, enablePgAnim, animSpeed, animStep, enablePDFAppr);
		m_EBookViewer.setAppletParameters(m_AppletParameters);
		JPanel contentPanel = new JPanel(new BorderLayout());
		int wt = m_AppletParameters.m_FrameWidth;
		int ht = m_AppletParameters.m_FrameHeight;
		contentPanel.setPreferredSize(new Dimension(wt, ht - 50));
		m_EBookViewer.populateContentPane(contentPanel);
		setContentPane(contentPanel);
	}

	public void populateData(String data) {
		try {
			this.populateAppletData(data);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	public void populateAppletData(String data) {
		System.out.println("In RGPTServices Dev. Data to Populate: " + data);
		HashMap appletParameters = new HashMap();
		String nameValue = "", name = "", value = "";
		java.util.StringTokenizer st = new java.util.StringTokenizer(data, ":");
		while (st.hasMoreTokens()) {
			nameValue = st.nextToken();
			System.out.println("Name Value: " + nameValue);
			int index = nameValue.lastIndexOf('=');
			name = nameValue.substring(0, index);
			value = nameValue.substring(index + 1);
			appletParameters.put(name, value);
		}
		StringBuffer errMsg = new StringBuffer();
		System.out.println("Applet Parameters: " + appletParameters.toString());
		System.out.println("Window Width: " + m_AppletParameters.m_FrameWidth
				+ " Window Height: " + m_AppletParameters.m_FrameHeight);
		boolean isSuccess = m_AppletParameters.uploadRequestParams(
				appletParameters, errMsg);
		if (!isSuccess) {
			this.showError(errMsg.toString(), "Specified wrong Parameters");
			return;
		}
		// m_AppletParameters.loadViewPDFParameters(appletParameters);
		this.viewPDFAsset();
		setSize(m_AppletParameters.m_FrameWidth,
				m_AppletParameters.m_FrameHeight);
		// setSize(800,600);
		setVisible(true);
		repaint();
		System.out.println("Is Visible1: " + isVisible());
		m_EBookViewer.repaintContent();
		repaint();
		System.out.println("Is Visible2: " + isVisible());
		// show();
	}

	public void showError(String mesg, String title) {
		JOptionPane.showMessageDialog(this, mesg, title,
				JOptionPane.ERROR_MESSAGE);
	}

	public void stop() {
		System.out.println("In Stop Method");
	}

	public void destroy() {
		System.out.println("In Destroy Method");
	}
}
