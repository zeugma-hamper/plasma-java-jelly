package com.oblong.jelly.communication;

/**
 * User: karol
 * Date: 12/3/13
 * Time: 6:01 PM
 */
public class UnableToConnectEvent extends ConnectionErrorEvent {

	public final Reason reason;

	public enum Reason {
		BAD_ADDRESS,
		UNABLE_TO_RESOLVE_ADDRESS,
		POOL_EXCEPTION,
		TIMEOUT,
		INVALID_CERTIFICATE,
		UNSUPPORTED_OPERATION,
		POOL_REQUIRES_TLS,
		CLIENT_REQUIRES_TLS,

	}

	public UnableToConnectEvent(Reason reason) {
		this.reason = reason;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof UnableToConnectEvent)
			return ((UnableToConnectEvent)other).reason == this.reason;
		return false;
	}
}
