package com.santander.efx.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.santander.efx.model.Price;
import com.santander.efx.service.PriceService;

@RestController
@RequestMapping("/price")
public class PriceController {

	@Autowired
	private PriceService priceService;

	@GetMapping
	public List<Price> getAllPrices() {
		return priceService.getAll();
	}

	@GetMapping("/{instrumentName}")
	public Price getPriceByInstrumentName(@PathVariable String instrumentName) {
		return priceService.getPriceByInstrumentName(instrumentName.replace("-", "/"));
	}
	
}
