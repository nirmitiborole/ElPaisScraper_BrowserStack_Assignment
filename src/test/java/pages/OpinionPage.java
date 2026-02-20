package pages;


import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;
import java.util.logging.Logger;

public class OpinionPage {
    private WebDriver driver;
    private WebDriverWait wait;
    private static final Logger logger = Logger.getLogger(OpinionPage.class.getName());

    public OpinionPage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    public void waitForLoad(String deviceLabel) {
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("article")));
        logger.info("[" + deviceLabel + "] Opinion section loaded successfully.");
    }

    public List<WebElement> getArticles() {
        return driver.findElements(By.tagName("article"));
    }

    public void scrollIntoView(WebElement article) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", article);
    }


    public String getArticleTitle(WebElement article) throws Exception {
        return article.findElement(By.tagName("h2")).getText();
    }

    public String getArticleContent(WebElement article) throws Exception {
        return article.findElement(By.tagName("p")).getText();
    }

    public String getArticleImageUrl(WebElement article) throws Exception {
        return article.findElement(By.tagName("img")).getAttribute("src");
    }
}