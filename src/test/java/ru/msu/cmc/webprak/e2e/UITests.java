package ru.msu.cmc.webprak.e2e;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.SelectOption;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitUntilState;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UITests {

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Playwright playwright;
    private Browser browser;
    private BrowserContext context;
    private Page page;

    @BeforeAll
    void setUpAll() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
    }

    @AfterAll
    void tearDownAll() {
        browser.close();
        playwright.close();
    }

    @BeforeEach
    void setUp() throws Exception {
        context = browser.newContext();
        page = context.newPage();
        page.setDefaultTimeout(10000);
    }

    @AfterEach
    void tearDown() {
        if (context != null) {
            context.close();
        }
    }

    private String baseUrl() {
        return "http://localhost:" + port;
    }

    private void goHome() {
        page.navigate(baseUrl() + "/", new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
        page.waitForTimeout(500);
    }

    private void goClients() {
        goHome();
        clickAndStabilize("#clientsLink");
    }

    private void goServices() {
        goHome();
        clickAndStabilize("#servicesLink");
    }

    private void goOperations() {
        goHome();
        clickAndStabilize("#operationsLink");
    }

    private void openDirect(String pathAndQuery) {
        page.navigate(baseUrl() + pathAndQuery, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
        page.waitForTimeout(500);
    }

    private BigDecimal parseMoney(String text) {
        if (text == null) {
            return BigDecimal.ZERO;
        }
        String normalized = text.replace(",", ".").replaceAll("[^0-9\\.-]", "");
        if (normalized.isBlank()) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(normalized);
    }

    private void selectByLabel(String selector, String label) {
        page.locator(selector).selectOption(new SelectOption().setLabel(label));
        page.waitForTimeout(250);
    }

    private void clickAndStabilize(String selector) {
        page.locator(selector).first().click();
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
        page.waitForTimeout(1000);
    }

    @Test
    void clientsList() {
        goClients();
        assertTrue(page.locator("table tbody tr").count() > 0);
    }

    @Test
    void clientsByServiceAndPeriod_positive() {
        goClients();
        selectByLabel("#serviceId", "Mobile voice plan");
        LocalDate from = LocalDate.now().minusDays(90);
        LocalDate to = LocalDate.now();
        page.fill("#from", from.toString());
        page.fill("#to", to.toString());
        clickAndStabilize("form[method='get'] button[type='submit']");
        assertTrue(page.locator("table tbody tr").count() > 0);
    }

    @Test
    void clientsByServiceAndPeriod_negative() {
        goClients();
        selectByLabel("#serviceId", "Mobile voice plan");
        page.fill("#from", "2000-01-01");
        page.fill("#to", "2000-01-02");
        clickAndStabilize("form[method='get'] button[type='submit']");
        assertEquals(0, page.locator("table tbody tr").count());
    }

    @Test
    void clientsNegativeBalance() {
        jdbcTemplate.update(
                "UPDATE account SET balance = -1 WHERE client_id = " +
                        "(SELECT id FROM client WHERE name = 'Ivan Petrov' LIMIT 1)"
        );
        goClients();
        page.locator("#accountState").selectOption("NEGATIVE_BALANCE");
        clickAndStabilize("form[method='get'] button[type='submit']");
        assertTrue(page.locator("table tbody tr").count() > 0);
    }

    @Test
    void clientsOverdueDebt() {
        jdbcTemplate.update(
                "UPDATE account SET debt_due_date = CURRENT_DATE - 1 WHERE client_id = " +
                        "(SELECT id FROM client WHERE name = 'Tech Solutions LLC' LIMIT 1)"
        );
        goClients();
        page.locator("#accountState").selectOption("OVERDUE_DEBT");
        clickAndStabilize("form[method='get'] button[type='submit']");
        assertTrue(page.locator("table tbody tr").count() > 0);
    }

    @Test
    void clientCardPositive() {
        goClients();
        clickAndStabilize("table tbody tr a[href^='/clients/']:not([href$='/edit'])");
        assertTrue(page.locator("text=Карточка клиента").isVisible());
    }

    @Test
    void clientCardInvalid() {
        openDirect("/clients/999999");
        page.waitForURL("**/clients");
        assertTrue(page.url().endsWith("/clients"));
    }

    @Test
    void addClient() {
        goClients();
        clickAndStabilize("a[href='/clients/new']");
        page.locator("#clientType").selectOption("PERSON");
        page.fill("#clientName", "Test Client");
        page.locator("#contactType0").selectOption("PHONE");
        page.fill("#contactValue0", "+70000000000");
        clickAndStabilize("form[method='post'] button[type='submit']");
        assertTrue(page.locator("text=Карточка клиента").isVisible());
        assertTrue(page.locator("text=Test Client").first().isVisible());
    }

    @Test
    void editClient() {
        goClients();
        clickAndStabilize("table tbody tr a[href^='/clients/'][href$='/edit']");
        page.fill("#clientName", "Updated Client");
        clickAndStabilize("form[method='post'] button[type='submit']");
        assertTrue(page.locator("text=Updated Client").first().isVisible());
    }

    @Test
    void servicesListAndFilter() {
        goServices();
        selectByLabel("#serviceTypeId", "SMS");
        clickAndStabilize("form[method='get'] button[type='submit']");
        assertTrue(page.locator("text=SMS basic").isVisible());
    }

    @Test
    void addService() {
        goServices();
        clickAndStabilize("a[href='/services/new']");
        page.fill("#name", "Test Service");
        selectByLabel("#serviceTypeId", "SMS");
        page.fill("#description", "Test description");
        page.locator("#isActive").check();
        page.locator("#unit").selectOption("PER_SMS");
        page.fill("#basePrice", "1.23");
        clickAndStabilize("form[method='post'] button[type='submit']");
        assertTrue(page.locator("text=Test Service").isVisible());
    }

    @Test
    void serviceCard() {
        goServices();
        clickAndStabilize("table tbody tr a[href^='/services/']:not([href$='/edit'])");
        assertTrue(page.locator("text=Карточка услуги").isVisible());
    }

    @Test
    void editService() {
        goServices();
        clickAndStabilize("table tbody tr a[href^='/services/'][href$='/edit']");
        page.fill("#description", "Updated description");
        clickAndStabilize("form[method='post'] button[type='submit']");
        assertTrue(page.locator("text=Updated description").isVisible());
    }

    @Test
    void operationsByClient() {
        goOperations();
        selectByLabel("#clientId", "Ivan Petrov");
        clickAndStabilize("form[method='get'] button[type='submit']");
        assertTrue(page.locator("table tbody tr").count() > 0);
    }

    @Test
    void operationsInvalidClient() {
        openDirect("/operations?clientId=999999");
        assertTrue(page.locator("text=Нет операций").isVisible());
    }

    @Test
    void paymentIncreasesBalance() {
        goClients();
        page.locator("tr:has-text(\"Ivan Petrov\") a[href^='/clients/']:not([href$='/edit'])").click();
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
        String balanceText = page.locator("p:has-text(\"Баланс\") span").first().textContent();
        BigDecimal before = parseMoney(balanceText);

        goOperations();
        clickAndStabilize("a[href='/operations/new']");
        selectByLabel("#clientId", "Ivan Petrov");
        page.locator("#opType").selectOption("PAYMENT");
        page.fill("#amount", "50");
        clickAndStabilize("form[method='post'] button[type='submit']");

        goClients();
        page.locator("tr:has-text(\"Ivan Petrov\") a[href^='/clients/']:not([href$='/edit'])").click();
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
        String balanceAfterText = page.locator("p:has-text(\"Баланс\") span").first().textContent();
        BigDecimal after = parseMoney(balanceAfterText);

        assertEquals(0, after.subtract(before).compareTo(new BigDecimal("50")));
    }

    @Test
    void chargeDecreasesBalance() {
        goClients();
        page.locator("tr:has-text(\"Ivan Petrov\") a[href^='/clients/']:not([href$='/edit'])").click();
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
        String balanceText = page.locator("p:has-text(\"Баланс\") span").first().textContent();
        BigDecimal before = parseMoney(balanceText);

        goOperations();
        clickAndStabilize("a[href='/operations/new']");
        selectByLabel("#clientId", "Ivan Petrov");
        page.locator("#opType").selectOption("CHARGE");
        selectByLabel("#subscriptionId", "Ivan Petrov - Mobile voice plan");
        page.fill("#amount", "10");
        clickAndStabilize("form[method='post'] button[type='submit']");

        goClients();
        page.locator("tr:has-text(\"Ivan Petrov\") a[href^='/clients/']:not([href$='/edit'])").click();
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
        String balanceAfterText = page.locator("p:has-text(\"Баланс\") span").first().textContent();
        BigDecimal after = parseMoney(balanceAfterText);

        assertEquals(0, before.subtract(after).compareTo(new BigDecimal("10")));
    }
}
