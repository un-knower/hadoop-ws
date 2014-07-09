#!/usr/bin/env bash

# This file contains environment variables required to run Tachyon. Copy it as tachyon-env.sh and
# edit that to configure Tachyon for your site. At a minimum,
# the following variables should be set:
#
# - JAVA_HOME, to point to your JAVA installation
# - TACHYON_MASTER_ADDRESS, to bind the master to a different IP address or hostname
# - TACHYON_UNDERFS_ADDRESS, to set the under filesystem address.
# - TACHYON_WORKER_MEMORY_SIZE, to set how much memory to use (e.g. 1000mb, 2gb) per worker
# - TACHYON_RAM_FOLDER, to set where worker stores in memory data
# - TACHYON_UNDERFS_HDFS_IMPL, to set which HDFS implementation to use (e.g. com.mapr.fs.MapRFileSystem,
#   org.apache.hadoop.hdfs.DistributedFileSystem)

# The following gives an example:

if [[ `uname -a` == Darwin* ]]; then
  # Assuming Mac OS X
  export JAVA_HOME=$(/usr/libexec/java_home)
  export TACHYON_RAM_FOLDER=/Volumes/ramdisk
  export TACHYON_JAVA_OPTS="-Djava.security.krb5.realm= -Djava.security.krb5.kdc="
else
  # Assuming Linux
  if [ -z "$JAVA_HOME" ]; then
    # export JAVA_HOME=/usr/lib/jvm/java-7-oracle
    export JAVA_HOME=/opt/java/jdk1.7.0_55
  fi
  export TACHYON_RAM_FOLDER=/mnt/ramdisk
fi

export JAVA="$JAVA_HOME/bin/java"
export TACHYON_MASTER_ADDRESS=master-hadoop

#export TACHYON_UNDERFS_ADDRESS=hdfs://localhost:9000
# hdfs: 在hdfs上的tachyon存放在/tmp/tachyon;format之后将有data和worker目录.
export TACHYON_UNDERFS_ADDRESS=hdfs://master-hadoop:9000
export TACHYON_MASTER_JOURNAL_HOME=${TACHYON_UNDERFS_ADDRESS}/tachyon/journal/

# local: 注意,需要重置TACHYON_MASTER_JOURNAL_HOME
#export TACHYON_UNDERFS_ADDRESS=$TACHYON_HOME/underfs
#export TACHYON_MASTER_JOURNAL_HOME=$TACHYON_HOME/journal/

export TACHYON_WORKER_MEMORY_SIZE=1GB
export TACHYON_UNDERFS_HDFS_IMPL=org.apache.hadoop.hdfs.DistributedFileSystem

CONF_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

export TACHYON_JAVA_OPTS+="
  -Dlog4j.configuration=file:$CONF_DIR/log4j.properties
  -Dtachyon.debug=false
  -Dtachyon.underfs.address=$TACHYON_UNDERFS_ADDRESS
  -Dtachyon.underfs.hdfs.impl=$TACHYON_UNDERFS_HDFS_IMPL
  -Dtachyon.data.folder=$TACHYON_UNDERFS_ADDRESS/tmp/tachyon/data
  -Dtachyon.workers.folder=$TACHYON_UNDERFS_ADDRESS/tmp/tachyon/workers
  -Dtachyon.worker.memory.size=$TACHYON_WORKER_MEMORY_SIZE
  -Dtachyon.worker.data.folder=$TACHYON_RAM_FOLDER/tachyonworker/
  -Dtachyon.master.worker.timeout.ms=60000
  -Dtachyon.master.hostname=$TACHYON_MASTER_ADDRESS
  -Dtachyon.master.journal.folder=$TACHYON_HOME/journal/
  -Dtachyon.master.pinlist=/pinfiles;/pindata
  -Dorg.apache.jasper.compiler.disablejsr199=true
"

# zookeeper出错, 所以屏蔽掉算了!
#	2014-07-07 15:05:48,928 WARN  zookeeper.ClientCnxn (ClientCnxn.java:run) - Session 0x0 for server null, unexpected error, closing socket connection and attempting reconnect
#	2014-07-07 15:05:49,030 ERROR WORKER_LOGGER (LeaderInquireClient.java:getMasterAddress) - KeeperErrorCode = ConnectionLoss for /leader
#	org.apache.zookeeper.KeeperException$ConnectionLossException: KeeperErrorCode = ConnectionLoss for /leader
# FaultTolerant: TACHYON_JAVA_OPTS增加了tachyon.usezookeeper和tachyon.zookeeper.address
#export TACHYON_JAVA_OPTS+="
#  -Dtachyon.usezookeeper=true
#  -Dtachyon.zookeeper.address=master-hadoop:2181
#"

# 在master上,还要重新配置-Dtachyon.master.journal.folder=hdfs://[namenodeserver]:[namenodeport]/tachyon/journal
# 会覆盖之前定义的-Dtachyon.master.journal.folder吗?
export TACHYON_JAVA_OPTS+="
  -Dtachyon.master.journal.folder=${TACHYON_MASTER_JOURNAL_HOME}
"

