package com.diego.lima.dev.startup.Product.Service;

import com.diego.lima.dev.startup.Category.Repository.CategoryRepository;
import com.diego.lima.dev.startup.Exceptions.Category.NotFoundCategoryException;
import com.diego.lima.dev.startup.Exceptions.Product.ConflictProductException;
import com.diego.lima.dev.startup.Product.Dtos.Request.CreateProductDTO;
import com.diego.lima.dev.startup.Product.Dtos.Response.ProductResponse;
import com.diego.lima.dev.startup.Product.Model.Product;
import com.diego.lima.dev.startup.Product.Repository.ProductRepository;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public ProductResponse create(CreateProductDTO request) {

        if (productRepository.existsByName(request.name())) {
            throw new ConflictProductException(String.format("Produto com nome %s já existe", request.name()));
        }

        var category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new NotFoundCategoryException(
                        String.format("Categoria com id %d não encontrada", request.categoryId())
                ));

        var product = new Product();

        product.setName(request.name());
        product.setSalePrice(request.salePrice());
        product.setCategory(category);

        Product savedProduct = productRepository.save(product);


        return new ProductResponse(
                savedProduct.getId(),
                savedProduct.getName(),
                savedProduct.getSalePrice(),
                savedProduct.getCategory().getName()
        );
    }
}
