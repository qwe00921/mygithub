package com.anfu.photowebviewer;

import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import android.os.AsyncTask;

public class Ftp {
	/**
	 * 通过ftp上传文件
	 * 
	 * @param url
	 *            ftp服务器地址 如： 192.168.1.110
	 * @param port
	 *            端口如 ： 21
	 * @param username
	 *            登录名
	 * @param password
	 *            密码
	 * @param remotePath
	 *            上到ftp服务器的磁盘路径
	 * @param fileNamePath
	 *            要上传的文件路径
	 * @param fileName
	 *            要上传的文件名
	 * @return
	 */
	public String url;
	public String port;
	public String username;
	public String password;
	public String remotePath;
	public String fileNamePath;
	public String fileName;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getRemotePath() {
		return remotePath;
	}

	public void setRemotePath(String remotePath) {
		this.remotePath = remotePath;
	}

	public String getFileNamePath() {
		return fileNamePath;
	}

	public void setFileNamePath(String fileNamePath) {
		this.fileNamePath = fileNamePath;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String ftpUpload() {
		FTPClient ftpClient = new FTPClient();
		FileInputStream fis = null;
		String returnMessage = "0";
		try {
			ftpClient.connect(url, Integer.parseInt(port));
			boolean loginResult = ftpClient.login(username, password);
			int returnCode = ftpClient.getReplyCode();
			if (loginResult && FTPReply.isPositiveCompletion(returnCode)) {// 如果登录成功
				ftpClient.makeDirectory(remotePath);
				// 设置上传目录
				ftpClient.changeWorkingDirectory(remotePath);
				ftpClient.setBufferSize(1024);
				ftpClient.setFileType(ftpClient.BINARY_FILE_TYPE);
				ftpClient.setControlEncoding("UTF-8");
				ftpClient.enterLocalPassiveMode();
				fis = new FileInputStream(fileNamePath + fileName);
				ftpClient.storeFile(fileName, fis);

				returnMessage = "1"; // 上传成功
			} else {// 如果登录失败
				returnMessage = "0";
			}

		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("FTP客户端出错！", e);
		} finally {
			// IOUtils.closeQuietly(fis);
			try {
				ftpClient.disconnect();
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException("关闭FTP连接发生异常！", e);
			}
		}
		return returnMessage;
	}
	
	public static final String STATUS_SUCCESS = "1";
	public static final String STATUS_FAIL = "0";
	
	public void ftpUploadAsync(String url, String port, String username,
			String password, String remotePath, String remoteFileName, String fileNamePath,
			String fileName, UploadListener listener) {
		mUploadListener = listener;
		new SendRequestAsyncTask().execute(url, port, username, password, remotePath, remoteFileName, fileNamePath, fileName);
	}
	
	private UploadListener mUploadListener;
	
	public interface UploadListener {
		public void onSuccess();
		public void onFail();
	}
	private class SendRequestAsyncTask extends AsyncTask<String,Void,String> {

		@Override
		protected String doInBackground(String... args) {
			if(args == null) {
				return null;
			}
			
			String url = args[0];
			String port = args[1];
			String username = args[2];
			String password = args[3];
			String remotePath = args[4];
			String remoteFileName = args[5];
			String fileNamePath = args[6];
			String fileName = args[7];
			
			String value = ftpUpload(url, port, username, password, remotePath, remoteFileName, fileNamePath, fileName);

			return value;
		}
		
		@Override
		protected void onPostExecute(String result) {
			if(STATUS_FAIL.equals(result)) {

				mUploadListener.onFail();
			} else {
				mUploadListener.onSuccess();
			}
		}
	}
	public String ftpUpload(String url, String port, String username,
			String password, String remotePath, String remoteFileName, String fileNamePath,
			String fileName) {
		FTPClient ftpClient = new FTPClient();
		FileInputStream fis = null;
		String returnMessage = STATUS_FAIL;
		try {
			ftpClient.connect(url, Integer.parseInt(port));
			boolean loginResult = ftpClient.login(username, password);
			int returnCode = ftpClient.getReplyCode();
			if (loginResult && FTPReply.isPositiveCompletion(returnCode)) {// 如果登录成功
//				ftpClient.makeDirectory(remotePath);
//				int code = ftpClient.getReplyCode();
				ftpClient.changeWorkingDirectory(remotePath);
				// 设置上传目录
				ftpClient.setBufferSize(1024);
				ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
				ftpClient.setControlEncoding("UTF-8");
				ftpClient.enterLocalPassiveMode();
				fis = new FileInputStream(fileNamePath + fileName);
				boolean success = ftpClient.storeFile(remoteFileName, fis);

				if(success) {
					returnMessage = STATUS_SUCCESS; // 上传成功
				}
			} else {// 如果登录失败
				returnMessage = STATUS_FAIL;
			}

		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("FTP客户端出错！", e);
		} finally {
			// IOUtils.closeQuietly(fis);
			try {
				ftpClient.disconnect();
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException("关闭FTP连接发生异常！", e);
			}
		}
		return returnMessage;
	}
}
