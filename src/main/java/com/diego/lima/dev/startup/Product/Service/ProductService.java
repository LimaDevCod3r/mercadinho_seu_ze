package com.diego.lima.dev.startup.Product.Service;

import com.diego.lima.dev.startup.Category.Repository.CategoryRepository;
import com.diego.lima.dev.startup.Exceptions.Category.NotFoundCategoryException;
import com.diego.lima.dev.startup.Exceptions.Product.ConflictProductException;
import com.diego.lima.dev.startup.Exceptions.Product.NotFoundProductException;
import com.diego.lima.dev.startup.Product.Dtos.Request.CreateProductDTO;
import com.diego.lima.dev.startup.Product.Dtos.Request.UpdateProductDTO;
import com.diego.lima.dev.startup.Product.Dtos.Response.ProductResponse;
import com.diego.lima.dev.startup.Product.Model.Product;
import com.diego.lima.dev.startup.Product.Repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public ProductResponse createProduct(CreateProductDTO request) {

        if (productRepository.existsByName(request.name().trim())) {
            throw new ConflictProductException(String.format("Produto com nome %s já existe", request.name()));
        }

        var categoryEntity = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new NotFoundCategoryException(
                        String.format("Categoria com id %d não encontrada", request.categoryId())
                ));

        var productEntity = new Product();
        productEntity.setName(request.name().trim());
        productEntity.setSalePrice(request.salePrice());
        productEntity.setCategory(categoryEntity);

        Product savedProductEntity = productRepository.save(productEntity);


        return new ProductResponse(
                savedProductEntity.getId(),
                savedProductEntity.getName(),
                savedProductEntity.getSalePrice(),
                savedProductEntity.getCategory().getName()
        );
    }


    public Page<ProductResponse> findAllProducts(Pageable pageable) {
        Page<Product> productPage = productRepository.findAll(pageable);

        return productPage.map(productEntity ->
                new ProductResponse(
                        productEntity.getId(),
                        productEntity.getName()
                        , productEntity.getSalePrice(),
                        productEntity.getCategory().getName()
                )
        );
    }

    public ProductResponse findProductById(Long productId) {

        var productEntity = productRepository.findById(productId)
                .orElseThrow(() ->
                        new NotFoundProductException(
                                String.format("Produto com id %d não encontrado", productId)
                        )
                );

        return new ProductResponse(
                productEntity.getId(),
                productEntity.getName(),
                productEntity.getSalePrice(),
                productEntity.getCategory().getName());

    }

    public void updateProduct(Long productId, UpdateProductDTO request) {
        // Verifica se o produto existe pelo ID
        var product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new NotFoundProductException(
                                String.format("Produto com id %d não encontrado", productId)
                        )
                );

        // Verifica se o nome foi enviado e se já existe outro produto com esse nome
        if (request.name() != null) {

            boolean productNameExists = productRepository
                    .existsByNameAndIdNot(request.name(), productId);

            if (productNameExists) {
                throw new ConflictProductException("Já existe um produto com esse nome");
            }
            product.setName(request.name());
        }

        // verifica se o preço foi enviado
        if (request.salePrice() != null) {
            product.setSalePrice(request.salePrice());
        }

        //  Verifica se a categoria foi enviada e se ela existe
        if (request.categoryId() != null) {
            var categoryEntity = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() ->
                            new NotFoundCategoryException(
                                    String.format("Categoria com id %d não encontrada", request.categoryId()
                                    )));

            product.setCategory(categoryEntity);
        }

        productRepository.save(product);
    }


    public void deleteProduct(Long productId) {
        var productEntity = productRepository.findById(productId).orElseThrow(() -> new NotFoundProductException(
                String.format("Produto com id %d não encontrado", productId)
        ));

        productRepository.delete(productEntity);
    }


}

