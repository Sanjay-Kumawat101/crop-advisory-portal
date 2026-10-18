package com.cropadvisory.ui;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

final class RecordsPage extends PageObject {
    RecordsPage(WebDriver driver, String baseUrl) {
        super(driver, baseUrl);
    }

    void open() {
        open("/records");
        visible(By.id("records-table"));
    }

    void openNewRecordForm() {
        open("/records/new");
        visible(By.id("crop"));
    }

    void search(String query) {
        replaceText(By.id("search-q"), query);
        click(By.id("search-button"));
        visible(By.id("records-table"));
    }

    void openRecord(String problemTitle) {
        click(By.linkText(problemTitle));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("record-title")));
    }

    boolean containsRecord(String problemTitle) {
        return visible(By.id("records-table")).getText().contains(problemTitle);
    }
}
