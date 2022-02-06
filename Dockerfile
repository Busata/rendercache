FROM openjdk:17
ADD target/*.jar rendercache
ENTRYPOINT ["java","-jar", "rendercache"]
EXPOSE 8080