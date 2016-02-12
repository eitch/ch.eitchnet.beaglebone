package ch.eitchnet.beaglebone;

/**
 * <p>
 * Defines the direction of a {@link Gpio}
 * </p>
 * 
 * <p>
 * A {@link Gpio} can be either an input pin, or an output pin. This is defined by this enum
 * </p>
 * 
 * @author Robert von Burg <eitch@eitchnet.ch>
 */
public enum Direction {
	IN("in"), OUT("out");

	private String direction;

	private Direction(String direction) {
		this.direction = direction;
	}

	public String getDirection() {
		return this.direction;
	}

	public static Direction getDirection(String directionS) {
		if (directionS.equals(IN.direction))
			return IN;
		else if (directionS.equals(OUT.direction))
			return OUT;
		throw new IllegalArgumentException("No direction for value " + directionS);
	}
}