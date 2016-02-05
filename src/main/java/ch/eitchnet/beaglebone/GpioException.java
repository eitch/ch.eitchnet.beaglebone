package ch.eitchnet.beaglebone;

public class GpioException extends Exception {

	private static final long serialVersionUID = 1L;

	public GpioException(String message, Throwable cause) {
		super(message, cause);
	}

	public GpioException(String message) {
		super(message);
	}
}
