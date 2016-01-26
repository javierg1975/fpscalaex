set SCRIPT_DIR=%~dp0
java -Xmx1024M -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005 -XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled -jar "%SCRIPT_DIR%sbt-launch.jar" %*