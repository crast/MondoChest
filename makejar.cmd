del target\*.jar
call mvn package
del target\*shaded.jar
move target\MondoChest*.jar .\MondoChest.jar

