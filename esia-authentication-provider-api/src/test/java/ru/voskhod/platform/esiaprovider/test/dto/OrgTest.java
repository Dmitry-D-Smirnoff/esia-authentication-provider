package ru.voskhod.platform.esiaprovider.test.dto;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.voskhod.platform.esiaprovider.api.dto.OrgDto;
import ru.voskhod.platform.esiaprovider.esia.dto.EsiaOrganizationInfo;

class OrgTest extends MappingTestBase {

    @Test
    void toOrgDtoTest() {

        EsiaOrganizationInfo esiaInfo = new EsiaOrganizationInfo();
        esiaInfo.oid = "068aa823-861f-4bc0-a328-99e97f877de8";
        esiaInfo.ogrn = "12345687";
        esiaInfo.fullName = "Test Organization";
        esiaInfo.shortName = "TestOrg";
        esiaInfo.type = "Type1";

        OrgDto dto = modelMapper.map(esiaInfo, OrgDto.class);

        Assertions.assertEquals(esiaInfo.oid, dto.getEsiaOrgId(), "oid");
        Assertions.assertEquals(esiaInfo.ogrn, dto.getOGRN(), "ogrn");
        Assertions.assertEquals(esiaInfo.fullName, dto.getFullName(), "fullName");
        Assertions.assertEquals(esiaInfo.shortName, dto.getShortName(), "shortName");
        Assertions.assertEquals(esiaInfo.type, dto.getType(), "type");

    }

}