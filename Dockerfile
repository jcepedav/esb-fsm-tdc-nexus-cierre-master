# Utilizamos una imagen base de Java
FROM maven:3.8.4-openjdk-8-slim AS build

# Establecemos el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copiamos el archivo pom.xml para descargar las dependencias
COPY pom.xml .

# Descargamos las dependencias del proyecto
RUN mvn dependency:go-offline

# Copiamos el resto del proyecto
COPY src ./src

# Compilamos el proyecto y generamos el archivo JAR
RUN mvn package -DskipTests

# Ahora, utilizamos una nueva imagen base para crear la imagen final de Docker
FROM openjdk:8u162-slim

ENV APP_DIR="/app"
RUN mkdir ${APP_DIR}

WORKDIR ${APP_DIR}

#Copiamos el script de inicio del springboot generado
COPY run-java-springboot.sh /run.sh

RUN chmod +x /run.sh

#Copiamos el jar previamente generado utilizando la imagen anterior
COPY --from=build /app/target/*.jar app.jar
COPY application.properties .
#Creamos la carpeta de logs
RUN mkdir logs

ENTRYPOINT ["/run.sh"]


