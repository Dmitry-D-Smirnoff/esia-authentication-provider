package ru.voskhod.platform.esiaprovider.logic;

import lombok.val;
import ru.voskhod.platform.core.security.Pau;
import ua_parser.Parser;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.NewCookie;
import java.util.Optional;

@RequestScoped
public class CookieUtils {

    private static final ThreadLocal<Parser> PARSER = ThreadLocal.withInitial(Parser::new);

    @EJB
    private PlatformSettings settings;

    @Inject
    private HttpServletRequest request;

    /**
     * Возвращает <code>true</code> если переданная строка содержит признак одного из следующих браузеров:
     * <ul>
     *     <li>Internet Explorer</li>
     *     <li>Microsoft Edge</li>
     * </ul>
     *
     * @param userAgent строка из заголовка запроса User-Agent
     * @return правда или ложь
     * @see <a href="https://www.whatismybrowser.com/guides/the-latest-user-agent/internet-explorer>User-Agent для Internet Explorer</a>
     * @see <a href="https://www.whatismybrowser.com/guides/the-latest-user-agent/edge>User-Agent для Microsoft Edge</a>
     */
    public static boolean isIEorME(String userAgent) {
        val client = PARSER.get().parse(userAgent);
        if (client.userAgent != null && (client.userAgent.family.equals("IE") || client.userAgent.family.startsWith("Edge"))) {
            return true;
        }

        return false;
    }

    public NewCookie create(String sessionToken) {
        return isIEorME(request.getHeader("user-agent"))
                ? createForIEorME(sessionToken)
                : createForOther(sessionToken);
    }

    private NewCookie createForIEorME(String sessionToken) {
        return create(sessionToken, null, settings.am.cookieSecure.value());
    }

    private NewCookie createForOther(String sessionToken) {
        return create(sessionToken, settings.am.cookieDomain.value(), settings.am.cookieSecure.value());
    }

    private NewCookie create(String sessionToken, String domain, boolean secure) {
        return new NewCookie(Pau.AUTH_COOKIE,
                sessionToken,
                "/",
                Optional.ofNullable(domain).filter(value -> !value.isEmpty()).orElse(null),
                null,
                NewCookie.DEFAULT_MAX_AGE,
                secure,
                true
        );
    }

}
