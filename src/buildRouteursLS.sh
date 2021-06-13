#!/bin/bash

docker build --build-arg ALGO_NAME=LS --build-arg HOST_1=A --build-arg HOST_2=F --build-arg IP_HOST2=172.17.0.8 . -f routeur.dockerfile -t routeur_ls_a
docker build --build-arg ALGO_NAME=LS --build-arg HOST_1=A --build-arg HOST_2=F --build-arg IP_HOST2=172.17.0.8 . -f routeur.dockerfile -t routeur_ls_b
docker build --build-arg ALGO_NAME=LS --build-arg HOST_1=A --build-arg HOST_2=F --build-arg IP_HOST2=172.17.0.8 . -f routeur.dockerfile -t routeur_ls_c
docker build --build-arg ALGO_NAME=LS --build-arg HOST_1=A --build-arg HOST_2=F --build-arg IP_HOST2=172.17.0.8 . -f routeur.dockerfile -t routeur_ls_d
docker build --build-arg ALGO_NAME=LS --build-arg HOST_1=A --build-arg HOST_2=F --build-arg IP_HOST2=172.17.0.8 . -f routeur.dockerfile -t routeur_ls_e
docker build --build-arg ALGO_NAME=LS --build-arg HOST_1=A --build-arg HOST_2=F --build-arg IP_HOST2=172.17.0.8 . -f routeur.dockerfile -t routeur_ls_f
