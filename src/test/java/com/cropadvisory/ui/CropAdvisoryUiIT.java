package com.cropadvisory.ui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CropAdvisoryUiIT {
    private static final String FARMER_USERNAME = requiredEnvironmentVariable("UI_FARMER_USERNAME");
    private static final String FARMER_PASSWORD = requiredEnvironmentVariable("UI_FARMER_PASSWORD");
    private static final String EXPERT_USERNAME = requiredEnvironmentVariable("UI_EXPERT_USERNAME");
    private static final String EXPERT_PASSWORD = requiredEnvironmentVariable("UI_EXPERT_PASSWORD");
    private static final String ADMIN_USERNAME = requiredEnvironmentVariable("UI_ADMIN_USERNAME");
    private static final String ADMIN_PASSWORD = requiredEnvironmentVariable("UI_ADMIN_PASSWORD");

    @RegisterExtension
    static final UiTestExtension UI = new UiTestExtension();

    private WebDriver driver;
    private String baseUrl;

    @BeforeEach
    void setUp() {
        driver = UiTestExtension.driver();
        baseUrl = UiTestExtension.baseUrl();
    }

    @Test
    void loginRejectsInvalidCredentialsAndAcceptsValidCredentials() {
        LoginPage login = new LoginPage(driver, baseUrl);
        login.open();
        login.submit(FARMER_USERNAME, "incorrect-password");
        assertTrue(login.loginErrorVisible(), "Invalid credentials should show the login error.");

        login.submit(FARMER_USERNAME, FARMER_PASSWORD);
        login.waitForHome();
        assertEquals("FARMER", driver.findElement(By.id("user-role")).getText());
    }

    @Test
    void farmerCreatesQuery() {
        loginAs(FARMER_USERNAME, FARMER_PASSWORD);
        String title = uniqueTitle();

        RecordDetailPage detail = createRecord(title);

        assertEquals(title, detail.title());
        assertEquals("Maize", detail.crop());
        assertEquals("Submitted", detail.status());
    }

    @Test
    void searchFindsCreatedRecord() {
        loginAs(FARMER_USERNAME, FARMER_PASSWORD);
        String title = uniqueTitle();
        createRecord(title);

        RecordsPage records = new RecordsPage(driver, baseUrl);
        records.open();
        records.search(title);

        assertTrue(records.containsRecord(title), "Search results should contain the newly created query.");
    }

    @Test
    void expertMovesQueryThroughReviewAndAdvice() {
        loginAs(FARMER_USERNAME, FARMER_PASSWORD);
        String title = uniqueTitle();
        RecordDetailPage created = createRecord(title);
        assertEquals("Submitted", created.status());
        signOut();

        loginAs(EXPERT_USERNAME, EXPERT_PASSWORD);
        RecordsPage records = new RecordsPage(driver, baseUrl);
        records.open();
        records.search(title);
        records.openRecord(title);

        RecordDetailPage detail = new RecordDetailPage(driver, baseUrl);
        detail.moveToUnderReview();
        String advice = "Check soil moisture and apply the recommended treatment.";
        detail.advise(advice);

        assertEquals("Advised", detail.status());
    }

    @Test
    void farmerGetsForbiddenAndAdminCanSeeDashboard() {
        loginAs(FARMER_USERNAME, FARMER_PASSWORD);

        Object responseStatus = ((JavascriptExecutor) driver).executeAsyncScript(
                "const done = arguments[arguments.length - 1];"
                        + "fetch('/dashboard').then(response => done(response.status))"
                        + ".catch(() => done(-1));");
        assertTrue(responseStatus instanceof Number, "Dashboard request should return an HTTP status.");
        assertEquals(403, ((Number) responseStatus).intValue(),
                "A farmer must receive HTTP 403 from /dashboard.");

        driver.get(baseUrl + "/dashboard");
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.urlContains("/dashboard"));
        signOutFromHome();
        loginAs(ADMIN_USERNAME, ADMIN_PASSWORD);

        DashboardPage dashboard = new DashboardPage(driver, baseUrl);
        dashboard.openDashboard();
        assertTrue(dashboard.totalCount().matches("\\d+"), "Admin dashboard should show the total query count.");
        assertTrue(driver.findElement(By.tagName("body")).getText().contains("Total queries"));
    }

    private void loginAs(String username, String password) {
        LoginPage login = new LoginPage(driver, baseUrl);
        login.open();
        login.submit(username, password);
        login.waitForHome();
    }

    private RecordDetailPage createRecord(String title) {
        RecordsPage records = new RecordsPage(driver, baseUrl);
        records.open();
        records.openNewRecordForm();
        return new RecordFormPage(driver, baseUrl).create(
                "Maize", "UI Test Village", title, "Leaves have developed spots and need assessment.");
    }

    private void signOut() {
        driver.findElement(By.id("logout-button")).submit();
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.urlContains("/login"));
    }

    private void signOutFromHome() {
        driver.get(baseUrl + "/home");
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(By.id("logout-button")));
        signOut();
    }

    private String uniqueTitle() {
        return "UI query " + UUID.randomUUID();
    }

    private static String requiredEnvironmentVariable(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Set the " + name + " environment variable before running UI tests.");
        }
        return value;
    }
}
