package com.api.myvanitys.domain.model;

import com.api.myvanitys.domain.valueobject.EntityId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ProductTest {
    private Product product;
    private User user;
    private Review review1;
    private Review review2;
    private Review review3;

    @BeforeEach
    void setUp() {
        // Crear IDs de prueba con UUID
        EntityId productId = new EntityId(UUID.randomUUID());
        EntityId userId = new EntityId(UUID.randomUUID());

        // Crear un producto y una categoría para la prueba
        Category category = new Category(new EntityId(UUID.randomUUID()), "Categoria de prueba");
        product = new Product(productId, "Producto de prueba", "Marca de prueba", category, "#FFFFFF");

        // Crear un usuario para las reseñas
        user = new User(userId, "Usuario de prueba", "prueba@gmail.com", "name");

        // Crear reseñas con diferentes calificaciones
        review1 = new Review(new EntityId(UUID.randomUUID()), user, product, 4, "Reseña 1");
        review2 = new Review(new EntityId(UUID.randomUUID()), user, product, 5, "Reseña 2");
        review3 = new Review(new EntityId(UUID.randomUUID()), user, product, 3, "Reseña 3");
    }

    @Test
    void testAddReview() {
        // Añadir una reseña
        product.addReview(review1);

        // Verificar que la reseña ha sido añadida
        assertEquals(1, product.getReviews().size(), "El número de reseñas debería ser 1");
        assertTrue(product.getReviews().contains(review1), "La reseña añadida no está en la lista de reseñas");
    }

    @Test
    void testRemoveReview() {
        // Añadir reseñas
        product.addReview(review1);
        product.addReview(review2);

        // Eliminar una reseña
        product.removeReview(review1);

        // Verificar que la reseña ha sido eliminada correctamente
        assertEquals(1, product.getReviews().size(), "El número de reseñas debería ser 1 después de eliminar");
        assertFalse(product.getReviews().contains(review1), "La reseña eliminada sigue estando en la lista");
    }

    @Test
    void testCalculateAverageRating() {
        // Añadir varias reseñas
        product.addReview(review1);  // 4 estrellas
        product.addReview(review2);  // 5 estrellas
        product.addReview(review3);  // 3 estrellas

        // Calcular el promedio de calificación esperado
        int expectedAverage = (4 + 5 + 3) / 3; // 4
        assertEquals(expectedAverage, product.getAverageRating(), "El promedio de calificación no es el esperado");
    }

    @Test
    void testProductInitialization() {
        // Verificar que los atributos del producto se inicializan correctamente
        assertEquals("Producto de prueba", product.getName(), "El nombre del producto no es correcto");
        assertEquals("Marca de prueba", product.getBrand(), "La marca del producto no es correcta");
        assertEquals("#FFFFFF", product.getColorHex(), "El color del producto no es correcto");
        assertEquals(0, product.getAverageRating(), "El promedio de calificación inicial debería ser 0");
    }

    @Test
    void testAddReviewTwice() {
        // Añadir la misma reseña dos veces
        product.addReview(review1);
        product.addReview(review1);  // No debería añadirse dos veces

        // Verificar que la reseña solo aparece una vez
        assertEquals(1, product.getReviews().size(), "La reseña no debería repetirse");
        assertTrue(product.getReviews().contains(review1), "La reseña debería estar en la lista");
    }

    @Test
    void testEmptyProductHasZeroAverageRating() {
        // Crear un producto vacío sin reseñas
        Product emptyProduct = new Product(
                new EntityId(UUID.randomUUID()),
                "Producto vacío",
                "Marca vacía",
                new Category(new EntityId(UUID.randomUUID()), "Categoría vacía"),
                "#000000"
        );

        // Verificar que el promedio de calificación es 0
        assertEquals(0, emptyProduct.getAverageRating(), "El promedio de calificación de un producto vacío debería ser 0");
    }

    @Test
    void testRemoveReviewUpdatesAverageRating() {
        // Añadir reseñas
        product.addReview(review1);  // 4 estrellas
        product.addReview(review2);  // 5 estrellas
        product.addReview(review3);  // 3 estrellas

        // Eliminar una reseña y verificar el nuevo promedio
        product.removeReview(review1);  // 4 estrellas
        int expectedAverageAfterRemoval = (5 + 3) / 2; // 4
        assertEquals(expectedAverageAfterRemoval, product.getAverageRating(), "El promedio de calificación no se actualizó correctamente");
    }

}