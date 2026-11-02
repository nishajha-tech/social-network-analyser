FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# Copy all project files
COPY . /app/

# Compile Java source code into bin directory
RUN mkdir -p bin && javac -d bin src/backend/*.java

# Default port (Render injects $PORT)
ENV PORT=8080
EXPOSE 8080

# Run Main
CMD ["java", "-cp", "bin", "backend.Main"]
