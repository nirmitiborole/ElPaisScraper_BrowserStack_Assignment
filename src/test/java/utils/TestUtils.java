package utils;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import java.io.File;
import java.nio.file.StandardCopyOption;

public class TestUtils {
    private static final Logger logger = Logger.getLogger(TestUtils.class.getName());

    public static String analyzeHeaders(List<String> titles, String deviceLabel) {
        StringBuilder analysis = new StringBuilder();
        analysis.append("========== Word Frequency Analysis ==========\n");

        logger.info("[" + deviceLabel + "] ========== Word Frequency Analysis ==========");

        Map<String, Integer> wordCounts = new HashMap<>();
        for (String title : titles) {
            String[] words = title.replaceAll("[^a-zA-Z0-9\\s]", "").toLowerCase().split("\\s+");
            for (String word : words) {
                if (word.length() > 0) {
                    wordCounts.put(word, wordCounts.getOrDefault(word, 0) + 1);
                }
            }
        }

        boolean found = false;
        for (Map.Entry<String, Integer> entry : wordCounts.entrySet()) {
            if (entry.getValue() > 2) {
                logger.info("[" + deviceLabel + "] Word: \"" + entry.getKey() + "\" -> Count: " + entry.getValue());
                analysis.append("Word: \"").append(entry.getKey()).append("\" -> Count: ").append(entry.getValue()).append("\n");
                found = true;
            }
        }

        if (!found) {
            logger.info("[" + deviceLabel + "] No words repeated more than twice across all headers.");
            analysis.append("No words repeated more than twice across all headers.\n");
        }

        analysis.append("=============================================\n");
        logger.info("[" + deviceLabel + "] =============================================");

        return analysis.toString();
    }

    public static String downloadImage(String imgUrl, String deviceLabel, int count) throws Exception {
        String deviceFolder = "images/" + deviceLabel.replaceAll("[^a-zA-Z0-9]", "_");
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
        return fileName;
    }

    public static String takeScreenshot(WebDriver driver, String deviceLabel) throws Exception {
        String deviceFolder = "images/" + deviceLabel.replaceAll("[^a-zA-Z0-9]", "_");
        Files.createDirectories(Paths.get(deviceFolder));

        File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        String destPath = deviceFolder + "/homepage_screenshot.png";

        Files.copy(srcFile.toPath(), Paths.get(destPath), StandardCopyOption.REPLACE_EXISTING);
        return destPath;
    }
}
