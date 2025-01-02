package com.inn.cafe.rest;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.inn.cafe.wrapper.ProductWrapper;



@RequestMapping("/product")
public interface ProductRest {

	@PostMapping("/addNewProduct")
	ResponseEntity<String> addNewProduct(@RequestBody Map<String, String> requestMap);
	
	
	@GetMapping("/getAllProduct")
	ResponseEntity<List<ProductWrapper>> getAllProduct();
	
	@PostMapping("/updateProduct")
	ResponseEntity<String> updateProduct(@RequestBody Map<String, String> requestMap);
	
	@PostMapping("/deleteProduct/{id}")
	ResponseEntity<String> deleteProduct(@PathVariable Integer id);
	
	@PostMapping("/updateStatus")
	ResponseEntity<String> updateStatus(@RequestBody Map<String, String> requestMap);
	
	@GetMapping("/getByCategory/{id}")
	ResponseEntity<List<ProductWrapper>> getByCategory(@PathVariable Integer id);
	
	@GetMapping("/getProductById/{id}")
	ResponseEntity<ProductWrapper> getProductById(@PathVariable Integer id);
}
