package com.santander.efx.consumer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;

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

@RunWith(SpringRunner.class)
@SpringBootTest(classes = RedisTestConfiguration.class)
public class PriceMessageConsumerImplTest extends BaseIntegrationTest{

	@Autowired
	private PriceMessageConsumerImpl consumer;
	
	@Autowired
	private PriceRepository priceRepository;
	
    @Test
    public void shouldSavePrice() {

		String csvPrice = getMockCsvPrice();
		consumer.onMessage(csvPrice);
        List<Price> findAll = (List<Price>) priceRepository.findAll();
        
        assertNotNull(findAll);
		assertThat(findAll.size(), is(1));
 
	}
	
}
