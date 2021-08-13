#!/bin/sh

projectName="esia-authentication-provider"
projectDir=`pwd`
mvn clean

#-----------------------------------------------------------------------------------------------------------------------
# Инициализация каталога build внутри каталога проекта
#-----------------------------------------------------------------------------------------------------------------------
if [ -d build ]; then rm -rf build; fi
mkdir build
#-----------------------------------------------------------------------------------------------------------------------


#-----------------------------------------------------------------------------------------------------------------------
# Установка в локальный Maven-репозиторий pom-файла родительского проекта
#-----------------------------------------------------------------------------------------------------------------------
cd ..
mvn install --non-recursive -DskipTests
#-----------------------------------------------------------------------------------------------------------------------


#-----------------------------------------------------------------------------------------------------------------------
# Выгрузка зависимостей в модуль для wildfly
#-----------------------------------------------------------------------------------------------------------------------
cd $projectDir
../dexter                                                            \
    --projectDir=$projectDir                                         \
    --module=ru.voskhod.platform.${projectName}-dependencies         \
    --moduleDir=$projectDir/build/${projectName}-module              \
    --moduleZip=$projectDir/build/${projectName}-module.zip          \
    --internalGroups=ru.voskhod.platform,ru.voskhod.platform.commons \
    --excludeGroups=org.slf4j,log4j,com.fasterxml.jackson,com.fasterxml.jackson.core,com.fasterxml.jackson.jaxrs,com.fasterxml.jackson.module,com.fasterxml.jackson.datatype \
    --dependencies=javax.ws.rs.api,javax.servlet.api,org.slf4j,com.fasterxml.jackson.core.jackson-core,com.fasterxml.jackson.core.jackson-annotations,com.fasterxml.jackson.core.jackson-databind,com.fasterxml.jackson.datatype.jackson-datatype-jdk8,com.fasterxml.jackson.datatype.jackson-datatype-jsr310,com.fasterxml.jackson.jaxrs.jackson-jaxrs-json-provider
#-----------------------------------------------------------------------------------------------------------------------


#-----------------------------------------------------------------------------------------------------------------------
# Сборка основного war-файла сервиса с включённым профилем 'deployment'
# для исключения зависимостей, вынесенных в модуль
#-----------------------------------------------------------------------------------------------------------------------
mvn package -Pdeployment -DskipTests
cp target/${projectName}-api-*.war build/
#-----------------------------------------------------------------------------------------------------------------------
