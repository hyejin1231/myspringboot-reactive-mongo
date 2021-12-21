package myspringbootreactivemongo.controller;

import lombok.RequiredArgsConstructor;
import myspringbootreactivemongo.dto.ProductDto;
import myspringbootreactivemongo.entity.Product;
import myspringbootreactivemongo.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService service;

    @GetMapping
    public Flux<ProductDto> getProducts() {
        return service.getAllProducts();
    }

    @GetMapping("{id}")
    public Mono<ProductDto> getProduct(@PathVariable String id) {
        return service.getProduct(id);
    }

    @GetMapping("/re/{id}")
    public Mono<ResponseEntity<ProductDto>> getProductRE(@PathVariable String id) {
        return service.getProductRE(id);
    }

    @PostMapping
    public Mono<ProductDto> saveProduct(@RequestBody Mono<ProductDto> productDtoMono) {
        return service.saveProduct(productDtoMono);
    }

    @PostMapping("/re")
    public Mono<ResponseEntity<ProductDto>> saveProductSE(@RequestBody Mono<ProductDto> productDtoMono) {
        return service.saveProductRE(productDtoMono);
    }

    @PutMapping("/{id}")
    public Mono<ProductDto> updateProduct(@RequestBody Mono<ProductDto> productDtoMono, @PathVariable String id) {
        return service.updateProduct(productDtoMono, id);
    }

    @PutMapping("/re/{id}")
    public Mono<ResponseEntity<ProductDto>> updateProductRE(@RequestBody Mono<ProductDto> productDtoMono, @PathVariable String id) {
        return service.updateProductRE(productDtoMono, id);
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteProduct(@PathVariable String  id) {
        return service.deleteProduct(id);
    }

    @GetMapping("/product-range")
    public Flux<ProductDto> getProductInRange(@RequestParam double min, @RequestParam double max) {
        return service.getPriceByRange(min, max);
    }

    public Flux<ProductDto> getProductByName(@RequestParam String name) {
        return service.getRegexByName(name);
    }

}
