package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.logging.Logger;

public class HomePage {
    private WebDriver driver;
    private WebDriverWait wait;
    private static final Logger logger = Logger.getLogger(HomePage.class.getName());

    public HomePage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    public void navigateTo(String deviceLabel) {
        driver.get("https://elpais.com/");
        logger.info("[" + deviceLabel + "] Navigated to El País homepage.");
    }

    public String getLanguage() {
        return driver.findElement(By.tagName("html")).getAttribute("lang");
    }

    public void acceptCookies(String deviceLabel) {
        try {
            WebElement acceptCookies = wait.until(ExpectedConditions.elementToBeClickable(By.id("didomi-notice-agree-button")));
            acceptCookies.click();
            logger.info("[" + deviceLabel + "] Cookie banner accepted.");
        } catch (Exception e) {
            logger.info("[" + deviceLabel + "] Cookie banner not found or already accepted.");
        }
    }

    public void navigateToOpinionSection(String deviceLabel) {
        try {
            // Desktop Navigation
            WebElement opinionLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[normalize-space(text())='Opinión']")));
            opinionLink.click();
            logger.info("[" + deviceLabel + "] Successfully clicked 'Opinión' via primary desktop navigation.");
        } catch (Exception e1) {
            logger.warning("[" + deviceLabel + "] UI navigation failed completely. Falling back to direct URL navigation.");
            driver.get("https://elpais.com/opinion/");
            logger.info("[" + deviceLabel + "] Navigated directly to Opinion section via URL.");
        }
    }
}


