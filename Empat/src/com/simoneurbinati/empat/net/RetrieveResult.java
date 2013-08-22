package com.simoneurbinati.empat.net;


class RetrieveResult {
	public RetrieveResult_Message[] messages;
}

class RegisterResult {
	public String private_key = null;
}

class RetrieveResult_Message {
	public String id;
	public String sender;
	public String recipient;
	public String message;
	public String ts_sent;
}
