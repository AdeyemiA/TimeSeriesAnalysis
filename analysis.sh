#!/bin/bash
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

function usage {
	echo "Usage: "
	echo $1
	echo ""
}

if [[ -z $JAVA_HOME ]];then
	echo "JAVA_HOME is not set"
	exit 1
fi

if [[ "$1" = "--help" ]] || [[ "$1" = "--?" ]]; then
	#statements
	usage "'./analysis.sh' <path to the file input file>"
	exit 0
fi

# check if HADOOP_HOME env variable is set
if [[ "$HADOOP_HOME" != "" ]]; then
	#statements
	HADOOP="$HADOOP_HOME/bin/hdfs"
	if [[ ! -e $HADOOP ]]; then
		#statements
		echo "Cannot find the hdfs shell script in $HADOOP"
		exit 1
	fi
fi

# check if the NameNode and DataNode are running
proc1=`jps | grep "NameNode"`
proc2=`jps | grep "DataNode"`

# keep a record of dir we reside in
START_PATH=`pwd`

# cleaning the temp directory
file1=household_data.zip
file2=household_power_consumption.txt
cd /tmp/ && rm -f $file1 && rm -fr $file2

# curl the data from the website
echo "Downloading the data file ... "
curl https://archive.ics.uci.edu/ml/machine-learning-databases/00235/household_power_consumption.zip -o /tmp/household_data.zip

# expand the zip file
cd /tmp && unzip household_data.zip

# check if to run locally or hadoop cluster
if [[ "$proc1" != "" ]] && [[ "$proc2" != "" ]]; then
	# found hdfs is running and create files in filesystem
	echo "Found NameNode and DataNode"

	temp="/user/${USER}"

	#check if the directory exists in hdfs
	if  [[ ! "$HADOOP dfs -test -e $temp" ]]; then
		#statements
		echo "$temp does not exist"
		$HADOOP dfs rm -r "/user/${USER}/tmp"
		$HADOOP dfs -mkdir "/user/${USER}"
		$HADOOP dfs -mkdir "user/${USER}/tmp"
	fi

	WORK_DIR="/user/${USER}/tmp"
	echo $WORK_DIR

	# cleaning the work directory
	echo "cleaning the work directory ... "
	$HADOOP dfs -rm -r $WORK_DIR/household_power_consumption.txt

	echo "Copying to the hadoop cluster ..."
	$HADOOP dfs -copyFromLocal /tmp/household_power_consumption.txt $WORK_DIR/household_power_consumption.txt
else	
	cd $START_PATH/
	tmp_dir="/tmp/"
	export CLASSPATH="$CLASSPATH:$START_PATH/lib:$START_PATH/resources:$START_PATH/target"
	echo "$CLASSPATH"
	$JAVA_HOME/bin/java -Dlog4j.configurationFile=./log/log4j2.xml -jar target/TimeSeriesAnalysis* "$tmp_dir"
fi

