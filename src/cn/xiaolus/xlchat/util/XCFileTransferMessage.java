package cn.xiaolus.xlchat.util;

public class XCFileTransferMessage extends XCMessage {
	/**
	 * 
	 */
	private static final long serialVersionUID = 9152074832484490408L;
	
	public static final int REQ_TRA = 1, ACCEPT_TRA = 2, REJECT_TRA = -2;
	protected int status;
	protected String fileName;
	protected double fileSize;
	protected String host;
	protected int port;
	
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public double getFileSize() {
		return fileSize;
	}
	public void setFileSize(double fileSize) {
		this.fileSize = fileSize;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}

	@Override
	public boolean isPublicMessage() {
		return false;
	}
	
}
