package com.santander.efx;

import static java.util.Arrays.asList;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.santander.efx.model.Price;
import com.santander.efx.utils.DateUtils;

public class BaseTest {

	@Autowired
	private DateUtils dateUtils;

	protected List<Price> getMockPriceList() {
		List<Price> priceList = asList(
				new Price("EUR/USD", 1, new BigDecimal("1.1000"), new BigDecimal("1.2000"),
						dateUtils.formatDate("01-06-2020 12:01:01:001")),
				new Price("EUR/JPY", 2, new BigDecimal("119.60"), new BigDecimal("119.90"),
						dateUtils.formatDate("01-06-2020 12:01:02:001")),
				new Price("GBP/USD", 3, new BigDecimal("1.2500"), new BigDecimal("1.2560"),
						dateUtils.formatDate("01-06-2020 12:01:02:001")),
				new Price("GBP/USD", 4, new BigDecimal("1.2500"), new BigDecimal("1.2560"),
						dateUtils.formatDate("01-06-2020 12:01:02:100")));
		return priceList;
	}

	protected Price getMockPrice() {
		return new Price("EUR/USD", 1, new BigDecimal("1.1000"), new BigDecimal("1.2000"),
				dateUtils.formatDate("01-06-2020 12:01:01:001"));
	}

	protected String getMockCsvPrice() {
		return "1, EUR/USD, 1.1900,1.2450,01-06-2020 12:01:01:001";
	}

	protected List<String> getMockCsvPrices() {
		return asList("1, EUR/USD, 1.1000,1.2000,01-06-2020 12:01:01:001",
				"2, EUR/JPY, 119.60,119.90,01-06-2020 12:01:02:001",
				"3, GBP/USD, 1.2500,1.2560,01-06-2020 12:01:02:001",
				"5, GBP/USD, 1.3500,1.3560,01-06-2020 12:02:02:100",
				"4, GBP/USD, 1.2500,1.2560,01-06-2020 12:01:02:100");
	}
}
