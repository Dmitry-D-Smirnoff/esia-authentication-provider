package ru.voskhod.platform.esiaprovider;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import ru.voskhod.platform.esiaprovider.api.dto.OrgDto;
import ru.voskhod.platform.esiaprovider.esia.dto.EsiaOrganizationInfo;
import ru.voskhod.platform.esiaprovider.logic.PlatformSettings;
import ru.voskhod.platform.esiaprovider.logic.SecurityLoggingProperties;

import javax.ejb.EJB;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

@ApplicationScoped
public class BeanConfig {

    @EJB
    private PlatformSettings settings;

    @Produces
    @ApplicationScoped
    ModelMapper modelMapper() {
        return new ModelMapper() {{

            // Глобальную конфигурацию необходимо задавать раньше мэппинга типов,
            // так как она учитывается при добавлении каждого мэппинга.
            getConfiguration()
                    .setMatchingStrategy(MatchingStrategies.STRICT)
                    .setFieldMatchingEnabled(true);

            createTypeMap(EsiaOrganizationInfo.class, OrgDto.class)
                    .setPostConverter(context -> {
                        context.getDestination().setEsiaOrgId(context.getSource().oid);
                        return context.getDestination();
                    });
        }};
    }

    @Produces
    @ApplicationScoped
    SecurityLoggingProperties securityLoggingProperties() {
        return new SecurityLoggingProperties(
                settings.systemName.value(),
                settings.am.moduleName.value(),
                "esiaprovider"
        );
    }

}
