package ru.voskhod.platform.esiaprovider;

import io.swagger.v3.core.converter.ModelConverters;
import ru.voskhod.platform.common.api.InstantParamConverterProvider;
import ru.voskhod.platform.common.api.InstantPropertyConverter;
import ru.voskhod.platform.common.api.JacksonConfig;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;

@Provider
public class InstantFeature implements Feature {

    @Override
    public boolean configure(FeatureContext context) {
        ModelConverters.getInstance().addConverter(new InstantPropertyConverter());
        context.register(InstantParamConverterProvider.class);
        context.register(JacksonConfig.class);
        return true;
    }

}
