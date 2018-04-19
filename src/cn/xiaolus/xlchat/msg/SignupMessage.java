package cn.xiaolus.xlchat.msg;

/**
 * 功能：
 * 注册消息
 * 
 * @author 小路
 *
 */
public class SignupMessage extends AbstractMessage {

	private static final long serialVersionUID = 8429862001867074251L;
//	用户昵称
	protected String name;
//	用户密码
	protected String password;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	@Override
	public boolean isPublicMessage() {
		return false;
	}
}
