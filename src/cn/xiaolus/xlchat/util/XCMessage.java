package cn.xiaolus.xlchat.util;

import java.io.Serializable;
import java.lang.reflect.Field;

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
	
	public static XCMessage fromJSONObject(JSONObject jsonObject, Class<? extends XCMessage> classObj) {
		try {
			XCMessage message = classObj.getDeclaredConstructor().newInstance();
			Field[] superFields = classObj.getSuperclass().getDeclaredFields();
			for (Field field : superFields) {
				if (field.getName().equals("serialVersionUID")) {
					continue;
				}
				field.set(message, jsonObject.get(field.getName()));
			}
			Field[] fields = classObj.getDeclaredFields();
			for (Field field : fields) {
				if (field.getName().equals("serialVersionUID")) {
					continue;
				}
				field.set(message, jsonObject.get(field.getName()));
			}
			return message;
		} catch (Exception e) {
			return null;
		}
	}
}
