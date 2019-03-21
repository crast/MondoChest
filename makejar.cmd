del target\*.jar
call docker-mvn mvn package
del target\*shaded.jar
move target\MondoChest*.jar .\MondoChest.jar

