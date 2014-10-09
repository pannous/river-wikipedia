package org.elasticsearch.river.wikipedia.support;

import org.elasticsearch.river.wikipedia.PageCallback;
import org.elasticsearch.river.wikipedia.WikipediaRiver;

import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright 2013 Pannous GmbH
 * <p/>
 * User: info@pannous.com / me
 * Date: 10/9/14
 * Time: 1:44 PM
 */

class TestPageCallback extends PageCallback {

    private FileWriter file;

    public TestPageCallback() {
        super(null);
    }

    @Override
    public void process(WikiPage page) {
        debugToFile(page);
        if (page.isRedirect() || !page.isArticle())
            System.out.println("-------- " + page.getTitle().trim() + " --------");
        else System.out.println("++++++++ " + page.getTitle().trim() + " ++++++++");

//            if (1 > 0)
//                return;
        if (page.isRedirect() || !page.isArticle()) return;
//            if (nr++ < 3) return;

//            System.out.println(page.getImages());
//            System.out.println(page.getInfoBox());
        System.out.println(page.getText());

        String[] phrases = page.getText().split("(?i)(?<=[.?!])\\\\S+(?=[a-z])");
        Pattern pattern = Pattern.compile("==(.*?)==");
        Pattern pattern0 = Pattern.compile("==([a-zA-Z ]*)==\\s");
        Pattern pattern2 = Pattern.compile("===(.*?)===");
        String segment = "";
        String subsegment = "";
        for (String phrase : phrases) {
//                phrase=phrase.replaceAll("\\[.*\\]", " ");
            Matcher matcher = pattern.matcher(phrase);
            Matcher matcher0 = pattern0.matcher(phrase);
            Matcher matcher2 = pattern2.matcher(phrase);
            if (matcher0.find()) {
                System.out.println(phrase);
                String newsegment = matcher0.group(1).trim();
                if (!empty(newsegment))
                    segment = newsegment;
                subsegment = "";
            }
            if (matcher2.find()) {
                subsegment = matcher2.group(1).trim();
            }
//                else if (matcher.find()){
//                    segment = matcher.group(1).trim();subsegment = "";
//                }
            System.out.println(segment + "," + subsegment);
        }

        System.exit(0);
//            System.out.println("YAY" + page.getWikiText());
    }

    private void debugToFile(WikiPage page) {
        try {
            if (file == null)
                file = new FileWriter("/Users/me/WikiPage.articles.list");
            file.append(page.getTitle().trim() + "\n");
        } catch (IOException e) {
        }
    }

    private boolean empty(String newsegment) {
        return newsegment == null || newsegment.length() == 0;
    }
}


//    private class WikipediaTestRiver extends WikipediaRiver {
//        public WikipediaTestRiver() throws MalformedURLException {
//            super(new RiverName("x","WikipediaTestRiver"),new RiverDummySettings(),null);
//        }
//    }
