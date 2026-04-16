package com.newsaggregator.application.service;

import com.newsaggregator.domain.model.Feed;
import com.newsaggregator.domain.port.in.AddFeedUseCase;
import com.newsaggregator.domain.port.out.FeedRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class OpmlService {

    private final FeedRepository feedRepository;
    private final AddFeedUseCase addFeedUseCase;

    public OpmlService(FeedRepository feedRepository, AddFeedUseCase addFeedUseCase) {
        this.feedRepository = feedRepository;
        this.addFeedUseCase = addFeedUseCase;
    }

    @Transactional(readOnly = true)
    public String exportToOpml() {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();

            // Root element
            Element rootElement = doc.createElement("opml");
            rootElement.setAttribute("version", "2.0");
            doc.appendChild(rootElement);

            // Head element
            Element head = doc.createElement("head");
            rootElement.appendChild(head);

            Element title = doc.createElement("title");
            title.setTextContent("News Aggregator Feeds");
            head.appendChild(title);

            Element dateCreated = doc.createElement("dateCreated");
            dateCreated.setTextContent(ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME));
            head.appendChild(dateCreated);

            // Body element
            Element body = doc.createElement("body");
            rootElement.appendChild(body);

            // Add feeds as outlines
            List<Feed> feeds = feedRepository.findAll();
            for (Feed feed : feeds) {
                Element outline = doc.createElement("outline");
                outline.setAttribute("type", "rss");
                outline.setAttribute("text", feed.getName());
                outline.setAttribute("title", feed.getName());
                outline.setAttribute("xmlUrl", feed.getUrl());
                if (feed.getDescription() != null) {
                    outline.setAttribute("description", feed.getDescription());
                }
                body.appendChild(outline);
            }

            // Transform to string
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            
            return writer.toString();
        } catch (Exception e) {
            throw new RuntimeException("Fehler beim Exportieren der OPML-Datei", e);
        }
    }

    public List<Feed> importFromOpml(String opmlContent) {
        List<Feed> importedFeeds = new ArrayList<>();
        
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // Security: prevent XXE attacks
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(opmlContent));
            Document doc = builder.parse(is);
            doc.getDocumentElement().normalize();

            NodeList outlines = doc.getElementsByTagName("outline");
            
            for (int i = 0; i < outlines.getLength(); i++) {
                Element outline = (Element) outlines.item(i);
                
                // Check if it's a feed outline (has xmlUrl attribute)
                String xmlUrl = outline.getAttribute("xmlUrl");
                if (xmlUrl == null || xmlUrl.trim().isEmpty()) {
                    continue; // Skip categories or non-feed outlines
                }

                // Skip if feed already exists
                if (feedRepository.existsByUrl(xmlUrl)) {
                    continue;
                }

                String name = outline.getAttribute("text");
                if (name == null || name.trim().isEmpty()) {
                    name = outline.getAttribute("title");
                }
                if (name == null || name.trim().isEmpty()) {
                    name = "Unnamed Feed";
                }

                String description = outline.getAttribute("description");
                if (description != null && description.trim().isEmpty()) {
                    description = null;
                }
                
                try {
                    Feed feed = addFeedUseCase.addFeed(name, xmlUrl, description);
                    importedFeeds.add(feed);
                } catch (IllegalArgumentException e) {
                    // Feed might already exist or be invalid, skip
                }
            }
            
            return importedFeeds;
        } catch (Exception e) {
            throw new RuntimeException("Fehler beim Importieren der OPML-Datei", e);
        }
    }
}
