# faulttolerence
//    docker run -d -it --name rabbit --hostname rabbit -p 30000:5672 -p 30001:15672 rabbitmq:management
//
//      998  docker run -d -it --name es -p 9200:9200 -p 9300:9300 elasticsearch
//  999  docker run -d -it --name kibana --link es:elasticsearch -p 5601:5601 kibana

//docker inspect --format '{{ .NetworkSettings.IPAddress }}' 8570b38e5877
 //  docker inspect --format '{{ .NetworkSettings.IPAddress }}'  36ba0255c836
//    docker run -d -it --name logstash logstash -e 'input { rabbitmq { host => "172.17.0.2" port => 5672 queue => "helloq"  durable => true } }  output { elasticsearch { hosts => ["172.17.0.3"] } }'
