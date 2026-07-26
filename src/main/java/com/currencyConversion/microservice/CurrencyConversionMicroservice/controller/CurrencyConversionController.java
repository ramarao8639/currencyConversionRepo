package com.currencyConversion.microservice.CurrencyConversionMicroservice.controller;

import java.math.BigDecimal;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.currencyConversion.microservice.CurrencyConversionMicroservice.configuration.CurrencyExchangeProxy;
import com.currencyConversion.microservice.CurrencyConversionMicroservice.model.CurrencyConversion;

@Configuration(proxyBeanMethods = false)
class RestTemplateConfiguration {
    
    @Bean
    RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }
}



@RestController
public class CurrencyConversionController {

	private Logger logger=LoggerFactory.getLogger(getClass());
	@Autowired
	private CurrencyExchangeProxy proxy;
	
	 @Autowired
	    private RestTemplate restTemplate; 
	
	@GetMapping("/currency-conversion/from/{from}/to/{to}/quantity/{quantity}")
	public CurrencyConversion calculateCurrencyConversion(
			@PathVariable String from,
			@PathVariable String to,
			@PathVariable BigDecimal quantity
			) {
		logger.info("calculateCurrencyConversion  called with {} to with {}",from,to);
		HashMap<String, String> uriVariables = new HashMap();
		uriVariables.put("from",from);
		uriVariables.put("to",to);
		
//		ResponseEntity<CurrencyConversion> responseEntity = new RestTemplate().getForEntity
		ResponseEntity<CurrencyConversion> responseEntity = restTemplate.getForEntity
				("http://currencymicroservice:8000/currency-exchange/from/{from}/to/{to}", 
						CurrencyConversion.class, uriVariables);
		
		CurrencyConversion currencyConversion = responseEntity.getBody();
		
		return new CurrencyConversion(currencyConversion.getId(), 
				from, to, quantity, 
				currencyConversion.getConversionMultiple(), 
				quantity.multiply(currencyConversion.getConversionMultiple()), 
				currencyConversion.getEnvironment()+ " " + "rest template");
		
	}

	@GetMapping("/currency-conversion-feign/from/{from}/to/{to}/quantity/{quantity}")
	public CurrencyConversion calculateCurrencyConversionFeign(
			@PathVariable String from,
			@PathVariable String to,
			@PathVariable BigDecimal quantity
			) {
				
		logger.info("calculateCurrencyConversion  called with {} to with {}",from,to);

		CurrencyConversion currencyConversion = proxy.retrieveExchangeValue(from, to);
		
		return new CurrencyConversion(currencyConversion.getId(), 
				from, to, quantity, 
				currencyConversion.getConversionMultiple(), 
				quantity.multiply(currencyConversion.getConversionMultiple()), 
				currencyConversion.getEnvironment() + " " + "feign");
		
	}

}
