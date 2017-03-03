package ch.eitchnet.beaglebone;

/**
 * Created by bds on 03/03/17.
 */
public interface GpioBridge {
    /**
     * <p>
     * Public API method to write the given {@link Signal} on the given {@link Gpio}'s pin.
     * </p>
     *
     * @param gpio
     *            the {@link Gpio} to which the {@link Signal} should be written
     * @param signal
     *            the {@link Signal} to write to the given {@link Gpio}
     *
     * @throws GpioException
     *             if the direction of the {@link Gpio} is not {@link Direction#OUT}, or if something goes wrong while
     *             writing to the file
     */
    void writeValue(Gpio gpio, Signal signal) throws GpioException;

    /**
     * <p>
     * Public API method to read the current {@link Signal} on the given {@link Gpio}'s pin.
     * </p>
     *
     * @param gpio
     *            the {@link Gpio} for which the {@link Signal} should be read
     *
     * @return The {@link Gpio}'s current signal
     *
     * @throws GpioException
     *             if the direction of the {@link Gpio} is not {@link Direction#IN}, or if something goes wrong while
     *             reading from the file
     */
    Signal readValue(Gpio gpio) throws GpioException;

    /**
     * Starts the {@link GpioBridge}'s signal observing {@link Thread}. If no observers are registered with the
     * {@link #register(Gpio, GpioSignalListener)}-method, then this method needs not to be called.
     */
    void start();

    /**
     * Stops observing any pins and stops the {@link Thread}
     */
    void stop();

    /**
     * <p>
     * Returns the {@link Gpio} for the given {@link Direction}.
     * </p>
     *
     * <p>
     * <b>Note:</b> This method can not be called multiple times with different {@link Direction}s. The
     * {@link GpioBridge} does not handle pins that are simultaneously input and output as this is not supported by the
     * Linux kernel.
     * </p>
     *
     * @param pin
     *            The {@link Pin} for which the {@link Gpio} in the given {@link Direction} is to be returned
     * @param direction
     *            the {@link Direction} for which this {@link Gpio} is to be returned
     *
     * @return The {@link Gpio} with the configured {@link Direction}
     *
     * @throws GpioException
     *             If the given {@link Direction} does not match the kernel's configured direction, or if the file
     *             permissions are not set so that the Java process can access the file (read access for input pin,
     *             write access for output pin.
     */
    Gpio getGpio(Pin pin, Direction direction) throws GpioException;

    /**
     * Registers the given {@link GpioSignalListener} for changes to {@link Signal}s on the given {@link Gpio}
     *
     * @param gpio
     *            the {@link Gpio} being observed
     * @param listener
     *            the {@link GpioSignalListener} to be notified on changes on the {@link Gpio}'s {@link Signal}
     *
     * @throws GpioException
     *             if the {@link Direction} of the {@link Gpio} is not {@link Direction#IN}
     */
    void register(Gpio gpio, GpioSignalListener listener) throws GpioException;

    /**
     * Unregisters a {@link GpioSignalListener} from changes to the given {@link Gpio}
     *
     * @param gpio
     *            the {@link Gpio} for which the listener is to be removed
     * @param listener
     *            the {@link GpioSignalListener} to be removed from changes to the given {@link Gpio}
     *
     * @return true if the listener was unregistered, false if not
     */
    boolean unregister(GpioBridgeTest gpio, GpioSignalListener listener);
}
