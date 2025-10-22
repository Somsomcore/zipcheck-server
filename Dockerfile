# 기본 이미지로 가벼운 Java 17 런타임 사용
FROM openjdk:17-jdk-slim

# 컨테이너 안에서 작업할 폴더 설정
WORKDIR /app

COPY zipcheck.jar app.jar

# 컨테이너가 시작될 때 실행할 명령어 지정
ENTRYPOINT ["java","-jar","/app/app.jar"]

# 앱이 사용하는 포트 노출 (Spring Boot 기본값 8080)
EXPOSE 8080