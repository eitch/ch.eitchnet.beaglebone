# BeagleBoneBlack Java Pin Bridge
This is a pure Java library to access the GPIO pins of a BeagleBoneBlack.

The main component is the `GpioBridge` which is a singleton. A client will retrieve the instance via the `GpioBridge.getInstance()` method and then manipulate the GPIOs.

Features are:
* Reading and writing of pins
* Registering for input pin changes (currently implemented as file-polling)
* Thread-safe
* Simple model for hardware: `Pin`, `Gpio`, `Signal`, `Direction`
* Clear exception handling for user and configuration errors (`GpioException`)
* No 3rd party dependencies - plain Java SE

If you have any issues, don't hesitate to add an issue to the GitHub project, write me an e-mail (eitch@eitchnet.ch) or reach me on Twitter!

## Java API
Before trying to use the Java API and wondering why it does not work, be very careful to ensure that the GPIOs are properly exported to userspace by using a device tree overlay and then using the `/sys/class/gpio/export` file to export the GPIO to userspace.

The Java API is simple and requires no configuration files. Everything is done programmitically. The main object is the `GpioBridge` instance which is retrieved as follows:
<pre>
GpioBridge gpioBridge = GpioBridge.getInstance();
</pre>

With a reference to the `GpioBridge`, `GPIO` objects can be retrieved by their `Pin` enum and a `Direction` as follows:
<pre>
GpioBridge gpioBridge = GpioBridge.getInstance();
Gpio pin8_07 = gpioBridge.getGpio(Pin.P8_07, Direction.IN);
</pre>

The `GpioBridge.getGpio()`-method will throw an exception if:
* the requested direction does not correspond to the direction configured in the kernel's exported pin
* the file permissions are not set so that the Java process can access the file (read access for input pin, write access for output pin.

### Reading Input Pins
To read the current signal of a pin use the `GpioBridge.readValue()`-method:
<pre>
GpioBridge gpioBridge = GpioBridge.getInstance();
Gpio pin8_07 = gpioBridge.getGpio(Pin.P8_07, Direction.IN);
Signal currentSignal = gpioBridge.readValue(pin8_07);
System.out.println(pin8_07 + " currently has signal " + currentSignal);
</pre>

### Writing Output Pins
To write the signal of a pin use the `GpioBridge.writeValue()`-method:
<pre>
GpioBridge gpioBridge = GpioBridge.getInstance();
Gpio pin8_08 = gpioBridge.getGpio(Pin.P8_08, Direction.OUT);
gpioBridge.writeValue(pin8_08, Signal.HIGH);
System.out.println("Set signal of " + pin8_08 + " to " + Signal.HIGH);
</pre>

### Observing Input Pins
To be notified of changes to an input GPIO, register a `GpioSignalListener`:
<pre>
GpioBridge gpioBridge = GpioBridge.getInstance();
Gpio pin8_07 = gpioBridge.getGpio(Pin.P8_07, Direction.IN);
gpioBridge.register(pin8_07, gpio -> System.out.println("Signal of "+pin8_07 + " has changed to " + gpio.getSignal()));
</pre>


## Setup BeagleBone
* Copy the scripts and files to the BeagleBone:
<pre>
rsync -a *.sh *.dts ubuntu@beaglebone:.
</pre>
* Configure the GPIOs in the kernel driver by:
** Compile the device tree using: `./compile_dts.sh`
** Enable the device tree overlay at boot by editing `/boot/uEnv.txt`:
<pre>
##Example v4.1.x
#cape_disable=bone_capemgr.disable_partno=
cape_enable=bone_capemgr.enable_partno=pinctrl-eitchnet
</pre>
* Export the GPIO pins to userspace using: `./exportPins.sh`

If you have a LED properly connected, then you can use the script `./setOutPin.sh` to set the state of the pin.

## Build Java Bridge
To build the Java bridge, Apache Maven 3.0 is used. This can be done on normal PC, the resulting JAR can then be copied to the BeagleBone:
<pre>
mvn clean package
rsync target/BeagleBone.jar ubuntu@beaglebone:.
</pre>

## Circuit requirements:
The circuit consists of three buttons (green, blue and red) and 6 LEDs. (2x green, 3x yellow, 1x red).

To use the JavaBridge example unchanged, you need to attach the components as follows:

Buttons (Input):
* P8_07 = green0
* P8_08 = blue0
* P8_09 = red0

LEDs (Output):
* P8_10 = green0
* P8_11 = yellow0
* P8_12 = yellow1
* P8_14 = yellow2
* P8_15 = green1
* P8_16 = red1

How a LED or a button is safely attached to the BeagleBone is explained online:
* Pushbuttons as Inputs: http://www.dummies.com/how-to/content/setting-beaglebone-gpios-as-inputs.html
* LEDs as Outputs: http://www.dummies.com/how-to/content/setting-beaglebone-gpios-as-outputs.html

## Running
Once the JAR is on the BeagleBone, running is as follows:
<pre>
java -jar BeagleBone.jar
</pre>

## Further reading:
* https://www.kernel.org/doc/Documentation/gpio/sysfs.txt
* http://beaglebone.cameon.net/home/using-the-gpios
* https://www.linux.com/learn/tutorials/776799-servo-control-from-the-beaglebone-black
* https://www.youtube.com/watch?v=wui_wU1AeQc
* https://www.youtube.com/watch?v=s9tnTcQlTDY
