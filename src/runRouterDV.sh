#!/bin/bash

docker container rm $(docker container list --quiet --filter status=exited)

tab=" --tab"
options=()

cmds[0]="docker run -it routeur_dv_a"
cmds[1]="docker run -it routeur_dv_b"
cmds[2]="docker run -it routeur_dv_c"
cmds[3]="docker run -it routeur_dv_d"
cmds[4]="docker run -it routeur_dv_e"
cmds[5]="docker run -it routeur_dv_f"

for i in {0..5}; do
options+=($tab -e "bash -c '${cmds[i]} ; bash'")
done

gnome-terminal "${options[@]}"

exit 0
