package org.elasticsearch.river.wikipedia;

import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.river.wikipedia.support.InfoBox;
import org.elasticsearch.river.wikipedia.support.PageCallbackHandler;
import org.elasticsearch.river.wikipedia.support.WikiPage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright 2013 Pannous GmbH
 * <p>
 * User: info@pannous.com / me
 * Date: 8/12/14
 * Time: 6:10 PM
 */
public class PageCallback implements PageCallbackHandler {

    private WikipediaRiver wikipediaRiver;
    private static BufferedWriter bufferWritter;
    private boolean saveWholePage=false;

    public PageCallback(WikipediaRiver wikipediaRiver) {
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
        if (wikipediaRiver.closed) {
            wikipediaRiver.logger.warn("river was closing while processing wikipedia page [{}]/[{}].",page.getID(), page.getTitle());
            return;
        }
        try {
            String title = wikipediaRiver.stripTitle(page.getTitle());
            if (wikipediaRiver.logger.isTraceEnabled()) {
                wikipediaRiver.logger.trace("page {} : {}", page.getID(), page.getTitle());
            }
            try {
                if (bufferWritter != null) {// log current title if it fails!
                    bufferWritter.write(title + "\n");
                    bufferWritter.flush();
                }
            } catch (IOException e) {
            }
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
            for (String s : page.getCategories()) pageDoc.value(s);
            pageDoc.endArray();
            pageDoc.startArray("link");
            for (String s : page.getLinks()) {
                pageDoc.value(s);
            }
            pageDoc.endArray();
            pageDoc.endObject();
            if (wikipediaRiver.closed) {
                wikipediaRiver.logger.warn("river was closing while processing wikipedia page [{}]/[{}].",page.getID(), page.getTitle());
                return;
            }
            if(saveWholePage)
            wikipediaRiver.bulkProcessor.add(new IndexRequest(wikipediaRiver.indexName, wikipediaRiver.typeName, page.getID()).source(pageDoc));

            // NOW PHRASES:
//            String[] phrases = page.getText().split("\\.\\s+");
//            String[] phrases = page.getText().split("(?i)(?<=[.?!])\\\\S+(?=[a-z])");
            String[] phrases = page.getText().split("(?<=[a-z])\\.\\s+");
            //String[] phrases = page.getText().split("[a-z]\\. ");
            //String[] phrases = page.getText().split("\\.\\r\\n");
            String last = "";
            String subsegment = "";
            String segment = "Description";
            Pattern pattern = Pattern.compile("==(.*?)==");
            Pattern pattern0 = Pattern.compile("==([a-zA-Z ]*)==\\s");
            Pattern pattern2 = Pattern.compile("===(.*?)===");
            int nr = 1;
            if (!page.isArticle()) {
                XContentBuilder phraseDoc = XContentFactory.jsonBuilder().startObject();
                phraseDoc.field("topic", title);
                if (page.isRedirect()) phraseDoc.field("phrase", "#REDIRECT " + page.getText());
                if (page.isDisambiguationPage()) phraseDoc.field("phrase", "#Disambiguation " + page.getText());
                phraseDoc.field("is_special", true);
                phraseDoc.endObject();
                wikipediaRiver.bulkProcessor.add(new IndexRequest(wikipediaRiver.indexName, "phrase", page.getID() + "_0").source(phraseDoc));
            } else {
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
                    if (!empty(image))
                        phraseDoc.field("image", image);
                    phraseDoc.startArray("category");
                    for (String s : page.getCategories()) phraseDoc.value(s);
                    phraseDoc.endArray();
                    phraseDoc.field("previous", last);
                    phraseDoc.field("phrase", phrase);
                    last = phrase;
                    phraseDoc.endObject();
                    wikipediaRiver.bulkProcessor.add(new IndexRequest(wikipediaRiver.indexName, "phrase", page.getID() + "_" + nr++).source(phraseDoc));
                }
//                wikipediaRiver.bulkProcessor.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
            wikipediaRiver.logger.warn("failed to push index request", e);
        }
    }

    private boolean empty(String image) {
        return image == null || image.trim().length() == 0;
    }
}
