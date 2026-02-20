package models;

public class ArticleReport {
    private String originalTitle;
    private String translatedTitle;
    private String content;
    private String imageLocalPath;

    public ArticleReport(String originalTitle, String translatedTitle, String content, String imageLocalPath) {
        this.originalTitle = originalTitle;
        this.translatedTitle = translatedTitle;
        this.content = content;
        this.imageLocalPath = imageLocalPath;
    }

    public String getOriginalTitle() { return originalTitle; }
    public String getTranslatedTitle() { return translatedTitle; }
    public String getContent() { return content; }
    public String getImageLocalPath() { return imageLocalPath; }
}