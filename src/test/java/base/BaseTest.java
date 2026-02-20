package base;


import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;
import utils.ReportGenerator;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.util.*;
import java.util.logging.Logger;

public class BaseTest {

    // Shared data structure to collect results from all parallel threads
    protected static final List<Map<String, String>> deviceReports = Collections.synchronizedList(new ArrayList<>());
    protected static final Logger logger = Logger.getLogger(BaseTest.class.getName());

    protected WebDriver driver;
    protected WebDriverWait wait;
    protected Properties config;
    protected String deviceLabel;

    @BeforeMethod(alwaysRun = true)
    @Parameters({"platform", "browserName", "device"})
    public void setup(String platform, String browserName, String device) throws IOException {
        // Load Configuration
        config = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                logger.severe("Sorry, unable to find config.properties");
                return;
            }
            config.load(input);
        }


        deviceLabel = device.isEmpty() ? platform + " - " + browserName : device + " (" + platform + "/" + browserName + ")";

        String username = config.getProperty("browserstack.username");
        String accessKey = config.getProperty("browserstack.key");
        String hubUrl = "https://" + username + ":" + accessKey + "@hub-cloud.browserstack.com/wd/hub";

        MutableCapabilities capabilities = new MutableCapabilities();
        HashMap<String, Object> bstackOptions = new HashMap<>();

        bstackOptions.put("userName", username);
        bstackOptions.put("accessKey", accessKey);
        bstackOptions.put("projectName", "El Pais Scraper");
        bstackOptions.put("buildName", "Build 1.0");
        bstackOptions.put("sessionName", "Test on " + (device.isEmpty() ? "Desktop " + browserName : device));
        bstackOptions.put("consoleLogs", "info");

        if (!device.isEmpty()) {
            bstackOptions.put("deviceName", device);
            if (platform.equalsIgnoreCase("ANDROID")) bstackOptions.put("osVersion", "13.0");
            else if (platform.equalsIgnoreCase("IOS")) bstackOptions.put("osVersion", "17");
        } else {
            if (platform.equalsIgnoreCase("Windows")) {
                bstackOptions.put("os", "Windows");
                bstackOptions.put("osVersion", "10");
            } else if (platform.equalsIgnoreCase("MAC")) {
                bstackOptions.put("os", "OS X");
                bstackOptions.put("osVersion", "Monterey");
            }
        }

        capabilities.setCapability("bstack:options", bstackOptions);
        capabilities.setCapability("browserName", browserName);

        driver = new RemoteWebDriver(new URL(hubUrl), capabilities);
        wait = new WebDriverWait(driver, Duration.ofSeconds(25));
    }

    protected void markTestStatus(String status, String reason) {
        try {
            JavascriptExecutor jse = (JavascriptExecutor) driver;
            jse.executeScript("browserstack_executor: {\"action\": \"setSessionStatus\", \"arguments\": {\"status\": \"" + status + "\", \"reason\": \"" + reason + "\"}}");
        } catch (Exception e) {
            logger.severe("Error marking test status: " + e.getMessage());
        }
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        if (driver != null) driver.quit();
    }

    @AfterSuite(alwaysRun = true)
    public void generatePDFReport() {
        ReportGenerator.generatePDF(deviceReports);
    }
}