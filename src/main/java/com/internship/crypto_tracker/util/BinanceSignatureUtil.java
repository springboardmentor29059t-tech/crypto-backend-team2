package com.internship.crypto_tracker.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Hex; 
import org.springframework.stereotype.Component;

@Component
public class BinanceSignatureUtil {

    private static final String HMAC_SHA256 = "HmacSHA256";
     
    public String createSignature(String data, String apiSecret) {
        try {
            
            SecretKeySpec secretKeySpec = new SecretKeySpec(apiSecret.getBytes(), HMAC_SHA256);

            
            Mac mac = Mac.getInstance(HMAC_SHA256);

            
            mac.init(secretKeySpec);

            
            byte[] hmacBytes = mac.doFinal(data.getBytes());

            
            return Hex.encodeHexString(hmacBytes);

        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate HMAC signature", e);
        }
    }
}