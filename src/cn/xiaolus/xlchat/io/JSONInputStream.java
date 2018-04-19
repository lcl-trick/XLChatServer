package cn.xiaolus.xlchat.io;

import java.io.IOException;
import java.io.InputStream;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 功能
 * JSON输入流，内部封装了一个输入流对象
 * 
 * @author 小路
 *
 */
public class JSONInputStream {
	
//	封装的标准输入流
	private InputStream inputStream;
	
	/**
	 * 构造函数，传入一个输入流对象
	 * @param is 输入流对象
	 */
	public JSONInputStream(InputStream is) {
		this.inputStream = is;
	}
	
	/**
	 * 获得JSON输入流的输入流对象
	 * @return 输入流对象
	 */
	public InputStream getInputStream() {
		return inputStream;
	}
	
	/**
	 * 读取JSON对象
	 * @return JSON对象
	 * @throws IOException I/O异常
	 * @throws JSONException JSON异常，读取到了非法的JSON对象
	 */
	public JSONObject readJSONObject() throws IOException,JSONException {
//		首先检测左大括号，这是JSON对象的开始
		int b = inputStream.read();
		while(b != '{') {
			b = inputStream.read();
			if (b == -1) {
				throw new JSONException("No JSON object found");
			}
		}
//		检测到之后开始拼接JSON对象
		StringBuilder source = new StringBuilder("{");
//		然后检测右大括号，这是JSON对象的结束
		while(b != '}') {
			b = inputStream.read();
			if (b == -1) {
				throw new JSONException("Invaid JSON source endding.");
			}
			source.append((char)b);
		}
//		输出读取到的JSON对象字符串值
		System.out.println("Receive JSON Object:"+source);
//		构造JSON对象并返回
		JSONObject jsonObject = new JSONObject(source.toString());
		return jsonObject;
	}
	
	/**
	 * 关闭流
	 * @throws IOException I/O异常
	 */
	public void close() throws IOException {
		inputStream.close();
	}
}




