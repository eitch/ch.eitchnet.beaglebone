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

public class GpioBridge {

	private Map<Pin, Gpio> cache;
	private Map<Gpio, List<GpioSignalListener>> listeners;
	private Thread thread;
	private volatile boolean run;

	public GpioBridge() {
		this.cache = new HashMap<>();
		this.listeners = Collections.synchronizedMap(new HashMap<>());
	}

	public void writeValue(Gpio gpio, Signal signal) {

		if (gpio.getDirection() != Direction.OUT)
			throw new IllegalArgumentException("For writing the direction must be " + Direction.OUT);

		File file = new File(GpioBridgeTest.GPIO_PATH, gpio.getName() + "/value");
		try (FileOutputStream out = new FileOutputStream(file)) {

			out.write(signal.getValueS().getBytes());
			out.flush();

			gpio.setSignal(signal);

		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("Set GPIO " + gpio.getPin() + " signal to " + gpio.getSignal());
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

							File file = new File(GpioBridgeTest.GPIO_PATH, gpio.getName() + "/value");
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
							System.out.println("GPIO " + gpio.getPin() + " changed to " + gpio.getSignal()
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

	public Gpio getGpio(Pin pin, Direction direction) {
		Gpio gpio = this.cache.get(pin);
		if (gpio == null) {
			gpio = new Gpio(pin, direction);

			File file = new File(GpioBridgeTest.GPIO_PATH, gpio.getName() + "/direction");
			try (BufferedReader fin = new BufferedReader(new FileReader(file))) {

				String directionS = fin.readLine();
				Direction dir = Direction.getDirection(directionS);
				if (dir != direction)
					throw new IllegalArgumentException(
							"Actual direction of GPIO " + gpio.getPin() + " is " + dir + " not " + directionS);

			} catch (IOException e) {
				e.printStackTrace();
			}

			this.cache.put(pin, gpio);
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