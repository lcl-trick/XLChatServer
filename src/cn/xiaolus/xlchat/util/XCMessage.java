package cn.xiaolus.xlchat.util;

import java.io.Serializable;
import java.lang.reflect.Field;

import org.json.JSONObject;

/**
 * 功能：
 * 消息抽象类
 * 
 * @author 小路
 *
 */
public abstract class XCMessage implements Serializable {
	private static final long serialVersionUID = -5075731033035195544L;
	
//	发送者和接收者用户名
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
	
	/**
	 * 是否为公聊消息，如果目标用户是空就是公聊消息
	 * @return 公聊消息返回true，不是公聊消息返回false
	 */
	public boolean isPublicMessage() {
		return getDstUser().equals("");
	}
	
	/**
	 * 反序列化JSON对象为消息对象
	 * @param jsonObject JSON对象
	 * @param classObj 欲反序列化成的消息类型
	 * @return 如果反序列化成功则返回一个与该JSON对象对应的消息对象，否则返回null
	 */
	public static XCMessage fromJSONObject(JSONObject jsonObject, Class<? extends XCMessage> classObj) {
		try {
//			从反射调用构造函数创建一个对象
			XCMessage message = classObj.getDeclaredConstructor().newInstance();
//			获得父类中定义的成员变量
			Field[] superFields = classObj.getSuperclass().getDeclaredFields();
//			遍历父类成员变量
			for (Field field : superFields) {
//				跳过序列化ID
				if (field.getName().equals("serialVersionUID")) {
					continue;
				}
//				设置所有其他的成员变量为JSON的键
				field.set(message, jsonObject.get(field.getName()));
			}
//			获得当前类中定义的成员变量
			Field[] fields = classObj.getDeclaredFields();
//			遍历当前类的成员变量
			for (Field field : fields) {
//				跳过序列化ID
				if (field.getName().equals("serialVersionUID")) {
					continue;
				}
//				设置所有其他的成员变量为JSON的键
				field.set(message, jsonObject.get(field.getName()));
			}
			return message;
		} catch (Exception e) {
			return null;
		}
	}
}
