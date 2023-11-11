package org.cryptomator.hubcli;

class UnexpectedStatusCodeException extends Exception {

	public final int status;

	public UnexpectedStatusCodeException(int status, String message) {
		super(message);
		this.status = status;
	}
}
