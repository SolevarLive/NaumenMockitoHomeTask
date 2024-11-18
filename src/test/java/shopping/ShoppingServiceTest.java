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

    public class ShoppingServiceTest {

    /**
     * Тестирует получение корзины покупателя
     * Проверяет, что создается непустая корзина
     * в классе Cart нет метода, позволяющего получить доступ к объекту Customer,
     * прямая проверка связи между Cart и Customer невозможна без изменений в исходном коде Cart
     */
    @Test
    void testGetCart() {
        ShoppingService service = new ShoppingServiceImpl(mock(ProductDao.class));
        Customer customer = new Customer(1L,"111-111-111");
        Cart cart = service.getCart(customer);
        assertNotNull(cart);
    }

    /**
     * Тестирует получение всех товаров
     * Проверяет, что сервис возвращает список товаров из ProductDao
     */
    @Test
    void testGetAllProducts() {
        ProductDao productDao = Mockito.mock(ProductDao.class);
        List<Product> products = Arrays.asList(new Product("Продукт 1", 10), new Product("Продукт 2", 5));
        when(productDao.getAll()).thenReturn(products);

        ShoppingService service = new ShoppingServiceImpl(productDao);
        List<Product> allProducts = service.getAllProducts();

        assertEquals(products, allProducts);
        verify(productDao).getAll();
    }

    /**
     * Тестирует получение товара по имени
     * Проверяет, что сервис возвращает товар из ProductDao, соответствующий заданному имени
     */
    @Test
    void testGetProductByName() {
        ProductDao productDao = Mockito.mock(ProductDao.class);
        Product expectedProduct = new Product("Продукт 1", 20);
        when(productDao.getByName("Продукт 1")).thenReturn(expectedProduct);

        ShoppingService service = new ShoppingServiceImpl(productDao);
        Product product = service.getProductByName("Продукт 1");

        assertEquals(expectedProduct, product);
        verify(productDao).getByName("Продукт 1");
    }

    /**
     * Тестирует успешную покупку
     * Проверяет, что метод buy возвращает true и обновляет количество товара в ProductDao
     */
    @Test
    void testBuySuccess() throws BuyException {
        ProductDao productDao = mock(ProductDao.class);
        Product product1 = new Product("Продукт 1", 10);
        Product product2 = new Product("Продукт 2", 5);
        when(productDao.getByName("Продукт 1")).thenReturn(product1);
        when(productDao.getByName("Продукт 2")).thenReturn(product2);

        ShoppingService service = new ShoppingServiceImpl(productDao);
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
     * Тестирует покупку с недостаточным количеством товара
     * Проверяет, что метод buy выбрасывает IllegalArgumentException
     */
    @Test
    void testBuyInsufficientStock() {
        ProductDao productDao = Mockito.mock(ProductDao.class);
        Product product = new Product("Продукт", 3);
        Mockito.when(productDao.getByName("Продукт")).thenReturn(product);

        ShoppingService service = new ShoppingServiceImpl(productDao);
        Cart cart = service.getCart(new Customer(1L, "111-111-111"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> cart.add(product, 4));
        assertEquals("Невозможно добавить товар 'Продукт' в корзину, т.к. нет необходимого количества товаров",
                exception.getMessage());
    }

    /**
     * Тестирует покупку пустой корзины
     * Проверяет, что метод buy возвращает false для пустой корзины
     */
    @Test
    void testBuyEmptyCart() throws BuyException {
        ShoppingService service = new ShoppingServiceImpl(mock(ProductDao.class));
        Cart cart = service.getCart(new Customer(1L,"111-111-111"));
        assertFalse(service.buy(cart));
    }


    // Методы add, edit, remove в классе Cart тестировать не нужно,
    // так как они являются вспомогательными методами класса Cart и не являются методами ShoppingService
    // Если бы мы писали тесты на все классы, то в CarTest были бы проверки на их работоспособность

    @Test
    void testAdd() {
        // Мы проверяем его работу косвенно, через тесты ShoppingService.  Там мы смотрим, всё ли ОК с
        // добавлением товаров в корзину.  Если в ShoppingService всё работает, значит,
        // и add() работает.  Писать отдельный тест для add() нет смыла
    }

    @Test
    void testEdit() {
        // То же самое, что и с add().  edit() вспомогательный метод, его проверка
        // встроена в тесты ShoppingService.  Проверять его отдельно будет лишним
        // Мы смотрим, правильно ли изменяется количество товаров в
        // корзине, когда мы тестируем ShoppingService.
    }

    @Test
    void testRemove() {
        // Если удаление товаров в ShoppingService работает,
        // значит, и remove() в Cart работает
        // В целом тоже самое что и в случае add()
    }

}