package com.cropadvisory.ui;

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;

public final class UiTestExtension implements BeforeEachCallback, TestWatcher {
    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();
    private static final String BASE_URL = System.getProperty("base.url", "http://localhost:8083")
            .replaceAll("/+$", "");

    public static WebDriver driver() {
        WebDriver driver = DRIVER.get();
        if (driver == null) {
            throw new IllegalStateException("The UI test browser has not been started.");
        }
        return driver;
    }

    public static String baseUrl() {
        return BASE_URL;
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new", "--disable-gpu", "--window-size=1440,1000");

        WebDriver driver = new ChromeDriver(options);
        DRIVER.set(driver);
        driver.manage().timeouts().implicitlyWait(Duration.ZERO);
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
    }

    @Override
    public void testSuccessful(ExtensionContext context) {
        closeBrowser();
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        try {
            saveScreenshot(context);
        } finally {
            closeBrowser();
        }
    }

    @Override
    public void testAborted(ExtensionContext context, Throwable cause) {
        closeBrowser();
    }

    @Override
    public void testDisabled(ExtensionContext context, java.util.Optional<String> reason) {
        closeBrowser();
    }

    private void saveScreenshot(ExtensionContext context) {
        WebDriver driver = DRIVER.get();
        if (!(driver instanceof TakesScreenshot screenshotDriver)) {
            return;
        }

        String testName = context.getRequiredTestClass().getSimpleName() + "_"
                + context.getRequiredTestMethod().getName() + "_"
                + Instant.now().toEpochMilli();
        Path screenshot = Path.of("target", "screenshots", testName + ".png");
        try {
            Files.createDirectories(screenshot.getParent());
            Files.write(screenshot, screenshotDriver.getScreenshotAs(OutputType.BYTES));
            System.out.println("Failure screenshot saved: " + screenshot.toAbsolutePath());
        } catch (IOException e) {
            throw new UncheckedIOException("Could not save UI test failure screenshot to " + screenshot, e);
        }
    }

    private void closeBrowser() {
        WebDriver driver = DRIVER.get();
        DRIVER.remove();
        if (driver != null) {
            driver.quit();
        }
    }
}
