package ru.voskhod.platform.esiaprovider.esia;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.US_ASCII;

public class KeyStoreUtils {

    private static final String JKS_KEYSTORE_TYPE = "jks";
    private static final String RSA_ALGORITHM = "RSA";

    /**
     * Шаблон для извлечения открытого ключа
     */
    private static final Pattern PUBLIC_KEY_PATTERN = Pattern.compile(
            "-+BEGIN\\s+.*PUBLIC\\s+KEY[^-]*-+(?:\\s|\\r|\\n)+" +
                    "([a-z0-9+/=\\r\\n]+)" +
                    "-+END\\s+.*PUBLIC\\s+KEY[^-]*-+",
            Pattern.CASE_INSENSITIVE);

    /**
     * Шаблон для извлечения сертификата
     */
    private static final Pattern CERT_PATTERN = Pattern.compile(
            "-+BEGIN\\s+.*CERTIFICATE[^-]*-+(?:\\s|\\r|\\n)+" +
                    "([a-z0-9+/=\\r\\n]+)" +
                    "-+END\\s+.*CERTIFICATE[^-]*-+",
            Pattern.CASE_INSENSITIVE);

    /**
     * Шаблон для извлечения закрытого ключа
     */
    private static final Pattern PRIVATE_KEY_PATTERN = Pattern.compile(
            "-+BEGIN\\s+.*PRIVATE\\s+KEY[^-]*-+(?:\\s|\\r|\\n)+" +
                    "([a-z0-9+/=\\r\\n]+)" +
                    "-+END\\s+.*PRIVATE\\s+KEY[^-]*-+",
            Pattern.CASE_INSENSITIVE);


    public static KeyStore loadJksKeyStore(String resourceName,
                                           String password,
                                           String certificateAlias) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, InvalidKeySpecException {

        return loadKeyStore(JKS_KEYSTORE_TYPE, resourceName, password, certificateAlias);

    }

    public static KeyStore loadJksKeyStoreFromResource(String resourceName,
                                                       String password) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {

        return loadKeyStoreFromResource(JKS_KEYSTORE_TYPE, resourceName, password);

    }


    /**
     * Возвращает открытый ключ
     *
     * @param keystore хранилище сретификатов в формате pem
     * @return открытый ключ
     * @throws KeyStoreException        ошибка хранилища сертификатов
     * @throws InvalidKeySpecException  ошибка получения закрытого ключа
     * @throws NoSuchAlgorithmException ошибка отсутствия алгоритма
     */
    public static PublicKey readPublicKey(String keystore) throws KeyStoreException, InvalidKeySpecException, NoSuchAlgorithmException {

        Matcher matcher = PUBLIC_KEY_PATTERN.matcher(keystore);
        if (!matcher.find()) {
            throw new KeyStoreException(String.format("Ошибка: не найден открытый ключ '%s'", keystore));
        }

        String base64 = matcher.group(1);

        return readBase64PublicKeyRsa(base64);

    }

    /**
     * Возвращает открытый ключ
     *
     * @param base64 строка с ключём в формате base64
     * @return открытый ключ
     * @throws InvalidKeySpecException  ошибка получения закрытого ключа
     * @throws NoSuchAlgorithmException ошибка отсутствия алгоритма
     */
    public static PublicKey readBase64PublicKeyRsa(String base64) throws InvalidKeySpecException, NoSuchAlgorithmException {

        return readBase64PublicKey(base64, RSA_ALGORITHM);

    }

    /**
     * Возвращает хранилище сертификатов
     *
     * @param keystore         хранилище сертификатов в формате pem
     * @param keystorePassword ключ хранилища сертификатов
     * @param certificateAlias алиас сертификата
     * @return хранилище сертификатов
     * @throws InvalidKeySpecException  ошибка закрытого ключа
     * @throws KeyStoreException        ошибка хранилища сертификатов
     * @throws NoSuchAlgorithmException ошибка отсутствия алгоритма
     * @throws CertificateException     ошибка сертификата
     * @throws IOException              ошибка IO
     */
    private static KeyStore loadKeyStore(String keystoreType,
                                         String keystore,
                                         String keystorePassword,
                                         String certificateAlias) throws InvalidKeySpecException, KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {

        PrivateKey privateKey = readPrivateKey(keystore);
        Certificate[] certificateChain = readCertificateChain(keystore);

        KeyStore keyStore = KeyStore.getInstance(JKS_KEYSTORE_TYPE);
        keyStore.load(null, null);
        keyStore.setKeyEntry(
                certificateAlias,
                privateKey,
                keystorePassword.toCharArray(),
                certificateChain
        );

        return keyStore;
    }

    /**
     * Возвращает закрытый ключ
     *
     * @param keystore хранилище сертификатов в формате pem
     * @return закрытый ключ
     * @throws KeyStoreException        ошибка хранилища сертификатов
     * @throws InvalidKeySpecException  ошибка получения закрытого ключа
     * @throws NoSuchAlgorithmException ошибка отсутствия алгоритма
     */
    private static PrivateKey readPrivateKey(String keystore) throws KeyStoreException, InvalidKeySpecException, NoSuchAlgorithmException {

        Matcher matcher = PRIVATE_KEY_PATTERN.matcher(keystore);
        if (!matcher.find()) {
            throw new KeyStoreException(String.format("Ошибка: не найден закрытый ключ '%s'", keystore));
        }
        byte[] decoded = base64Decode(matcher.group(1));

        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);

        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);

        return keyFactory.generatePrivate(spec);
    }

    /**
     * Возвращает цепочку сертификатов
     *
     * @param keystore хранилище сретификатов в формате pem
     * @return цепочка сертификатов
     * @throws CertificateException ошибка получения сертификата
     */
    private static Certificate[] readCertificateChain(String keystore) throws CertificateException {

        Matcher matcher = CERT_PATTERN.matcher(keystore);
        if (!matcher.find()) {
            throw new CertificateException(String.format("Ошибка: не найден сертификат '%s'", keystore));
        }
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        List<X509Certificate> certificates = new ArrayList<>();

        int start = 0;
        while (matcher.find(start)) {
            byte[] buffer = base64Decode(matcher.group(1));
            certificates.add((X509Certificate) certificateFactory.generateCertificate(new ByteArrayInputStream(buffer)));
            start = matcher.end();
        }

        if (certificates.isEmpty()) {
            throw new CertificateException(String.format("Ошибка: не найдено ни одного сертификата '%s'", keystore));
        }

        return certificates.stream().toArray(Certificate[]::new);
    }

    private static PublicKey readBase64PublicKey(String base64, String algorithm) throws InvalidKeySpecException, NoSuchAlgorithmException {

        byte[] decoded = base64Decode(base64);

        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);

        KeyFactory keyFactory = KeyFactory.getInstance(algorithm);

        return keyFactory.generatePublic(spec);
    }

    private static KeyStore loadKeyStoreFromResource(String keystoreType,
                                                     String resourceName,
                                                     String password) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {

        final KeyStore keystore = KeyStore.getInstance(keystoreType);
        try (InputStream is = KeyStoreUtils.class.getClassLoader().getResourceAsStream(resourceName)) {
            keystore.load(is, null == password ? null : password.toCharArray());
        }

        return keystore;
    }

    /**
     * Возвращает декодированное тело сертификата или закрытого ключа
     *
     * @param body тело закрытого ключа или сертификата
     * @return декодированное тело сертификата или закрытого ключа
     */
    private static byte[] base64Decode(String body) {
        return Base64.getMimeDecoder().decode(body.getBytes(US_ASCII));
    }

}
