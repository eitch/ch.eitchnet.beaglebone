package ch.eitchnet.beaglebone;

public class Gpio {

	private final Pin pin;
	private final String kernelName;
	private final Direction direction;
	private String label;
	private Signal signal;

	public Gpio(Pin pin, Direction direction) {
		this.pin = pin;
		this.direction = direction;
		this.kernelName = "gpio" + pin.getGpioNr();
		this.label = kernelName;
		this.signal = Signal.LOW;
	}

	public Pin getPin() {
		return this.pin;
	}

	public String getKernelName() {
		return this.kernelName;
	}

	public Gpio setLabel(String label) {
		this.label = label;
		return this;
	}

	public String getLabel() {
		return this.label;
	}

	public Direction getDirection() {
		return this.direction;
	}

	public Signal getSignal() {
		return this.signal;
	}

	public Gpio setSignal(Signal signal) {
		this.signal = signal;
		return this;
	}

	@Override
	public String toString() {
		return this.pin.toString();
	}
}