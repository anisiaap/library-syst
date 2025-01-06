#Use an OpenJDK image as the base
FROM openjdk:17-jdk-slim
WORKDIR /app

#Copy the JAR file into the container
COPY target/library-1.0-SNAPSHOT.jar backend.jar

#Expose the port used by the Spring Boot app
EXPOSE 8080

#Run the Spring Boot app
CMD ["java", "-jar", "backend.jar"]
