#!/bin/bash

set -e

echo -e "Exporting Pins to userspace..."

if [ "$(whoami)" != "root" ] ; then
  echo "Please run as root!"
  exit 1
fi

GPIO_DIR="/sys/class/gpio"
USER="ubuntu"

if [ ! -d "${GPIO_DIR}" ] ; then
  echo -e "ERROR: Missing GPIO directory ${GPIO_DIR}"
fi

cd "${GPIO_DIR}"

function export_pin() {
  direction="$1"
  gpio_no="$2"
  gpio_name="gpio${gpio_no}"
  
  echo "Setting direction of ${gpio_name} to ${direction}..."
  
  if [ ! -d "${gpio_name}" ] ; then
    echo "${gpio_no}" > export
  else
    echo "GPIO ${gpio_no} was already exported, only verifying direction..."
  fi
  
  echo "${direction}" > "${gpio_name}/direction"
  
  if [ "${direction}" == "in" ] ; then
    echo "Current value of ${gpio_name} is $(cat ${gpio_name}/value)"
  else
    echo "Setting value of ${gpio_name} to 0"
  fi
  
  chown ${USER} "${gpio_name}/value"
}

##
## IN Pins
##

echo
echo "Setting input pins..."

# P8.7
export_pin in 66
# P8.8
export_pin in 67
# P8.9
export_pin in 69

##
## Out pins
##

echo
echo "Setting output pins..."

# P8.10
export_pin out 68
# P8.11
export_pin out 45
# P8.12
export_pin out 44
# P8.14
export_pin out 26
# P8.15
export_pin out 47
# P8.16
export_pin out 46

echo -e "Done."

exit 0