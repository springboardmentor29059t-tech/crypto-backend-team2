package com.crypto.portfolio.dto;

import java.util.List;

public class TaxSummaryDTO {
    private String financialYear;
    private double shortTermGains;
    private double longTermGains;
    private double totalTaxableAmount;
    private List<TaxReportItemDTO> details;

    public TaxSummaryDTO(String financialYear, double shortTermGains, double longTermGains, double totalTaxableAmount,
            List<TaxReportItemDTO> details) {
        this.financialYear = financialYear;
        this.shortTermGains = shortTermGains;
        this.longTermGains = longTermGains;
        this.totalTaxableAmount = totalTaxableAmount;
        this.details = details;
    }

    public String getFinancialYear() {
        return financialYear;
    }

    public double getShortTermGains() {
        return shortTermGains;
    }

    public double getLongTermGains() {
        return longTermGains;
    }

    public double getTotalTaxableAmount() {
        return totalTaxableAmount;
    }

    public List<TaxReportItemDTO> getDetails() {
        return details;
    }
}
