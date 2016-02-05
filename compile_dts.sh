#!/bin/bash

SLOTS=/sys/devices/platform/bone_capemgr/slots
PINS=/sys/kernel/debug/pinctrl/44e10800.pinmux/pins

# http://kilobaser.com/blog/2014-07-28-beaglebone-black-devicetreeoverlay-generator
# https://github.com/jadonk/validation-scripts/tree/master/test-capemgr

echo "Compiling pinctrl-eitchnet..."
dtc -O dtb -o pinctrl-eitchnet-00A0.dtbo -b 0 -@ pinctrl-eitchnet.dts

echo
echo "Install with:"
echo "$ sudo cp pinctrl-eitchnet-00A0.dtbo /lib/firmware/"
echo "$ echo pinctrl-eitchnet | sudo tee $SLOTS"

echo
echo "Remove with:"
echo "$ cat $SLOTS | grep pinctrl-eitchnet"
echo "15: P-O-L-   0 Override Board Name,00A0,Override Manuf,pinctrl-eitchnet"
echo "$ echo -15 | sudo tee $SLOTS"

exit 0