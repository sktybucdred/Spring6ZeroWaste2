package projekt.zespolowy.zero_waste.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import projekt.zespolowy.zero_waste.entity.Product;
import projekt.zespolowy.zero_waste.entity.ProductCategory;
import projekt.zespolowy.zero_waste.entity.UnitOfMeasure;
import projekt.zespolowy.zero_waste.entity.User;
import projekt.zespolowy.zero_waste.services.ProductService;
import projekt.zespolowy.zero_waste.services.UserService;

import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private final UserService userService;

    @Autowired
    public ProductController(ProductService productService, UserService userService) {
        this.productService = productService;
        this.userService = userService;
    }

    @GetMapping("/list")
    public String listProducts(Model model) {
        List<Product> products = productService.getAllProducts();
        model.addAttribute("products", products);
        return "list-products";
    }

    @GetMapping("/showFormForAddProduct")
    public String showFormForAddProduct(Model model) {
        Product product = new Product();
        model.addAttribute("product", product);
        model.addAttribute("categories", ProductCategory.values());
        model.addAttribute("units", UnitOfMeasure.values());
        return "product-form";
    }

    @PostMapping("/save")
    public String createProduct(@ModelAttribute("product") Product product, Authentication authentication) {
        String currentUsername = authentication.getName();
        User currentUser = userService.findByUsername(currentUsername);
        product.setOwner(currentUser);
        productService.saveProduct(product);
        return "redirect:/products/list";
    }

    @GetMapping("/edit/{id}")
    public String showFormForUpdate(@PathVariable("id") Long id, Model model, Authentication authentication) {
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Product ID: " + id));
        String currentUsername = authentication.getName();
        if (!product.getOwner().getUsername().equals(currentUsername)) {
            throw new AccessDeniedException("You do not have permission to edit this product.");
        }
        model.addAttribute("product", product);
        model.addAttribute("categories", ProductCategory.values());
        model.addAttribute("units", UnitOfMeasure.values());
        return "product-form";
    }

    @PostMapping("/update")
    public String updateProduct(@ModelAttribute("product") Product product, Authentication authentication) {
        Product existingProduct = productService.getProductById(product.getId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid product ID: " + product.getId()));

        String currentUsername = authentication.getName();
        if (!existingProduct.getOwner().getUsername().equals(currentUsername)) {
            throw new AccessDeniedException("You are not authorized to edit this product.");
        }
        product.setCreatedAt(existingProduct.getCreatedAt());
        product.setOwner(existingProduct.getOwner());
        productService.saveProduct(product);
        return "redirect:/products/list";
    }


    @DeleteMapping("/delete/{id}")
    public String deleteProduct(@PathVariable("id") Long id, Authentication authentication) {
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Product ID: " + id));
        String currentUsername = authentication.getName();
        if (!product.getOwner().getUsername().equals(currentUsername)) {
            throw new AccessDeniedException("You do not have permission to delete this product.");
        }
        productService.deleteProduct(id);
        return "redirect:/products/list";
    }

    @GetMapping("/view/{id}")
    public String viewProductDetails(@PathVariable("id") Long id, Model model) {
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Product ID: " + id));
        model.addAttribute("product", product);
        return "product-detail";
    }
}
