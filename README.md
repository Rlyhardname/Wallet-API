### Technologies  
1. Java SE (Logic/Validations/Exception handling)
2. PostgresDB (Database manager)
3. JDBC (Database Seeding, Persistence, Transactions)
4. Slf4J API + Logback (Logging)
5. Jackson-Databind (JSON serialization/Deserialization for endpoints)
6. Spring-web-started (Endpoints creation/managment)
7. Postman (API manual testing)
8. JBcrypt (BCrypt hashing library)

### My approach

I tried to create everything from scratch, as low level as possible, starting from db schemas, business logic, validations etc.. At some point I became very time constaint, and added spring for server endpoints, and logback for logging since it take way to long
to figure out how to implement endpoints in native java code. 

### Requirements: 

-Maven  
-Java 17 or newer  
-Postman(for using the collections to manually test) 

#### Edit POM.XML properties if needed
<java.version>17</java.version> -> java/spring project version  
<maven.compiler.source>20</maven.compiler.source> - local jdk used


### Set up API for Windows Terminal/PowerShell / Or Graphical user interface(steps are the same):



1. Edit https://github.com/Rlyhardname/Wallet-API/blob/master/src/main/resources/db.properties password, dbUrl and dbName fields.  dbName and dbUrl last part (after /) should be the same. Save the edited file
2. Open Terminal/Powershell in the root directory of the project ../Wallet-API
3. mvn clean compile assembly:single
4. mvn install
5. cd target ( switch to target folder ) 
6. cd . > run.bat ( create empty batch file ) 
7. notepad run.bat ( opens empty batch in graphical interface)
8. Copy paste these two lines in bat file and save

java -jar Wallet-API-1.0-SNAPSHOT-jar-with-dependencies.jar  
pause

8. Start run.bat with double click or ctrl+enter when selected.

9. Use provided postman collections to manually test in root of project or from here
https://github.com/Rlyhardname/Wallet-API/tree/master/postmanCollection

### Testing with postman collections
1. Import collectinons into postman  

#### Option one: Run requests manually as they are, or edit their body in "raw" format to change the requests parameters.  

#### Option two: Run recollection automatically (will work the first time, but register and addWallet will fail on the second run, since with email would already be registered and a wallet with id and currency would already exists).

