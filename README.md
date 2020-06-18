
# Santander EFX - Market Price Handler

## Assumptions
For the present project I used Redis as a database. In real a world development, I would persist data in a more robust database, for example a SQL or a NoSQL one. For the given purpose, however, I chose to use a Key-Value database to simplify the implementation. Either way, the best solution for a real implementation would be with this Key-Value database along with another database, for reasons further to be discussed. 

This software is lacking a better exception treatment. I "drafted" a few in the `ServiceException.java`, but the code doesn't catch all exceptions in order to give a better response to the API. I started to develop a `@ControllerAdvice` but since time was an issue, I deleted it and assumed that the API response wouldn't cover the exceptions.

The [mutation tests](https://pitest.org) gave almost 100% of coverage, which is great. It only fails in `DateUtils` which isn't a priority for me. So, for the moment, I believe I have enough unit and integration tests to guarantee a high quality of implementation.

For the code's presentation, I removed the imports and a few methods that wouldn't alter it, but they're all available in the git repository. I also ignored the `.properties` files, since they're not relevant for this purpose.

## Files
On the first section, I'll present the following and most important classes:
- `Price.java` (Model that define price object)
- `PriceController.java` (API Rest Controller)
- `PriceControllerTest.java` (Controller integration tests)
- `PriceControllerUnitTest.java` (Controller unit tests)
- `PriceService.java` (Service)
- `PriceServiceTest.java` (Service integration tests)
- `PriceServiceUnitTest.java` (Servuce unit tests)

For the second one, these relevant classes:
- `PriceRepository.java` (Redis repository)
- `PriceRepositoryTest.java` (Repository integration tests)
- `PriceMessageConsumerImpl.java` (Consumer implementation)
- `PriceMessageConsumerImplTest.java` (Consumer tests)

Finally, some other classes that are also part of the code:
- `BaseTest.java` (Parent test class)
- `BaseIntegrationTest.java`(Base integration test. Extends base test)
- `DateUtils.java`(Format date)
- `ServiceException.java`(Custom exception class)

These remaining classes won't be shown here, but you can find them in the git repository:
- `Application.java` (Main application class)
- `ApplicationTests.java` (Default mais application test)
- `RedisConfiguration.java` (Configuration redis class)
- `RedisTestConfiguration.java` (Test of configuration)
- `PriceMessageConsumer.java` (Consumer inteface)



## Code
### Main classes
`Price.java`
```java
@RedisHash("price")
public class Price implements Serializable {

	private static final long serialVersionUID = -116205108527327894L;

	@Id
	private String instrumentName;
	private Integer externalId;
	private BigDecimal bid;
	private BigDecimal ask;
	
	@JsonFormat(pattern="dd-MM-yyyy HH:mm:ss:SSS", timezone="Europe/Lisbon")
	private Date date;

	public Price() {
		super();
	}

	public Price(String instrumentName, Integer externalId, BigDecimal bid, BigDecimal ask, Date date) {
		super();
		this.instrumentName = instrumentName;
		this.externalId = externalId;
		this.bid = bid;
		this.ask = ask;
		this.date = date;
	}

	//getters and setters

	@Override
	public String toString() {
		return String.format("%s, %s, %s, %s, %s", instrumentName, externalId, bid, ask, date);
	}

}

```

`PriceController.java`
```java
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

```

`PriceControllerTest.java`
```java
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

```

`PriceControllerUnitTest.java`
```java
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

```

`PriceService.java`
```java
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

```

`PriceServiceTest.java`
```java
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

```

`PriceServiceUnitTest.java`
```java
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

```


### Relevant classes:
`PriceRepository.java`
```java
@Repository
public interface PriceRepository extends CrudRepository<Price, String> {

	Optional<Price> findById(String instrumentName);
	List<Price> findAll();

}

```

`PriceRepositoryTest.java`
```java
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

```

`PriceMessageConsumerImpl.java`
```java
@Component
public class PriceMessageConsumerImpl implements PriceMessageConsumer {

	@Autowired
	private PriceService priceService;
	
	@Override
	public void onMessage(String csvPrice) {
		priceService.saveCsvPrice(csvPrice);
	}

}

```

`PriceMessageConsumerImplTest.java`
```java
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

```


### Part of the code:
`BaseTest.java`
```java
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

```

`BaseIntegrationTest.java`
```java
public class BaseIntegrationTest extends BaseTest {

	@Autowired 
	private RedisTemplate< String, String > template;
	
	@Before
	public void setUp() {
		template.execute(new RedisCallback<Void>() {
			@Override
			public Void doInRedis(RedisConnection connection) throws DataAccessException {
				connection.flushDb();
				return null;
			}
		});
	}
	
}

```

`DateUtils.java`
```java
@Component
public class DateUtils {

	private static final String DATE_FORMAT = "dd-MM-yyyy HH:mm:ss:SSS";

	public Date formatDate(String strDate) {
		try {
			return new SimpleDateFormat(DATE_FORMAT).parse(strDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

}

```

`ServiceException.java`
```java
public class ServiceException extends RuntimeException {

	private static final long serialVersionUID = 6769798308099674356L;

	public ServiceException() {
		super("Some error occurred");
		printStackTrace();
	}

}

```

