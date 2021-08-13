VERSION=$(head -n 1 $(dirname "$0")/../version.txt)

if [ -n "$SERVICE" ]; then

  mvn deploy:deploy-file                                    \
      -Durl=https://nexus.12.voskhod.ru/repository/releases \
      -DrepositoryId=nexus-releases                         \
      -DgroupId=ru.voskhod.platform.security                \
      -DartifactId=$SERVICE-module                          \
      -Dversion=$VERSION                                    \
      -Dpackaging=zip                                       \
      -Dfile=build/$SERVICE-module.zip

  mvn deploy:deploy-file                                    \
      -Durl=https://nexus.12.voskhod.ru/repository/releases \
      -DrepositoryId=nexus-releases                         \
      -DgroupId=ru.voskhod.platform.security                \
      -DartifactId=$SERVICE-api                             \
      -Dversion=$VERSION                                    \
      -Dpackaging=war                                       \
      -Dfile=build/$SERVICE-api-$VERSION.war

else
    echo "Данный скрипт предназначен только для вызова из других скриптов"
fi
