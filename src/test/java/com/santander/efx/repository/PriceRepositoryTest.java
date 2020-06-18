package com.santander.efx.repository;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
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
import com.santander.efx.utils.DateUtils;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = RedisTestConfiguration.class)
public class PriceRepositoryTest extends BaseIntegrationTest {

	@Autowired
	private PriceRepository priceRepository;

	@Autowired
	private DateUtils dateUtils;
	
	@Test
	public void shouldSavePrice() {

		Price price = getMockPrice();
		assertNotNull(priceRepository.save(price));
		assertThat(price.getInstrumentName(), is("EUR/USD"));
		assertThat(price.getBid(), is(new BigDecimal("1.1000")));
		assertThat(price.getAsk(), is(new BigDecimal("1.2000")));
		assertThat(price.getExternalId(), is(1));
		assertThat(price.getDate(), is(dateUtils.formatDate("01-06-2020 12:01:01:001")));
		
	}

	@Test
	public void shouldSavePriceList() {

		List<Price> priceList = getMockPriceList();
		assertNotNull(priceRepository.saveAll(priceList));

	}

	@Test
	public void shouldSaveAndResturnCorrectList() {

		List<Price> priceList = getMockPriceList();

		priceRepository.saveAll(priceList);
		List<Price> result = (List<Price>) priceRepository.findAll();

		assertEquals(3, result.size());

	}

}
