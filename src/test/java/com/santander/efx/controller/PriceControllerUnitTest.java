package com.santander.efx.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import com.santander.efx.BaseTest;
import com.santander.efx.service.PriceService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PriceControllerUnitTest extends BaseTest {

	@Autowired
	private PriceController priceController;

	@MockBean
	private PriceService priceService;

	@Test
	public void shouldReplaceChar() throws Exception {

		ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

		priceController.getPriceByInstrumentName("GNP-USD");

		verify(this.priceService).getPriceByInstrumentName(captor.capture());
		assertThat(captor.getValue(), is("GNP/USD"));

	}

}
