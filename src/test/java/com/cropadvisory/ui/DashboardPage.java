package com.cropadvisory.ui;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

final class DashboardPage extends PageObject {
    DashboardPage(WebDriver driver, String baseUrl) {
        super(driver, baseUrl);
    }

    void openDashboard() {
        open("/dashboard");
        visible(By.id("total-count"));
    }

    String totalCount() {
        return visible(By.id("total-count")).getText();
    }
}
