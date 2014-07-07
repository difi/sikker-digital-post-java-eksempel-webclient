@echo off
set MAVEN_OPTS=-Xmx512m -XX:MaxNewSize=128m -XX:MaxPermSize=128m
mvn clean install tomcat7:run