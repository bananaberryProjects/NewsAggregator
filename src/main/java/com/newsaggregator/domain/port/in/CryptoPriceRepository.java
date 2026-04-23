package com.newsaggregator.domain.port.in;

import com.newsaggregator.domain.model.CryptoPrice;
import java.util.List;
import java.util.Optional;

public interface CryptoPriceRepository {
    CryptoPrice save(CryptoPrice price);
    List<CryptoPrice> findAllCurrent();
    Optional<CryptoPrice> findByCoinId(String coinId);
}
