#!/usr/bin/env bash
export JAVA_HOME=/usr/local/jdk1.8.0_65/
#获取output
#if [ -n "$1" ]; then
#    output=$1
#else
#    output="output"
#fi
#
#
#if [ "${output:0:1}" != "/" ]; then
#    output="`pwd`/$output"
#    mkdir -p $output
#fi

output="$(pwd)/output"
mkdir -p $output

echo "++++++++++++++++++++++++++++++++++++++++++++++++++++++++++"
echo "[INFO]开始更新代码......"
echo "++++++++++++++++++++++++++++++++++++++++++++++++++++++++++"

git pull

echo "++++++++++++++++++++++++++++++++++++++++++++++++++++++++++"
echo "[INFO]开始Maven打包......"
echo "++++++++++++++++++++++++++++++++++++++++++++++++++++++++++"


mvn  clean package  -Dmaven.test.skip=true -U

if [ $? -ne 0 ]; then
    echo ""
    echo "***********************************************************"
    echo "[INFO]Maven打包失败"
    echo "***********************************************************"
    exit 1
fi
### 根据自身情况，修改路径
#cp shield-ui/target/shield-ui.jar ${output}/shield-ui.jar.build
#cp cleanLog.sh ${output}
#cp control.sh ${output}
#cp Dockerfile ${output}
#chmod 744 ${output}/control.sh
#if [ $? -ne 0 ]; then
#    echo ""
#    echo "***********************************************************"
#    echo "[INFO]移动打包文件失败"
#    echo "***********************************************************"
#    exit 1
#fi
echo ""
echo "***********************************************************"
echo "[INFO]Maven打包成功!!!!"
echo "***********************************************************"