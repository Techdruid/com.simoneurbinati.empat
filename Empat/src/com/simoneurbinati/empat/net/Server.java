package com.simoneurbinati.empat.net;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.simoneurbinati.empat.model.Message;

import android.util.Log;


public class Server {
	private static ConnectionManager cm = ConnectionManager.getInstance();

	
	public static String register(URL serverURL,String phoneNumber,String google_id){
		Map<String, String> params = new HashMap<String, String>();
		params.put("phone_number", phoneNumber);
		params.put("device_type", "Android");
		params.put("device_id", google_id);
		RegisterResponse response = cm.getResponse("register.php", params, RegisterResponse.class);
//		if (response.code != 0) {
//			throw new IOException("errore server: " + response.code + " " + response.description);
//		}
//		if (response.result == null || response.result.private_key == null) {
//			throw new IOException("errore server: risposta non valida");
//		}
		return response.result.private_key;
		}
	
	public static Message[] retrieve(URL serverBaseUrl, String privateKey, String phoneNumber) throws IOException {
		Map<String, String> params = new HashMap<String, String>();
		params.put("private_key", privateKey);
		params.put("phone_number", phoneNumber);
		
		RetrieveResponse response = cm.getResponse("retrieve.php", params, RetrieveResponse.class);
		if (response.code != 0) {
			throw new IOException("errore server: " + response.code + " " + response.description);
		}
		if (response.result == null || response.result.messages == null) {
			return new Message[0];
		}
		int size = response.result.messages.length;
		Message[] ret = new Message[size];
		for (int i = 0; i < size; i++) {
			ret[i] = new Message();
			ret[i].id = response.result.messages[i].id;
			ret[i].senderPhoneNumber = response.result.messages[i].sender;
			ret[i].recipientPhoneNumber = response.result.messages[i].recipient;
			ret[i].text = response.result.messages[i].message;
			try {
				ret[i].sentTimestamp = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss Z", Locale.US).parse(response.result.messages[i].ts_sent).getTime();
			} catch (Exception e) {
				Log.w("freem", "formato timestamp non valido in " + response.result.messages[i].ts_sent, e);
				ret[i].sentTimestamp = System.currentTimeMillis();
			}
		}
		return ret;
	}

	
	public static class UnknownRecipientException extends Exception {

		private static final long serialVersionUID = 1L;

	}
	
}

