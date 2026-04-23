package com.newsaggregator.infrastructure.adapter.web;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.newsaggregator.application.service.CryptoPriceService;

@RestController
@RequestMapping("/api/crypto")
public class CryptoPriceController {

    private final CryptoPriceService cryptoPriceService;

    public CryptoPriceController(CryptoPriceService cryptoPriceService) {
        this.cryptoPriceService = cryptoPriceService;
    }

    @GetMapping("/prices")
    public Map<String, Object> getCurrentPrices() {
        return cryptoPriceService.getCurrentPrices();
    }

    @GetMapping("/history/{coinId}")
    public Map<String, Object> getPriceHistory(@PathVariable String coinId, @RequestParam(defaultValue = "7") int days) {
        return cryptoPriceService.getPriceHistory(coinId, days);
    }
}
