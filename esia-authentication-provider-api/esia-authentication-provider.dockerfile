# Собираем из эталонного локального WildFly 16 c jdk8 версии
FROM harbor.12.voskhod.ru/share/wildfly-19.1.0-openjdk-postgres:42.2.18
ENV JBOSS_HOME /opt/wildfly
ENV LANG ru_RU.UTF-8

# Наполняем контейнер
# Модули созданные Dexter'ом
COPY --chown=jboss:0 ./build/modules/ $JBOSS_HOME/modules/
# конфигурационный файл на переменных окружения копируется в buld publish.sh
COPY --chown=jboss:0 ./build/standalone_19.1.0_envVariables.xml $JBOSS_HOME/standalone/configuration/standalone.xml
# Непосредственно наша аппликуха
COPY --chown=jboss:0 ./build/*.war $JBOSS_HOME/standalone/deployments/

# Запускаем WildFly в standalone режиме и биндимся на любой интерфеейс
CMD ["/opt/wildfly/bin/standalone.sh", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0"]
