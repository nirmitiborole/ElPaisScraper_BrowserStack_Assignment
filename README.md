El Pais Opinion Scraper

This is a web scraping and automation project built with Java and Selenium. It runs cross-browser tests on BrowserStack to extract articles from the El Pais opinion section.

What it does:
Navigates to El Pais and accepts cookies.
Scrapes the first 5 articles from the opinion page.
Translates the Spanish titles to English using a translation API.
Downloads the cover image for each article.
Counts repeated words in the translated headers.
Generates a PDF report containing screenshots, the translated content, and the word frequency analysis.

Tech Stack:
Java
Selenium WebDriver
TestNG (for parallel execution)
iText (for PDF generation)
BrowserStack (for cross-device testing)

Project Structure:
The code follows the Page Object Model (POM) to keep things clean.
models: Data structures for the articles.
pages: Locators and methods for the homepage and opinion page.
utils: Helper functions for translating, downloading images, and making the PDF.
tests: The main test script.

How to run:
Add your BrowserStack and RapidAPI keys to src/test/resources/config.properties.
Run the testng.xml file to start the parallel execution across the configured desktop and mobile devices.