#!/usr/bin/env bash

SERVER=localhost:48080
PROJECT=esia-authentication-provider

if test -f "$PROJECT-openapi.yaml"; then
    curl "http://$SERVER/$PROJECT/api/openapi.yaml" --output "$PROJECT-openapi-parsed.yaml"
else
    echo "Файл $PROJECT-openapi-parsed.yaml не найден в текущем каталоге!"
    echo "Скрипт нужно запускать в подкаталоге openapi каталога проекта."
fi
