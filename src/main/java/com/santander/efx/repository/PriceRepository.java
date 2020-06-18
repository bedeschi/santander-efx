package com.santander.efx.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.santander.efx.model.Price;

@Repository
public interface PriceRepository extends CrudRepository<Price, String> {

	Optional<Price> findById(String instrumentName);
	List<Price> findAll();

}
