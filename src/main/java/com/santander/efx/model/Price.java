package com.santander.efx.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import com.fasterxml.jackson.annotation.JsonFormat;

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

	public String getInstrumentName() {
		return instrumentName;
	}

	public void setInstrumentName(String instrumentName) {
		this.instrumentName = instrumentName;
	}

	public Integer getExternalId() {
		return externalId;
	}

	public void setExternalId(Integer externalId) {
		this.externalId = externalId;
	}

	public BigDecimal getBid() {
		return bid;
	}

	public void setBid(BigDecimal bid) {
		this.bid = bid;
	}

	public BigDecimal getAsk() {
		return ask;
	}

	public void setAsk(BigDecimal ask) {
		this.ask = ask;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	@Override
	public String toString() {
		return String.format("%s, %s, %s, %s, %s", instrumentName, externalId, bid, ask, date);
	}

}
