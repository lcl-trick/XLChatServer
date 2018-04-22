package cn.xiaolus.xlchat.msg;

/**
 * 功能：
 * 登录消息
 * 
 * @author 小路
 *
 */
public class SigninMessage extends AbstractMessage {

	private static final long serialVersionUID = 6375555371279883352L;
//	用户密码
	protected String password;
//	是否以隐身方式登录
	protected boolean secretSignin;
	
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public boolean isSecretSignin() {
		return secretSignin;
	}
	public void setSecretSignin(boolean isSecertSignin) {
		this.secretSignin = isSecertSignin;
	}
	
	@Override
	public boolean isPublicMessage() {
		return false;
	}
	
}
