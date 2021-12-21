package myspringbootreactivemongo.handler;

import lombok.RequiredArgsConstructor;
import myspringbootreactivemongo.dto.ProductDto;
import myspringbootreactivemongo.entity.Product;
import myspringbootreactivemongo.repository.ProductRepository;
import myspringbootreactivemongo.utils.AppUtils;
import org.springframework.data.domain.Range;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.BodyInserters.fromValue;

@Service
@RequiredArgsConstructor
public class ProductHandler {
    private final ProductRepository repository;

    private Mono<ServerResponse> response404 = ServerResponse.notFound().build(); // 404
    private Mono<ServerResponse> response406 = ServerResponse.status(HttpStatus.NOT_ACCEPTABLE).build(); // 406

    public Mono<ServerResponse> getProducts(ServerRequest request) {
        Flux<ProductDto> productDtoFlux = repository.findAll().map(AppUtils::entityToDto);

        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(productDtoFlux, ProductDto.class);
    }

    public  Mono<ServerResponse> getProduct(ServerRequest request) {
        String id = request.pathVariable("id");
        return repository.findById(id)
                .map(AppUtils::entityToDto)
                .flatMap(productDto -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(fromValue(productDto))
                )
                .switchIfEmpty(response404);
    }

    public Mono<ServerResponse> getProductInRange(ServerRequest request) {
        // Optional<String> request.queryParam("min")
        double min = Double.parseDouble(request.queryParam("min").orElseGet(() -> Double.toString(Double.MIN_VALUE)));
        double max = Double.parseDouble(request.queryParam("max").orElseGet(() -> Double.toString(Double.MAX_VALUE)));

        Flux<ProductDto> productDtoFlux = repository.findByPriceBetween(Range.closed(min, max)).map(AppUtils::entityToDto);

        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(productDtoFlux, ProductDto.class)
                .switchIfEmpty(response404);
    }


    public Mono<ServerResponse> saveProduct(ServerRequest request) {
        // Mono<ProductDto> -> Mono<Product>
        Mono<Product> unSavedproductMono = request.bodyToMono(ProductDto.class).map(AppUtils::dtoToEntity);
        return unSavedproductMono.flatMap(product ->
                repository.save(product).map(AppUtils::entityToDto)
                        .flatMap(savedProductDto -> ServerResponse.accepted()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(savedProductDto)
                        )
        ).switchIfEmpty(response404);
    }

    public Mono<ServerResponse> updateProduct(ServerRequest request) {
        Mono<Product> unUpdatedProductMono = request.bodyToMono(ProductDto.class).map(AppUtils::dtoToEntity);
        String id = request.pathVariable("id");

        Mono<ProductDto> updatedProductedDtoMono = unUpdatedProductMono.flatMap(product ->
                    repository.findById(id)
                            .flatMap(existProduct -> {
                                if (product.getQty() != 0) {
                                    existProduct.setQty(product.getQty());
                                }
                                if (product.getPrice() != 0.0) {
                                    existProduct.setPrice(product.getPrice());
                                }
                                return repository.save(existProduct).map(AppUtils::entityToDto);
                            })
                );
        return updatedProductedDtoMono.flatMap(productDto ->
                ServerResponse.accepted()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(productDto)
        ).switchIfEmpty(response404);
    }


    public Mono<ServerResponse> deleteProduct(ServerRequest request) {
        String id = request.pathVariable("id");
        return repository.findById(id)
                .flatMap(existProduct -> ServerResponse.ok()
                        .build(repository.delete(existProduct))
                ).switchIfEmpty(response404);
    }
}
