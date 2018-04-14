package cn.xiaolus.xlchat.util;

/**
 * 功能：
 * 注销消息
 * 
 * @author 小路
 *
 */
public class XCSignoutMessage extends XCMessage {

	private static final long serialVersionUID = -4290233543928590957L;
	
	@Override
	public boolean isPublicMessage() {
		return false;
	}
}
