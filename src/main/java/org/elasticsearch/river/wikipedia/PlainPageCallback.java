package org.elasticsearch.river.wikipedia;

import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.river.wikipedia.support.InfoBox;
import org.elasticsearch.river.wikipedia.support.PageCallbackHandler;
import org.elasticsearch.river.wikipedia.support.Segment;
import org.elasticsearch.river.wikipedia.support.WikiPage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright 2013 Pannous GmbH
 * <p/>
 * User: info@pannous.com / me
 * Date: 8/12/14
 * Time: 6:10 PM
 */
public class PlainPageCallback implements PageCallbackHandler {

    private WikipediaRiver wikipediaRiver;
    private static BufferedWriter bufferWritter;
    private boolean saveWholePage = false;

    public PlainPageCallback(WikipediaRiver wikipediaRiver) {
        this.wikipediaRiver = wikipediaRiver;
        try {
            if (bufferWritter == null)
                prepareLog();
        } catch (IOException e) {
        }
    }

    private void prepareLog() throws IOException {

        File file = new File("current-WikipediaRiver-article.txt");
        //if file doesnt exists, then create it
        if (!file.exists()) {
            file.createNewFile();
        }

        //true = append file
        FileWriter fileWritter = new FileWriter(file.getName(), true);
        bufferWritter = new BufferedWriter(fileWritter);
    }

