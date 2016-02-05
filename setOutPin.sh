#!/bin/bash

set -e

GPIO_DIR="/sys/class/gpio"

if [ ! -d "${GPIO_DIR}" ] ; then
  echo -e "ERROR: Missing GPIO directory ${GPIO_DIR}"
fi

cd "${GPIO_DIR}"

if [ "$#" != 2 ] ; then
  echo "Usage: $0 <gpio_no> <value>"
  exit 1
fi

gpio_no="$1"
gpio_name="gpio${gpio_no}"
value="$2"

direction="$(cat ${gpio_name}/direction)"
if [ "${direction}" != "out" ] ; then
  echo "Current direction is not in, can't set output pin!"
fi

echo "Setting output pin ${gpio_name} to value ${value}"
echo ${value} | sudo tee "${GPIO_DIR}/${gpio_name}/value"

exit 0
