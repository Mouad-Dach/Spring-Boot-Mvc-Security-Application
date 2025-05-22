package net.dach.springmvcsecurity.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Represents an item in the shopping cart.
 * This class is used to store cart items in the session.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem implements Serializable {
    private Long id;
    private String name;
    private double price;
    private int quantity;
    
    /**
     * Calculate the total price for this cart item.
     * @return The total price (price * quantity)
     */
    public double getTotal() {
        return price * quantity;
    }
    
    /**
     * Create a cart item from a product.
     * @param product The product to create a cart item from
     * @param quantity The quantity to add
     * @return A new cart item
     */
    public static CartItem fromProduct(Product product, int quantity) {
        return new CartItem(
            product.getId(),
            product.getName(),
            product.getPrice(),
            quantity
        );
    }
}