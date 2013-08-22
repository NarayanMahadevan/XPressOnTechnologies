// RGPT PACKAGES
package com.rgpt.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class RGPTParams {
	public static String LOAD_APPLET_REQUEST = "Load Applet Request";
	public static String EDIT_PDF_TEMPLATE_REQUEST = "Edit PDF Template";
	public static String VIEW_PDF_REQUEST = "View PDF Request";
	public static String EDIT_SAVED_PDF_REQUEST = "Edit Save PDF Request";
	public static String APPROVE_PDF_REQUEST = "Approve PDF Request";
	public static String VIEW_IMAGE_ASSETS = "View Customer Assets";
	public static String UPLOAD_BATCH_FILE = "Upload Batch File";
	public static String UPLOAD_PSP_ASSET = "Upload PSP Asset";
	public static String UPLOAD_UPDATED_ASSET = "Upload Updated Asset";
	public static String UPLOAD_DIGITAL_ASSETS = "Upload Digital Assets";

	// Asset Types Supported
	public static String PDF_TEMPLATE_ASSET = "PDF Template Asset";
	public static String FAST_SHOPPING_ASSET = "Fast Shopping Asset";

	public static String m_RequestType;
	public static double m_TransferRate;
	public int m_FrameWidth;
	public int m_FrameHeight;
	public static int m_ServiceIdentifier;
	public static int m_DigitalAssetId;

	// This indicates the Server that will execute the request in the backend.
	public static String m_ServerName;

	// URL from where the Applet got loaded
	public static URL m_AppletURL;

	// This parameters are added by PDFPageInfoManager as place holders to use
	// in future Server Calls
	public static String m_SrcDir;
	public static String m_SerializedFileName;

	// This variables are used to set the PDF Approval with in EBook Viewer
	public static boolean m_ImageBatchUpload;

	// pointer to resource folder
	public static String IMAGE_PATH = "res/pdfviewer/";

	public static Map<String, String> m_RGPTParamValues = new HashMap<String, String>();

	// Default Constructor used by Standalone Application
	public RGPTParams() {
	}

	public static void putVal(String name, String value) {
		m_RGPTParamValues.put(name, value);
	}

	public static String getVal(String name) {
		return getVal(name, "");
	}

	public static String getVal(String name, String defVal) {
		String val = m_RGPTParamValues.get(name);
		if (val == null)
			val = defVal;
		return val;
	}

	public static int getIntVal(String name) {
		String val = m_RGPTParamValues.get(name);
		if (val == null)
			return -1;
		return Integer.parseInt(val);
	}

	public static float getFloatVal(String name) {
		String val = m_RGPTParamValues.get(name);
		if (val == null)
			return 0.0f;
		return Float.parseFloat(val);
	}

	public static boolean getBoolVal(String name) {
		return getBoolVal(name, false);
	}

	public static boolean getBoolVal(String name, boolean defVal) {
		String val = m_RGPTParamValues.get(name);
		if (val == null)
			return defVal;
		return Boolean.parseBoolean(val);
	}

	public static RGPTFileFilter getFileFilter() {
		String name = "file_type";
		String val = m_RGPTParamValues.get(name);
		if (val == null)
			return null;
		return FileFilterFactory.getFileFilter4FileType(val);
	}

	public String toString() {
		StringBuffer mesg = new StringBuffer("Applet Parameters for: "
				+ m_RequestType);
		mesg.append(" is: Service Provider Id: " + m_ServiceIdentifier);
		mesg.append(" Server Name: " + m_ServerName);
		mesg.append(" " + m_RGPTParamValues.toString());
		if (m_AppletURL != null)
			mesg.append("\n URL: " + m_AppletURL.toString());
		return mesg.toString();
	}

	// Server Settings
	public static Properties m_RGPTProperties;

	public static void setServerProperties(Properties servProp) {
		m_RGPTProperties = servProp;
	}

	public static Properties createServerProperties() {
		return createServerProperties(null, false);
	}

	public static Properties createServerProperties(Class<?> caller,
			boolean isServerMode) {
		if (m_RGPTProperties != null)
			return m_RGPTProperties;
		String propFile = null;
		InputStream ipStream = null;
		BufferedInputStream propStream = null;
		try {
			// Read properties file. This will be moved out into a seperate
			// Utility class in future
			if (isServerMode)
				propFile = "res/server_prod.properties";
			else
				propFile = "res/server_dev.properties";
			try {
				ipStream = new FileInputStream(new File(propFile));
			} catch (Exception ex) {
				// ex.printStackTrace();
				propFile = "/" + propFile;
				try {
					ipStream = caller.getResourceAsStream(propFile);
				} catch (Exception exp) {
					exp.printStackTrace();
				}
			}
			if (ipStream == null) {
				System.err.println("Couldn't find file: " + propFile);
				return null;
			}
			m_RGPTProperties = new Properties();
			propStream = new BufferedInputStream(ipStream);
			RGPTLogger.logToConsole("Property Stream Not Null: " + propStream);
			RGPTLogger.logToConsole("Property Stream: "
					+ propStream.available());
			m_RGPTProperties.load(propStream);
			// RGPTLogger.logToConsole("Server Property: " +
			// m_ServerProperties.toString());
			createPropertySet();
			return m_RGPTProperties;
		} catch (Throwable ex) {
			try {
				java.io.FileInputStream fileStream = new java.io.FileInputStream(
						propFile);
				RGPTLogger.logToConsole("Available Size: "
						+ fileStream.available());
				m_RGPTProperties.load(fileStream);
				return m_RGPTProperties;
			} catch (Exception exp) {
				exp.printStackTrace();
				throw new RuntimeException("Unable to Load Server Properties");
			}
		} finally {
			if (ipStream != null) {
				try {
					ipStream.close();
				} catch (Exception ex) {
				}
			}
			if (propStream != null) {
				try {
					propStream.close();
				} catch (Exception ex) {
				}
			}
		}
	}

	public static void createPropertySet() {
		Set<String> propNameSet = m_RGPTProperties.stringPropertyNames();
		RGPTLogger.logToConsole("Prop Name Set Value: " + m_RGPTProperties);
		String[] propNames = propNameSet.toArray(new String[0]);
		for (int i = 0; i < propNames.length; i++) {
			m_RGPTParamValues.put(propNames[i],
					m_RGPTProperties.getProperty(propNames[i]));
		}
		RGPTLogger.logToConsole("Server Properties: " + m_RGPTParamValues);
	}

}
