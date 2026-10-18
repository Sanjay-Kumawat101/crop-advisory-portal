package com.cropadvisory.ui;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

final class RecordDetailPage extends PageObject {
    RecordDetailPage(WebDriver driver, String baseUrl) {
        super(driver, baseUrl);
    }

    String status() {
        return visible(By.id("record-status")).getText();
    }

    String title() {
        return visible(By.id("record-title")).getText();
    }

    String crop() {
        return visible(By.id("record-crop")).getText();
    }

    void moveToUnderReview() {
        visible(By.id("btn-under_review")).submit();
        wait.until(ExpectedConditions.textToBe(By.id("record-status"), "Under Review"));
    }

    void advise(String advice) {
        replaceText(By.id("advice"), advice);
        visible(By.id("btn-advised")).submit();
        wait.until(ExpectedConditions.textToBe(By.id("record-status"), "Advised"));
        wait.until(ExpectedConditions.textToBe(By.id("record-advice"), advice));
    }
}
