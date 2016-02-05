package ch.eitchnet.beaglebone;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GpioBridge {

	private Map<Pin, Gpio> cache;
	private Map<Gpio, List<GpioSignalListener>> listeners;
	private Thread thread;
	private volatile boolean run;

	public GpioBridge() {
		this.cache = new HashMap<>();
		this.listeners = Collections.synchronizedMap(new HashMap<>());
	}

	private File getGpioValuePath(Gpio gpio) {
		return new File(GpioBridgeTest.GPIO_PATH, gpio.getKernelName() + "/value");
	}

	private File getGpioDirectionPath(Gpio gpio) {
		return new File(GpioBridgeTest.GPIO_PATH, gpio.getKernelName() + "/direction");
	}

	public void writeValue(Gpio gpio, Signal signal) throws GpioException {

		if (gpio.getDirection() != Direction.OUT)
			throw new IllegalArgumentException("For writing the direction must be " + Direction.OUT);

		File file = getGpioValuePath(gpio);
		try (FileOutputStream out = new FileOutputStream(file)) {

			out.write(signal.getValueS().getBytes());
			out.flush();

			gpio.setSignal(signal);

		} catch (Exception e) {
			throw new GpioException("Failed to write GPIO " + gpio + " with signal " + signal, e);
		}

		System.out.println("Set GPIO " + gpio.getPin() + " signal to " + gpio.getSignal());
	}

	public Signal readValue(Gpio gpio) throws GpioException {

		synchronized (gpio) {

			if (gpio.getDirection() != Direction.IN)
				throw new IllegalArgumentException("For reading the direction must be " + Direction.IN);

			File file = getGpioValuePath(gpio);
			try (BufferedReader fin = new BufferedReader(new FileReader(file))) {

				String valueS = fin.readLine();
				Signal signal = Signal.getSignal(valueS);
				if (!gpio.getSignal().equals(signal))
					gpio.setSignal(signal);

				return signal;

			} catch (Exception e) {
				throw new GpioException("Failed to read GPIO " + gpio, e);
			}
		}
	}

	public void start() {

		this.run = true;
		this.thread = new Thread(() -> {
			while (this.run) {
				if (this.listeners.isEmpty()) {
					synchronized (this) {
						try {
							wait(1000l);
						} catch (InterruptedException e) {
							System.out.println("Was interrupted. Stopping thread.");
							this.run = false;
							break;
						}
					}
				} else {

					List<Gpio> changes = new ArrayList<>();

					synchronized (this.listeners) {
						for (Gpio gpio : this.listeners.keySet()) {
							try {
								synchronized (gpio) {
									Signal currentSignal = gpio.getSignal();
									Signal newSignal = readValue(gpio);
									if (currentSignal != newSignal)
										changes.add(gpio);
								}
							} catch (Exception e) {
								System.out.println("Failed to read GPIO " + gpio + " due to:");
								e.printStackTrace();
								this.run = false;
								break;
							}
						}
					}

					if (!changes.isEmpty())
						System.out.println("Found " + changes.size() + " GPIO changes.");

					for (Gpio gpio : changes) {
						synchronized (this.listeners) {
							List<GpioSignalListener> listeners = this.listeners.get(gpio);
							System.out.println("GPIO " + gpio.getPin() + " changed to " + gpio.getSignal()
									+ ". Notifying " + listeners.size() + " listeners.");

							for (GpioSignalListener listener : listeners) {
								try {
									listener.notify(gpio);
								} catch (Exception e) {
									System.out.println("Failed to update listener " + listener + " due to:");
									e.printStackTrace();
								}
							}
						}
					}

					try {
						Thread.sleep(200l);
					} catch (InterruptedException e) {
						System.out.println("Was interrupted. Stopping thread.");
						this.run = false;
						break;
					}
				}
			}
		} , "gpio_reader");
		this.thread.start();
		System.out.println("Started GPIO bridge.");
	}

	public void stop() {
		this.run = false;
		this.thread.interrupt();
		try {
			this.thread.join(5000l);
		} catch (InterruptedException e) {
			System.out.println("Was interrupted while waiting for thread to stop?!");
		}
	}

	public synchronized Gpio getGpio(Pin pin, Direction direction) throws GpioException {
		Gpio gpio = this.cache.get(pin);
		if (gpio == null) {

			gpio = new Gpio(pin, direction);

			// validate direction
			validateDirection(pin, direction, gpio);

			// validate file permissions
			validateFilePermissions(direction, gpio);

			this.cache.put(pin, gpio);
			System.out.println("Initialized pin " + pin + " with direction " + direction + ".");
		}

		return gpio;
	}

	private void validateDirection(Pin pin, Direction direction, Gpio gpio) throws GpioException {
		File file = getGpioDirectionPath(gpio);
		try (BufferedReader fin = new BufferedReader(new FileReader(file))) {

			String directionS = fin.readLine();
			Direction dir = Direction.getDirection(directionS);
			if (dir != direction)
				throw new GpioException(
						"Actual direction of GPIO " + gpio.getPin() + " is " + dir + " not " + directionS);

		} catch (FileNotFoundException e) {
			throw new GpioException("GPIO " + pin + " does not exist, was the pin exported to user space?", e);
		} catch (IOException e) {
			throw new GpioException("Failed to open GPIO " + pin, e);
		}
	}

	private void validateFilePermissions(Direction direction, Gpio gpio) throws GpioException {
		File gpioValuePath = getGpioValuePath(gpio);
		if (direction == Direction.IN) {
			if (!gpioValuePath.canRead())
				throw new GpioException("GPIO " + gpio + " has direction " + direction
						+ " and is not readable. Are the file permissions ok?");

		} else if (direction == Direction.OUT) {
			if (!gpioValuePath.canWrite())
				throw new GpioException("GPIO " + gpio + " has direction " + direction
						+ " and is not writable. Are the file permissions ok?");
		} else {
			throw new RuntimeException("Unhandled Direction " + direction);
		}
	}

	public void register(Gpio gpio, GpioSignalListener listener) throws GpioException {

		if (gpio.getDirection() != Direction.IN)
			throw new GpioException("For reading the direction must be " + Direction.IN);

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

	public void unregister(GpioBridgeTest gpio, GpioSignalListener listener) {
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