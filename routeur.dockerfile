FROM java:8
ARG ALGO_NAME
ARG HOST_1
ARG HOST_2
ENV ALGO_NAME ${ALGO_NAME}
ENV HOST_1 ${HOST_1}
ENV HOST_2 ${HOST_2}
WORKDIR /var/www/java
COPY . /var/www/java
RUN javac routeur.java
CMD java routeur ${ALGO_NAME} ${HOST_1} ${HOST_2} 
