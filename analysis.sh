#!/bin/sh
#
#
#
#
#
#
#
#
#
#
#
#
#
#
# Downloads the household data from https://archive.ics.uci.edu/ml/machine-learning-databases/00235/household_power_consumption.zip
# if not specified on the command line and runs the example for Time Series Analysis

function usage() {
	echo "";
	echo "./analysis.sh <path to the file input file>";
	echo "";
	echo "";
}

if [[ "$1" = "--help" ]] || [[ "$1" = "--?"]]; then
	#statements
	usage()
	exit 0
fi

if [[ "$HADOOP_HOME" != "" ]]; then
	#statements
	HADOOP="$HADOOP_HOME/bin/hdfs"
	if [[ ! -e $HADOOP ]]; then
		#statements
		echo "Cannot find the hdfs shell script in $HADOOP"
		exit 1
	fi
fi

WORK_DIR=hdfs://user/${USER}
echo "$WORK_DIR"


