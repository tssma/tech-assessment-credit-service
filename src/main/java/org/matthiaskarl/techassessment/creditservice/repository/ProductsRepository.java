package org.matthiaskarl.techassessment.creditservice.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import org.matthiaskarl.techassessment.creditservice.domain.Product;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class ProductsRepository {

    private static final String PATH = "20231214_TestData_PRODUCTS.json";
    private final Map<Long, Product> byId;

    public ProductsRepository() {
        List<Product> list = RepositoryUtils.read(PATH, new TypeReference<>() {
        });
        this.byId = list.stream().collect(Collectors.toMap(Product::id, product -> product));
    }

    public Product findById(long id) {
        return byId.get(id);
    }

    public List<Product> findByIds(List<Long> ids) {
        List<Product> products = new ArrayList<>();
        for (Long id : ids) {
            Product product = findById(id);
            products.add(product);
        }
        return products;
    }

}
