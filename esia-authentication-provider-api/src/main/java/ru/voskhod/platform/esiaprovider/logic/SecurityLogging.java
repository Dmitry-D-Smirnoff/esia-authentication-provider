package ru.voskhod.platform.esiaprovider.logic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.apache.commons.lang3.StringUtils;
import ru.voskhod.platform.common.settings.SettingsManager;
import ru.voskhod.platform.core.security.IdentityContext;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings({"unused", "UnusedReturnValue"})
@Slf4j
@ApplicationScoped
public class SecurityLogging {

    @SuppressWarnings("WeakerAccess")
    public static final String MODE_DISABLED = "DISABLED";

    @SuppressWarnings("WeakerAccess")
    public static final String MODE_ALL = "ALL";

    @SuppressWarnings("WeakerAccess")
    public static final String MODE_SUCCESS = "SUCCESS";

    public static final String MODE_FAIL = "FAIL";

    private static final String SEP = " | ";

    private static final String SEP_R = "| ";

    private ObjectMapper objectMapper;

    private final Map<Class<?>, Set<String>> ignoredFieldByEntity = new ConcurrentHashMap<>();

    @Resource(lookup = "java:/TransactionManager")
    private TransactionManager transactionManager;

    @Inject
    private IdentityContext identityContext;

    @Inject
    private SecurityLoggingProperties properties;

    public SecurityLogger newLogger(String event) {
        return newLogger(event, MODE_ALL);
    }

    /**
     * Создаёт новый логер, предназначенный для логирования типа событий, заданного указанной
     * настройкой. Тип события берётся из имени настройки. Режим логирования задаётся значением
     * настройки.
     */
    public SecurityLogger newLogger(SettingsManager<?, String>.Setting<String> logSetting) {
        return newLogger(logSetting.key(), logSetting.value());
    }

    public SecurityLogger newLogger(String event, String mode) {

        try {

            var slog = new SecurityLogger(event, mode);

            if (!MODE_DISABLED.equals(mode)) {
                transactionManager.getTransaction().registerSynchronization(new Synchronization() {

                    @Override
                    public void beforeCompletion() {
                    }

                    @Override
                    public void afterCompletion(int status) {

                        if (status == Status.STATUS_COMMITTED) {
                            slog.committed();
                        } else {
                            slog.rejected();
                        }

                    }

                });
            }

            return slog;

        } catch (RollbackException | SystemException e) {
            throw new RuntimeException(e);
        }
    }


    public void instantInfo(SettingsManager<?, String>.Setting<String> logSetting) {
        instantInfo(logSetting, null);
    }

    public void instantInfo(SettingsManager<?, String>.Setting<String> logSetting, String message) {
        switch (logSetting.value()) {
            case MODE_ALL:
            case MODE_SUCCESS:
                if (log.isInfoEnabled()) {
                    ObjectNode result = objectMapper.createObjectNode();

                    result.put("timestamp", Instant.now().toString());

                    result.put("systemName", properties.getSystemName());
                    result.put("moduleName", properties.getModuleName());
                    result.put("serviceName", properties.getServiceName());
                    result.put("instanceName", properties.getInstanceName());

                    result.put("event", logSetting.key());
                    result.put("success", true);

                    if (StringUtils.isNotBlank(message)) {
                        result.put("message", message);
                    }

                    log.info(result.toString());
                }
            default:
        }
    }


    @PostConstruct
    private void initMetaModel() {
        this.objectMapper = new ObjectMapper();
    }

    public class SecurityLogger {

        private final boolean logSuccess;
        private final boolean logFail;
        private final String event;
        private String mode;
        private String entityName;
        private String currentUserId;
        private String currentSnils;
        private String currentOgrn;
        private String message;
        private Instant timestamp;
        private Boolean success;


        SecurityLogger(String event, String mode) {
            this.event = event;
            this.logSuccess = MODE_ALL.equals(mode) || MODE_SUCCESS.equals(mode);
            this.logFail = MODE_ALL.equals(mode) || MODE_FAIL.equals(mode);
            timestamp();
        }

