package org.elasticsearch.river.wikipedia.support;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.river.RiverName;
import org.elasticsearch.river.RiverSettings;
import org.elasticsearch.river.wikipedia.PageCallback;
import org.elasticsearch.river.wikipedia.WikipediaRiver;
import org.junit.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class WikiPageTest {


    @Test
    public void testGetTextOne() throws Exception {
//        String topic = "Hydrocodone";//Gate_City,_Virginia
//        String topic = "Borussia_Mönchengladbach";
//        String topic = "Avogadro constant";// TODOOO NUMBERS {{3|e=-2}} ...
//        String topic = "Sonne";
        String topic = "Affection";
//        String topic = "Czechoslovakia";
        String url = "http://en.wikipedia.org/w/api.php?format=xml&action=query&titles=" + topic.replace(" ", "_") + "&prop=revisions&rvprop=content";
//        String url = "http://de.wikipedia.org/w/api.php?format=xml&action=query&titles=" + topic.replace(" ", "_") + "&prop=revisions&rvprop=content";
        WikiPage page = new WikiPage();
        String xml = download(url);
        String wiki = xml.substring(xml.indexOf("serve\">") + 7, xml.indexOf("</rev>"));
        page.setWikiText(wiki);
        String text = page.getText();
        String image = page.getImage();
        System.out.println(image);
//        System.out.println(wiki);
        System.out.println(text);
        // MESS!!! uncertainty in the value of the [[Molar mass constant | align=`center`|M |u | 0.001 kg/mol = 1 g/mol | align=`center`|defined | — |- | [[Planck constant | align=`center`|h | 6.626 068 96(33){{e|–34 J s | align=`center`|5.0{{e|–8 | −0.9996 |- | [[Fine structure constant | align=`center`|α | 7.297 352 5376(50){{e|–3 | align=`center`|6.8{{e|–10 | 0.0269 |- | Avogadro constant | align=`center`|N |A | 6.022 141 79(30){{e|23 mol −1 | align=`
//        System.out.println(wiki);
//        assertTrue(wiki.contains("serum half-life that averages 3.8 hours"));
//        assertTrue(text.contains("serum half-life that averages 3.8 hours"));
    }


    @Test
    public void testOneCallback() throws Exception {
//        String topic = "Hydrocodone";//Gate_City,_Virginia
//        String topic = "Borussia_Mönchengladbach";
//        String topic = "Avogadro constant";// TODOOO NUMBERS {{3|e=-2}} ...
        String topic = "Czechoslovakia";
        String url = "http://en.wikipedia.org/w/api.php?format=xml&action=query&titles=" + topic.replace(" ", "_") + "&prop=revisions&rvprop=content";
        System.out.println(url);
        WikiXMLParser parser = WikiXMLParserFactory.getSAXParser(new URL(url));
        parser.setPageCallback(new TestPageCallback());
        parser.parse();
    }

    @Test
    public void testGetTextXML() throws Exception {
        String url = "file:///Users/me/dev/ai/nlp/qa/elasticsearch-river-wikiphrases/enwiki-latest-pages-articles.xml.bz2";
        WikiXMLParser parser = WikiXMLParserFactory.getSAXParser(new URL(url));
//        parser.setPageCallback(new org.elasticsearch.river.wikipedia.PageCallback(new WikipediaTestRiver()));
        parser.setPageCallback(new TestPageCallback());
        parser.parse();
//        WikiPageIterator iterator = parser.getIterator();
//        WikiPage page = iterator.nextPage();// UnsupportedOperationException WTF
    }

    private String download(String address) throws IOException {
        URL url = new URL(address);
        StringBuffer stringBuffer = new StringBuffer();
        URLConnection conn = url.openConnection(Proxy.NO_PROXY);
        InputStream in;
        try {
            in = conn.getInputStream();
        } catch (Exception e1) {// FileNotFoundException krasser android 4 bug ??
            in = url.openStream();
        }
        byte[] buffer = new byte[1024];
        int numRead;
        while ((numRead = in.read(buffer)) != -1) {
            stringBuffer.append(new String(buffer, 0, numRead, "UTF-8"));
        }
        String txt = stringBuffer.toString();
        return txt;
    }

    private int nr = 0;

    static private FileWriter file;

    private class TestPageCallback implements PageCallbackHandler {

        @Override
        public void process(WikiPage page) {
            try {
                if (file == null)
                    file = new FileWriter("/Users/me/WikiPage.articles.list");
                file.append(page.getTitle().trim() + "\n");
            } catch (IOException e) {
            }
//            if (page.isRedirect() || !page.isArticle())
//                 System.out.println("-------- " + page.getTitle().trim() + " --------");
//            else System.out.println("++++++++ " + page.getTitle().trim() + " ++++++++");

            if (1 > 0)
                return;
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
    }

    private boolean empty(String newsegment) {
        return newsegment == null || newsegment.length() == 0;
    }

//    private class WikipediaTestRiver extends WikipediaRiver {
//        public WikipediaTestRiver() throws MalformedURLException {
//            super(new RiverName("x","WikipediaTestRiver"),new RiverDummySettings(),null);
//        }
//    }
}
