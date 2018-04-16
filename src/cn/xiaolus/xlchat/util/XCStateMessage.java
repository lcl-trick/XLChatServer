package cn.xiaolus.xlchat.util;

/**
 * 功能：
 * 状态消息
 * 
 * @author 小路
 *
 */
public class XCStateMessage extends XCMessage {
	
	private static final long serialVersionUID = 2420995190931098975L;
	public static final int SUCCESS = 0, FAILED = -1, REQ_TRA = 1, ACCEPT_TRA = 2, REJECT_TRA = -2;
//	状态类型
	protected int status;
//	错误消息
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
