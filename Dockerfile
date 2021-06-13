FROM adoptopenjdk:16-jre
COPY server/target/modules/ /app/modules/
COPY server/target/classes /app/server/
COPY server/target/web /app/web
#COPY server/target/server-1.0-SNAPSHOT.jar  /app/server.jar
#CMD [ "java", "-cp", "/app/core:/app/modules/utils-1.0-SNAPSHOT.jar:/app/modules/gson-2.8.7.jar", "x.snowroller.Main" ]
ENTRYPOINT [ "java", "--module-path", "/app/server:/app/modules", "-m" ,"server/x.snowroller.MainClass" ]