#!/bin/bash

echo "Esperando que Cassandra esté disponible..."

# docker exec -it cassandra-test cqlsh
# CREATE KEYSPACE IF NOT EXISTS voting_system WITH replication = {'class': 'SimpleStrategy', 'replication_factor' : 1};

until cqlsh -e "describe cluster" > /dev/null 2>&1; do
  sleep 5
done

echo "Creando keyspace voting_system si no existe..."

cqlsh -e "CREATE KEYSPACE IF NOT EXISTS voting_system WITH replication = {'class': 'SimpleStrategy', 'replication_factor' : 1};"

echo "Listo."

# Este script puede eliminarse o marcarse como obsoleto.
# La inicialización de Cassandra se realiza en config/docker/docker-compose.yaml con cassandra-init.