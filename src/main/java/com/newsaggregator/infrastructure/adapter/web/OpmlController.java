package com.newsaggregator.infrastructure.adapter.web;

import com.newsaggregator.domain.model.Feed;
import com.newsaggregator.domain.port.in.AddFeedUseCase;
import com.newsaggregator.domain.port.out.FeedRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
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
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/feeds/opml")
public class OpmlController {

    private final FeedRepository feedRepository;
    private final AddFeedUseCase addFeedUseCase;

    public OpmlController(FeedRepository feedRepository, AddFeedUseCase addFeedUseCase) {
        this.feedRepository = feedRepository;
        this.addFeedUseCase = addFeedUseCase;
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> importOpml(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Datei ist leer");
        }

        try {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(content)));
            
            NodeList outlines = doc.getElementsByTagName("outline");
            int importedCount = 0;
            
            for (int i = 0; i < outlines.getLength(); i++) {
                Element outline = (Element) outlines.item(i);
                String xmlUrl = outline.getAttribute("xmlUrl");
                String title = outline.getAttribute("title");
                String text = outline.getAttribute("text");
                
                if (!xmlUrl.isEmpty()) {
                    try {
                        String feedName = title.isEmpty() ? (text.isEmpty() ? "Unnamed Feed" : text) : title;
                        addFeedUseCase.addFeed(feedName, xmlUrl, null);
                        importedCount++;
                    } catch (Exception e) {
                        // Feed existiert bereits oder anderer Fehler - überspringen
                    }
                }
            }
            
            return ResponseEntity.ok("Erfolgreich " + importedCount + " Feeds importiert");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Fehler beim Import: " + e.getMessage());
        }
    }

    @GetMapping(value = "/export", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<byte[]> exportOpml() {
        try {
            List<Feed> feeds = feedRepository.findAll();
            
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();
            
            Element opml = doc.createElement("opml");
            opml.setAttribute("version", "2.0");
            doc.appendChild(opml);
            
            Element head = doc.createElement("head");
            opml.appendChild(head);
            
            Element title = doc.createElement("title");
            title.setTextContent("News Aggregator Feeds");
            head.appendChild(title);
            
            Element dateCreated = doc.createElement("dateCreated");
            dateCreated.setTextContent(LocalDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME));
            head.appendChild(dateCreated);
            
            Element body = doc.createElement("body");
            opml.appendChild(body);
            
            for (Feed feed : feeds) {
                Element outline = doc.createElement("outline");
                outline.setAttribute("text", feed.getName());
                outline.setAttribute("title", feed.getName());
                outline.setAttribute("type", "rss");
                outline.setAttribute("xmlUrl", feed.getUrl());
                if (feed.getDescription() != null) {
                    outline.setAttribute("description", feed.getDescription());
                }
                body.appendChild(outline);
            }
            
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            
            byte[] bytes = writer.toString().getBytes(StandardCharsets.UTF_8);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_XML);
            headers.setContentDispositionFormData("attachment", "feeds.opml");
            
            return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
