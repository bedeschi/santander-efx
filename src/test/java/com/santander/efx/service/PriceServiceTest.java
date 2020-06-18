package com.santander.efx.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.santander.efx.BaseIntegrationTest;
import com.santander.efx.config.RedisTestConfiguration;
import com.santander.efx.model.Price;
import com.santander.efx.repository.PriceRepository;
import com.santander.efx.utils.DateUtils;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = RedisTestConfiguration.class)
public class PriceServiceTest extends BaseIntegrationTest {

	@Autowired
	private DateUtils dateUtils;
	
	@Autowired
	private PriceService priceService;

	@Autowired
	private PriceRepository priceRepository;

	@Test
	public void shouldReturnAllList() throws Exception {

		List<Price> prices = getMockPriceList();
		priceRepository.saveAll(prices);

		List<Price> result = priceService.getAll();
		assertNotNull(result);
		assertThat(result.size()).isEqualTo(3);
		assertThat(result).extracting("instrumentName").containsExactlyInAnyOrder("EUR/USD", "GBP/USD", "EUR/JPY");

	}
	
	@Test
	public void shouldReturnPriceByInstrumentName() throws Exception {

		List<Price> prices = getMockPriceList();
		priceRepository.saveAll(prices);
		
		Price result = priceService.getPriceByInstrumentName("GBP/USD");
		assertNotNull(result);
		assertThat(result.getInstrumentName()).isEqualTo("GBP/USD");
		assertThat(result.getExternalId()).isEqualTo(4);
		assertThat(result.getBid()).isEqualTo(new BigDecimal("1.2500"));
		assertThat(result.getAsk()).isEqualTo(new BigDecimal("1.2560"));
		assertThat(result.getDate()).isEqualTo(dateUtils.formatDate("01-06-2020 12:01:02:100"));
		
	}
	
	@Test
	public void shouldReturnEmptyPriceWhenInstrumentNameDoesntExist() throws Exception {

		List<Price> prices = getMockPriceList();
		priceRepository.saveAll(prices);
		
		Price result = priceService.getPriceByInstrumentName("BRL/USD");
		assertNotNull(result);
		assertThat(result.getInstrumentName()).isNull();
		assertThat(result.getExternalId()).isNull();
		assertThat(result.getBid()).isNull();
		assertThat(result.getAsk()).isNull();
		assertThat(result.getDate()).isNull();
		
	}

    @Test
    public void shouldSaveCsvPrice() {

    	String csvPrice = getMockCsvPrice();
        priceService.saveCsvPrice(csvPrice);
        
        List<Price> result = priceRepository.findAll();
		
        assertNotNull(result);
		assertThat(result.size()).isEqualTo(1);
		assertThat(result.get(0).getInstrumentName()).isEqualTo("EUR/USD");
		assertThat(result.get(0).getExternalId()).isEqualTo(1);
		assertThat(result.get(0).getBid()).isEqualTo(new BigDecimal("1.0710"));
		assertThat(result.get(0).getAsk()).isEqualTo(new BigDecimal("1.3695"));
		assertThat(result.get(0).getDate()).isEqualTo(dateUtils.formatDate("01-06-2020 12:01:01:001"));
		
        
	}



}
