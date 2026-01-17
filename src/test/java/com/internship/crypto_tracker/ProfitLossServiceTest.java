package com.internship.crypto_tracker;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.internship.crypto_tracker.dto.ProfitLossReportDTO;
import com.internship.crypto_tracker.service.ProfitLossService;

@SpringBootTest
class ProfitLossServiceTest {

    @Autowired
    private ProfitLossService profitLossService;

    @Test
    void testTaxLogic() {
        Long userId = 1L; 
        List<ProfitLossReportDTO> report = profitLossService.generateReport(userId);
        
        assertThat(report).isNotNull();
        if(!report.isEmpty()) {
            ProfitLossReportDTO dto = report.get(0);
            System.out.println("---- QA REPORT ----");
            System.out.println("Asset: " + dto.getAssetSymbol());
            System.out.println("Total Realized: " + dto.getRealizedProfit());
            System.out.println("Short Term (High Tax): " + dto.getShortTermProfit());
            System.out.println("Long Term (Low Tax): " + dto.getLongTermProfit());
            System.out.println("-------------------");
        }
    }
}