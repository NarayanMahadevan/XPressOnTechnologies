// RGPT PACKAGES
package com.rgpt.serverutil;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

import com.rgpt.serverhandler.FileDownloadInterface;
import com.rgpt.util.RGPTUtil;

public class HTTPFileDownload implements FileDownloadInterface {
	private boolean allowFileDownload(String fileURLPath, String fileName,
			String outDirPath) {
		HttpURLConnection urlConn = null;
		boolean allowFileDownload = false;
		String urlStr = "", servName = "";
		ObjectInputStream dataStr = null;
		urlStr = fileURLPath;
		servName = "FileUploadInfoServer";
		Vector<String> reqParam = new Vector<String>();
		reqParam.addElement("RequestName=" + "GET_FILE_DATA");
		reqParam.addElement("FileName=" + fileName);
		try {
			URL serverURL = ServerProxy.getURL(urlStr, servName, reqParam);
			dataStr = new ObjectInputStream(
					ServerProxy.makeServerRequest(serverURL));
			HashMap fileInfo = (HashMap) dataStr.readObject();
			System.out.println("Server File Info: " + fileInfo.toString());
			String serFileInfo = outDirPath + "FileInfo.ser";
			if (!(new File(serFileInfo)).exists())
				allowFileDownload = true;
			if (!allowFileDownload) {
				HashMap origFileInfo = (HashMap) RGPTUtil
						.getSerializeObject(serFileInfo);
				System.out.println("Original File Info: "
						+ origFileInfo.toString() + "\nServer File Info: "
						+ fileInfo.toString());
				if (((Long) fileInfo.get("FileLength"))
						.equals(((Long) origFileInfo.get("FileLength")))
						&& ((Long) fileInfo.get("FileLastModified"))
								.equals(((Long) origFileInfo
										.get("FileLastModified")))) {
					return allowFileDownload;
				}
			}
			// In this case the Server has new Modified File and hence File
			// Download
			// should be allowed
			allowFileDownload = true;
			RGPTUtil.serializeObject(serFileInfo, fileInfo);
			return allowFileDownload;
		} catch (Exception ex) {
			ex.printStackTrace();
			return allowFileDownload;
		} finally {
			try {
				if (dataStr != null)
					dataStr.close();
			} catch (Exception ex) {
			}
		}
	}

	public void downloadZipFile(String fileURLPath, String inputFileName,
			String outDirPath) throws Exception {
		byte buf[] = null;
		System.out.println("Download File: " + inputFileName + " from : "
				+ fileURLPath + " into: " + outDirPath);
		URL url = null;
		ZipEntry zipEntry = null;
		FileOutputStream fileOutStr = null;
		ZipInputStream zipIpStream = null;
		HttpURLConnection urlConn = null;
		File outputFile = null;
		String fullFileName = "";
		try {
			url = new URL(fileURLPath + inputFileName);
			urlConn = (HttpURLConnection) url.openConnection();
			urlConn.connect();
			System.out.println("Opened URL: " + url.toString());
			zipIpStream = new ZipInputStream(url.openStream());
			if (zipIpStream == null) {
				System.err.println("Couldn't find file: " + inputFileName
						+ " in : " + fileURLPath);
				return;
			}
			System.out.println("URL File Stream is Not Null: " + zipIpStream);
			System.out.println("URI is: " + url.toURI().toString());
			if (!allowFileDownload(fileURLPath, inputFileName, outDirPath)) {
				System.out.println("FILE EXISTS. NO FILE DOWNLOAD NEEDED");
				return;
			}
			while (true) {
				zipEntry = zipIpStream.getNextEntry();

				if (zipEntry == null) {
					System.out.println("No More ZIP Entry.");
					break;
				}
				System.out.println("ZIP ENTRY: " + zipEntry.toString());
				fullFileName = zipEntry.getName();
				String dest = outDirPath + fullFileName;
				System.out.println("Destination path is :- " + dest);
				if (zipEntry.isDirectory()) {
					File f = new File(dest);
					System.out.println("f.mkdirs() :- " + f.mkdirs());
					continue;
				}

				// upldDir = new File(upldDirPath);
				outputFile = new File(outDirPath, fullFileName);
				fileOutStr = new FileOutputStream(outputFile);

				int read;
				buf = new byte[1024];
				double avgBuffSize = 0.0;
				int counter = 0;
				try {
					while ((read = zipIpStream.read(buf)) > 0) {
						counter++;
						avgBuffSize = (avgBuffSize + read) / 2;
						fileOutStr.write(buf, 0, read);
					}
					System.out.println("Read: " + counter + " times"
							+ " Avg Buff Size: " + avgBuffSize);
					fileOutStr.flush();
					fileOutStr.close();
				} catch (ZipException e) {
					e.printStackTrace();
				}
				int size = (int) zipEntry.getSize();
				System.out.println("ZIP File Stream Size: " + size);
			}
		} finally {
			if (zipIpStream != null)
				zipIpStream.close();
			if (urlConn != null)
				urlConn.disconnect();
		}
	}

