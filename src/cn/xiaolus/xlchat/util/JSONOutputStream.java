package cn.xiaolus.xlchat.util;

import java.io.IOException;
import java.io.OutputStream;

import org.json.JSONObject;

public class JSONOutputStream {
	private OutputStream outputStream;
	
	public JSONOutputStream(OutputStream os) {
		this.outputStream = os;
	}
	
	public OutputStream getOutputStream() {
		return outputStream;
	}
	
	public void writeJSONObject(JSONObject jsonObject) throws IOException {
		System.out.println("Sending JSON Object:"+jsonObject.toString());
		outputStream.write(jsonObject.toString().getBytes());
	}
	
	public void flush() throws IOException {
		outputStream.flush();
	}
}
