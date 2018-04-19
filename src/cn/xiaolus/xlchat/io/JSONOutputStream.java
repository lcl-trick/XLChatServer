package cn.xiaolus.xlchat.io;

import java.io.IOException;
import java.io.OutputStream;

import org.json.JSONObject;

/**
 * 功能
 * JSON输出流，内部封装了一个输出流对象
 * 
 * @author 小路
 *
 */
public class JSONOutputStream {
	
//	封装的标准输出流	
	private OutputStream outputStream;
	
	/**
	 * 构造函数，传入一个输出流对象
	 * @param os 输出流对象
	 */
	public JSONOutputStream(OutputStream os) {
		this.outputStream = os;
	}
	
	/**
	 * 获得JSON输入流的输出流对象
	 * @return 输出流对象
	 */
	public OutputStream getOutputStream() {
		return outputStream;
	}
	
	/**
	 * 写入JSON对象
	 * @param jsonObject JSON对象
	 * @throws IOException I/O异常
	 */
	public void writeJSONObject(JSONObject jsonObject) throws IOException {
		System.out.println("Sending JSON Object:"+jsonObject.toString());
		outputStream.write(jsonObject.toString().getBytes());
	}
	
	/**
	 * 刷新流
	 * @throws IOException I/O异常
	 */
	public void flush() throws IOException {
		outputStream.flush();
	}
	
	/**
	 * 关闭流
	 * @throws IOException I/O异常
	 */
	public void close() throws IOException {
		outputStream.close();
	}
}
