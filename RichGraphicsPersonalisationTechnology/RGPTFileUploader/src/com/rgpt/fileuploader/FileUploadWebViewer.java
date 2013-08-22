/*******************************************************************************
 *
 *    Rich Graphics Personalization Technology
 *
 ******************************************************************************/

package com.rgpt.fileuploader;

import java.awt.Color;
import java.io.File;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.JApplet;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.rgpt.util.AppletParameters;
import com.rgpt.util.RGPTFileFilter;
import com.rgpt.util.RGPTUIManager;

/**
 * This applet allows users to select files from their file system using drag
 * and drop and upload them to a remote server. All files will be zipped and
 * sent to the server as a single file to improve upload performance. This
 * applet will use the HTTP protocol to communicate with the server.
 * 
 */

public class FileUploadWebViewer extends JApplet {
	// Hashmap of Applet Parameters
	AppletParameters m_AppletParameters;

	// File Upload UI
	FileUploadViewer m_FileUploadViewer;

	public void init() {
		try {
			this.loadAppletParameters();
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
		} catch (Exception ex) {
			ex.printStackTrace();
			this.showError("Unable to load Applet Parameters",
					"Load Applet Parameter Error");
		}

		Color col = this.definecolor();
		System.out.println("Color " + col);
		// setBackground(col);

		// For Server Mode the PDFPageInfoManager acts as both the
		// PDFViewInterface
		// and the holder of the PDFPageInfo Holder.

		String name = "maxwidth";
		String value = getParameter(name);
		m_AppletParameters.m_FrameWidth = new Integer(value).intValue();
		name = "maxheight";
		value = getParameter(name);
		m_AppletParameters.m_FrameHeight = new Integer(value).intValue();
		System.out.println("Setting Window Width: "
				+ m_AppletParameters.m_FrameWidth + " And Height: "
				+ m_AppletParameters.m_FrameHeight);
		JFrame.setDefaultLookAndFeelDecorated(true);
		JDialog.setDefaultLookAndFeelDecorated(true);
		setVisible(true);
	}

