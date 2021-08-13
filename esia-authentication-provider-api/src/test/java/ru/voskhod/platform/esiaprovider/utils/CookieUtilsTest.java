package ru.voskhod.platform.esiaprovider.utils;

import org.junit.jupiter.api.Test;
import ru.voskhod.platform.esiaprovider.logic.CookieUtils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CookieUtilsTest {

    @Test
    void isIEorME() {
        assertTrue(CookieUtils.isIEorME("Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0)"));
        assertTrue(CookieUtils.isIEorME("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0; WOW64; Trident/4.0;)"));
        assertTrue(CookieUtils.isIEorME("Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; Trident/4.0)"));
        assertTrue(CookieUtils.isIEorME("Mozilla/4.0 (compatible; MSIE 9.0; Windows NT 6.0)"));
        assertTrue(CookieUtils.isIEorME("Mozilla/4.0 (compatible; MSIE 9.0; Windows NT 6.1)"));
        assertTrue(CookieUtils.isIEorME("Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; WOW64; Trident/6.0)"));
        assertTrue(CookieUtils.isIEorME("Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.2)"));
        assertTrue(CookieUtils.isIEorME("Mozilla/5.0 (Windows NT 6.1; Trident/7.0; rv:11.0) like Gecko"));
        assertTrue(CookieUtils.isIEorME("Mozilla/5.0 (Windows NT 6.2; Trident/7.0; rv:11.0) like Gecko"));
        assertTrue(CookieUtils.isIEorME("Mozilla/5.0 (Windows NT 6.3; Trident/7.0; rv:11.0) like Gecko"));
        assertTrue(CookieUtils.isIEorME("Mozilla/5.0 (Windows NT 10.0; Trident/7.0; rv:11.0) like Gecko"));

        assertTrue(CookieUtils.isIEorME("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.75 Safari/537.36 Edg/86.0.622.43"));
        assertTrue(CookieUtils.isIEorME("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.75 Safari/537.36 Edg/86.0.622.38"));
        assertTrue(CookieUtils.isIEorME("Mozilla/5.0 (Linux; Android 10; HD1913) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.99 Mobile Safari/537.36 EdgA/45.8.4.5074"));
        assertTrue(CookieUtils.isIEorME("Mozilla/5.0 (Linux; Android 10; SM-G973F) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.99 Mobile Safari/537.36 EdgA/45.8.4.5074"));
        assertTrue(CookieUtils.isIEorME("Mozilla/5.0 (Linux; Android 10; Pixel 3 XL) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.99 Mobile Safari/537.36 EdgA/45.8.4.5074"));
        assertTrue(CookieUtils.isIEorME("Mozilla/5.0 (Linux; Android 10; ONEPLUS A6003) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.99 Mobile Safari/537.36 EdgA/45.8.4.5074"));
        assertTrue(CookieUtils.isIEorME("Mozilla/5.0 (Windows NT 10.0; Win64; x64; Xbox; Xbox One) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.75 Safari/537.36 Edge/44.18363.8131"));

        assertFalse(CookieUtils.isIEorME("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:81.0) Gecko/20100101 Firefox/81.0"));
        assertFalse(CookieUtils.isIEorME("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:81.0) Gecko/20100101 Firefox/81.0"));
        assertFalse(CookieUtils.isIEorME("Mozilla/5.0 (X11; Linux i686; rv:81.0) Gecko/20100101 Firefox/81.0"));
        assertFalse(CookieUtils.isIEorME("Mozilla/5.0 (Linux x86_64; rv:81.0) Gecko/20100101 Firefox/81.0"));
        assertFalse(CookieUtils.isIEorME("Mozilla/5.0 (X11; Ubuntu; Linux i686; rv:81.0) Gecko/20100101 Firefox/81.0"));
        assertFalse(CookieUtils.isIEorME("Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:81.0) Gecko/20100101 Firefox/81.0"));
        assertFalse(CookieUtils.isIEorME("Mozilla/5.0 (X11; Fedora; Linux x86_64; rv:81.0) Gecko/20100101 Firefox/81.0"));
        assertFalse(CookieUtils.isIEorME("Mozilla/5.0 (iPhone; CPU iPhone OS 10_15_7 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) FxiOS/29.0 Mobile/15E148 Safari/605.1.15"));
        assertFalse(CookieUtils.isIEorME("Mozilla/5.0 (iPad; CPU OS 10_15_7 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) FxiOS/29.0 Mobile/15E148 Safari/605.1.15"));
        assertFalse(CookieUtils.isIEorME("Mozilla/5.0 (iPod touch; CPU iPhone OS 10_15_7 like Mac OS X) AppleWebKit/604.5.6 (KHTML, like Gecko) FxiOS/29.0 Mobile/15E148 Safari/605.1.15"));
        assertFalse(CookieUtils.isIEorME("Mozilla/5.0 (Android 11; Mobile; rv:68.0) Gecko/68.0 Firefox/81.0"));
        assertFalse(CookieUtils.isIEorME("Mozilla/5.0 (Android 11; Mobile; LG-M255; rv:81.0) Gecko/81.0 Firefox/81.0"));
        assertFalse(CookieUtils.isIEorME("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:78.0) Gecko/20100101 Firefox/78.0"));
        assertFalse(CookieUtils.isIEorME("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:78.0) Gecko/20100101 Firefox/78.0"));
        assertFalse(CookieUtils.isIEorME("Mozilla/5.0 (X11; Linux i686; rv:78.0) Gecko/20100101 Firefox/78.0"));
        assertFalse(CookieUtils.isIEorME("Mozilla/5.0 (Linux x86_64; rv:78.0) Gecko/20100101 Firefox/78.0"));
        assertFalse(CookieUtils.isIEorME("Mozilla/5.0 (X11; Ubuntu; Linux i686; rv:78.0) Gecko/20100101 Firefox/78.0"));
        assertFalse(CookieUtils.isIEorME("Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:78.0) Gecko/20100101 Firefox/78.0"));
        assertFalse(CookieUtils.isIEorME("Mozilla/5.0 (X11; Fedora; Linux x86_64; rv:78.0) Gecko/20100101 Firefox/78.0"));

        assertFalse(CookieUtils.isIEorME(""));
        assertFalse(CookieUtils.isIEorME("gdhydtghjfjghjfgysertgrefgertgertgr"));
        assertFalse(CookieUtils.isIEorME(null));
    }
}