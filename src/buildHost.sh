#!/bin/bash

docker build . -f sender.dockerfile -t sender
docker build . -f receiver.dockerfile -t receiver
