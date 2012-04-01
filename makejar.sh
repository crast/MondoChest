#!/bin/bash
cd bin
cp ../plugin.yml ../config.yml .
#mkdir -p res
#cp ../config.yml res/config-t.yml
jar cvf ../MondoChest.jar .
