@echo off

scp elink-juliet-flow-domain\target\elink-juliet-flow-domain-srm-dev.tar.gz efn@10.98.250.69:/home/efn

ssh efn@10.98.250.69 ./deploy.sh elink-juliet-flow-domain srm-dev

exit