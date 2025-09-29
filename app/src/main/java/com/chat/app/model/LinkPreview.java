package com.chat.app.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LinkPreview {
    private String url;
    private String title;
    private String description;
    private String imageUrl;
    private String siteName;
}
