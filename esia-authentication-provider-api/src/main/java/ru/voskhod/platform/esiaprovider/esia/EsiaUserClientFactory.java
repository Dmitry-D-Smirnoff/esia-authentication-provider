package ru.voskhod.platform.esiaprovider.esia;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import ru.voskhod.platform.common.exception.UnauthenticatedException;
import ru.voskhod.platform.esiaprovider.client.CryptoServiceRestClient;
import ru.voskhod.platform.esiaprovider.client.dto.EsiaOAuthAccessTokenResponse;
import ru.voskhod.platform.esiaprovider.client.dto.FileSignResultDto;
import ru.voskhod.platform.esiaprovider.client.dto.VerifySignatureDataRequest;
import ru.voskhod.platform.esiaprovider.client.dto.VerifySignatureDataResponse;
import ru.voskhod.platform.esiaprovider.logic.LoggingSettings;
import ru.voskhod.platform.esiaprovider.logic.PlatformSettings;
import ru.voskhod.platform.esiaprovider.logic.SecurityLogging;
import sun.security.pkcs.ContentInfo;
import sun.security.pkcs.PKCS7;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.x509.AlgorithmId;
import sun.security.x509.X500Name;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static java.text.MessageFormat.format;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@Stateless
public class EsiaUserClientFactory {

    private static final String SIGNATURE_TYPE        = "Sha256WithRSA";
    private static final String ESIA_TOKEN_GRANT_TYPE = "authorization_code";
    private static final String ESIA_TOKEN_TYPE       = "Bearer";

    private static final String CRYPTO_RS256          = "RS256";
    private static final String CRYPTO_GOST2012       = "GOST3410_2012_256";

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss Z");


    @EJB
    private PlatformSettings settings;

    @EJB
    private LoggingSettings loggingSettings;

    @Inject
    private SecurityLogging securityLogging;

    @EJB
    private EsiaUriManager esiaUriManager;

    @Inject
    private CryptoServiceRestClient cryptoServiceRestClient;

    private final ObjectMapper objectMapper = new ObjectMapper();


    /**
     * Создание экземпляра {@link EsiaUserClient}. По указанному коду доступа выполняет запрос токена,
     * проверку подписи токена. Если указано значение для параметра {@code organizationId}, в запрос
     * токена добавляется scope для указанной организации.
     */
    public EsiaUserClient createClient(String accessCode, String organizationId) throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, IOException, SignatureException, InvalidKeySpecException {

        String timestamp = DATE_FORMAT.format(Calendar.getInstance().getTime());
        String state = UUID.randomUUID().toString();

        String clientSecret = createClientSecret(timestamp, state, organizationId);

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("client_id",     settings.esia.clientId.value()));
        params.add(new BasicNameValuePair("code",          accessCode));
        params.add(new BasicNameValuePair("grant_type",    ESIA_TOKEN_GRANT_TYPE));
        params.add(new BasicNameValuePair("client_secret", clientSecret));
        params.add(new BasicNameValuePair("state",         state));
        params.add(new BasicNameValuePair("redirect_uri",  settings.esia.redirectUri.value()));
        params.add(new BasicNameValuePair("scope",         getFullScope(organizationId)));
        params.add(new BasicNameValuePair("timestamp",     timestamp));
        params.add(new BasicNameValuePair("token_type",    ESIA_TOKEN_TYPE));

        HttpPost httpPost = new HttpPost(esiaUriManager.tokenUrl());
        httpPost.setEntity(new UrlEncodedFormEntity(params));

        EsiaOAuthAccessTokenResponse accessTokenResponse;

        try (CloseableHttpClient client = HttpClients.createDefault();
             CloseableHttpResponse authResponse = client.execute(httpPost)) {

            if (authResponse.getStatusLine().getStatusCode() == 200) {

                accessTokenResponse = objectMapper.readValue(
                        authResponse.getEntity().getContent(),
                        EsiaOAuthAccessTokenResponse.class);

                if (!state.equals(accessTokenResponse.getState())) {
                    throw new IllegalArgumentException(format(
                            "Проверочная последовательность в запросе ({0}) и в ответе ({1}) не совпадают",
                            state, accessTokenResponse.getState())
                    );
                }

            } else {
                throw new IllegalArgumentException(format(
                        "getToken: code {0}, error {1} ",
                        authResponse.getStatusLine().getStatusCode(),
                        EntityUtils.toString(authResponse.getEntity())
                ));
            }

        }


