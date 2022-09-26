# Banking API
Simple REST API for a banking application. Provides authentication and authorization with JSON Web Token.
Made for learning purposes.
# Getting started
To get started, JDK 17 or newer is required.
1. Clone this repository using
```
git clone https://github.com/yawek9/banking-api.git
```
3. Create MySQL database and setup configuration in `src/main/resources/application.properties`
   and `src/test/resources/application.properties` with your MySQL server settings.
4. To build a fat jar, run
```
mvn clean package
```
2. Run executable jar file by
```
java -jar banking-1.0.jar
```
# Short details
After running application for the first time, Hibernate creates all tables needed for the entities.
Every request to the endpoint other than `/api/auth` is rejected with 401 Unauthorized response status,
since correct token is not attached in the header. To receive access token and refresh token there
is a requirement to create an account by request to `/api/auth/register` and login by `/api/auth/login` then.
Received access token must be added as bearer authorization header to access endpoints defined in
`UserController`, `PaymentController` and `LoanController`. Access token is valid for 30 minutes
since its creation, then new token can be issued using refresh token by request to `/api/auth/refresh-token`.
Example of running API is ready to access at https://api.yawek.xyz/banking.
# Documentation
All REST API endpoints are documented using OpenAPI 3 standard.
Swagger documentation is accessible on `SERVER_NAME:PORT/api/swagger-ui/index.html` by default.