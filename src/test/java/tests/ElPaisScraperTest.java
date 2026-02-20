package tests;

import base.BaseTest;
import pages.HomePage;
import pages.OpinionPage;
import utils.TestUtils;
import utils.TranslationService;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ElPaisScraperTest extends BaseTest {

    @Test
    public void scrapeTranslateAndAnalyze() {
        TranslationService translationService = new TranslationService();
        HomePage homePage = new HomePage(driver, wait);
        OpinionPage opinionPage = new OpinionPage(driver, wait);


        StringBuilder report = new StringBuilder();
        report.append("============================================\n");
        report.append("DEVICE: ").append(deviceLabel).append("\n");
        report.append("============================================\n\n");


        String screenshotPath = null;

        try {
            // Navigate and Verify language
            homePage.navigateTo(deviceLabel);

            // Take Screenshot immediately after loading
            try {
                screenshotPath = TestUtils.takeScreenshot(driver, deviceLabel);
                logger.info("[" + deviceLabel + "] Home page screenshot saved.");
            } catch (Exception e) {
                logger.warning("[" + deviceLabel + "] Failed to take screenshot: " + e.getMessage());
            }


            String lang = homePage.getLanguage();

            if (lang != null && lang.startsWith("es")) {
                logger.info("[" + deviceLabel + "] Language verified: Website is in Spanish (lang='" + lang + "').");
                report.append("Language Check : PASSED (lang='").append(lang).append("')\n\n");
            } else {
                logger.warning("[" + deviceLabel + "] Language check failed: Expected 'es' but found '" + lang + "'. Proceeding anyway.");
                report.append("Language Check : FAILED (lang='").append(lang).append("')\n\n");
            }

            // Accept cookies and Navigate
            homePage.acceptCookies(deviceLabel);
            homePage.navigateToOpinionSection(deviceLabel);

            // Scrape first 5 articles
            opinionPage.waitForLoad(deviceLabel);
            List<WebElement> articles = opinionPage.getArticles();
            List<String> translatedTitles = new ArrayList<>();

            logger.info("[" + deviceLabel + "] Found " + articles.size() + " articles on the page. Beginning extraction...");

            int count = 0;
            for (WebElement article : articles) {
                if (count >= 5) break;

                opinionPage.scrollIntoView(article);

                // Title in Spanish
                String titleOriginal = "";
                try {
                    titleOriginal = opinionPage.getArticleTitle(article);
                    if (titleOriginal == null || titleOriginal.trim().isEmpty()) {
                        logger.warning("[" + deviceLabel + "] Empty title for article " + (count + 1) + ", skipping...");
                        continue;
                    }
                } catch (Exception e) {
                    logger.warning("[" + deviceLabel + "] Could not find <h2> tag for article " + (count + 1) + ", skipping...");
                    continue;
                }

                logger.info("[" + deviceLabel + "] ========== Article " + (count + 1) + " ==========");
                logger.info("[" + deviceLabel + "] Spanish Title  : " + titleOriginal);
                report.append("========== Article ").append(count + 1).append(" ==========\n");
                report.append("Spanish Title  : ").append(titleOriginal).append("\n");

                // Translate Title to English
                String translatedTitle = translationService.translate(titleOriginal);
                logger.info("[" + deviceLabel + "] English Title  : " + translatedTitle);
                report.append("English Title  : ").append(translatedTitle).append("\n");
                translatedTitles.add(translatedTitle);

                // Content in Spanish
                try {
                    String content = opinionPage.getArticleContent(article);
                    if (content != null && !content.trim().isEmpty()) {
                        logger.info("[" + deviceLabel + "] Content (Spanish) : " + content);
                        report.append("Content        : ").append(content).append("\n");
                    } else {
                        logger.info("[" + deviceLabel + "] Content (Spanish) : Not available");
                        report.append("Content        : Not available\n");
                    }
                } catch (Exception e) {
                    logger.warning("[" + deviceLabel + "] Could not find content for article " + (count + 1));
                    report.append("Content        : Not available\n");
                }

                // Image Downloading
                try {
                    String imgUrl = opinionPage.getArticleImageUrl(article);

                    if (imgUrl != null && !imgUrl.trim().isEmpty()) {
                        logger.info("[" + deviceLabel + "] Image URL : " + imgUrl);
                        report.append("Image URL      : ").append(imgUrl).append("\n");

                        String savedFileName = TestUtils.downloadImage(imgUrl, deviceLabel, count);

                        logger.info("[" + deviceLabel + "] Image saved to : " + savedFileName);
                        report.append("Image Saved to : ").append(savedFileName).append("\n");
                    } else {
                        logger.info("[" + deviceLabel + "] Image : Not available");
                        report.append("Image          : Not available\n");
                    }
                } catch (Exception e) {
                    logger.warning("[" + deviceLabel + "] Could not download image for article " + (count + 1));
                    report.append("Image          : Not available\n");
                }

                report.append("\n");
                count++;
            }

            // Word Frequency Analysis
            String analysis = TestUtils.analyzeHeaders(translatedTitles, deviceLabel);
            report.append(analysis);


            Map<String, String> finalReport = new HashMap<>();
            finalReport.put("screenshot", screenshotPath);
            finalReport.put("text", report.toString());
            deviceReports.add(finalReport);

            markTestStatus("passed", "Scraping and Translation successful!");

        } catch (Exception e) {
            logger.severe("[" + deviceLabel + "] Test failed: " + e.getMessage());
            report.append("TEST FAILED: ").append(e.getMessage()).append("\n");

            // Still capture the report and screenshot even if a failure occurs later in the test
            Map<String, String> failReport = new HashMap<>();
            failReport.put("screenshot", screenshotPath);
            failReport.put("text", report.toString());
            deviceReports.add(failReport);

            markTestStatus("failed", e.getMessage().replaceAll("\"", "'"));
            throw new RuntimeException(e);
        }
    }
}