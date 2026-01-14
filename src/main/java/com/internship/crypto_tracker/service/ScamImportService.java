package com.internship.crypto_tracker.service;

import java.time.LocalDateTime;
import java.util.Iterator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.internship.crypto_tracker.model.ScamToken;
import com.internship.crypto_tracker.repository.ScamTokenRepository;

@Service
public class ScamImportService {

    private static final String SCAM_DB_URL = "https://raw.githubusercontent.com/CryptoScamDB/blacklist/master/data/scams.json";

    @Autowired
    private ScamTokenRepository scamTokenRepository;

    @Autowired
    private RestTemplate restTemplate;

    public String syncScamList() {
        try {
            String jsonResponse = restTemplate.getForObject(SCAM_DB_URL, String.class);
            
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(jsonResponse);

            int count = 0;

            Iterator<String> fieldNames = root.fieldNames();
            while (fieldNames.hasNext()) {
                String key = fieldNames.next(); 
                JsonNode scamNode = root.get(key);

                if (scamNode.has("addresses")) {
                    JsonNode addresses = scamNode.get("addresses");
                    for (JsonNode addr : addresses) {
                        String contractAddress = addr.asText();

                        if (!scamTokenRepository.findByContractAddress(contractAddress).isPresent()) {
                            ScamToken token = new ScamToken();
                            token.setContractAddress(contractAddress);
                            token.setChain("ETH"); 
                            token.setRiskLevel(ScamToken.RiskLevel.HIGH);
                            token.setSource("CryptoScamDB");
                            token.setLastSeen(LocalDateTime.now());
                            
                            scamTokenRepository.save(token);
                            count++;
                        }
                    }
                }
            }
            return "✅ Synced successfully. Added " + count + " new scam tokens.";

        } catch (Exception e) {
            return "❌ Error syncing scam list: " + e.getMessage();
        }
    }
}