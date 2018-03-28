package cn.xiaolus.xlchat.util;

import java.io.IOException;
import java.io.InputStream;

import org.json.JSONException;
import org.json.JSONObject;

public class JSONInputStream {
	private InputStream inputStream;
	
	public JSONInputStream(InputStream is) {
		this.inputStream = is;
	}

	public InputStream getInputStream() {
		return inputStream;
	}
	
	public JSONObject readJSONObject() throws IOException,JSONException {
		int b = inputStream.read();
		while(b != '{') {
			b = inputStream.read();
			if (b == -1) {
				throw new JSONException("No JSON object found");
			}
		}
		StringBuilder source = new StringBuilder("{");
		while(b != '}') {
			b = inputStream.read();
			if (b == -1) {
				throw new JSONException("Invaid JSON source endding.");
			}
			source.append((char)b);
		}
		System.out.println("Receive JSON Object:"+source);
		JSONObject jsonObject = new JSONObject(source.toString());
		return jsonObject;
	}
	
	public void close() throws IOException {
		inputStream.close();
	}
}




