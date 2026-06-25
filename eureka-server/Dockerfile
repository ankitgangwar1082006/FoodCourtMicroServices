# Step 1: Java 21 ka lightweight mahol taiyar karna
FROM eclipse-temurin:21-jdk-alpine

# Step 2: Temporary files ke liye ek volume create karna
VOLUME /tmp

# Step 3: Container ke andar ki dukan (working directory)
WORKDIR /app

# Step 4: Apni build ki hui jar file ko andar copy karna
COPY target/eureka-server-0.0.1.jar app.jar

# Step 5: Bahar se traffic aane ke liye port kholna
EXPOSE 8080

# Step 6: Container start hote hi app chala dena
ENTRYPOINT ["java", "-jar", "app.jar"]
