
# Integracion esb-fsm-tdc-nexus-cierre


* SpringBoot
* Apache CXF par la publicación de APIs Rest
* mapstruct
* camel-bean-validator
* Ejemplo de invocacion hacia un endpoint soap de Geocall , cuyo resultado es mapeado a modelo de objetos del negocio utilizando mapstruct.


```sh

server.port=8180
server.contextPath=/
cxf.path=/service
edenor.integration.name=esb-fsm-tdc-nexus-cierre
edenor.integration.type=ONLINE
edenor.source.system=GEOCALL

logging.config=classpath:logback.xml

# the options from org.apache.camel.spring.boot.CamelConfigurationProperties can be configured here
#Todo borrar
camel.springboot.name=eps-rest-services

# lets listen on all ports to ensure we can be invoked from the pod IP
server.address=0.0.0.0
management.address=0.0.0.0

# lets use a different management port in case you need to listen to HTTP requests on 8080
management.server.port=8081
management.endpoint.health.probes.enabled=true
management.endpoints.web.exposure.include=health,info
management.security.enabled=false

# disable all management enpoints except health
endpoints.enabled = true
endpoints.health.enabled = true


#Geocall Api
endpoint.geoCallApi.address=https://edenor-prep.geocall.cloud/edenor/cxf/WorkOrder
endpoint.cancelsuspendWorkOrder.wsdlURL=wsdl/WorkOrder.wsdl

#----SERVICES----#

#SSL
disableHostnameCheck=true
keystoreLocation=src/jks/keystore.jks
keystorePassword=Abcd1234
connectionTimeout=60000


#---- Edenor Key Cloak ----#
edenor.keycloak.enabled=false
edenor.keycloak.connect-timeout = 5000
edenor.keycloak.roles=EAM_FSM

#Localhost Gforrade
edenor.keycloak.realm=Edenor-dev-eam-realm
edenor.keycloak.auth-server-url=http://localhost:8080/auth
#Edenor VM
#edenor.keycloak.realm=edenor-myrealm
#edenor.keycloak.auth-server-url=https://sso-sso-rh.prod-apps.pro.edenor/auth

#----SERVICES----#
edenor.esb.coordenadas = http://servicio-conversion-coordenadas-esb-uat.noprod-apps.pro.edenor/camel/ll84?x=%s&y=%s

#Login Service Geogall
endpoint.login.address=http://localhost:8083/service/token


```


Ejecutar el siguiente comando maven :

```sh

mvn clean compile

```

Ejecutar el siguiente comando maven :

```sh

mvn package

```

Ejecutar el siguiente comando maven :

```sh
java -jar -Dext.properties.file="file:./application.properties" -DLOGGING_LEVEL=INFO target/esb-fsm-tdc-nexus-cierre-1.0.0.jar


```



```sh
