FROM openjdk:25-jdk

WORKDIR /app

COPY . .

RUN javac org2/*.java
CMD ["java", "org2.Main"]