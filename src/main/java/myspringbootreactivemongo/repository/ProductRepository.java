package myspringbootreactivemongo.repository;

import myspringbootreactivemongo.entity.Product;
import org.springframework.data.domain.Range;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface ProductRepository extends ReactiveMongoRepository<Product, String> {
    Flux<Product> findByPriceBetween(Range<Double> priceRange);

    @Query("{'name' : {$regex: ?0}}")
    Flux<Product> findByName(String name);

}

