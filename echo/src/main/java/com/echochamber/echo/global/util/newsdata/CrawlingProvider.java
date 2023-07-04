package com.echochamber.echo.global.util.newsdata;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class CrawlingProvider {
    private String formatText(Element element) {
        if (element.is("p")) {
            return element.text() + "\n\n";
        } else {
            return element.text() + "\n";
        }
    }

    private String getDataList(Document doc, String query) {
        Elements selects = doc.select(query);
        StringBuilder formattedText = new StringBuilder();

        for (Element element : selects) {
            formattedText.append(formatText(element));
        }

        return formattedText.toString();
    }

    public String getNewsContent(String url, String query) throws IOException {
        Connection conn = Jsoup.connect(url);

        Document doc = null;
        doc = conn.get();

        return getDataList(doc, query);
    }
}
