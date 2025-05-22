package net.dach.springmvcsecurity;

import net.dach.springmvcsecurity.entities.Product;
import net.dach.springmvcsecurity.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;


//@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@SpringBootApplication

public class SpringMvcSecurityApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringMvcSecurityApplication.class, args);
    }
    @Bean
    public CommandLineRunner start(ProductRepository productRepository){
        return args -> {
            // Original products
            productRepository.save(Product.builder()
                            .name("Ordinateur Portable")
                            .price(10000)
                            .quantity(10)
                    .build());
            productRepository.save(Product.builder()
                    .name("Imprimante Laser")
                    .price(1200)
                    .quantity(11)
                    .build());
            productRepository.save(Product.builder()
                    .name("Smartphone Premium")
                    .price(12000)
                    .quantity(33)
                    .build());
                    
            // Additional products with varied names and realistic prices/quantities
            productRepository.save(Product.builder()
                    .name("Souris Sans Fil")
                    .price(250)
                    .quantity(45)
                    .build());
            productRepository.save(Product.builder()
                    .name("Casque Audio Bluetooth")
                    .price(800)
                    .quantity(20)
                    .build());
            productRepository.save(Product.builder()
                    .name("Écran 27 Pouces")
                    .price(2500)
                    .quantity(15)
                    .build());
            productRepository.save(Product.builder()
                    .name("Clavier Mécanique")
                    .price(600)
                    .quantity(25)
                    .build());
            productRepository.save(Product.builder()
                    .name("Disque Dur Externe 1TB")
                    .price(700)
                    .quantity(30)
                    .build());
            productRepository.save(Product.builder()
                    .name("Webcam HD")
                    .price(450)
                    .quantity(22)
                    .build());
            productRepository.save(Product.builder()
                    .name("Tablette Graphique")
                    .price(1500)
                    .quantity(8)
                    .build());
            productRepository.save(Product.builder()
                    .name("Enceinte Bluetooth")
                    .price(350)
                    .quantity(40)
                    .build());
            productRepository.save(Product.builder()
                    .name("Routeur WiFi")
                    .price(550)
                    .quantity(18)
                    .build());
            productRepository.save(Product.builder()
                    .name("Carte Graphique")
                    .price(4500)
                    .quantity(7)
                    .build());
            productRepository.save(Product.builder()
                    .name("Processeur i9")
                    .price(3800)
                    .quantity(12)
                    .build());
            productRepository.save(Product.builder()
                    .name("Mémoire RAM 16GB")
                    .price(650)
                    .quantity(35)
                    .build());
            productRepository.save(Product.builder()
                    .name("Batterie Externe")
                    .price(300)
                    .quantity(50)
                    .build());
            productRepository.save(Product.builder()
                    .name("Câble HDMI 2m")
                    .price(120)
                    .quantity(60)
                    .build());
            productRepository.save(Product.builder()
                    .name("Station d'Accueil")
                    .price(1800)
                    .quantity(14)
                    .build());
                    
            productRepository.findAll().forEach(product -> {
                System.out.println(product.toString());
            });
        };
    }
}