FROM java:8
ARG NUM
ENV NUM ${NUM}
WORKDIR /var/www/java
COPY . /var/www/java
RUN javac Host.java
CMD java Host ${NUM}