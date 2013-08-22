package com.simoneurbinati.empat.net;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;


public class Server {
	public static String register(URL serverURL,String phoneNumber,String google_id){
		Map<String, String> params = new HashMap<String, String>();
		params.put("phone_number", phoneNumber);
		params.put("device_type", "Android");
		params.put("device_id", google_id);
		ConnectionManager cm = ConnectionManager.getInstance();
		RegisterResponse response = cm.getResponse("register.php", params, RegisterResponse.class);
//		if (response.code != 0) {
//			throw new IOException("errore server: " + response.code + " " + response.description);
//		}
//		if (response.result == null || response.result.private_key == null) {
//			throw new IOException("errore server: risposta non valida");
//		}
		return response.result.private_key;
		}
	

	public static class UnknownRecipientException extends Exception {

		private static final long serialVersionUID = 1L;

	}
	
}

