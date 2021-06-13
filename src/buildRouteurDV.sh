#!/bin/bash

docker build --build-arg ALGO_NAME=DV --build-arg HOST_1=A --build-arg HOST_2=F --build-arg IP_HOST2=172.17.0.8 . -f routeur.dockerfile -t routeur_dv_a
docker build --build-arg ALGO_NAME=DV --build-arg HOST_1=A --build-arg HOST_2=F --build-arg IP_HOST2=172.17.0.8 . -f routeur.dockerfile -t routeur_dv_b
docker build --build-arg ALGO_NAME=DV --build-arg HOST_1=A --build-arg HOST_2=F --build-arg IP_HOST2=172.17.0.8 . -f routeur.dockerfile -t routeur_dv_c
docker build --build-arg ALGO_NAME=DV --build-arg HOST_1=A --build-arg HOST_2=F --build-arg IP_HOST2=172.17.0.8 . -f routeur.dockerfile -t routeur_dv_d
docker build --build-arg ALGO_NAME=DV --build-arg HOST_1=A --build-arg HOST_2=F --build-arg IP_HOST2=172.17.0.8 . -f routeur.dockerfile -t routeur_dv_e
docker build --build-arg ALGO_NAME=DV --build-arg HOST_1=A --build-arg HOST_2=F --build-arg IP_HOST2=172.17.0.8 . -f routeur.dockerfile -t routeur_dv_f
