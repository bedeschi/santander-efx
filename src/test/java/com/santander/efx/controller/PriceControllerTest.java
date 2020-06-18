package com.santander.efx.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.santander.efx.BaseIntegrationTest;
import com.santander.efx.config.RedisTestConfiguration;
import com.santander.efx.consumer.PriceMessageConsumerImpl;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = RedisTestConfiguration.class)
public class PriceControllerTest extends BaseIntegrationTest {

	protected MockMvc mockMvc;

	@Autowired
	private PriceController priceController;
	
	@Autowired
	private PriceMessageConsumerImpl consumer;

	@Before
	public void setUp() {
		super.setUp();
		
		MockitoAnnotations.initMocks(this);
		this.mockMvc = MockMvcBuilders.standaloneSetup(priceController).build();

	}

	@Test
	public void shouldGetAllPrices() throws Exception {
		
		String csvPrice = getMockCsvPrice();
		consumer.onMessage(csvPrice);
		
		mockMvc.perform(get("/price").contentType(APPLICATION_JSON))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].instrumentName", is("EUR/USD")))
			.andExpect(jsonPath("$[0].externalId", is(1)))
			.andExpect(jsonPath("$[0].bid", is(1.0710)))
			.andExpect(jsonPath("$[0].ask", is(1.3695)))
			.andExpect(jsonPath("$[0].date", is("01-06-2020 12:01:01:001")));
		
	}

	@Test
	public void shouldGetPriceByInstrumentName() throws Exception {
		
		getMockCsvPrices().forEach(csv -> consumer.onMessage(csv));
		
		mockMvc.perform(get("/price/GBP-USD").contentType(APPLICATION_JSON))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.instrumentName", is("GBP/USD")))
			.andExpect(jsonPath("$.externalId", is(5)))
			.andExpect(jsonPath("$.bid", is(1.2150)))
			.andExpect(jsonPath("$.ask", is(1.4916)))
			.andExpect(jsonPath("$.date", is("01-06-2020 12:02:02:100")));
		
	}
	
	@Test
	public void shouldReturnEmptyIfInstrumentNameDoesntExist() throws Exception {
		
		getMockCsvPrices().forEach(csv -> consumer.onMessage(csv));
		
		mockMvc.perform(get("/price/BRL-USD").contentType(APPLICATION_JSON))
		.andDo(MockMvcResultHandlers.print())
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.instrumentName", nullValue()))
		.andExpect(jsonPath("$.externalId", nullValue()))
		.andExpect(jsonPath("$.bid", nullValue()))
		.andExpect(jsonPath("$.ask", nullValue()))
		.andExpect(jsonPath("$.date", nullValue()));
		
	}

}