        DecodedJWT decoded = JWT.decode(accessTokenResponse.getAccessToken());
        verifyToken(decoded, organizationId);

        return new EsiaUserClient(
                accessTokenResponse.getAccessToken(),
                decoded.getClaim(settings.esia.jwtClaimUserIdKey.value()).asLong().toString(),
                organizationId,
                esiaUriManager,
                securityLogging,
                loggingSettings);

    }


    /**
     * Выполняет проверку полученного токена: проверяются основные параметры, подпись и наличие информации
     * о группах, если при запросе был указан идентификатор организации пользователя.
     */
    private void verifyToken(DecodedJWT decoded, String organizationId) {

        checkSimpleProperties(decoded);

        switch (settings.esia.jwtAlgorithm.value()) {
            case CRYPTO_RS256:
                verifySignatureRs256(decoded.getToken());
                break;
            case CRYPTO_GOST2012:
                verifySignatureGost2012(decoded);
                break;
            default:
                throw new IllegalStateException(format(
                        "В настройках сервиса указан неизвестный алгоритм подписи токена ЕСИА: {0}",
                        settings.esia.jwtAlgorithm.value()
                ));
        }

        /*
            Для получения информации о группах scope accessToken-а должен содержать
            ESIA_SCOPE_FOR_GROUP, иначе будет ошибка
             {"code":"ESIA-005029","message":"SecurityErrorEnum.scopeNotAllowed"},
             url: https://esia-portal1.test.gosuslugi.ru/rs/orgs/1000298922/emps/1000404446/grps%3Fembed=(elements)
             при попытке получить информацию о группах пользователя в данной организации
         */
        if (organizationId != null && !isEmpty(settings.esia.organizationDataScope.value())) {
            String scope = decoded.getClaim(settings.esia.jwtClaimClientScopeKey.value()).asString();
            if (scope == null || !scope.contains(settings.esia.organizationDataScope.value())) {
                throw new IllegalArgumentException("В полученном токене отсутствуют данные о группах пользователя");
            }
        }

    }

    /**
     * Проверка подписи токена по алгоритму ГОСТ P 34.10-2012
     */
    private void verifySignatureGost2012(DecodedJWT jwt) {
        VerifySignatureDataResponse response;

        try {

            VerifySignatureDataRequest request = new VerifySignatureDataRequest();
            request.setData((jwt.getHeader() + "." + jwt.getPayload()).getBytes(StandardCharsets.UTF_8));
            request.setSignature(Base64.getUrlDecoder().decode(jwt.getSignature()));
            request.setCertificate(settings.esia.gostJwtPublicKeyCertBase64.value().getBytes(StandardCharsets.UTF_8));

            response = cryptoServiceRestClient.verify(request);

        } catch (Exception exception) {
            throw new RuntimeException("Не удалось выполнить проверку подписи", exception);
        }

        if (!response.isArithmeticSignatureCorrect()) {
            throw new UnauthenticatedException("Пользователь не аутентифицирован");
        }
    }

    /**
     * Проверка подписи токена по алгоритму RS256 с использованием хэширования SHA-256
     */
    private void verifySignatureRs256(String accessToken) {
        try {
            String publicKeyAsString = settings.esia.jwtPublicKeyBase64.value();
            RSAPublicKey publicKey = (RSAPublicKey) KeyStoreUtils.readBase64PublicKeyRsa(publicKeyAsString);
            Algorithm algorithm = Algorithm.RSA256(publicKey, null);
            JWTVerifier verifier = JWT.require(algorithm).build();
            verifier.verify(accessToken);

        } catch (JWTVerificationException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private void checkSimpleProperties(DecodedJWT decoder) {
        checkSimpleProperty(settings.esia.jwtAlgorithm.value(), decoder.getAlgorithm(),
                "Алгоритм подписи токена, заданный в настройках ({0}) не совпадает с алгоритмом подписи полученного токена ({1})");
        checkSimpleProperty(settings.esia.jwtType.value(), decoder.getType(),
                "Тип токена ЕСИА, заданный в настройках ({0}) не совпадает с типом полученного токена ({1})");
        checkSimpleProperty(settings.esia.jwtIssuer.value(), decoder.getIssuer(),
                "Issuer для проверки токенов JWT ЕСИА, заданный в настройках ({0}) не совпадает для полученного токена ({1})");
        String clientId = decoder.getClaim(settings.esia.jwtClaimClientIdKey.value()).asString();
        checkSimpleProperty(settings.esia.clientId.value(), clientId,
                "Мнемоника системы, заданная в настройках ({0}) не совпадает для полученного токена ({1})");
        if (new Date().after(decoder.getExpiresAt())) {
            throw new IllegalArgumentException(format("Получен просроченный токен, дата окончания действия {0}", decoder.getExpiresAt()));
        }
    }

    private void checkSimpleProperty(@NonNull String expected, String value, String errorMessageTemplate) {
        if (!expected.equals(value)) {
            throw new IllegalArgumentException(format(errorMessageTemplate, value, expected));
        }
    }



    private String createClientSecret(String timestamp,
                                     String state,
                                     String organizationOid) throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, InvalidKeySpecException, SignatureException, IOException {

        String message = getFullScope(organizationOid) +
                timestamp + settings.esia.clientId.value() + state;
        byte[] data = message.getBytes(StandardCharsets.UTF_8);

        switch (settings.esia.requestSignatureAlgorithm.value()) {
            case CRYPTO_RS256:
                return encode(signRs256(data));
            case CRYPTO_GOST2012:
                return encode(signGost2012(data));
            default:
                throw new IllegalStateException(format(
                        "В настройках сервиса указан неизвестный алгоритм шифрования для запросов к ЕСИА: {0}",
                        settings.esia.requestSignatureAlgorithm.value()
                ));
        }

    }

    private String getFullScope(String organizationOid) {
        return organizationOid == null ?
                settings.esia.userDataScope.value() :
                format("{0} {1}={2}",
                        settings.esia.userDataScope.value(),
                        settings.esia.organizationDataScope.value(),
                        organizationOid);
    }

    private String encode(byte[] data) {
        return new String(Base64.getEncoder().encode(data));
    }


    private byte[] signGost2012(byte[] data) throws SignatureException {

        try {

            FileSignResultDto result = cryptoServiceRestClient.sign(data);

            PKCS7 p7 = new PKCS7(result.getSignature());
            ByteArrayOutputStream bOut = new DerOutputStream();
            p7.encodeSignedData(bOut);
            return bOut.toByteArray();

        } catch (Exception exception) {
            throw new SignatureException("Не удалось выполнить подпись", exception);
        }

    }

    private byte[] signRs256(byte[] data) throws InvalidKeySpecException, CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, UnrecoverableKeyException, SignatureException {

        String keyStoreFile     = settings.esia.keystoreFile.value();
        String keystorePassword = settings.esia.keystorePassword.value();
        String keyAlias         = settings.esia.certificateAlias.value();

        KeyStore keyStore = KeyStoreUtils.loadJksKeyStore(keyStoreFile, keystorePassword, keyAlias);

        PrivateKey privateKey = (PrivateKey) keyStore.getKey(
                keyAlias,
                keystorePassword != null ? keystorePassword.toCharArray() : new char[]{});

        X509Certificate certificate = (X509Certificate) keyStore.getCertificate(keyAlias);

        try {

            Signature signature = Signature.getInstance(SIGNATURE_TYPE);
            signature.initSign(privateKey);
            signature.update(data);
            byte[] signedData = signature.sign();
            X500Name xName = X500Name.asX500Name(certificate.getSubjectX500Principal());
            BigInteger serial = certificate.getSerialNumber();
            AlgorithmId digestAlgorithmId = new AlgorithmId(AlgorithmId.SHA256_oid);
            AlgorithmId signAlgorithmId = new AlgorithmId(AlgorithmId.RSAEncryption_oid);
            sun.security.pkcs.SignerInfo sInfo = new sun.security.pkcs.SignerInfo(xName, serial, digestAlgorithmId, signAlgorithmId, signedData);
            ContentInfo cInfo = new ContentInfo(ContentInfo.DATA_OID, new DerValue(DerValue.tag_OctetString, data));
            PKCS7 p7 = new PKCS7(
                    new AlgorithmId[]{digestAlgorithmId},
                    cInfo,
                    new X509Certificate[]{certificate},
                    new sun.security.pkcs.SignerInfo[]{sInfo}
            );
            ByteArrayOutputStream bOut = new DerOutputStream();
            p7.encodeSignedData(bOut);
            return bOut.toByteArray();

        } catch (Exception exception) {
            throw new SignatureException("Не удалось выполнить подпись", exception);
        }

    }


    public URI getAccessCodeUrl(String organizationOid, String redirectUri) throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, IOException, SignatureException, InvalidKeySpecException, URISyntaxException {

        String timestamp = DATE_FORMAT.format(Calendar.getInstance().getTime());
        String state = UUID.randomUUID().toString();

        String clientSecret = createClientSecret(timestamp, state, organizationOid);

        /*
            В.2 Модель контроля на основе делегированного принятия решения
            В.2.2 Получение авторизационного кода

            - <client_id> – идентификатор системы-клиента (мнемоника системы в ЕСИА);
            - <client_secret> – подпись запроса в формате PKCS#7 detached signature в кодировке UTF8
            от значений четырех параметров HTTP–запроса: scope, timestamp, clientId, state (без
            разделителей). <client_secret> должен быть закодирован в формате base64 url safe.
            Используемый для проверки подписи сертификат должен быть предварительно
            зарегистрирован в ЕСИА и привязан к учетной записи системы-клиента в ЕСИА. ЕСИА
            поддерживает сертификаты в формате X.509. ЕСИА поддерживает алгоритмы
            формирования электронной подписи RSA с длиной ключа 2048 и алгоритмом
            криптографического хэширования SHA-256, а также алгоритмы электронной подписи
            ГОСТ Р 34.10-2001, ГОСТ Р 34.10-2012 и алгоритм криптографического хэширования
            ГОСТ Р 34.11-94.
            - <redirect_uri> – ссылка, по которой должен быть направлен пользователь после того, как
            даст разрешение на доступ к ресурсу;
            - <scope> – область доступа, т.е. запрашиваемые права; например, если система-клиент
            запрашивает доступ к сведениям о сотрудниках организации, то scope должна иметь
            значение http://esia.gosuslugi.ru/org_emps (с необходимыми параметрами); если
            запрашивается scope id_doc62 (данные о пользователе), то не нужно в качестве параметра
            указывать oid этого пользователя;
            - <response_type> – это тип ответа, который ожидается от ЕСИА, имеет значение code, если
            система-клиент должна получить авторизационный код;
            - <state> – набор случайных символов, имеющий вид 128-битного идентификатора запроса
            (необходимо для защиты от перехвата), генерируется по стандарту UUID;
            - <timestamp> - время запроса авторизационного кода в формате yyyy.MM.dd HH:mm:ss Z
            (например, 2013.01.25 14:36:11 +0400), необходимое для фиксации начала временного
            промежутка, в течение которого будет валиден запрос с данным идентификатором
            (<state>);
            - <access_type> – принимает значение “offline”, если требуется иметь доступ к ресурсам и
            тогда, когда владелец не может быть вызван (в этом случае выпускается маркер
            обновления); значение “online” – доступ требуется только при наличии владельца.
            Если в ходе авторизации не возникло ошибок, то ЕСИА осуществляет редирект
            пользователя по ссылке, указанной в redirect_uri, а также возвращает два обязательных
         */

        // Адрес в тестовой среде: https://esia-portal1.test.gosuslugi.ru/aas/oauth2/ac
        URIBuilder builder = esiaUriManager.authCodeBuilder();
        builder.addParameter("client_id", settings.esia.clientId.value());
        builder.addParameter("client_secret", clientSecret);
        builder.addParameter("redirect_uri", redirectUri);
        builder.addParameter("scope", getFullScope(organizationOid));
        builder.addParameter("response_type", "code");
        builder.addParameter("state", state);
        builder.addParameter("timestamp", timestamp);
        builder.addParameter("access_type", "online");

        return builder.build();

    }

    public URI getLogoutUri(String redirectUri) throws URISyntaxException {
        return esiaUriManager
                .logoutBuilder()
                .addParameter("client_id", settings.esia.clientId.value())
                .addParameter("redirect_url", redirectUri)
                .build();
    }

}
