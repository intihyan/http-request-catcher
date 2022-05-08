FROM openjdk:11-jre-slim@sha256:31a5d3fa2942eea891cf954f7d07359e09cf1b1f3d35fb32fedebb1e3399fc9e
RUN mkdir /app

WORKDIR /app
COPY build/distributions/echo-shadow*.tar /tmp/
RUN tar xf /tmp/*.tar

CMD ["echo-shadow-1.0.0-SNAPSHOT/bin/echo"]

