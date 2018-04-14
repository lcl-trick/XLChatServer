package cn.xiaolus.xlchat.util;

/**
 * 功能：
 * 聊天消息
 * 
 * @author 小路
 *
 */
public class XCChatMessage extends XCMessage {
	
	private static final long serialVersionUID = -2208082869128050072L;
//	消息正文
	protected String msgContent;

	public String getMsgContent() {
		return msgContent;
	}

	public void setMsgContent(String msgContent) {
		this.msgContent = msgContent;
	}

}
