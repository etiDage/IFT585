#!/bin/bash

docker build --build-arg NUM=1 . -f host.dockerfile -t host_1
docker build --build-arg NUM=2 . -f host.dockerfile -t host_2
