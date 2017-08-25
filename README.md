# log4j2-extended-jsonlayout-spring-auth
Extended JSONLayout for Log4j2 that includes Spring Authentication details


[![Build Status](https://travis-ci.org/savantly-net/log4j2-extended-jsonlayout-spring-auth.svg?branch=master)](https://travis-ci.org/savantly-net/log4j2-extended-jsonlayout-spring-auth)  



### Include the artifact  
```
	<dependency>
		<groupId>net.savantly.log4j2</groupId>
		<artifactId>extended-jsonlayout-spring-auth</artifactId>
		<version>1.0.0-RELEASE</version>
	</dependency>
```

### Example Usage  

Inside your log4j2 configuration, you can configure the new pattern "ExtendedJsonLayout"

``` 
	<Appenders>
		<Console name="STDOUT" target="SYSTEM_OUT">
			<ExtendedJsonLayout complete="false" properties="true"
				 jsonAdapterClassName="net.savantly.log4j.layout.SpringAuthExtender"/>
		</Console>
	</Appenders>
	
```  


### Example output

```
{
  "timeMillis" : 1503390773796,
  "thread" : "main",
  "level" : "INFO",
  "loggerName" : "org.springframework.data.repository.config.RepositoryConfigurationDelegate",
  "message" : "Multiple Spring Data modules found, entering strict repository configuration mode!",
  "endOfBatch" : false,
  "loggerFqcn" : "org.apache.logging.slf4j.Log4jLogger",
  "contextMap" : { },
  "threadId" : 1,
  "threadPriority" : 5,
  "authentication" {username: "testUser"}
}
```  

*Note -
You can subclass the [SpringAuthExtender](./src/main/java/net/savantly/log4j/layout/SpringAuthExtender.java) to add your own "authentication" objects to the log message.