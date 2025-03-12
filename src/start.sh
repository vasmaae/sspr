#!/bin/bash

if [ -z "$1" ]; then
  echo "Usage: $0 MainClassName [additional .java files]"
  exit 1
fi

MAIN_CLASS="$1"
ADDITIONAL_FILES="${@:2}"

echo "Компиляция файлов..."
javac -cp .:$MPJ_HOME/lib/mpj.jar "$MAIN_CLASS.java" $ADDITIONAL_FILES

if [ $? -ne 0 ]; then
  echo "Компиляция не удалась!"
  exit 1
fi

mpjboot machines

CLASS_FILES=$(find . -name "*.class" -type f)

echo "Копирование файлов на удаленные машины..."
for CLASS_FILE in $CLASS_FILES; do
  scp "$CLASS_FILE" root@192.168.1.101:/root/
  scp "$CLASS_FILE" root@192.168.1.102:/root/
done

echo "Запуск приложения..."
mpjrun.sh -np 2 -dev niodev -wdir /root/ "$MAIN_CLASS" 10000 10000 2 2

mpjhalt machines

