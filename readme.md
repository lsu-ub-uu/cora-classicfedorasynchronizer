#Usage Examples

##1.Call with the default properties file
```
/usr/lib/jvm/java-17-openjdk/bin/java -cp cora-classicfedorasynchronizer-1.4.0.jar se.uu.ub.cora.classicfedorasynchronizer.batch.FedoraToDbCatchUpBatch
```

##2.Call with a custom properties file
```
/usr/lib/jvm/java-17-openjdk/bin/java -cp cora-classicfedorasynchronizer-1.4-SNAPSHOT.jar:. se.uu.ub.cora.classicfedorasynchronizer.batch.FedoraToDbCatchUpBatch custom.properties
```

##3.Call with all parameters
```
/usr/lib/jvm/java-17-openjdk/bin/java -cp cora-classicfedorasynchronizer-1.4.0.jar se.uu.ub.cora.classicfedorasynchronizer.batch.FedoraToDbCatchUpBatch jdbc:postgresql://diva-cora-docker-postgresql:5432/diva diva diva http://diva-docker-fedora:8088/fedora/ http://localhost:8182/apptokenverifier/ http://localhost:8082/diva/rest/ coraUser:490742519075086 2e57eb36-55b9-4820-8c44-8271baab4e8e 2000-10-10T10:10:10Z
```


#Example PropertiesFile
```
database.url=someDatabaseUrl
database.user=dbUserName
database.password=dbUserPassword
fedora.baseUrl=someFedoraBaseUrl
cora.apptokenVerifierUrl=someApptokenVerifierUrl
cora.baseUrl=someCoraBaseUrl
cora.userId=someCoraUserId
cora.apptoken=someCoraApptoken
cora.afterTimestamp=2021-10-10T10:10:10Z
```





