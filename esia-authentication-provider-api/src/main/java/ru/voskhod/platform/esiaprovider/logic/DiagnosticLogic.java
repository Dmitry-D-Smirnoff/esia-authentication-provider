package ru.voskhod.platform.esiaprovider.logic;

import ru.voskhod.platform.common.diagnostic.DiagnosticLogicBase;

import javax.ejb.EJB;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DiagnosticLogic extends DiagnosticLogicBase {

    @EJB
    PlatformSettings platformSettings;

    public DiagnosticLogic() {
        super("esia-authentication-provider");
    }

    @Override
    public void reconfigure() {
        platformSettings.refreshCache();
    }
}
