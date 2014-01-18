package test;

import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

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

	public String ftpUpload(String url, String port, String username,
			String password, String remotePath, String fileNamePath,
			String fileName) {
		FTPClient ftpClient = new FTPClient();
		FileInputStream fis = null;
		String returnMessage = "0";
		try {
			ftpClient.connect(url, Integer.parseInt(port));
			boolean loginResult = ftpClient.login(username, password);
			int returnCode = ftpClient.getReplyCode();
			if (loginResult && FTPReply.isPositiveCompletion(returnCode)) {// 如果登录成功
//				ftpClient.makeDirectory(remotePath);
				// 设置上传目录
				ftpClient.changeWorkingDirectory(remotePath);
				int code = ftpClient.getReplyCode();
				ftpClient.setBufferSize(1024);
				ftpClient.setFileType(ftpClient.BINARY_FILE_TYPE);
				ftpClient.setControlEncoding("UTF-8");
				ftpClient.enterLocalPassiveMode();
				fis = new FileInputStream(fileNamePath + fileName);
				ftpClient.storeFile(fileName, fis);
				code = ftpClient.getReplyCode();
				
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
	
	public static void main(String args[]) {
		Ftp ftp  = new Ftp();
		ftp.ftpUpload("61.129.33.98", "21", "anonymous", "test", "pub", "C:\\Users\\chaoqun_liu\\Desktop\\ScreenCaptureProject\\ScreenCapture\\", "log-file.txt");
//		ftp.ftpUpload("127.0.0.1", "21", "anonymous", "test", "pub", "C:\\Users\\chaoqun_liu\\Desktop\\ScreenCaptureProject\\ScreenCapture\\", "log_file.txt");
	}
}
