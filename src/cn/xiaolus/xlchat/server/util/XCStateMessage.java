package cn.xiaolus.xlchat.server.util;

public class XCStateMessage extends XCMessage {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2420995190931098975L;
	protected int status;
	protected String error;
	
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public String getError() {
		return error;
	}
	public void setError(String error) {
		this.error = error;
	}

	@Override
	public boolean isPublicMessage() {
		return false;
	}
}
