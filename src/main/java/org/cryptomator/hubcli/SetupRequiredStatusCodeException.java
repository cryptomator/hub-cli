package org.cryptomator.hubcli;

class SetupRequiredStatusCodeException extends UnexpectedStatusCodeException {

	private static final int STATUS = 449;
	private static final String MESSAGE = "Cryptomator Hub CLI is not setup. Execute the setup command first.";

	public SetupRequiredStatusCodeException() {
		super(STATUS, MESSAGE);
	}

	public SetupRequiredStatusCodeException(UnexpectedStatusCodeException e) {
		super(e, STATUS, MESSAGE);
	}
}
