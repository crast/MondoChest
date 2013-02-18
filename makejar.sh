#!/bin/bash
cd bin
cp ../plugin.yml ../config.yml ../config-example.yml .
#mkdir -p res
#cp ../config.yml res/config-t.yml
rsync -av \
   --exclude "tests" \
   --exclude "us/crast/quadtree" \
   --exclude "us/crast/bukkitstuff" \
   --exclude "SecureKey.class" \
   ../../CrastBukkitUtils/bin/* .
jar cvf ../MondoChest.jar .
