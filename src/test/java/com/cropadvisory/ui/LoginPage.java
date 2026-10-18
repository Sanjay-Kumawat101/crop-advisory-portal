package com.cropadvisory.ui;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

final class LoginPage extends PageObject {
    private static final By USERNAME = By.id("username");
    private static final By PASSWORD = By.id("password");

    LoginPage(WebDriver driver, String baseUrl) {
        super(driver, baseUrl);
    }

    void open() {
        open("/login");
        visible(USERNAME);
    }

    void submit(String username, String password) {
        replaceText(USERNAME, username);
        replaceText(PASSWORD, password);
        driver.findElement(By.id("login-button")).submit();
    }

    boolean loginErrorVisible() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-error"))).isDisplayed();
    }

    void waitForHome() {
        wait.until(ExpectedConditions.urlToBe(baseUrl + "/home"));
        visible(By.id("user-role"));
    }
}
