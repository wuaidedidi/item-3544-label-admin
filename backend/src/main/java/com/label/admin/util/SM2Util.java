package com.label.admin.util;

import org.bouncycastle.asn1.gm.GMNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.engines.SM2Engine;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.Security;

@Component
public class SM2Util {

    private static final Logger log = LoggerFactory.getLogger(SM2Util.class);

    @Value("${app.sm2.private-key}")
    private String privateKeyHex;

    @Value("${app.sm2.public-key}")
    private String publicKeyHex;

    private ECDomainParameters domainParameters;

    @PostConstruct
    public void init() {
        Security.addProvider(new BouncyCastleProvider());
        X9ECParameters gmParameters = GMNamedCurves.getByName("sm2p256v1");
        domainParameters = new ECDomainParameters(
                gmParameters.getCurve(),
                gmParameters.getG(),
                gmParameters.getN(),
                gmParameters.getH()
        );
        log.info("SM2加密组件初始化完成");
    }

    public String decrypt(String cipherTextHex) {
        try {
            byte[] cipherBytes = hexToBytes(cipherTextHex);
            log.debug("SM2解密: 输入长度={}, 首字节=0x{}", cipherBytes.length, String.format("%02x", cipherBytes[0]));

            // 确保有04前缀
            byte[] withPrefix;
            if (cipherBytes[0] != 0x04) {
                withPrefix = new byte[cipherBytes.length + 1];
                withPrefix[0] = 0x04;
                System.arraycopy(cipherBytes, 0, withPrefix, 1, cipherBytes.length);
            } else {
                withPrefix = cipherBytes;
            }

            BigInteger privateKey = new BigInteger(privateKeyHex, 16);
            ECPrivateKeyParameters privateKeyParameters = new ECPrivateKeyParameters(privateKey, domainParameters);

            // 尝试C1C3C2模式（sm-crypto mode=0）
            try {
                SM2Engine engine = new SM2Engine(SM2Engine.Mode.C1C3C2);
                engine.init(false, privateKeyParameters);
                byte[] decrypted = engine.processBlock(withPrefix, 0, withPrefix.length);
                return new String(decrypted, StandardCharsets.UTF_8);
            } catch (Exception e1) {
                log.debug("C1C3C2模式解密失败，尝试C1C2C3模式: {}", e1.getMessage());
            }

            // 回退到C1C2C3模式
            SM2Engine engine2 = new SM2Engine(SM2Engine.Mode.C1C2C3);
            engine2.init(false, privateKeyParameters);
            byte[] decrypted = engine2.processBlock(withPrefix, 0, withPrefix.length);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("SM2解密失败: {}", e.getMessage());
            throw new RuntimeException("数据解密失败", e);
        }
    }

    public String getPublicKey() {
        return publicKeyHex;
    }

    private byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}
