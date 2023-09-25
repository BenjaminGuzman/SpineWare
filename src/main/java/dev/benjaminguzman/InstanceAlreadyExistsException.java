package dev.benjaminguzman;

public class InstanceAlreadyExistsException extends RuntimeException {
	public InstanceAlreadyExistsException() {
		super();
	}

	public InstanceAlreadyExistsException(String msg) {
		super(msg);
	}
}
