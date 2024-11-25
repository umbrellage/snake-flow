#! /bin/shell

#======================================================================
# 项目重启shell脚本
# 先调用shutdown.sh停服
# 然后调用startup.sh启动服务
#======================================================================

# 项目名称
APPLICATION="@project.name@"

# 项目启动jar包名称
APPLICATION_JAR="@build.finalName@.jar"

# 停服
echo stop ${APPLICATION} Application...
sh shutdown.sh

sleep 3

# 启动服务
echo start ${APPLICATION} Application...
sh startup.sh $1 $2
