package com.chat.app.service;

import com.chat.app.model.LinkPreview;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class LinkPreviewService {

    // A more robust regex to find URLs within a text body
    private static final Pattern URL_PATTERN = Pattern.compile(
            "\\b((https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|])");

    public String findFirstUrl(String text) {
        if (text == null) return null;
        Matcher matcher = URL_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group(0);
        }
        return null;
    }

    public LinkPreview generatePreview(String url) {
        try {
            // Set a timeout and a more common user agent
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36")
                    .timeout(5000) // 5 second timeout
                    .get();

            String title = getTagContent(doc, "og:title", "title");
            String description = getTagContent(doc, "og:description", "description");
            String imageUrl = getTagContent(doc, "og:image", null);
            String siteName = getTagContent(doc, "og:site_name", null);

            if (title == null || title.isEmpty()) {
                return null;
            }

            if (siteName == null || siteName.isEmpty()) {
                try {
                    siteName = new URI(url).getHost();
                    if (siteName.startsWith("www.")) {
                        siteName = siteName.substring(4);
                    }
                } catch (URISyntaxException e) {
                    // ignore if host can't be parsed
                }
            }

            return new LinkPreview(url, title, description, imageUrl, siteName);

        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Could not generate link preview for URL: " + url + " - " + e.getMessage());
            return null;
        }
    }

    private String getTagContent(Document doc, String ogProperty, String fallbackTag) {
        // Try Open Graph meta tags first (e.g., <meta property="og:title" ...>)
        String content = doc.select("meta[property=" + ogProperty + "]").attr("content");
        if (!content.isEmpty()) {
            return content;
        }

        // Try standard meta tags (e.g., <meta name="description" ...>)
        content = doc.select("meta[name=" + ogProperty + "]").attr("content");
        if (!content.isEmpty()) {
            return content;
        }

        // Fallback to the standard HTML tag if specified (e.g., <title>...</title>)
        if (fallbackTag != null) {
            content = doc.select(fallbackTag).text();
            if (content.isEmpty() && "description".equals(fallbackTag)) {
                content = doc.select("meta[name=description]").attr("content");
            }
        }

        return content.isEmpty() ? null : content;
    }
}

