package utils;

import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class ReportGenerator {
    private static final Logger logger = Logger.getLogger(ReportGenerator.class.getName());

    public static void generatePDF(List<Map<String, String>> deviceReports) {
        try {
            com.itextpdf.text.Document document = new com.itextpdf.text.Document();
            com.itextpdf.text.pdf.PdfWriter.getInstance(document, new FileOutputStream("ElPais_Report.pdf"));
            document.open();

            // Fonts
            com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 22,
                    com.itextpdf.text.Font.BOLD, new com.itextpdf.text.BaseColor(30, 30, 30));

            com.itextpdf.text.Font deviceFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 16,
                    com.itextpdf.text.Font.BOLD, new com.itextpdf.text.BaseColor(0, 70, 127));

            com.itextpdf.text.Font articleHeaderFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 13,
                    com.itextpdf.text.Font.BOLD, new com.itextpdf.text.BaseColor(50, 50, 50));

            com.itextpdf.text.Font labelFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 11,
                    com.itextpdf.text.Font.BOLD, new com.itextpdf.text.BaseColor(80, 80, 80));

            com.itextpdf.text.Font valueFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 11,
                    com.itextpdf.text.Font.NORMAL, new com.itextpdf.text.BaseColor(30, 30, 30));

            com.itextpdf.text.Font smallFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 9,
                    com.itextpdf.text.Font.ITALIC, new com.itextpdf.text.BaseColor(100, 100, 100));

            // Report Title
            com.itextpdf.text.Paragraph reportTitle = new com.itextpdf.text.Paragraph(
                    "El Pais Scraper - Test Report\n\n", titleFont);
            reportTitle.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            document.add(reportTitle);

            // Separator line
            com.itextpdf.text.pdf.draw.LineSeparator line = new com.itextpdf.text.pdf.draw.LineSeparator();
            line.setLineColor(new com.itextpdf.text.BaseColor(0, 70, 127));
            document.add(new com.itextpdf.text.Chunk(line));
            document.add(com.itextpdf.text.Chunk.NEWLINE);

            boolean firstDevice = true;

            for (Map<String, String> reportData : deviceReports) {

                // New page for every device except the first
                if (!firstDevice) {
                    document.newPage();
                }
                firstDevice = false;

                String fullText = reportData.get("text");
                String screenshotPath = reportData.get("screenshot");

                if (fullText == null) fullText = "";

                //  1. Device Label
                String deviceLabel = extractValue(fullText, "DEVICE: ", "\n");

                com.itextpdf.text.Paragraph devicePara = new com.itextpdf.text.Paragraph(
                        "Device / Browser: " + deviceLabel, deviceFont);
                devicePara.setSpacingAfter(10f);
                document.add(devicePara);

                // 2. Screenshot (right after device name)
                if (screenshotPath != null && !screenshotPath.isEmpty()) {
                    try {
                        com.itextpdf.text.Paragraph ssLabel = new com.itextpdf.text.Paragraph(
                                "Homepage Screenshot", labelFont);
                        ssLabel.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                        ssLabel.setSpacingAfter(4f);
                        document.add(ssLabel);

                        com.itextpdf.text.Image img = com.itextpdf.text.Image.getInstance(screenshotPath);
                        img.scaleToFit(480, 320);
                        img.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                        document.add(img);
                        document.add(com.itextpdf.text.Chunk.NEWLINE);
                    } catch (Exception e) {
                        logger.warning("Could not add screenshot to PDF: " + e.getMessage());
                    }
                }

                //  3. Language Check (after screenshot)
                String langCheck = extractValue(fullText, "Language Check : ", "\n");
                if (!langCheck.isEmpty()) {
                    com.itextpdf.text.Paragraph langPara = new com.itextpdf.text.Paragraph();
                    langPara.add(new com.itextpdf.text.Chunk("Language Check : ", labelFont));
                    langPara.add(new com.itextpdf.text.Chunk(langCheck, valueFont));
                    langPara.setSpacingAfter(10f);
                    document.add(langPara);
                }

                // Separator
                document.add(new com.itextpdf.text.Chunk(line));
                document.add(com.itextpdf.text.Chunk.NEWLINE);

                //  4. Parse and render each article
                String[] sections = fullText.split("={10} Article \\d+ ={10}");


                for (int i = 1; i < sections.length; i++) {
                    String section = sections[i];

                    com.itextpdf.text.Paragraph articleHeader = new com.itextpdf.text.Paragraph(
                            "Article " + i, articleHeaderFont);
                    articleHeader.setSpacingBefore(8f);
                    articleHeader.setSpacingAfter(6f);
                    document.add(articleHeader);

                    // Spanish Title
                    addLabelValue(document, "Spanish Title  : ", extractValue(section, "Spanish Title  : ", "\n"),
                            labelFont, valueFont);

                    // English Title
                    addLabelValue(document, "English Title  : ", extractValue(section, "English Title  : ", "\n"),
                            labelFont, valueFont);

                    // Content
                    addLabelValue(document, "Content        : ", extractValue(section, "Content        : ", "\n"),
                            labelFont, valueFont);

                    // Image URL
                    String imgUrl = extractValue(section, "Image URL      : ", "\n");
                    if (!imgUrl.isEmpty()) {
                        addLabelValue(document, "Image URL      : ", imgUrl, labelFont, smallFont);
                    }

                    // Image Source (saved path) or Not available
                    String imgSaved = extractValue(section, "Image Saved to : ", "\n");
                    if (imgSaved.isEmpty()) {
                        imgSaved = extractValue(section, "Image          : ", "\n");
                        if (!imgSaved.isEmpty()) {
                            addLabelValue(document, "Image Source   : ", imgSaved, labelFont, valueFont);
                        }
                    } else {
                        addLabelValue(document, "Image Source   : ", imgSaved, labelFont, valueFont);
                    }

                    // Dotted divider between articles
                    com.itextpdf.text.pdf.draw.DottedLineSeparator dotLine =
                            new com.itextpdf.text.pdf.draw.DottedLineSeparator();
                    dotLine.setLineColor(new com.itextpdf.text.BaseColor(180, 180, 180));
                    document.add(new com.itextpdf.text.Chunk(dotLine));
                    document.add(com.itextpdf.text.Chunk.NEWLINE);
                }

                // 5. Word Frequency Analysis
                String analysisStartMarker = "========== Word Frequency Analysis ==========\n";
                String analysisEndMarker   = "=============================================";
                int analysisStart = fullText.indexOf(analysisStartMarker);
                if (analysisStart != -1) {
                    int contentStart = analysisStart + analysisStartMarker.length();
                    int contentEnd   = fullText.indexOf(analysisEndMarker, contentStart);
                    if (contentEnd == -1) contentEnd = fullText.length();
                    String analysisPart = fullText.substring(contentStart, contentEnd).trim();

                    com.itextpdf.text.Paragraph analysisHeader = new com.itextpdf.text.Paragraph(
                            "Word Frequency Analysis", deviceFont);
                    analysisHeader.setSpacingBefore(10f);
                    analysisHeader.setSpacingAfter(6f);
                    document.add(analysisHeader);

                    if (!analysisPart.isEmpty()) {
                        document.add(new com.itextpdf.text.Paragraph(analysisPart, valueFont));
                    }
                }
            }

            document.close();
            logger.info("PDF Report generated successfully: ElPais_Report.pdf");

        } catch (Exception e) {
            logger.severe("Failed to generate PDF report: " + e.getMessage());
        }
    }

    // Helper: extract value between a known prefix and a delimiter
    private static String extractValue(String text, String prefix, String delimiter) {
        int start = text.indexOf(prefix);
        if (start == -1) return "";
        start += prefix.length();
        int end = text.indexOf(delimiter, start);
        if (end == -1) end = text.length();
        return text.substring(start, end).trim();
    }

    // Helper: add a label+value paragraph
    private static void addLabelValue(com.itextpdf.text.Document document,
                                      String label, String value,
                                      com.itextpdf.text.Font labelFont,
                                      com.itextpdf.text.Font valueFont)
            throws com.itextpdf.text.DocumentException {

        if (value == null || value.isEmpty()) return;
        com.itextpdf.text.Paragraph para = new com.itextpdf.text.Paragraph();
        para.add(new com.itextpdf.text.Chunk(label, labelFont));
        para.add(new com.itextpdf.text.Chunk(value, valueFont));
        para.setSpacingAfter(4f);
        document.add(para);
    }
}