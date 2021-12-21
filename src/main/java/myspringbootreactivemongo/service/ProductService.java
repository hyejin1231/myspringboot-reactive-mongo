package myspringbootreactivemongo.service;

import lombok.RequiredArgsConstructor;
import myspringbootreactivemongo.dto.ProductDto;
import myspringbootreactivemongo.entity.Product;
import myspringbootreactivemongo.repository.ProductRepository;
import myspringbootreactivemongo.utils.AppUtils;
import org.springframework.data.domain.Range;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository repository;

    public Flux<ProductDto> getAllProducts() {
        return repository.findAll()
                .map(AppUtils::entityToDto);
//                .map(product -> AppUtils.entityToDto(product));
    }

    public Mono<ProductDto> getProduct(String id) {
        return repository.findById(id)
                .map(AppUtils::entityToDto);
    }

    // ResponseEntity = body + status + header
    public Mono<ResponseEntity<ProductDto>> getProductRE(String id) {
        return repository.findById(id)
                .map(product -> ResponseEntity.ok(AppUtils.entityToDto(product)))
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND)); // 404 에러 발생했을 때(?)
    }


    public Mono<ProductDto> saveProduct(Mono<ProductDto> productDtoMono) {
        return productDtoMono.map(AppUtils::dtoToEntity) // Mono<ProductDto> -> Mono<Product>
//                .flatMap(product -> repository.insert(product))
                .flatMap(repository::insert) // Mono<Product>
                .map(AppUtils::entityToDto); // Mono<Product> -> Mono<ProductDto>
    }

    public Mono<ResponseEntity<ProductDto>> saveProductRE(Mono<ProductDto> productDtoMono) {
        return productDtoMono.map(AppUtils::dtoToEntity)
                .flatMap(repository::insert)
                .map(insProduct -> ResponseEntity.ok(AppUtils.entityToDto(insProduct)))
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build());
    }

    public Mono<ProductDto> updateProduct (Mono<ProductDto> productDtoMono, String id) {
        Mono<Product> productMono = productDtoMono.map(AppUtils::dtoToEntity);
       return productMono.flatMap(product ->
                        repository.findById(id)
                                .flatMap(existProduct -> {
                                    existProduct.setName(product.getName());
                                    if(product.getQty() != 0) {
                                        existProduct.setQty(product.getQty());
                                    }
                                   if(product.getPrice() != 0.0) {
                                       existProduct.setPrice(product.getPrice());
                                   }
                                    return repository.save(existProduct).map(AppUtils::entityToDto);
                                })
        );

    }

    public Mono<ResponseEntity<ProductDto>> updateProductRE(Mono<ProductDto> productDtoMono, String id){
        Mono<Product> productMono = productDtoMono.map(AppUtils::dtoToEntity);
        Mono<ProductDto> updatedProductDtoMono = productMono.flatMap(product ->
                repository.findById(id)
                        .flatMap(existProduct -> {
                            existProduct.setName(product.getName());
                            if (product.getQty() != 0) {
                                existProduct.setQty(product.getQty());
                            }
                            if (product.getPrice() != 0.0) {
                                existProduct.setPrice(product.getPrice());
                            }
                            return repository.save(existProduct).map(AppUtils::entityToDto);
                        })
        );
        return updatedProductDtoMono.map(p -> ResponseEntity.ok(p))
                .defaultIfEmpty(ResponseEntity.notFound().build());
//        return repository.findById(id)
//                .flatMap(existProduct -> productDtoMono.map(AppUtils::dtoToEntity))
//                .doOnNext(product -> product.setId(id))
//                .flatMap(repository::save)
//                .map(updProduct -> ResponseEntity.ok(AppUtils.entityToDto(updProduct)))
//                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    public Mono<ResponseEntity<Void>> deleteProduct(String id) {
        return repository.findById(id)
                .flatMap(existProduct -> repository.delete(existProduct))
                .then(Mono.just(ResponseEntity.ok().<Void>build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    public Flux<ProductDto> getRegexByName(String name) {
        return repository.findByName(name).map(AppUtils::entityToDto);
    }


    public Flux<ProductDto> getPriceByRange(double min, double max) {
        return repository.findByPriceBetween(Range.closed(min, max)).map(AppUtils::entityToDto);
    }
}
