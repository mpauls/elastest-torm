FROM maven:3.5.0-alpine

RUN apk update

RUN apk add git

RUN apk add --update \
    python \
    python-dev \
    py-pip \
    build-base \
    procps \
  && pip install virtualenv \
  && rm -rf /var/cache/apk/*

CMD git clone https://github.com/EduJGURJC/testcontainers-java-examples.git; cd testcontainers-java-examples; mvn clean install;
