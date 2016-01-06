package ch.eitchnet.beaglebone;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GpioTest {

	private static final String GPIO_PATH = "/sys/class/gpio/";

	public static void main(String[] args) throws Exception {

		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

		GpioHandler gpioHandler = new GpioHandler();
		Gpio p8_11 = gpioHandler.getGpio(45, Direction.IN);
		Gpio p8_12 = gpioHandler.getGpio(44, Direction.OUT);
		gpioHandler.register(p8_11, (g) -> {
			System.out.println("New GPIO " + g.getNumber() + " signal " + g.getSignal());
		});

		gpioHandler.start();

		while (true) {
			gpioHandler.writeValue(p8_12, p8_12.getSignal().getOpposite());
			Thread.sleep(1000);
		}
	}

	public interface GpioSignalListener {
		public void notify(Gpio gpio);
	}

	public static class GpioHandler {

		private Map<Integer, Gpio> cache;
		private Map<Gpio, List<GpioSignalListener>> listeners;
		private Thread thread;
		private volatile boolean run;

		public GpioHandler() {
			this.cache = new HashMap<>();
			this.listeners = Collections.synchronizedMap(new HashMap<>());
		}

		public void writeValue(Gpio gpio, Signal signal) {

			if (gpio.getDirection() != Direction.OUT)
				throw new IllegalArgumentException("For writing the direction must be " + Direction.OUT);

			File file = new File(GPIO_PATH, gpio.getName() + "/value");
			try (FileOutputStream out = new FileOutputStream(file)) {

				out.write(signal.getValueS().getBytes());
				out.flush();

				gpio.setSignal(signal);

			} catch (Exception e) {
				e.printStackTrace();
			}

			System.out.println("Set GPIO " + gpio.getNumber() + " signal to " + gpio.getSignal());
		}

		public void start() {

			this.run = true;
			this.thread = new Thread(() -> {
				while (this.run) {
					if (this.listeners.isEmpty()) {
						synchronized (this) {
							try {
								wait(1000l);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					} else {

						List<Gpio> changes = new ArrayList<>();

						synchronized (this.listeners) {
							for (Gpio gpio : this.listeners.keySet()) {

								File file = new File(GPIO_PATH, gpio.getName() + "/value");
								try (BufferedReader fin = new BufferedReader(new FileReader(file))) {

									String valueS = fin.readLine();
									Signal signal = Signal.getSignal(valueS);
									if (!gpio.getSignal().equals(signal)) {
										gpio.setSignal(signal);
										changes.add(gpio);
									}

								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}

						if (!changes.isEmpty())
							System.out.println("Found " + changes.size() + " GPIO changes.");

						for (Gpio gpio : changes) {
							synchronized (this.listeners) {
								List<GpioSignalListener> listeners = this.listeners.get(gpio);
								System.out.println("GPIO " + gpio.getNumber() + " changed to " + gpio.getSignal()
										+ ". Notifying " + listeners.size() + " listeners.");

								for (GpioSignalListener listener : listeners) {
									listener.notify(gpio);
								}
							}
						}

						try {
							Thread.sleep(200l);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			} , "gpio_reader");
			this.thread.start();
		}

		public void stop() {
			this.run = false;
			this.thread.interrupt();
			try {
				this.thread.join(5000l);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		public Gpio getGpio(int number, Direction direction) {
			Gpio gpio = this.cache.get(number);
			if (gpio == null) {
				gpio = new Gpio(number, direction);

				File file = new File(GPIO_PATH, gpio.getName() + "/direction");
				try (BufferedReader fin = new BufferedReader(new FileReader(file))) {

					String directionS = fin.readLine();
					Direction dir = Direction.getDirection(directionS);
					if (dir != direction)
						throw new IllegalArgumentException(
								"Actual direction of GPIO " + gpio.getNumber() + " is " + dir + " not " + directionS);

				} catch (IOException e) {
					e.printStackTrace();
				}

				this.cache.put(number, gpio);
			}

			return gpio;
		}

		public void register(Gpio gpio, GpioSignalListener listener) {

			if (gpio.getDirection() != Direction.IN)
				throw new IllegalArgumentException("For reading the direction must be " + Direction.IN);

			synchronized (this.listeners) {
				List<GpioSignalListener> listeners = this.listeners.get(gpio);
				if (listeners == null) {
					listeners = new ArrayList<>();
					this.listeners.put(gpio, listeners);
				}

				listeners.add(listener);
			}

			synchronized (this) {
				notifyAll();
			}
		}

		public void unregister(Gpio gpio, GpioSignalListener listener) {
			synchronized (this.listeners) {
				List<GpioSignalListener> listeners = this.listeners.get(gpio);
				if (listeners == null) {
					return;
				}

				listeners.remove(listener);

				if (listeners.isEmpty())
					this.listeners.remove(gpio);
			}
		}
	}

	public static class Gpio {

		private final int number;
		private final String name;
		private final Direction direction;
		private Signal signal;

		Gpio(int number, Direction direction) {
			this.number = number;
			this.direction = direction;
			this.name = "gpio" + number;
			this.signal = Signal.LOW;
		}

		public int getNumber() {
			return this.number;
		}

		public String getName() {
			return this.name;
		}

		public Direction getDirection() {
			return this.direction;
		}

		public Signal getSignal() {
			return this.signal;
		}

		public void setSignal(Signal signal) {
			this.signal = signal;
		}
	}

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
}
