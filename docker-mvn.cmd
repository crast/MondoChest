docker run -it --rm --name my-maven-project -v %cd%:/usr/src/mymaven -v %USERPROFILE%\AppData\Local\maven:/root/.m2 -w /usr/src/mymaven maven:3.6.0-jdk-12 %*