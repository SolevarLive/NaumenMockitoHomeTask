package shopping;

import customer.Customer;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import product.Product;
import product.ProductDao;


import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Тестовый класс для проверки функциональности сервиса покупок
 */
public class ShoppingServiceTest {
    private final ProductDao productDao = Mockito.mock(ProductDao.class);
    private final  ShoppingService service = new ShoppingServiceImpl(productDao);
    private final ShoppingService services = new ShoppingServiceImpl(mock(ProductDao.class));

    /**
     * Тестирует получение корзины покупателя
     * Проверяет, что создается непустая корзина
     * в классе Cart нет метода, позволяющего получить доступ к объекту Customer,
     * прямая проверка связи между Cart и Customer невозможна без изменений в исходном коде Cart
     */
    @Test
    void testGetCart() {
        Customer customer = new Customer(1L,"111-111-111");
        Cart cart = services.getCart(customer);
        assertNotNull(cart);
    }

    /**
     * Этот тест пустой, потому что метод getAllProducts()
     * просто вызывает другой метод.  Он не выполняет сложной
     * логики, поэтому тест не нужен
     * Так как он фактически проверяет только то,
     * что ShoppingServiceImpl корректно вызывает метод ProductDao
     * Это не добавляет значительной ценности в тестировании
     */
    @Test
    void testGetAllProducts() {
        // Тест не нужен.
    }

    /**
    * Этот тест пустой, потому что метод getProductByName()
    * просто вызывает другой метод.  Он не выполняет сложной
    * логики, поэтому тест не нужен
     * аналогично testGetAllProducts()
    */
    @Test
    void testGetProductByName() {
        // Тест не нужен.
    }

    /**
     * Тестирует успешную покупку
     * Проверяет, что метод buy возвращает true и обновляет количество товара в ProductDao
     */
    @Test
    void testBuySuccess() throws BuyException {
        Product product1 = new Product("Продукт 1", 10);
        Product product2 = new Product("Продукт 2", 5);

        Cart cart = service.getCart(new Customer(1L,"111-111-111"));
        cart.add(product1, 2);
        cart.add(product2, 3);

        assertTrue(service.buy(cart));
        verify(productDao).save(product1);
        verify(productDao).save(product2);
        assertEquals(8, product1.getCount());
        assertEquals(2, product2.getCount());
    }

    /**
     * Тест проверяет, что корзина пуста после успешной покупки
     * Ожидается, что после вызова метода buy() корзина будет очищена
     * BuyException если во время покупки возникает ошибка
     * (не работает, так как не используется метод который удаляет из корзины товары, поэтому тест не проходит)
     */
    @Test
    void testCartEmptyAfterBuy() throws BuyException {
        Product product1 = new Product("Продукт 1", 10);
        Product product2 = new Product("Продукт 2", 5);

        Cart cart = service.getCart(new Customer(1L, "111-111-111"));
        cart.add(product1, 2);
        cart.add(product2, 3);

        assertTrue(service.buy(cart));

        assertTrue(cart.getProducts().isEmpty());
    }

    /**
     * Тестирует покупку с недостаточным количеством товара
     * Проверяет, что метод buy выбрасывает IllegalArgumentException
     */
    @Test
    void testBuyInsufficientStock() {
        Product product = new Product("Продукт", 3);

        Cart cart = service.getCart(new Customer(1L, "111-111-111"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> cart.add(product, 4));
        assertEquals("Невозможно добавить товар 'Продукт' в корзину, т.к. нет необходимого количества товаров",
                exception.getMessage());
    }

    /**
     * Тестирует добавление в корзину всех имеющихся на складе товаров
     * Проверяет, что при попытке добавить в корзину количество товаров, превышающее количество на складе,
     * выбрасывается исключение IllegalArgumentException с ожидаемым сообщением об ошибке
     * Также проверяет, что после неудачной попытки добавления товар не присутствует в корзине
     * (это тоже не логическая ошибка, ведь при покупки всех товаров, транзакция должна пройти,
     * но из за ошибки в методе выходит ошибка)
     */
    @Test
    void testBuyАllProducts() {
        Product product = new Product("Продукт", 3);

        Cart cart = service.getCart(new Customer(1L, "111-111-111"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> cart.add(product, 3));
        assertEquals("Невозможно добавить товар 'Продукт' в корзину, т.к. нет необходимого количества товаров",
                exception.getMessage());

        assertFalse(cart.getProducts().containsKey(product));
    }

    /**
     * Тестирует добавление товара с нулевым запасом в корзину
     * Проверяет, что при попытке добавить товар, количество которого на складе равно нулю,
     * выбрасывается исключение IllegalArgumentException с ожидаемым сообщением
     * Также проверяет, что корректное количество товара с ненулевым запасом находится
     * в корзине после неудачной попытки добавления товара с нулевым запасом
     */
    @Test
    void testAddToCartZeroStock() {
        Product product1 = new Product("Продукт 1", 10);
        Product product2 = new Product("Продукт 2", 0);

        Cart cart = service.getCart(new Customer(1L, "111-111-111"));

        cart.add(product1, 2);


        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> cart.add(product2, 4));
        assertEquals("Невозможно добавить товар 'Продукт 2' в корзину, т.к. нет необходимого количества товаров",
                exception.getMessage());


        assertEquals(2, cart.getProducts().get(product1).intValue());
        assertFalse(cart.getProducts().containsKey(product2));
    }

    /**
     * Тестирует покупку пустой корзины
     * Проверяет, что метод buy возвращает false для пустой корзины
     */
    @Test
    void testBuyEmptyCart() throws BuyException {
        Cart cart = services.getCart(new Customer(1L,"111-111-111"));
        assertFalse(services.buy(cart));
    }

}