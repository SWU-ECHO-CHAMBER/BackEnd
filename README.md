# ECHO-CHAMBER :: SERVER

## Built With

<div>
  <img src="https://img.shields.io/badge/springboot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white">
  <img src="https://img.shields.io/badge/redis-%23DD0031.svg?&style=for-the-badge&logo=redis&logoColor=white">
  <img src="https://img.shields.io/badge/mysql-4479A1?style=for-the-badge&logo=mysql&logoColor=white">
  <img src="https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=JSON%20web%20tokens&logoColor=white">
</div>

<br>

## Getting Started

### 1. Get API Key and Client ID <br>

애플리케이션에 필요한 API Key 생성

- [News API] https://newsapi.org/
- [Google OAUTH2] https://console.cloud.google.com/

<br>

### 2. Download repository <br>

파일 다운로드

```bash
git clone https://github.com/SWU-ECHO-CHAMBER/BackEnd.git
```

<br>

### 3. Create `application.yml` at `/BackEnd/echo/src/main/resources` <br>

`/BackEnd/echo/src/main/resources`에 `application.yml` 파일 생성

```yml
# PORT
server:
  port: 8080

# JPA
spring:
  devtools:
    livereload:
      enabled: true
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: ${SPRING_DATASOURCE_URL}
    username: root
    password: ${SPRING_DATASOURCE_PASSWORD}
  jpa:
    database: mysql
    database-platform: org.hibernate.dialect.MySQL8Dialect
    show-sql: true
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        format_sql: true
  cache:
    type: redis
    redis:
      host: localhost
      port: 6379
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB
```

<br>

### 4. Create `application.properties` at `/BackEnd/echo/src/main/resources` <br>

`/BackEnd/echo/src/main/resources`에 `application.properties` 파일 생성

```properties
# Database
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/{YOUR_DATABASE_NAME}
SPRING_DATASOURCE_PASSWORD={YOUR_MYSQL_PASSWORD}
PROFILE_DATABASE_URL={PATH_TO_SAVE_IMAGES}

# JWT
JWT_SECRET_KEY=jwt_secret_key_sample

# OAuth
GOOGLE_OAUTH_CLIENT_ID={YOUR_GOOGLE_OAUTH_CLIENT_ID}

# News API
NEWS_API_KEY={YOUR_NEWS_API_KEY}
```

<br>

### 5. Run the application <br>

애플리케이션 실행

<br>

## Contact

Server Developer - nsuy.ch@gmail.com