	public void downloadFile(String fileURLPath, String fileName,
			String outDirFilePath) throws Exception {
		File outputFile = new File(outDirFilePath);
		downloadFile(fileURLPath, fileName, outputFile);
	}

	public void downloadFile(String fileURLPath, String fileName,
			File outputFile) throws Exception {
		byte buf[] = null;
		int count = 0;
		System.out.println("Download File: " + fileName + " from : "
				+ fileURLPath);
		URL url = null;
		HttpURLConnection urlConn = null;
		BufferedInputStream fileStream = null;
		FileOutputStream fileOutStr = null;
		try {
			url = new URL(fileURLPath + fileName);
			urlConn = (HttpURLConnection) url.openConnection();
			System.out.println("URL Conn Openned: "
					+ Calendar.getInstance().getTime().getTime());
			urlConn.connect();
			System.out.println("Connected to URL: " + url.toString());
			System.out.println("URL Connected at: "
					+ Calendar.getInstance().getTime().getTime());
			fileOutStr = new FileOutputStream(outputFile);

			int read;
			buf = new byte[1024];
			double avgBuffSize = 0.0;
			int counter = 0;
			while ((read = fileStream.read(buf)) > 0) {
				counter++;
				avgBuffSize = (avgBuffSize + read) / 2;
				fileOutStr.write(buf, 0, read);
			}
			System.out.println("Read: " + counter + " times"
					+ " Avg Buff Size: " + avgBuffSize);
			fileOutStr.flush();
			// fileOutStr.close();
			System.out.println("Downloaded File: " + outputFile.getName()
					+ " Size: " + outputFile.length());
		} finally {
			if (fileOutStr != null)
				fileOutStr.close();
			if (urlConn != null)
				urlConn.disconnect();
		}
	}

	public ByteArrayOutputStream downloadFile(String fileURLPath,
			String fileName) throws Exception {
		byte buf[] = null;
		int count = 0;
		System.out.println("Download File: " + fileName + " from : "
				+ fileURLPath);
		URL url = null;
		HttpURLConnection urlConn = null;
		BufferedInputStream fileStream = null;
		ByteArrayOutputStream outputStream = null;
		try {
			url = new URL(fileURLPath + fileName);
			urlConn = (HttpURLConnection) url.openConnection();
			System.out.println("URL Conn Openned: "
					+ Calendar.getInstance().getTime().getTime());
			urlConn.connect();
			System.out.println("Connected to URL: " + url.toString());
			System.out.println("URL Connected at: "
					+ Calendar.getInstance().getTime().getTime());
			fileStream = new BufferedInputStream(url.openStream());
			if (fileStream == null) {
				System.err.println("Couldn't find file: " + fileName + " in : "
						+ fileURLPath);
				return null;
			}
			System.out.println("URL File Stream is Not Null: " + fileStream);
			outputStream = new ByteArrayOutputStream();
			int read = 0, byteSize = 1024, off = 0;
			buf = new byte[byteSize];
			while ((read = fileStream.read(buf)) > 0) {
				outputStream.write(buf, 0, buf.length);
				off += buf.length;
				byteSize = fileStream.available();
				// System.out.println("Total Bytes read: " + off +
				// " Available to Read: " + byteSize);
			}
			System.out.println("Total Bytes read: " + off + " Total Size: "
					+ outputStream.size());

		} finally {
			if (fileStream != null)
				fileStream.close();
			if (urlConn != null)
				urlConn.disconnect();
		}
		return outputStream;
	}

