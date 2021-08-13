#!/usr/bin/env bash
DKREGISTRY=${DKREGISTRY:=harbor.12.voskhod.ru}
#version=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout);
version=$(cat ../version.txt)
mkdir ./build/modules
unzip ./build/esia-authentication-provider-module.zip -d ./build/modules/
cp ../devops/standalone_19.1.0_envVariables.xml ./build/.
# $1 - username from docker registry account
# $2 - password from docker registry account
docker login -u $1 -p $2 $DKREGISTRY
docker build -f esia-authentication-provider.dockerfile \
  -t $DKREGISTRY/security/auth-providers/esia-authentication-provider:"$version" .
docker tag $DKREGISTRY/security/auth-providers/esia-authentication-provider:"$version" \
  $DKREGISTRY/security/auth-providers/esia-authentication-provider:latest
docker push $DKREGISTRY/security/auth-providers/esia-authentication-provider:"$version"
docker push $DKREGISTRY/security/auth-providers/esia-authentication-provider:latest
# docker logout $DKREGISTRY
