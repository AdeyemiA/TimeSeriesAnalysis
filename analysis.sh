#!/bin/bash
#
#
# Shell script to run the Time Series Analysis tool
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
file3=Out*
tmp_dir="/temp/"

# check if a directory
if [[ ! -d "$tmp_dir" ]]; then
	cd ~ && tmp_dir=`pwd`
	if [[ -d "$tmp_dir/big_data_tmp/" ]]; then
		rm -rf "$tmp_dir/big_data_tmp/"
	fi

	mkdir "$tmp_dir/big_data_tmp/" && tmp_dir="$tmp_dir/big_data_tmp/"
	#cd $START_PATH/
fi
cd "$tmp_dir" && rm -f $file1 && rm -fr $file2 && rm -fr $file3

# curl the data from the website
echo "Downloading the data file ... "
curl https://archive.ics.uci.edu/ml/machine-learning-databases/00235/household_power_consumption.zip -o "$tmp_dir/"household_data.zip

# check if download finished
if [[ "$?" != 0 ]]; then
	echo "An error occured while downloading the data set"
	exit 1
fi

# expand the zip file
cd "$tmp_dir" && unzip household_data.zip

# will add functionality to run on hadoop if time permits
run_local=""


# define the variables that can be predicted
variable=( Global_Active_Power Global_Reactive_Power Voltage Global_Intensity Sub_Metering_1 Sub_Metering_2 Sub_Metering_3 )
echo "Please select a number to choose the variable to predict"
echo "0. ${variable[0]}"
echo "1. ${variable[1]}"
echo "2. ${variable[2]}"
echo "3. ${variable[3]}"
echo "4. ${variable[4]}"
echo "5. ${variable[5]}"
echo "6. ${variable[6]}"
read -p "Enter your choice here : " predchoice

#if [[ "$predchoice" = 0 ]] || [[ "$predchoice" = 1 ]] || [[ "$predchoice" = 2 ]] || [[ "$predchoice" = 3 ]] || [[ "$predchoice" = 4 ]] || [[ "$predchoice" = 5 ]] || [[ "$predchoice" = 6 ]]; then
if [[ "$predchoice" =~ ^[0-9]$ ]] && [[ "$predchoice" -le 6 ]]; then
	echo ""
	else
	echo "Please make a valid choice"
	exit 1
fi

echo ""
echo "Enter the number of years to predict the variable (Number has to be an Integer)"
read -p "Enter your choice here : " numyears
echo ""

if [[ "$numyears" =~ ^[0-9]+$ ]] && [[ "$numyears" -ge 1 ]]; then
	echo
	else
	echo "Please make a valid choice, with 1 being the minimum"
	exit 1
fi

# check if to run locally or hadoop cluster
if [[ "$proc1" != "" ]] && [[ "$proc2" != "" ]] && [[ "$run_local" != "" ]]; then
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

	# run on hadoop
	# pass "mapreduce to the embedded script in Pigserver()"
else	
	cd $START_PATH/
	#tmp_dir="/temp/"
	#if[[ ! -d "$tmp_dir"]]
	#	cd ~ && tmp_dir=`pwd` && mkdir "$tmp_dir/tmp/" && tmp_dir="$tmp_dir/tmp/"
	#	cd $START_PATH/
	#fi
	export CLASSPATH="$CLASSPATH:$START_PATH/lib:$START_PATH/resources:$START_PATH/target"
	echo "$CLASSPATH"
	$JAVA_HOME/bin/java -jar target/TimeSeriesAnalysis* "$tmp_dir" "$predchoice" "$numyears"
fi

