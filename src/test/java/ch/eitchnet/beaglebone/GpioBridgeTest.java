package ch.eitchnet.beaglebone;

public class GpioBridgeTest {

	static final String GPIO_PATH = "/sys/class/gpio/";

	private static Gpio redBtn;
	private static Gpio blueBtn;
	private static Gpio greenBtn;

	private static Gpio green0;

	private static Gpio yellow0;
	private static Gpio yellow1;
	private static Gpio yellow2;

	private static Gpio green1;
	private static Gpio red0;

	private static Thread workThread;
	private static volatile boolean doWork;
	private static volatile Gpio workBtnPressed;

	public static void main(String[] args) throws Exception {

		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

		GpioBridge gpioBridge = new GpioBridge();

		redBtn = gpioBridge.getGpio(Pin.P9_11, Direction.IN).setLabel("Red");
		blueBtn = gpioBridge.getGpio(Pin.P9_12, Direction.IN).setLabel("Blue");
		greenBtn = gpioBridge.getGpio(Pin.P9_13, Direction.IN).setLabel("Green");

		green0 = gpioBridge.getGpio(Pin.P9_14, Direction.OUT).setLabel("green0");
		yellow0 = gpioBridge.getGpio(Pin.P9_15, Direction.OUT).setLabel("yellow0");
		yellow1 = gpioBridge.getGpio(Pin.P9_16, Direction.OUT).setLabel("yellow1");
		yellow2 = gpioBridge.getGpio(Pin.P9_17, Direction.OUT).setLabel("yellow2");
		green1 = gpioBridge.getGpio(Pin.P9_18, Direction.OUT).setLabel("green1");
		red0 = gpioBridge.getGpio(Pin.P9_21, Direction.OUT).setLabel("red0");

		GpioSignalListener btnListener = (g) -> {
			if (g.getSignal().isHigh())
				System.out.println("User pressed " + g.getLabel() + " button.");
			else
				System.out.println("User released " + g.getLabel() + " button.");
		};
		gpioBridge.register(redBtn, btnListener);
		gpioBridge.register(blueBtn, btnListener);
		gpioBridge.register(greenBtn, btnListener);

		gpioBridge.register(greenBtn, g -> startWork(gpioBridge));
		gpioBridge.register(blueBtn, g -> stopWork(blueBtn, gpioBridge));
		gpioBridge.register(redBtn, g -> stopWork(redBtn, gpioBridge));

		gpioBridge.start();
	}

	private static void startWork(GpioBridge gpioBridge) {

		doWork = true;

		workThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					gpioBridge.writeValue(green0, Signal.HIGH);

					while (doWork) {
						gpioBridge.writeValue(yellow0, yellow0.getSignal().getOpposite());
						Thread.sleep(250L);
						if (!doWork)
							break;
						gpioBridge.writeValue(yellow1, yellow1.getSignal().getOpposite());
						Thread.sleep(250L);
						if (!doWork)
							break;
						gpioBridge.writeValue(yellow2, yellow2.getSignal().getOpposite());
						Thread.sleep(250L);
					}

					gpioBridge.writeValue(green0, Signal.LOW);
					if (workBtnPressed == null) {
						System.out.println("Uh-oh, no workBtnPressed set! Something is bad!");
						gpioBridge.writeValue(green1, Signal.HIGH);
						gpioBridge.writeValue(red0, Signal.HIGH);
					} else if (workBtnPressed == blueBtn) {
						System.out.println("Work completed successfully!");
						gpioBridge.writeValue(green1, Signal.HIGH);
					} else {
						System.out.println("Work failed!");
						gpioBridge.writeValue(red0, Signal.HIGH);
					}

				} catch (InterruptedException e) {
					throw new RuntimeException("Failed to do work.", e);
				}
			}
		}, "work");
		workThread.start();
	}

	private static void stopWork(Gpio btnPressed, GpioBridge gpioBridge) {
		workBtnPressed = btnPressed;
		doWork = false;
		try {
			workThread.join(2000l);
		} catch (InterruptedException e) {
			throw new RuntimeException("Was interrupted while stopping work!", e);
		}
	}
}
