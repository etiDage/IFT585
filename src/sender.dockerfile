#Sender

FROM java:8
WORKDIR /var/www/java
COPY . /var/www/java
RUN javac Host.java
CMD java Host 1