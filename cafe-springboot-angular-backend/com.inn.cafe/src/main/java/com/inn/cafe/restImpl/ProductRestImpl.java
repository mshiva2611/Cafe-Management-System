package com.inn.cafe.restImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.inn.cafe.constants.CafeConstants;
import com.inn.cafe.rest.ProductRest;
import com.inn.cafe.service.ProductService;
import com.inn.cafe.utils.CafeUtils;
import com.inn.cafe.wrapper.ProductWrapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class ProductRestImpl implements ProductRest {
	
	@Autowired
	ProductService productService;

	@Override
	public ResponseEntity<String> addNewProduct(Map<String, String> requestMap) {
		log.info("ProductRestImpl || addNewProduct method invoked ");
		try {
			
			return productService.addNewProduct(requestMap);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@Override
	public ResponseEntity<List<ProductWrapper>> getAllProduct() {
		
		log.info("ProductRestImpl || getAllProduct method invoked ");
		try {
			
		return	productService.getAllProduct();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR); 
	}

	@Override
	public ResponseEntity<String> updateProduct(Map<String, String> requestMap) {
		log.info("ProductRestImpl || updateProduct method invoked ");
		try {
			return productService.updateProduct(requestMap);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@Override
	public ResponseEntity<String> deleteProduct(Integer id) {
		log.info("ProductRestImpl || deleteProduct method invoked ");
	  try {
		  return productService.deleteProduct(id);
		
	} catch (Exception e) {
		e.printStackTrace();
	}
		return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@Override
	public ResponseEntity<String> updateStatus(Map<String, String> requestMap) {
		log.info("ProductRestImpl || updateStatus method invoked ");
		  try {
			  return productService.updateStatus(requestMap);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
			return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	@Override
	public ResponseEntity<List<ProductWrapper>> getByCategory(Integer id) {
		log.info("ProductRestImpl || getByCategory method invoked ");
		  try {
			  return productService.getByCategory(id);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
			return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
		}

	@Override
	public ResponseEntity<ProductWrapper> getProductById(Integer id) {
		log.info("ProductRestImpl || getProductById method invoked ");
		try {
			
			return productService.getProductById(id);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ResponseEntity<>(new ProductWrapper(), HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
