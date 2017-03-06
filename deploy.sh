#!/bin/bash

# Your mvn settings.xml:
# 
# <settings>
#   <servers>
#     <server>
#       <id>oss.sonatype.org</id>
#       <username>...</username>
#       <password>...</password>
#     </server>
#   </servers>
# </settings>
# 

# usage
if [ $# != 1 ] ; then
  echo -e "Usage: ${0} <version>"
  exit 1
fi


# Confirm
releaseVersion=${1}
echo -e "INFO: Do you want to deploy version ${releaseVersion} to Maven Central? y/n"
read a
if [[ "${a}" != "y" && "${a}" != "Y" ]] ; then
  exit 0;
fi


# Verify tag
echo -e "\nINFO: Verifying tag ${releaseVersion}\n\n"
if ! git tag --verify ${releaseVersion} ; then
  echo -e "ERROR: Failed to verify tag ${releaseVersion}"
  exit 1
fi
echo
echo


# Checkout tag
echo -e "\nINFO: Checking out tag ${releaseVersion}"
if ! git checkout ${releaseVersion} ; then
  echo -e "ERROR: Failed to checkout tag ${releaseVersion}"
  exit 1
fi


# Build and deploy
echo -e "\nINFO: Building and deploying to Maven Central..."
if ! mvn clean deploy -DskipTests -Pdeploy > /dev/null ; then
  echo -e "ERROR: Failed to build and deploy to Maven Central!"
  exit 1
fi


echo -e "\nINFO: Release ${releaseVersion} deployed to Maven Central."
echo -e "INFO: Remember, it can take up to 10minutes to be available, and 2 hours for all synching."

echo -e "\nRepository is at: https://oss.sonatype.org/service/local/repositories/releases/content/ch/eitchnet/"

exit 0
