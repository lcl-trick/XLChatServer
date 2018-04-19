package cn.xiaolus.xlchat.msg;

/**
 * 功能：
 * 文件传输消息
 * 
 * @author 小路
 *
 */
public class FileTransferMessage extends AbstractMessage {
	
	private static final long serialVersionUID = 9152074832484490408L;
//	消息类型：文件请求=1、接受文件=2、拒绝文件=-2
	public static final int REQUEST = 1, ACCEPT = 2, REJECT = -2;
//	消息类型码
	protected int status;
//	文件名称
	protected String fileName;
//	文件大小
	protected double fileSize;
//	接收方主机名
	protected String host;
//	接收方端口
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
