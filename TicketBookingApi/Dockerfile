# Build stage
FROM maven:3.6.3-jdk-11 as Builder
# Specify the working dir
WORKDIR /app
COPY ./pom.xml ./
RUN mvn dependency:go-offline
COPY ./ ./
RUN mvn package

# Run stage
FROM openjdk:11.0.7-jre
WORKDIR /app
COPY --from=Builder /app/target/TicketBooking*.jar /app
CMD [ "java","-jar","TicketBooking-0.0.1-SNAPSHOT.jar"]