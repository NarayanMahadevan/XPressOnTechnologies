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

public class AppletParameters {
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

	public static Map<String, String[]> m_RequestParamSet = new HashMap<String, String[]>();
	public static Map<String, Boolean> m_RequestReqParamSet = new HashMap<String, Boolean>();
	public static Map<String, String> m_RequestParamValues = new HashMap<String, String>();

	public static final String[] m_EditPDFParameters = { "psp_id",
			"customer_id", "customer_basket_id", "template_id", "page_count",
			"server_name", "data_source_name", "pdf_viewer_name" };

	public static final String[] m_ViewPDFParameters = { "psp_id",
			"digital_asset_id", "page_count", "enable_page_animation",
			"page_animation_speed", "page_animation_step",
			"enable_pdf_approval", "batch_id", "template_id",
			"template_page_count", "server_name" };

	public static final String[] m_ViewImageAsset = { "psp_id", "customer_id",
			"server_name" };

	public static final String[] m_UploadUpdatedDigitalAsset = { "psp_id",
			"customer_account_id", "customer_id", "orig_asset_id",
			"upload_file_path", "order_id", "order_job_id",
			"wait_for_file_upload", "server_name", "file_type" };

	public static final String[] m_UploadDigitalAsset = { "psp_id",
			"customer_account_id", "customer_id", "file_type",
			"allow_multi_file_upload", "wait_for_file_upload", "server_name",
			"upload_file_path" };

	public static final String[] m_UploadBatchAsset = { "psp_id",
			"customer_account_id", "customer_id", "template_id", "file_type",
			"wait_for_file_upload", "server_name" };

	public static final String[] m_UploadPSPAsset = { "psp_id", "session_id",
			"file_type", "asset_type", "wait_for_file_upload", "server_name" };

	public static final String[] m_EditSavedPDFParameters = { "session_id",
			"template_id", "customer_id", "digital_asset_id", "server_name" };

	public static final String[] m_ApprovePDFParameters = { "psp_id",
			"customer_id", "customer_basket_id", "digital_asset_id",
			"page_count", "server_name" };

	static {
		m_RequestParamSet.put(EDIT_PDF_TEMPLATE_REQUEST, m_EditPDFParameters);
		m_RequestParamSet.put(VIEW_PDF_REQUEST, m_ViewPDFParameters);
		m_RequestParamSet.put(VIEW_IMAGE_ASSETS, m_ViewImageAsset);
		m_RequestParamSet.put(UPLOAD_DIGITAL_ASSETS, m_UploadDigitalAsset);
		m_RequestParamSet.put(UPLOAD_PSP_ASSET, m_UploadPSPAsset);
		m_RequestParamSet
				.put(UPLOAD_UPDATED_ASSET, m_UploadUpdatedDigitalAsset);
		m_RequestParamSet.put(UPLOAD_BATCH_FILE, m_UploadBatchAsset);
		m_RequestParamSet.put(EDIT_SAVED_PDF_REQUEST, m_EditSavedPDFParameters);
		m_RequestParamSet.put(APPROVE_PDF_REQUEST, m_ApprovePDFParameters);
		m_RequestReqParamSet.put(EDIT_PDF_TEMPLATE_REQUEST, true);
		m_RequestReqParamSet.put(VIEW_PDF_REQUEST, false);
		m_RequestReqParamSet.put(VIEW_IMAGE_ASSETS, true);
		m_RequestReqParamSet.put(UPLOAD_DIGITAL_ASSETS, true);
		m_RequestReqParamSet.put(UPLOAD_PSP_ASSET, true);
		m_RequestReqParamSet.put(UPLOAD_UPDATED_ASSET, true);
		m_RequestReqParamSet.put(UPLOAD_BATCH_FILE, true);
		m_RequestReqParamSet.put(EDIT_SAVED_PDF_REQUEST, true);
		m_RequestReqParamSet.put(APPROVE_PDF_REQUEST, true);
	}

	// Default Constructor used by Standalone Application
	public AppletParameters() {
	}

	// This constructor is invoked when the call is made from the Applet and
	// needs
	// Server Interaction
	public AppletParameters(String reqType, java.net.URL appletURL) {
		m_AppletURL = appletURL;
		m_RequestType = reqType;
		m_DigitalAssetId = -1;
		m_ServiceIdentifier = -1;
		m_ImageBatchUpload = false;
	}

