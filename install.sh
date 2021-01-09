#!/usr/bin/env bash
#
# Copyright (c) 2021. Benjamín Antonio Velasco Guzmán
# Author: Benjamín Antonio Velasco Guzmán <bg@benjaminguzman.dev>
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
#

echo -e "\033[46;97m---------------------------------\033[0m"
echo -e "\033[95;1m       SpineWare installer\033[0m"
echo -e "\033[46;97m---------------------------------\033[0m"

function ask_input() {
	# First param: text to show to the user
	# Second param: default value
	read -r -p "$1 [$2]: " response
	response=${response:-"$2"}
	echo "$response"
}

function check_dir() {
	if [ ! -d "$1" ]; then
		echo "$1 does not exists, trying to create it"
		mkdir -p "$1"
		return $? # return exit status code from mkdir
	fi

	return 0
}

# ask and check the installation dir

installation_dir=$(ask_input "Installation directory (enter preferably a directory belonging to the PATH variable)" "$HOME/bin")

check_dir "$installation_dir"
check_dir_res=$?

if [ $check_dir_res != 0 ]; then
	echo -e "\033[91;1mSomething bad happened, check the $installation_dir directory exists & you have write permissions\033[0m"
	exit
fi

# Compile and copy built jar in installation dir

if [ ! -d "./target" ]; then
	echo "target directory (with the built jar) was not found, running maven to compile from source..."
	mvn clean package
fi

jar_filename=$(find target -type f -name "SpineWareV*.jar" -exec basename {} \;) # find the name of the jar

echo "Copying jar file to installation directory ($installation_dir)..."
cp "./target/$jar_filename" "$installation_dir"

# Add to the path the installation dir
case :$PATH: in
*:$installation_dir:*)
	echo -e "\033[93m$installation_dir IS part of the PATH\033[0m"
	;;
*)
	echo -e "\033[93;1mAdding $installation_dir to the END of the path\033[0m"
	PATH=$PATH:"$installation_dir"
	;;
esac

create_sys_service=$(ask_input "Do you want to create a system service to run SpineWare at startup (Y/n)" "n")

if [ "$create_sys_service" != "Y" ]; then
	echo "Not creating the system service, quitting now"
	exit
fi

# Create the system service

systemd_service_dir="/usr/lib/systemd/system"

run_with_sudo=false
# check if user has rights to write in the systemd dir
if [ ! -w "$systemd_service_dir" ]; then
	echo -e "\033[91;1mYou don't have rights to install the system service in the $systemd_service_dir\033[0m"
	echo -e "\033[91;1mMaybe you wanna try again with \"sudo\"? (just remember, sudo doesn't always fix everything, use it with care)\033[0m"
	echo -e "\033[93;1mThe commands that need writing privileges will be executed with \"sudo\" ok? (press Ctrl-C if you're not ok)\033[0m"
	read -r
	run_with_sudo=true
fi

echo "Creating .service config file..."
java_location=$(which java)                                   # find the java command location

cmd="$java_location -jar \"$installation_dir/$jar_filename\"" # construct the complete command
cmd=$(echo "$cmd" | sed 's/\//\\\//g')                        # escape forward slashes

sed -i "s/^ExecStart=.*/ExecStart=$cmd/" installation/spineware.service
sed -i "s/^User=.*/User=$(whoami | sed 's/\//\\\//g')/" installation/spineware.service

# copy the service file
echo "Installing the service..."
if [ "$run_with_sudo" == "true" ]; then
	sudo cp installation/spineware.service "$systemd_service_dir"
else
	cp installation/spineware.service "$systemd_service_dir"
fi

# set the right permissions
if [ "$run_with_sudo" == "true" ]; then
	sudo chmod 0640 "$systemd_service_dir/spineware.service"
else
	chmod 0640 "$systemd_service_dir/spineware.service"
fi

echo "Starting the service..."
if [ "$run_with_sudo" == "true" ]; then
	sudo systemctl start spineware
	sudo systemctl status spineware
else
	systemctl start spineware
	systemctl status spineware
fi

sudo -K # remove the password from the cache

echo "Done."
