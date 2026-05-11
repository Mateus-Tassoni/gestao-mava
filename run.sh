#!/bin/bash
# Carrega o .env e inicia a aplicação
# Uso: ./run.sh

if [ ! -f .env ]; then
    echo "Arquivo .env nao encontrado. Copie .env.example para .env e configure as credenciais:"
    echo "  cp .env.example .env"
    exit 1
fi

set -a
source .env
set +a

mvn spring-boot:run