	public static void populateAppletParams() {
		m_ServiceIdentifier = getIntVal("ServiceIdentifier");
		m_ServerName = getVal("ServerName");
		String url = AppletParameters.getVal("CodeBase");
		try {
			m_AppletURL = new URL(url);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static boolean isRequestValid(String reqName) {
		String[] reqSet = m_RequestParamSet.get(reqName);
		if (reqSet == null)
			return false;
		return true;
	}

	public boolean uploadRequestParams(Map<String, String> appletParameters,
			StringBuffer errMsg) {
		String value = "";
		boolean isSuccess = true;
		String[] reqParams = m_RequestParamSet.get(m_RequestType);
		boolean isAllParamReq = ((Boolean) m_RequestReqParamSet
				.get(m_RequestType)).booleanValue();
		for (int i = 0; i < reqParams.length; i++) {
			value = (String) appletParameters.get(reqParams[i]);
			if (value == null) {
				if (!isAllParamReq)
					continue;
				isSuccess = false;
				errMsg.append("Invalid " + reqParams[i] + "\n");
				continue;
			}

			if (reqParams[i] == "psp_id")
				m_ServiceIdentifier = Integer.parseInt(value);
			else if (reqParams[i] == "server_name")
				m_ServerName = value;
			else if (reqParams[i] == "digital_asset_id")
				m_DigitalAssetId = Integer.parseInt(value);
			else if (reqParams[i] == "asset_type") {
				if (!isAssetTypeValid(value)) {
					isSuccess = false;
					errMsg.append("Invalid Asset Type Defines" + "\n");
					continue;
				}
			}
			if (reqParams[i] == "file_type") {
				if (!checkFileType(value)) {
					isSuccess = false;
					errMsg.append("Invalid File Type Defined " + "\n");
					continue;
				}
			}

			m_RequestParamValues.put(reqParams[i], value);
		}
		RGPTLogger.logToConsole("Request Params for Request: " + m_RequestType
				+ " are: " + m_RequestParamValues.toString());
		return isSuccess;
	}

	public boolean checkFileType(String value) {
		RGPTFileFilter fileFilter = FileFilterFactory
				.getFileFilter4FileType(value);
		if (fileFilter == null)
			return false;
		if (m_RequestType.equals(UPLOAD_BATCH_FILE)
				&& fileFilter.m_FileType.equals("ZIP"))
			m_ImageBatchUpload = true;
		return true;
	}

	public boolean isAssetTypeValid(String assetType) {
		if (!assetType.equals(PDF_TEMPLATE_ASSET)) {
			if (!assetType.equals(FAST_SHOPPING_ASSET))
				return false;
		}
		return true;
	}

	public static void putVal(String name, String value) {
		m_RequestParamValues.put(name, value);
	}

	public static String getVal(String name) {
		return getVal(name, "");
	}

	public static String getVal(String name, String defVal) {
		String val = m_RequestParamValues.get(name);
		if (val == null)
			val = defVal;
		return val;
	}

	public static int getIntVal(String name) {
		String val = m_RequestParamValues.get(name);
		if (val == null)
			return -1;
		return Integer.parseInt(val);
	}

	public static float getFloatVal(String name) {
		String val = m_RequestParamValues.get(name);
		if (val == null)
			return 0.0f;
		return Float.parseFloat(val);
	}

	public static boolean getBoolVal(String name) {
		return getBoolVal(name, false);
	}

	public static boolean getBoolVal(String name, boolean defVal) {
		String val = m_RequestParamValues.get(name);
		if (val == null)
			return defVal;
		return Boolean.parseBoolean(val);
	}

	public static RGPTFileFilter getFileFilter() {
		String name = "file_type";
		String val = m_RequestParamValues.get(name);
		if (val == null)
			return null;
		return FileFilterFactory.getFileFilter4FileType(val);
	}

	public String toString() {
		StringBuffer mesg = new StringBuffer("Applet Parameters for: "
				+ m_RequestType);
		mesg.append(" is: Service Provider Id: " + m_ServiceIdentifier);
		mesg.append(" Server Name: " + m_ServerName);
		mesg.append(" " + m_RequestParamValues.toString());
		if (m_AppletURL != null)
			mesg.append("\n URL: " + m_AppletURL.toString());
		return mesg.toString();
	}

	// Server Settings
	public static Properties m_ServerProperties;

	public static void setServerProperties(Properties servProp) {
		m_ServerProperties = servProp;
	}

	public static Properties createServerProperties() {
		return createServerProperties(null, false);
	}

	public static Properties createServerProperties(Class<?> caller,
			boolean isServerMode) {
		if (m_ServerProperties != null)
			return m_ServerProperties;
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
			m_ServerProperties = new Properties();
			propStream = new BufferedInputStream(ipStream);
			RGPTLogger.logToConsole("Property Stream Not Null: " + propStream);
			RGPTLogger.logToConsole("Property Stream: "
					+ propStream.available());
			m_ServerProperties.load(propStream);
			// RGPTLogger.logToConsole("Server Property: " +
			// m_ServerProperties.toString());
			createPropertySet();
			return m_ServerProperties;
		} catch (Throwable ex) {
			try {
				java.io.FileInputStream fileStream = new java.io.FileInputStream(
						propFile);
				RGPTLogger.logToConsole("Available Size: "
						+ fileStream.available());
				m_ServerProperties.load(fileStream);
				return m_ServerProperties;
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
		Set<String> propNameSet = m_ServerProperties.stringPropertyNames();
		String[] propNames = propNameSet.toArray(new String[0]);
		for (int i = 0; i < propNames.length; i++) {
			m_RequestParamValues.put(propNames[i],
					m_ServerProperties.getProperty(propNames[i]));
		}
	}

}
