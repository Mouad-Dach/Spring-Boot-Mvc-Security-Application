package net.dach.springmvcsecurity.web;


import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import net.dach.springmvcsecurity.entities.CartItem;
import net.dach.springmvcsecurity.entities.Product;
import net.dach.springmvcsecurity.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class ProductController {
    @Autowired
    private ProductRepository productRepository;
    @GetMapping("/user/index")
    @PreAuthorize("hasRole('USER')")
    public String index(Model model, 
                       @RequestParam(name = "page", defaultValue = "0") int page,
                       @RequestParam(name = "size", defaultValue = "5") int size){
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productRepository.findAll(pageable);

        model.addAttribute("productList", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalItems", productPage.getTotalElements());

        // Add page numbers for pagination
        if (productPage.getTotalPages() > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(0, productPage.getTotalPages() - 1)
                    .boxed()
                    .collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }

        // Add suggested search terms
        List<String> suggestedTerms = productRepository.findRandomProductNames(5);
        model.addAttribute("suggestedTerms", suggestedTerms);

        return "products";
    }
    @GetMapping("/")
    public String home(){
        return "redirect:/user/index";
    }
    @PostMapping("/admin/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@RequestParam(name = "id") Long id) {
        productRepository.deleteById(id);
        return "redirect:/user/index";
    }
    @GetMapping("/admin/newProduct")
    @PreAuthorize("hasRole('ADMIN')")
    public String newProduct(Model model){
        model.addAttribute("product", new Product());
        return "new-product";
    }
    @GetMapping("/search")
    public String search(Model model, 
                        @RequestParam String keyword,
                        @RequestParam(name = "page", defaultValue = "0") int page,
                        @RequestParam(name = "size", defaultValue = "5") int size){
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productRepository.findByNameContains(keyword, pageable);

        model.addAttribute("productList", productPage.getContent());
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalItems", productPage.getTotalElements());

        // Add page numbers for pagination
        if (productPage.getTotalPages() > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(0, productPage.getTotalPages() - 1)
                    .boxed()
                    .collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }

        // Add suggested search terms
        List<String> suggestedTerms = productRepository.findRandomProductNames(5);
        model.addAttribute("suggestedTerms", suggestedTerms);

        return "products";
    }

    @GetMapping("/api/suggestions")
    @ResponseBody
    public ResponseEntity<List<String>> getSuggestions(@RequestParam String query) {
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        List<String> suggestions = productRepository.findSuggestions(query);
        return ResponseEntity.ok(suggestions);
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/edit/{id}")
    public String editProduct(@PathVariable Long id, Model model){
        Product product = productRepository.findById(id).orElseThrow();
        model.addAttribute("product", product);
        return "edit-product";
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/update")
    public String updateProduct(@Valid Product product, BindingResult result){
        if(result.hasErrors()){
            return "edit-product";
        }
        productRepository.save(product);
        return "redirect:/user/index";
    }
    @PostMapping("/admin/saveProduct")
    @PreAuthorize("hasRole('ADMIN')")
    public String saveProduct(@Valid Product product, BindingResult bindingResult, Model model){
        if(bindingResult.hasErrors()){
            return "new-product";
        }
        productRepository.save(product);
        return "redirect:/admin/newProduct";
    }
    @GetMapping("/notAuthorized")
    public String notAuthorized(){
        return "notAuthorized";
    }
    @GetMapping("/login")
    public String login(){
        return "login";
    }
    @GetMapping("/logout")
    public String logout(HttpSession session){
        session.invalidate();
        return "login";
    }

    /**
     * Cart view
     */
    @GetMapping("/cart")
    public String viewCart(Model model, HttpSession session) {
        // Get cart from session or create empty cart
        List<CartItem> cart = getCartFromSession(session);

        // Add cart to model
        model.addAttribute("cartItems", cart);

        // Calculate total
        double total = cart.stream()
                .mapToDouble(CartItem::getTotal)
                .sum();
        model.addAttribute("cartTotal", total);

        // Check if cart is empty
        model.addAttribute("cartEmpty", cart.isEmpty());

        return "cart";
    }

    /**
     * Add product to cart
     */
    @PostMapping("/cart/add")
    public String addToCart(@RequestParam("id") Long id, 
                           @RequestParam(value = "quantity", defaultValue = "1") int quantity,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        // Get product from database
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product ID: " + id));

        // Get cart from session or create empty cart
        List<CartItem> cart = getCartFromSession(session);

        // Check if product already exists in cart
        boolean productExists = false;
        for (CartItem item : cart) {
            if (item.getId().equals(id)) {
                // Update quantity if product already exists
                item.setQuantity(item.getQuantity() + quantity);
                productExists = true;
                break;
            }
        }

        // Add new product to cart if it doesn't exist
        if (!productExists) {
            cart.add(CartItem.fromProduct(product, quantity));
        }

        // Save cart to session
        session.setAttribute("cart", cart);

        // Add success message
        redirectAttributes.addFlashAttribute("successMessage", 
                product.getName() + " ajouté au panier");

        // Redirect to previous page
        return "redirect:/user/index";
    }

    /**
     * Update cart item quantity
     */
    @PostMapping("/cart/update")
    public String updateCartItem(@RequestParam("id") Long id, 
                                @RequestParam("quantity") int quantity,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        // Get cart from session
        List<CartItem> cart = getCartFromSession(session);

        // Find cart item
        for (CartItem item : cart) {
            if (item.getId().equals(id)) {
                if (quantity <= 0) {
                    // Remove item if quantity is 0 or less
                    cart.remove(item);
                    redirectAttributes.addFlashAttribute("successMessage", 
                            item.getName() + " removed from cart");
                } else {
                    // Update quantity
                    item.setQuantity(quantity);
                    redirectAttributes.addFlashAttribute("successMessage", 
                            "Quantity updated for " + item.getName());
                }
                break;
            }
        }

        // Save cart to session
        session.setAttribute("cart", cart);

        return "redirect:/cart";
    }

    /**
     * Remove item from cart
     */
    @PostMapping("/cart/remove")
    public String removeFromCart(@RequestParam("id") Long id,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        // Get cart from session
        List<CartItem> cart = getCartFromSession(session);

        // Find cart item
        for (CartItem item : cart) {
            if (item.getId().equals(id)) {
                // Remove item
                cart.remove(item);
                redirectAttributes.addFlashAttribute("successMessage", 
                        item.getName() + " removed from cart");
                break;
            }
        }

        // Save cart to session
        session.setAttribute("cart", cart);

        return "redirect:/cart";
    }

    /**
     * Empty cart
     */
    @PostMapping("/cart/empty")
    public String emptyCart(HttpSession session, RedirectAttributes redirectAttributes) {
        // Clear cart in session
        session.setAttribute("cart", new ArrayList<CartItem>());

        // Add success message
        redirectAttributes.addFlashAttribute("successMessage", "Cart emptied successfully");

        return "redirect:/cart";
    }

    /**
     * Helper method to get cart from session
     */
    private List<CartItem> getCartFromSession(HttpSession session) {
        @SuppressWarnings("unchecked")
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");

        if (cart == null) {
            cart = new ArrayList<>();
            session.setAttribute("cart", cart);
        }

        return cart;
    }

    /**
     * Import products from CSV file (Admin only)
     */
    @PostMapping("/admin/importCsv")
    @PreAuthorize("hasRole('ADMIN')")
    public String importCsv(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        List<String> errors = new ArrayList<>();
        List<String> successes = new ArrayList<>();

        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please select a file to upload");
            return "redirect:/user/index";
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            int lineNumber = 0;
            int successCount = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                // Skip header line if present
                if (lineNumber == 1 && line.toLowerCase().contains("name") && line.toLowerCase().contains("price")) {
                    continue;
                }

                String[] fields = line.split(",");

                // Validate CSV format
                if (fields.length < 3) {
                    errors.add("Line " + lineNumber + ": Invalid format. Expected name,price,quantity");
                    continue;
                }

                try {
                    String name = fields[0].trim();
                    double price = Double.parseDouble(fields[1].trim());
                    double quantity = Double.parseDouble(fields[2].trim());

                    // Validate data
                    if (name.length() < 3 || name.length() > 50) {
                        errors.add("Line " + lineNumber + ": Name must be between 3 and 50 characters");
                        continue;
                    }

                    if (price < 0) {
                        errors.add("Line " + lineNumber + ": Price must be non-negative");
                        continue;
                    }

                    if (quantity < 0) {
                        errors.add("Line " + lineNumber + ": Quantity must be non-negative");
                        continue;
                    }

                    // Create and save product
                    Product product = Product.builder()
                            .name(name)
                            .price(price)
                            .quantity(quantity)
                            .build();

                    productRepository.save(product);
                    successCount++;

                } catch (NumberFormatException e) {
                    errors.add("Line " + lineNumber + ": Invalid number format");
                }
            }

            if (successCount > 0) {
                successes.add(successCount + " products imported successfully");
                redirectAttributes.addFlashAttribute("successMessage", successCount + " products imported successfully");
            }

        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error reading file: " + e.getMessage());
            return "redirect:/user/index";
        }

        if (!errors.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Errors during import: " + String.join(", ", errors));
            return "redirect:/user/index";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Products imported successfully");
        return "redirect:/user/index";
    }
}
