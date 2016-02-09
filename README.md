# BeagleBoneBlack Java Pin Bridge
This is a pure Java library to access the GPIO pins of a BeagleBoneBlack.

The main component is the `GpioBridge` which is a singleton. A client will retrieve the instance via the `GpioBridge.getInstance()` method and then manipulate the GPIOs.

Features are:
* Reading and writing of pins
* Registering for input pin changes (currently implemented as file-polling)
* Thread-safe
* Simple model for hardware: `Pin`, `Gpio`, `Signal`, `Direction`
* Clear exception handling for user and configuration errors (`GpioException`)

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

