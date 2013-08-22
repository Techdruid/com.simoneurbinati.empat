package com.simoneurbinati.empat.net;


public abstract class Response {
	public int code = -1;
	public String description = null;
}
class RegisterResponse extends Response {
	public RegisterResult result;
}

class SendResponse extends Response {
}

class RetrieveResponse extends Response {
	public RetrieveResult result;
}