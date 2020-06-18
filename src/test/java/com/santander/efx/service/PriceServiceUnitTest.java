package com.santander.efx.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import com.santander.efx.BaseTest;
import com.santander.efx.exception.ServiceException;
import com.santander.efx.model.Price;
import com.santander.efx.repository.PriceRepository;
import com.santander.efx.utils.DateUtils;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PriceServiceUnitTest extends BaseTest {

	private static final String INSTRUMENT_NAME = "GBP/USD";
	private static final int EXTERNAL_ID = 4;
	private static final String ASK = "1.2560";
	private static final String BID = "1.2500";
	private static final String DATE = "01-06-2020 12:01:02:100";

	@Autowired
	private PriceService priceService;
	
	@Autowired
	private DateUtils dateUtils;

	@MockBean
	private PriceRepository priceRepository;

	@Before
	public void setUp() {
		
		Price price = new Price();
		price.setInstrumentName(INSTRUMENT_NAME);
		price.setExternalId(EXTERNAL_ID);
		price.setBid(new BigDecimal(BID));
		price.setAsk(new BigDecimal(ASK));
		price.setDate(dateUtils.formatDate(DATE));
		
		Optional<Price> opPrice = Optional.of(price);
		
		when(this.priceRepository.save(any(Price.class))).then(returnsFirstArg());
		when(this.priceRepository.findById(anyString())).thenReturn(opPrice);
		
	}

	@Test
	public void shouldGetPriceByInstrumentNameReturnValue() {
		
		Price price = priceService.getPriceByInstrumentName(anyString());
		assertThat(price.getInstrumentName()).isEqualTo(INSTRUMENT_NAME);
		assertThat(price.getExternalId()).isEqualTo(EXTERNAL_ID);
		assertThat(price.getBid()).isEqualTo(new BigDecimal(BID));
		assertThat(price.getAsk()).isEqualTo(new BigDecimal(ASK));
		assertThat(price.getDate()).isEqualTo(dateUtils.formatDate(DATE));
		
	}
	

	@Test
	public void shouldGetPriceByInstrumentNameReturnNullValues() {
		
		when(this.priceRepository.findById(anyString())).thenReturn(Optional.empty());
		
		Price price = priceService.getPriceByInstrumentName(anyString());
		assertThat(price.getInstrumentName()).isNull();
		assertThat(price.getExternalId()).isNull();
		assertThat(price.getBid()).isNull();
		assertThat(price.getAsk()).isNull();
		assertThat(price.getDate()).isNull();
		
	}
	
	@Test
	public void shouldThrowErrorIfStrPriceIsWrong() {
		assertThatThrownBy(() -> {
			priceService.saveCsvPrice("1, EUR/USD, ,1.2450,01-06-2020 12:01:01:001");
		}).isInstanceOf(ServiceException.class);
	}
	
	@Test
	public void shouldThrowErrorIfStrPriceIsMissing() {
		assertThatThrownBy(() -> {
			priceService.saveCsvPrice("1, EUR/USD, 1.2450,01-06-2020 12:01:01:001");
		}).isInstanceOf(ServiceException.class);
	}
	
	@Test
	public void shouldAdjustBidPrice() {
		
		ArgumentCaptor<Price> captor = ArgumentCaptor.forClass(Price.class);

		priceService.saveCsvPrice("5, GBP/USD, 1.2500, 1.2560, 01-06-2020 13:01:02:100");
		
		verify(this.priceRepository).save(captor.capture());
		assertThat(captor.getValue().getBid()).isEqualTo(new BigDecimal("1.1250"));
		
	}
	
	@Test
	public void shouldAdjustAskPrice() {
		
		ArgumentCaptor<Price> captor = ArgumentCaptor.forClass(Price.class);

		priceService.saveCsvPrice("5, GBP/USD, 1.2500, 1.2560, 01-06-2020 13:01:02:100");
		
		verify(this.priceRepository).save(captor.capture());
		assertThat(captor.getValue().getAsk()).isEqualTo(new BigDecimal("1.3816"));
		
	}
	
	@Test
	public void shouldNotSaveIfDateIsBefore() {
		
		priceService.saveCsvPrice("4, GBP/USD, 1.2500, 1.2560, 01-06-2020 11:01:02:100");
		
		verify(this.priceRepository, never()).save(any(Price.class));
		
	}	
	
	@Test
	public void shouldSaveIfDateIsAfter() {
		
		ArgumentCaptor<Price> captor = ArgumentCaptor.forClass(Price.class);
		
		priceService.saveCsvPrice("4, GBP/USD, 1.2500, 1.2560, 01-06-2020 13:01:02:100");
		
		verify(this.priceRepository).save(captor.capture());
		
		assertThat(captor.getValue().getInstrumentName()).isEqualTo("GBP/USD");
		assertThat(captor.getValue().getExternalId()).isEqualTo(4);
		assertThat(captor.getValue().getBid()).isEqualTo(new BigDecimal("1.1250"));
		assertThat(captor.getValue().getAsk()).isEqualTo(new BigDecimal("1.3816"));
		assertThat(captor.getValue().getDate()).isEqualTo(dateUtils.formatDate("01-06-2020 13:01:02:100"));
		
	}

}