	public Color definecolor() {
		int rcol = 220, gcol = 208, bcol = 180;
		Color col = new Color(rcol, gcol, bcol);
		return col;
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

	// Called by init.
	protected void loadAppletParameters() throws Exception {
		// Retriving the Request Type
		String name = "request_type";
		String value = getParameter(name);
		if (value == null)
			throw new RuntimeException(name + " Applet Parameter Undefined");

		// HashMap loads Applet Parameters
		String reqType = value;

		if (!reqType.equals(AppletParameters.UPLOAD_DIGITAL_ASSETS)
				&& !reqType.equals(AppletParameters.UPLOAD_UPDATED_ASSET)
				&& !reqType.equals(AppletParameters.UPLOAD_PSP_ASSET)
				&& !reqType.equals(AppletParameters.UPLOAD_BATCH_FILE)) {
			this.showError("Unknown Request", "Error");
			return;
		}

		// This Request Type is used to Load Applet Libraries. In this case
		// Applet
		// UI is not loaded.
		System.out.println("URL: " + getCodeBase().toString());
		m_AppletParameters = new AppletParameters(reqType, getCodeBase());
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

	public void populateData(String data) {
		try {
			this.populateAppletData(data);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	public void populateAppletData(String data) {
		if (m_AppletParameters == null)
			throw new RuntimeException("Applet Parameter Undefined");
		boolean isSuccess = true;
		RGPTFileFilter fileFilter = null;
		System.out.println("Data to Populate: " + data);
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
		String reqType = m_AppletParameters.m_RequestType;
		m_AppletParameters.uploadRequestParams(appletParameters, errMsg);
		/*
		 * if (reqType.equals(AppletParameters.UPLOAD_DIGITAL_ASSETS)) {
		 * isSuccess = m_AppletParameters.
		 * uploadDigitalAssetParameters(appletParameters, errMsg); } else if
		 * (reqType.equals(AppletParameters.UPLOAD_PSP_ASSET)) { isSuccess =
		 * m_AppletParameters. uploadPSPAssetParameter(appletParameters,
		 * errMsg); } else if
		 * (reqType.equals(AppletParameters.UPLOAD_BATCH_FILE)) { isSuccess =
		 * m_AppletParameters. uploadBatchFileParameters(appletParameters,
		 * errMsg); } else if
		 * (reqType.equals(AppletParameters.UPLOAD_UPDATED_ASSET)) { isSuccess =
		 * m_AppletParameters. uploadUpdatedAssetParameters(appletParameters,
		 * errMsg); }
		 */
		if (!isSuccess) {
			this.showError(errMsg.toString(), "Specified wrong Parameters");
			return;
		}

		int wth = m_AppletParameters.m_FrameWidth;
		int ht = m_AppletParameters.m_FrameHeight;
		// setSize(wth, ht);
		// System.out.println("Window Size Wth: " +
		// m_AppletParameters.m_FrameWidth +
		// " Window Size Ht: " + m_AppletParameters.m_FrameHeight);

		// String selectedFiles = this.selectFiles();
		// System.out.println("Shown Selected Files: " + selectedFiles);

		System.out.println("Trying with new Content Pane");
		m_FileUploadViewer = new FileUploadViewer(this, m_AppletParameters,
				new HTTPFileUpload());
		JPanel cp = m_FileUploadViewer.createContentPane(wth, ht);
		// JPanel cp = this.addUploadFileButton();
		// add(cp);
		setContentPane(cp);
		System.out.println("Main Content Pane size: "
				+ cp.getPreferredSize().toString());

		// setContentPane(m_FileUploadViewer);
		setSize(wth, ht);
		System.out.println("Call1 " + getPreferredSize().toString());
		m_FileUploadViewer.repaintUI();
		System.out.println("Call2 " + isVisible());
		setVisible(true);
		System.out
				.println("Finished processing request. FileUpload UI isvisible "
						+ isVisible());
	}

	boolean m_IsFileUploadMesgDisplayed = false;

	public boolean minimizeWindow() {
		if (m_FileUploadViewer == null)
			return false;
		// System.out.println("Can Window be Minimized: " +
		// m_FileUploadViewer.m_MinimizeWindow);
		if (!m_FileUploadViewer.m_MinimizeWindow)
			return false;

		if (!m_IsFileUploadMesgDisplayed) {
			String mesg = "The Selected Files will be uploaded in the Background";
			JOptionPane.showMessageDialog(this, mesg, "File Upload Message",
					JOptionPane.INFORMATION_MESSAGE);
			m_IsFileUploadMesgDisplayed = true;
		}
		return m_FileUploadViewer.m_MinimizeWindow;
	}

	public String selectFiles() {
		Vector choosenFiles = null;
		choosenFiles = new Vector(
				(Vector) java.security.AccessController
						.doPrivileged(new java.security.PrivilegedAction() {
							public Object run() {
								Vector choosenFiles = new Vector();
								JFileChooser fc = new JFileChooser();
								fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
								fc.setMultiSelectionEnabled(true);
								int returnVal = fc.showOpenDialog(null);
								if (returnVal == JFileChooser.APPROVE_OPTION) {
									File[] selFiles = fc.getSelectedFiles();
									for (int i = 0; i < selFiles.length; i++) {
										choosenFiles.addElement(selFiles[i]);
									}
								}
								// choosenFiles = "Files Selected";
								return choosenFiles; // return whatever you want
							}
						}));

		StringBuffer fileNames = new StringBuffer();
		for (int i = 0; i < choosenFiles.size(); i++) {
			File file = (File) choosenFiles.elementAt(i);
			fileNames.append(file.toString() + "\n");
		}
		return fileNames.toString();
	}

}
