package myspringbootreactivemongo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {

    @Id
    private String id;
    private String name;
    private int qty;
    private double price;

    public double getTotalPrice() {
        double tPrice = qty * price;
        return Math.round(tPrice * 100) / 100.0;
    }
}
