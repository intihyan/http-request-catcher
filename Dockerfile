FROM amd64/openjdk:11
RUN mkdir /app

WORKDIR /app
COPY build/distributions/echo-shadow*.tar /tmp/
RUN tar xf /tmp/*.tar

CMD ["echo-shadow-1.0.0-SNAPSHOT/bin/echo"]

