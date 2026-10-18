package com.cropadvisory.ui;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class RecordFormPage extends PageObject {
    RecordFormPage(WebDriver driver, String baseUrl) {
        super(driver, baseUrl);
    }

    RecordDetailPage create(String crop, String village, String problemTitle, String description) {
        replaceText(By.id("crop"), crop);
        replaceText(By.id("village"), village);
        replaceText(By.id("problemTitle"), problemTitle);
        replaceText(By.id("description"), description);
        assertEquals(crop, driver.findElement(By.id("crop")).getDomProperty("value"));
        assertEquals(village, driver.findElement(By.id("village")).getDomProperty("value"));
        assertEquals(problemTitle, driver.findElement(By.id("problemTitle")).getDomProperty("value"));
        assertEquals(description, driver.findElement(By.id("description")).getDomProperty("value"));
        visible(By.id("save-record")).submit();
        wait.until(currentDriver -> currentDriver.getCurrentUrl().matches(".*/records/\\d+$")
                || currentDriver.findElements(By.className("field-error")).stream().anyMatch(WebElement::isDisplayed));
        if (!driver.getCurrentUrl().matches(".*/records/\\d+$")) {
            List<String> errors = driver.findElements(By.className("field-error")).stream()
                    .map(WebElement::getText)
                    .toList();
            throw new AssertionError("Record form submission was rejected: " + errors);
        }
        visible(By.id("record-title"));
        return new RecordDetailPage(driver, baseUrl);
    }
}
