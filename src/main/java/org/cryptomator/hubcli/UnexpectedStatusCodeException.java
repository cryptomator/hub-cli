package org.cryptomator.hubcli;

class UnexpectedStatusCodeException extends Exception {

	final int status;

	public UnexpectedStatusCodeException(int status, String message) {
		super(message);
		this.status = status;
	}

	/**
	 * Subtracts 300 from HTTP status codes to make most codes (400-555) fit into an unsigned byte
	 * @return A value suitable for use as exit code (0-255)
	 */
	public int asExitCode() {
		if (status >= 400 && status <= 555) {
			return status - 300;
		} else {
			return 1;
		}
	}
}
