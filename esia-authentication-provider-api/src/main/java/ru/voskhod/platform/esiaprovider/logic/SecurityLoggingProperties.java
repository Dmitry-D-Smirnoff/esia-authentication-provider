package ru.voskhod.platform.esiaprovider.logic;

import lombok.Getter;

import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;

@Getter
public class SecurityLoggingProperties {

    private String systemName;

    private String moduleName;

    private String serviceName;

    private String instanceName;

    @SuppressWarnings("unused")
    public SecurityLoggingProperties() {}

    public SecurityLoggingProperties(String systemName, String moduleName, String serviceName) {

        this.systemName = systemName;
        this.moduleName = moduleName;

        this.serviceName = System.getProperty(serviceName + "_logServiceName");
        if (this.serviceName == null) {
            this.serviceName = serviceName;
        }

        try {
            String host = InetAddress.getLocalHost().getHostAddress();
            String port = ManagementFactory.getPlatformMBeanServer().getAttribute(
                    new ObjectName("jboss.as:socket-binding-group=standard-sockets,socket-binding=http"),
                    "boundPort"
            ).toString();

            this.instanceName = host + ":" + port;

        } catch (Exception ignore) {
            this.instanceName = "UNKNOWN_INSTANCE";
        }
    }

}
