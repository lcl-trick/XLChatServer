package cn.xiaolus.xlchat.server.util;

import java.io.Serializable;
import java.lang.reflect.Field;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class XCMessage implements Serializable {
	private static final long serialVersionUID = -5075731033035195544L;
	
	protected String srcUser,dstUser;

	public String getSrcUser() {
		return srcUser;
	}

	public void setSrcUser(String srcUser) {
		this.srcUser = srcUser;
	}

	public String getDstUser() {
		return dstUser;
	}

	public void setDstUser(String dstUser) {
		this.dstUser = dstUser;
	}
	
	public boolean isPublicMessage() {
		return getDstUser().equals("");
	}
	
	public static boolean toJSONObject(XCMessage message, JSONObject jsonObject) {
		try {
			Field[] fields = message.getClass().getDeclaredFields();
			for (Field field : fields) {
				field.set(message, jsonObject.get(field.getName()));
			}
			return true;
		} catch (SecurityException | IllegalArgumentException | IllegalAccessException | JSONException e) {
			e.printStackTrace();
			return false;
		}
	}
}
