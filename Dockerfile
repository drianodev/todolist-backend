# Estágio de build
FROM ubuntu:latest AS build

# Atualize os repositórios e instale o JDK
RUN apt-get update && apt-get install -y openjdk-17-jdk

# Diretório de trabalho para a construção
WORKDIR /app

# Copie o código-fonte para o contêiner
COPY . .

# Compile o projeto com Maven
RUN apt-get install -y maven && mvn clean install

# Estágio final
FROM openjdk:17-jdk-slim

# Copie o arquivo JAR construído no estágio anterior
COPY --from=build /app/target/todolist-1.0.0.jar /app.jar

# Exponha a porta em que a aplicação será executada
EXPOSE 8080

# Comando de entrada para executar a aplicação
ENTRYPOINT ["java", "-jar", "/app.jar"]