    @Override
    public void process(WikiPage page) {
        if (wikipediaRiver != null && wikipediaRiver.closed) {
            wikipediaRiver.logger.warn("river was closing while processing wikipedia page [{}]/[{}].", page.getID(), page.getTitle());
            return;
        }
        String title = WikipediaRiver.stripTitle(page.getTitle());
        trace("page {} : {}", page.getID(), page.getTitle());
        try {
            if (bufferWritter != null) {// log current title if it fails!
                bufferWritter.write(title + "\n");
                bufferWritter.flush();
            }
        } catch (IOException e) {
        }
//        addPage(page);
//        addPhrases(page);
//        if (!page.isArticle())
//        addPhraseSpecial(page);
        try {
            addSegments(page);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addSegments(WikiPage page) throws IOException {
        String title = page.getTitle();
//        String text = page.getText();
        String id = page.getID();
        String image = page.getImage();
        for (Segment segment : page.getSegments()) {

            if (forbidden(segment.topic)) continue;
            if (forbidden(segment.subSubTopic)) continue;
            if (forbidden(segment.subSubTopic)) continue;

            XContentBuilder segmentDoc = XContentFactory.jsonBuilder().startObject();
            segmentDoc.field("topic", title);
            segmentDoc.field("subtopic", segment.topic);
            segmentDoc.field("segment", segment.subTopic);
            segmentDoc.field("subsegment", segment.subSubTopic);
            if(segment.image!=null)
            segmentDoc.field("image", segment.image);
            else
            segmentDoc.field("image", image);
            segmentDoc.field("text", segment.text);
            segmentDoc.field("wiki_id", id);
            segmentDoc.endObject();
            if (wikipediaRiver != null) {
                IndexRequest topic = new IndexRequest(wikipediaRiver.indexName, "topic", page.getID() + "_0").source(segmentDoc);
                wikipediaRiver.bulkProcessor.add(topic);
            } else debug(segmentDoc);
        }

    }

    private boolean forbidden(String topic) {
        topic = topic.trim();
        if (topic.equals("Siehe auch")) return true;
        if (topic.equals("Literatur")) return true;
        if (topic.equals("Einzelnachweise")) return true;
        if (topic.equals("Weblinks")) return true;
        if (topic.equals("See also")) return true;
        if (topic.equals("External links")) return true;
        if (topic.equals("References")) return true;
        if (topic.equals("Notes")) return true;
        return false;
    }

    private void addPhraseSpecial(WikiPage page) throws IOException {
        String title = page.getTitle();
        XContentBuilder phraseDoc = XContentFactory.jsonBuilder().startObject();
        phraseDoc.field("topic", title);
        if (page.isRedirect()) phraseDoc.field("phrase", "#REDIRECT " + page.getText());
        if (page.isDisambiguationPage()) phraseDoc.field("phrase", "#Disambiguation " + page.getText());
        phraseDoc.field("is_special", true);
        phraseDoc.endObject();
        if (wikipediaRiver != null)
            wikipediaRiver.bulkProcessor.add(new IndexRequest(wikipediaRiver.indexName, "phrase", page.getID() + "_0").source(phraseDoc));
        else debug(phraseDoc);
    }


    private void addPage(WikiPage page) {
        try {
            String title = page.getTitle();
            XContentBuilder pageDoc = XContentFactory.jsonBuilder().startObject();
            pageDoc.field("title", title);
//            pageDoc.field("images", page.getImages());
            String image = page.getImage();
            if (!empty(image))
                pageDoc.field("image", image);
            pageDoc.startArray("images");
            for (String image1 : page.getImages())
                if (!empty(image1))
                    pageDoc.value(image1);
            pageDoc.endArray();

            InfoBox infoBox = null;
            try {
                infoBox = page.getInfoBox();
            } catch (Exception e) {
                wikipediaRiver.logger.warn("Error parsing infoBox " + e.getMessage());
            }
            pageDoc.field("text", page.getText());
            pageDoc.field("redirect", page.isRedirect());
//                builder.field("is_category", page.isCategory());
//                builder.field("is_template", page.isTemplate());
            pageDoc.field("is_article", page.isArticle());
            pageDoc.field("redirect_page", page.getRedirectPage());
            pageDoc.field("special", page.isSpecialPage());// is_article
            pageDoc.field("stub", page.isStub());
            pageDoc.field("disambiguation", page.isDisambiguationPage());
            pageDoc.field("infoBox", infoBox);
            pageDoc.startArray("category");
            if (page.getCategories() != null)
                for (String s : page.getCategories()) pageDoc.value(s);
            pageDoc.endArray();
            pageDoc.startArray("link");
            if (page.getLinks() != null)
                for (String s : page.getLinks()) {
                    pageDoc.value(s);
                }
            pageDoc.endArray();
            pageDoc.endObject();
            if (wikipediaRiver != null && wikipediaRiver.closed) {
                wikipediaRiver.logger.warn("river was closing while processing wikipedia page [{}]/[{}].", page.getID(), page.getTitle());
                return;
            }
            if (saveWholePage)
                wikipediaRiver.bulkProcessor.add(new IndexRequest(wikipediaRiver.indexName, wikipediaRiver.typeName, page.getID()).source(pageDoc));

            addPhrases(page);
        } catch (Exception e) {
            e.printStackTrace();
            warn("failed to push index request", e);
        }
    }

    private void addPhrases(WikiPage page) throws IOException {
        String title = page.getTitle();

        // NOW PHRASES:
//            String[] phrases = page.getText().split("\\.\\s+");
//            String[] phrases = page.getText().split("(?i)(?<=[.?!])\\\\S+(?=[a-z])");
        String[] phrases = page.getText().split("(?<=[a-z])\\.\\s+");
        //String[] phrases = page.getText().split("[a-z]\\. ");
        //String[] phrases = page.getText().split("\\.\\r\\n");
        String last = "";
        String subsegment = "";
        String segment = "Description";
        Pattern pattern = Pattern.compile("<h1>(.*?)</h1>");
        Pattern pattern0 = Pattern.compile("<h2>(.*?)</h2>");
        Pattern pattern2 = Pattern.compile("<h3>(.*?)</h3>");
        int nr = 1;
        for (String phrase : phrases) {
            if (phrase.length() < 10)
                continue;
            phrase = phrase.replaceAll("\\(", "");
            phrase = phrase.replaceAll("\\)", "");
            phrase = phrase.replaceAll("\\*", "");
            XContentBuilder phraseDoc = XContentFactory.jsonBuilder().startObject();
            Matcher matcher = pattern.matcher(phrase);
            Matcher matcher0 = pattern0.matcher(phrase);
            Matcher matcher2 = pattern2.matcher(phrase);
            if (matcher0.find()) {
                if (!empty(matcher0.group(1).trim()))// HOW?????
                    segment = matcher0.group(1).trim();
                subsegment = "";
            }
            if (matcher2.find()) subsegment = matcher2.group(1).trim();
            else if (matcher.find()) {
                segment = matcher.group(1).trim();
                subsegment = "";
            }//CLEAR
            phraseDoc.field("topic", title);
            phraseDoc.field("segment", segment);
            phraseDoc.field("subsegment", subsegment);
            String image = page.getImage();
            if (!empty(image))
                phraseDoc.field("image", image);
            phraseDoc.startArray("category");
            if (page.getCategories() != null)
                for (String s : page.getCategories()) phraseDoc.value(s);
            phraseDoc.endArray();
            phraseDoc.field("previous", last);
            phraseDoc.field("phrase", phrase);
            last = phrase;
            phraseDoc.endObject();
            if (wikipediaRiver != null)
                wikipediaRiver.bulkProcessor.add(new IndexRequest(wikipediaRiver.indexName, "phrase", page.getID() + "_" + nr++).source(phraseDoc));
            else debug(phraseDoc);
        }
//                wikipediaRiver.bulkProcessor.flush();
    }

    private void debug(XContentBuilder phraseDoc) {
        try {
            System.out.println(phraseDoc.string());
        } catch (IOException e) {

        }
    }

//    private void debug(Object o) {
//        System.out.println(o);
//    }

    private void warn(String s, Exception e) {
        if (wikipediaRiver != null)// && wikipediaRiver.logger.isTraceEnabled()) {
            wikipediaRiver.logger.warn(s, e);
    }

    private void trace(String s, Object... params) {
        if (wikipediaRiver != null && wikipediaRiver.logger.isTraceEnabled()) {
            wikipediaRiver.logger.trace(s, params);
        }
    }

    private boolean empty(String image) {
        return image == null || image.trim().length() == 0;
    }
}
