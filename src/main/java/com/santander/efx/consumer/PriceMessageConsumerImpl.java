package com.santander.efx.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.santander.efx.service.PriceService;

@Component
public class PriceMessageConsumerImpl implements PriceMessageConsumer {

	@Autowired
	private PriceService priceService;
	
	@Override
	public void onMessage(String csvPrice) {
		priceService.saveCsvPrice(csvPrice);
	}

}
