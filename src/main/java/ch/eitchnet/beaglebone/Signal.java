package ch.eitchnet.beaglebone;

public enum Signal {
	LOW(0, "0", false, "low"), HIGH(1, "1", true, "high");

	private int value;
	private String valueS;
	private boolean high;
	private String signal;

	private Signal(int value, String valueS, boolean high, String signal) {
		this.value = value;
		this.valueS = valueS;
		this.high = high;
		this.signal = signal;
	}

	public int getValue() {
		return this.value;
	}

	public String getValueS() {
		return this.valueS;
	}

	public boolean isHigh() {
		return this.high;
	}

	public String getSignal() {
		return this.signal;
	}

	public Signal getOpposite() {
		if (this.high)
			return LOW;
		return HIGH;
	}

	public static Signal getSignal(int value) {
		if (value == 0)
			return LOW;
		else if (value == 1)
			return HIGH;
		throw new IllegalArgumentException("No signal for value " + value);
	}

	public static Signal getSignal(String valueS) {
		if (valueS.equals(LOW.valueS))
			return LOW;
		else if (valueS.equals(HIGH.valueS))
			return HIGH;
		throw new IllegalArgumentException("No signal for value " + valueS);
	}
}