        public SecurityLogger timestamp() {
            this.timestamp = Instant.now();
            return this;
        }

        public SecurityLogger currentUserId(UUID currentUserId) {
            this.currentUserId = currentUserId != null ? currentUserId.toString() : null;
            timestamp();
            return this;
        }

        public SecurityLogger setSystemAsCurrentUser() {
            this.currentUserId = "SYSTEM";
            timestamp();
            return this;
        }

        public SecurityLogger currentSnils(String currentSnils) {
            this.currentSnils = currentSnils;
            timestamp();
            return this;
        }

        public SecurityLogger currentOgrn(String currentOgrn) {
            this.currentOgrn = currentOgrn;
            timestamp();
            return this;
        }

        public SecurityLogger message(String errorMessage) {
            this.message = errorMessage;
            timestamp();
            return this;
        }

        /**
         * Устанавливает признак "успеха". Если данный признак установлен, запись в лог попадёт с признаком
         * успешного наступления события даже в том, случае, если текущая транзакция будет отменена.
         * <br/><br/>
         * Может использоваться для обозначения успеха каких-то промежуточных событий, например, успешной
         * идентификации пользователя внутри процедуры аутентификации. В таком случае, даже если аутентификация
         * не пройдёт, идентификации будет залогирована с признаком "успех".
         */
        public SecurityLogger markSuccess() {
            this.success = true;
            timestamp();
            return this;
        }

        public SecurityLogger markFail() {
            this.success = false;
            timestamp();
            return this;
        }


        public void runWithRuntimeExceptionLogging(Runnable runnable) {

            try {
                runnable.run();
            } catch (RuntimeException e) {
                message(e.getMessage());
                throw e;
            }
        }


        void committed() {
            if (success != null && !success) {
                rejected();
            } else {
                if (logSuccess && log.isInfoEnabled()) {
                    log.info(build(true));
                }
            }
        }

        void rejected() {
            if (success != null && success) {
                committed();
            } else {
                if (logFail && log.isWarnEnabled()) {
                    log.warn(build(false));
                }
            }
        }

        String currentUser() {
            if (currentUserId == null) {
                currentUserId = identityContext.getPrincipal() != null
                                ? identityContext.getPrincipal().getName()
                                : "ANONYMOUS";
            }
            return currentUserId;
        }

        String event() {
            return event.split(" ")[0];
        }

        Optional<String> entityName() {
            if (entityName != null) {
                return Optional.of(entityName);
            } else {
                String[] parts = event.split(" ");
                if (parts.length > 1) {
                    return Optional.of(parts[1]);
                }
            }
            return Optional.empty();
        }

        String build(boolean success) {

            ObjectNode result = objectMapper.createObjectNode();

            result.put("timestamp", timestamp.toString());

            result.put("systemName", properties.getSystemName());
            result.put("moduleName", properties.getModuleName());
            result.put("serviceName", properties.getServiceName());
            result.put("instanceName", properties.getInstanceName());

            result.put("event", event);
            result.put("success", success);
            result.put("currentUserID", currentUser());

            if (StringUtils.isNotBlank(currentSnils)) {
                result.put("esiaUserSnils", currentSnils);
            }

            if (StringUtils.isNotBlank(currentOgrn)) {
                result.put("esiaUserOgrn", currentOgrn);
            }

            entityName().ifPresent(
                    entityName -> result.put("entity", entityName)
            );

            if (message != null) {
                result.put("message", message);
            }

            return result.toString();
        }

        String buildText(boolean success) {

            var builder = new StringBuilder()
                    .append(event()).append(SEP)
                    .append(entityName()).append(SEP)
                    .append(success ? "SUCCESS" : "FAILURE").append(SEP);

            if (message != null) {
                builder.append(message).append(SEP);
            }

            builder.append("currentUserID=").append(currentUser());

            return builder.toString();
        }

    }

}
