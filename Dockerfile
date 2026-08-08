FROM eclipse-temurin:21-jdk
COPY target/booking-service.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
