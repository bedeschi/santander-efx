package com.santander.efx.service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.santander.efx.exception.ServiceException;
import com.santander.efx.model.Price;
import com.santander.efx.repository.PriceRepository;
import com.santander.efx.utils.DateUtils;

@Service
public class PriceService {

	@Autowired
	private DateUtils dateUtils;

	@Autowired
	private PriceRepository priceRepository;

	private final static BigDecimal SPREAD = new BigDecimal("0.1");
	private final static int SCALE = 4;
	private final static int ROUNDING_MODE = BigDecimal.ROUND_HALF_DOWN;

	public List<Price> getAll() {
		return this.priceRepository.findAll();
	}

	public Price getPriceByInstrumentName(String instrumentName) {

		Optional<Price> opPrice = this.priceRepository.findById(instrumentName);
		return opPrice.orElse(new Price());
	}

	public void saveCsvPrice(String strPrice) {

		String[] arrPrice = extractCsv(strPrice);

		Price price = new Price();
		price.setExternalId(Integer.valueOf(arrPrice[0]));
		price.setInstrumentName(arrPrice[1]);
		price.setBid(new BigDecimal(arrPrice[2]).setScale(SCALE, ROUNDING_MODE));
		price.setAsk(new BigDecimal(arrPrice[3]).setScale(SCALE, ROUNDING_MODE));
		price.setDate(dateUtils.formatDate(arrPrice[4]));

		Optional<Price> oldPrice = this.priceRepository.findById(price.getInstrumentName());

		if (!oldPrice.isPresent() || oldPrice.isPresent() && price.getDate().after(oldPrice.get().getDate())) {
			this.priceRepository.save(adjustedPrice(price));
		}

	}

	private String[] extractCsv(String strPrice) {

		List<String> csvList = Arrays.asList(strPrice.split(","));

		
		
		if (csvList.size() != 5 || csvList.stream().anyMatch(e -> StringUtils.isBlank(e))) {
			throw new ServiceException();
		}

		return csvList.stream().map(e -> e.trim()).toArray(String[]::new);
	}

	private Price adjustedPrice(Price price) {

		BigDecimal bidSpread = BigDecimal.ONE.subtract(SPREAD);
		BigDecimal askSpread = BigDecimal.ONE.add(SPREAD);

		price.setBid(price.getBid().multiply(bidSpread).setScale(SCALE, ROUNDING_MODE));
		price.setAsk(price.getAsk().multiply(askSpread).setScale(SCALE, ROUNDING_MODE));

		return price;
	}

}
