FROM maven:3.5.0-alpine

RUN apk update

RUN apk add git

CMD git clone https://github.com/EduJGURJC/springbootdemotest.git; cd springbootdemotest; mvn clean test;
