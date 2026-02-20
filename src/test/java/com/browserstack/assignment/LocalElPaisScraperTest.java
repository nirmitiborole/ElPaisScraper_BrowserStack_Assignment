package com.browserstack.assignment;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class LocalElPaisScraperTest {

    private static final Logger logger = Logger.getLogger(LocalElPaisScraperTest.class.getName());

    public static void main(String[] args) {
        WebDriver driver = null;

        StringBuilder report = new StringBuilder();
        report.append("============================================\n");
        report.append("DEVICE: Local Chrome\n");
        report.append("============================================\n\n");

        try {

            ChromeOptions options = new ChromeOptions();

            driver = new ChromeDriver(options);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(25));
            TranslationService translationService = new TranslationService();

            logger.info("Chrome browser launched successfully.");

            // --- Navigate to El Pais ---
            driver.get("https://elpais.com/");
            logger.info("Navigated to El País homepage.");

            // --- Verify Language ---
            String lang = driver.findElement(By.tagName("html")).getAttribute("lang");
            if (lang != null && lang.startsWith("es")) {
                logger.info("Language verified: Website is in Spanish (lang='" + lang + "').");
                report.append("Language Check : PASSED (lang='").append(lang).append("')\n\n");
            } else {
                logger.warning("Language check failed: Expected 'es' but found '" + lang + "'. Proceeding anyway.");
                report.append("Language Check : FAILED (lang='").append(lang).append("')\n\n");
            }

            // --- Accept Cookies ---
            try {
                WebElement acceptCookies = wait.until(ExpectedConditions.elementToBeClickable(By.id("didomi-notice-agree-button")));
                acceptCookies.click();
                logger.info("Cookie banner accepted.");
            } catch (Exception e) {
                logger.info("Cookie banner not found or already accepted.");
            }

            // --- Navigate to Opinion section ---
            try {
                WebElement opinionLink = wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//a[normalize-space(text())='Opinión']")));
                opinionLink.click();
                logger.info("Successfully clicked 'Opinión' via XPath navigation.");
            } catch (Exception e1) {
                logger.warning("UI navigation failed. Falling back to direct URL navigation.");
                driver.get("https://elpais.com/opinion/");
                logger.info("Navigated directly to Opinion section via URL.");
            }

            // --- Wait for Opinion page to fully load ---
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("article")));
            logger.info("Opinion section loaded successfully.");

            // --- Scrape first 5 articles ---
            List<WebElement> articles = driver.findElements(By.tagName("article"));
            List<String> translatedTitles = new ArrayList<>();

            logger.info("Found " + articles.size() + " articles on the page. Beginning extraction...");

            int count = 0;
            for (WebElement article : articles) {
                if (count >= 5) break;

                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", article);

                // --- Title in Spanish ---
                String titleOriginal = "";
                try {
                    titleOriginal = article.findElement(By.tagName("h2")).getText();
                    if (titleOriginal == null || titleOriginal.trim().isEmpty()) {
                        logger.warning("Empty title for article " + (count + 1) + ", skipping...");
                        continue;
                    }
                } catch (Exception e) {
                    logger.warning("Could not find <h2> tag for article " + (count + 1) + ", skipping...");
                    continue;
                }

                logger.info("========== Article " + (count + 1) + " ==========");
                logger.info("Spanish Title  : " + titleOriginal);
                report.append("========== Article ").append(count + 1).append(" ==========\n");
                report.append("Spanish Title  : ").append(titleOriginal).append("\n");

                // --- Translate Title to English ---
                String translatedTitle = translationService.translate(titleOriginal);
                logger.info("English Title  : " + translatedTitle);
                report.append("English Title  : ").append(translatedTitle).append("\n");
                translatedTitles.add(translatedTitle);

                // --- Content in Spanish ---
                try {
                    WebElement contentElement = article.findElement(By.tagName("p"));
                    String content = contentElement.getText();
                    if (content != null && !content.trim().isEmpty()) {
                        logger.info("Content (Spanish) : " + content);
                        report.append("Content        : ").append(content).append("\n");
                    } else {
                        logger.info("Content (Spanish) : Not available");
                        report.append("Content        : Not available\n");
                    }
                } catch (Exception e) {
                    logger.warning("Could not find content for article " + (count + 1));
                    report.append("Content        : Not available\n");
                }

                // --- Image Downloading ---
                try {
                    WebElement imgElement = article.findElement(By.tagName("img"));
                    String imgUrl = imgElement.getAttribute("src");

                    if (imgUrl != null && !imgUrl.trim().isEmpty()) {
                        logger.info("Image URL : " + imgUrl);
                        report.append("Image URL      : ").append(imgUrl).append("\n");


                        String deviceFolder = "images/local_chrome";
                        Files.createDirectories(Paths.get(deviceFolder));

                        String fileName = deviceFolder + "/article_" + (count + 1) + ".jpg";
                        HttpURLConnection connection = (HttpURLConnection) new URL(imgUrl).openConnection();
                        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                        connection.connect();

                        try (InputStream in = connection.getInputStream();
                             FileOutputStream out = new FileOutputStream(fileName)) {
                            byte[] buffer = new byte[4096];
                            int bytesRead;
                            while ((bytesRead = in.read(buffer)) != -1) {
                                out.write(buffer, 0, bytesRead);
                            }
                        }
                        logger.info("Image saved to : " + fileName);
                        report.append("Image Saved to : ").append(fileName).append("\n");

                    } else {
                        logger.info("Image : Not available");
                        report.append("Image          : Not available\n");
                    }
                } catch (Exception e) {
                    logger.warning("Could not download image for article " + (count + 1));
                    report.append("Image          : Not available\n");
                }

                report.append("\n");
                count++;
            }

            // --- Word Frequency Analysis ---
            String analysis = getAnalysisReport(translatedTitles);
            report.append(analysis);

            // --- Generate Local Report ---
            generateLocalReport(report.toString());

        } catch (Exception e) {
            logger.severe("Test failed: " + e.getMessage());
            report.append("TEST FAILED: ").append(e.getMessage()).append("\n");
            generateLocalReport(report.toString());
            e.printStackTrace();
        } finally {

            if (driver != null) {
                driver.quit();
                logger.info("Browser closed successfully.");
            }
        }
    }

    private static String getAnalysisReport(List<String> titles) {
        StringBuilder analysis = new StringBuilder();
        analysis.append("========== Word Frequency Analysis ==========\n");

        logger.info("========== Word Frequency Analysis ==========");

        Map<String, Integer> wordCounts = new HashMap<>();
        for (String title : titles) {
            String[] words = title.replaceAll("[^a-zA-Z0-9\\s]", "").toLowerCase().split("\\s+");
            for (String word : words) {
                if (word.length() > 2) {
                    wordCounts.put(word, wordCounts.getOrDefault(word, 0) + 1);
                }
            }
        }

        boolean found = false;
        for (Map.Entry<String, Integer> entry : wordCounts.entrySet()) {
            if (entry.getValue() > 2) {
                logger.info("Word: \"" + entry.getKey() + "\" -> Count: " + entry.getValue());
                analysis.append("Word: \"").append(entry.getKey()).append("\" -> Count: ").append(entry.getValue()).append("\n");
                found = true;
            }
        }

        if (!found) {
            logger.info("No words repeated more than twice across all headers.");
            analysis.append("No words repeated more than twice across all headers.\n");
        }

        analysis.append("=============================================\n");
        logger.info("=============================================");

        return analysis.toString();
    }

    private static void generateLocalReport(String reportContent) {
        try {
            Files.write(Paths.get("localreport.txt"), reportContent.getBytes());
            logger.info("Local report generated successfully: localreport.txt");
        } catch (Exception e) {
            logger.severe("Failed to generate local report: " + e.getMessage());
        }
    }
}