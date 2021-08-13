package ru.voskhod.platform.esiaprovider.test.dto;

import org.junit.jupiter.api.BeforeEach;
import org.modelmapper.ModelMapper;
import ru.voskhod.platform.esiaprovider.BeanConfig;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

abstract class MappingTestBase {

    ModelMapper modelMapper;

    @BeforeEach
    void init() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        Method method = BeanConfig.class.getDeclaredMethod("modelMapper");
        method.setAccessible(true);

        modelMapper = (ModelMapper) method.invoke(new BeanConfig());
    }

}