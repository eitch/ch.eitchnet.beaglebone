package ch.eitchnet.beaglebone;

public class Gpio {

	private final Pin pin;
	private final String name;
	private final Direction direction;
	private String label;
	private Signal signal;

	public Gpio(Pin pin, Direction direction) {
		this.pin = pin;
		this.direction = direction;
		this.name = "gpio" + pin.getGpioNr();
		this.label = name;
		this.signal = Signal.LOW;
	}

	public Pin getPin() {
		return this.pin;
	}

	public String getName() {
		return this.name;
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
}