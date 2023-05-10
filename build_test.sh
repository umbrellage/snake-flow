#!/usr/bin/env bash
#export JAVA_HOME=/usr/local/jdk1.8.0_65/
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

output="$(pwd)/boot/elink-juliet-flow"
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
## 根据自身情况，修改路径
cp elink-item-center-domain/target/elink-item-center.jar ${output}/elink-item-center.jar
cp startup.sh ${output}
cp shutdown.sh ${output}
chmod 744 ${output}/startup.sh
chmod 744 ${output}/shutdown.sh
if [ $? -ne 0 ]; then
    echo ""
    echo "***********************************************************"
    echo "[INFO]移动打包文件失败"
    echo "***********************************************************"
    exit 1
fi
echo ""
echo "***********************************************************"
echo "[INFO]Maven打包成功!!!!"
echo "***********************************************************"