	public double getBandwidthSpeed(String uRLPath) throws Exception {
		double transferRate = 0.0, transferRate1 = 0.0, transferRate2 = 0.0, transferRate3 = 0.0;
		transferRate1 = this.getBandwidthSpeed(uRLPath, "ganesha.jpg");
		transferRate2 = this.getBandwidthSpeed(uRLPath, "ganesha1.jpg");
		transferRate3 = this.getBandwidthSpeed(uRLPath, "ganesha2.jpg");
		transferRate = (transferRate1 + transferRate2 + transferRate3) / 3;
		System.out.println("Transfer Rate (Kbps) 1: " + transferRate1 + " 2: "
				+ transferRate2 + " 3: " + transferRate3);
		System.out.println("Avg Transfer Rate (Kbps) " + transferRate);
		// if (transferRate != 0.0) return transferRate;
		transferRate = this.getBandwidthSpeed(uRLPath, "AcharyaBalaji.jpg");
		if (transferRate != 0.0)
			return transferRate;
		// Setting Transfer Rate to 1Mbps
		return 1000.00;
	}

	public double getBandwidthSpeed(String uRLPath, String fileName)
			throws Exception {
		Date currTime = Calendar.getInstance().getTime();
		long startTime = currTime.getTime();
		System.out.println("Start Time: " + currTime.toString());
		System.out.println("Start Time(ms): " + startTime);
		ByteArrayOutputStream outputStream = this.downloadFile(uRLPath,
				fileName);
		int size = outputStream.size();
		currTime = Calendar.getInstance().getTime();
		long endTime = currTime.getTime();
		System.out.println("End Time: " + currTime.toString() + " File size: "
				+ size);
		System.out.println("Start Time (ms) : " + startTime + " End Time: "
				+ endTime);
		double totalTimeTaken = (double) (endTime - startTime);
		System.out.println("Total Time Taken: " + totalTimeTaken);
		double transferRate = 0.0;
		if (totalTimeTaken != 0)
			transferRate = size / (totalTimeTaken);
		System.out.println("Transfer Rate Kbps: " + transferRate);
		return transferRate;
	}

	public static void main(String[] args) {
		String tempDir = "";
		String codeBase = "http://localhost:8080/RGPTWebServices/";
		String pdfNetFiles = "RGPTLibraries1.zip";
		String outDir = System.getProperty("java.io.tmpdir");
		// String outDir = "C:/Nijesh";
		System.out.println("Temp Dir: " + tempDir + "\nCode Base: " + codeBase
				+ "\npdfNetFile: " + pdfNetFiles);
		// System.out.println("Get Env: " + System.getenv().toString());
		// System.out.println("Get System Properties: " +
		// System.getProperties().toString());
		System.out.println("Get Output Dir: " + outDir);
		ByteArrayOutputStream byteStream = null;
		HTTPFileDownload fileDownload = new HTTPFileDownload();
		try {
			// Enter arg[0] -Music, 1 - Song Start Id, 2 - Song End Id, 3 -
			// output dir
			if (args[0] != null && args[0].equals("Music")) {
				int songStId = new Integer(args[1]).intValue();
				int songEndId = new Integer(args[2]).intValue();
				// http://link.songs.pk/songs.pk/bviking10.mp3
				codeBase = "http://link.songs.pk/song.php?songid=";
				outDir = "C:/Users/Acharya/Music/";
				outDir = outDir + args[3] + "/";
				File outFile = RGPTUtil.createDir(outDir, true);
				System.out.println("Downloading Music file to Dir: "
						+ outFile.getPath());
				for (int i = songStId; i <= songEndId; i++) {
					File musicFile = new File(outDir + i + ".mp3");
					System.out.println("Downloading Music file: "
							+ musicFile.getPath());
					fileDownload.downloadFile(codeBase, String.valueOf(i),
							musicFile);
					System.out.println("Finished Downloading Music file: "
							+ musicFile.getName());
					/*
					 * FileOutputStream fos = new FileOutputStream(musicFile);
					 * String songURL = codeBase+i;
					 * System.out.println("Code Base: " + songURL); byteStream =
					 * fileDownload.downloadFile(codeBase, String.valueOf(i));
					 * fos.write(byteStream.toByteArray()); fos.flush();
					 * fos.close();
					 */
				}
			} else
				fileDownload.downloadZipFile(codeBase, pdfNetFiles, outDir);
			// byteStream = fileDownload.downloadFile(codeBase, pdfNetFiles);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
