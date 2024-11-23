package shopping;

import customer.Customer;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import product.Product;
import product.ProductDao;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Тестовый класс для проверки функциональности сервиса покупок
 * <b>
 *     Недочеты в коде
 *     1.`getCart` всегда создает новую корзину,а не возвращает существующую, как указано в JavaDoc
 *     2. После покупки корзина не очищается, не используется метод который удаляет из корзины товары
 *     3. При покупки всех товаров, транзакция должна пройти, но из за ошибки в методе выходит сообщение о ошибке
 *     4. Не рассмотрен случай с добавлением товара с отрицательным числом, если добавить твоар c отрицательным числом,
 *     тест пройдет
 * </b>
 */
public class ShoppingServiceTest {
    private final ProductDao productDao = Mockito.mock(ProductDao.class);
    private final  ShoppingService service = new ShoppingServiceImpl(productDao);

    /**
     * Тестирует получение корзины покупателя
     * Проверяет, что создается непустая корзина
     * в классе Cart нет метода, позволяющего получить доступ к объекту Customer,
     * прямая проверка связи между Cart и Customer невозможна без изменений в исходном коде Cart
     */
    @Test
    void testGetCart() {
        Customer customer = new Customer(1L,"111-111-111");
        Cart cart = service.getCart(customer);
        assertNotNull(cart);
    }

    /**
     * Проверяет, что метод getCart() возвращает один и тот же экземпляр корзины при повторном вызове с тем же клиентом
     * Этот тест будет падать, так как текущая реализация метода `getCart` всегда создает новую корзину,
     * а не возвращает существующую, как указано в JavaDoc
     */
    @Test
    void testGetCartReturnsExistingCart() {
        Customer customer = new Customer(1L, "123-456-7890");

        Cart cart1 = service.getCart(customer);
        assertNotNull(cart1);
        assertTrue(cart1.getProducts().isEmpty());


        Cart cart2 = service.getCart(customer);
        assertSame(cart1, cart2);

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
     * Ожидается, что после вызова метода buy() корзина будет очищена
     * (не работает, так как не используется метод который удаляет из корзины товары, поэтому тест не проходит)
     */
    @Test
    void testBuySuccess() throws BuyException {
        Product product1 = new Product("Продукт 1", 10);
        Product product2 = new Product("Продукт 2", 5);

        Cart cart = service.getCart(new Customer(1L,"111-111-111"));
        int initialProduct1Count = product1.getCount();
        int initialProduct2Count = product2.getCount();

        cart.add(product1, 2);
        cart.add(product2, 3);

        assertTrue(service.buy(cart));

        verify(productDao).save(argThat(product -> product.getName().equals("Продукт 1")
                && product.getCount() == initialProduct1Count - 2));
        verify(productDao).save(argThat(product -> product.getName().equals("Продукт 2")
                && product.getCount() == initialProduct2Count - 3));


        assertEquals(initialProduct1Count - 2, product1.getCount());
        assertEquals(initialProduct2Count - 3, product2.getCount());

        assertTrue(cart.getProducts().isEmpty());
    }

    /**
     * Тестирует добавление в корзину всех имеющихся на складе товаров
     * Проверяет, что при попытке добавить в корзину количество товаров, равное количеству на складе,
     * все успешно пройдет.
     * Также проверяет, что после удачной попытки добавления товар они присутствует в корзине
     * (это тоже логическая ошибка, ведь при покупки всех товаров, транзакция должна пройти,
     * но из за ошибки в методе выходит ошибка)
     */
    @Test
    void testAddАllProducts() {
        Product product = new Product("Продукт", 3);
        Cart cart = service.getCart(new Customer(1L, "111-111-111"));

        cart.add(product, 3);

        assertEquals(3, cart.getProducts().get(product).intValue());
    }

    /**
     * Тестирует добавление товара с нулевым запасом (недостаточным колличеством товара) в корзину
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
     * Тестирует попытку добавления товара в корзину с отрицательным количеством
     * Ожидается исключение  IllegalArgumentException с соответствующим сообщением
     * (Тоже ошибка в коде, не рассмотрен случай с добавлением товара с отрицательным числом)
     */
    @Test
    void testAddingToCartWithANegativeNumber() {
        Product product = new Product("Продукт 1", 10);
        Cart cart = service.getCart(new Customer(1L, "111-111-111"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> cart.add(product, -4));
        assertEquals("Количество товара должно быть неотрицательным",
                exception.getMessage());
    }

    /**
     * Тестирует покупку пустой корзины
     * Проверяет, что метод buy возвращает false для пустой корзины
     */
    @Test
    void testBuyEmptyCart() throws BuyException {
        Cart cart = service.getCart(new Customer(1L,"111-111-111"));
        assertFalse(service.buy(cart));
    }

}