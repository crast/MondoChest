#!/bin/bash
cd bin
cp ../plugin.yml ../config.yml .
#mkdir -p res
#cp ../config.yml res/config-t.yml
rsync -av \
   --exclude "us/crast/datastructures/builders" \
   --exclude "us/crast/bukkitstuff" \
   ../../CrastBukkitUtils/bin/* .
jar cvf ../MondoChest.jar .
