FROM adoptopenjdk:16-jre
COPY server/target/modules/ /app/modules
COPY server/target/classes /app/server/
COPY server/target/web /web
#CMD [ "java", "-cp", "/app/core:/app/modules/utils-1.0-SNAPSHOT.jar:/app/modules/gson-2.8.7.jar", "x.snowroller.Main" ]
ENTRYPOINT [ "java", "--module-path", "/app/server:/app/modules", "-m" ,"server/MainClass.Main" ]