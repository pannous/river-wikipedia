package org.elasticsearch.river.wikipedia.support;

import org.junit.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
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
//        String topic = "Affection";
//        String topic = "        Atoms in molecules";
        String topic = "Tara_(name)";
//        String topic = "Czechoslovakia";
        String url = "http://en.wikipedia.org/w/api.php?format=xml&action=query&titles=" + topic.replace(" ", "_") + "&prop=revisions&rvprop=content";
//        String url = "http://de.wikipedia.org/w/api.php?format=xml&action=query&titles=" + topic.replace(" ", "_") + "&prop=revisions&rvprop=content";
        WikiPage page = new WikiPage();
        String xml = download(url);
        String wiki = xml.substring(xml.indexOf("serve\">") + 7, xml.indexOf("</rev>"));
        page.setWikiText(wiki, false);
        String text = page.getText();
        String image = page.getImage();
//        String[] phrases =text.split("(?i)(?<=[.?!])\\S+(?=[a-z])");
//        String[] phrases =text.split("(?<=[a-z])\\.\\s+");
        String[] phrases =text.split("\n");

//        String[] phrases =text.split("(?i)(?<=[.?!])\\\\S+(?=[a-z])");
//        System.out.println(image);
//        System.out.println(join(phrases));
//        System.out.println(wiki);
        System.out.println(text);
        assertTrue(phrases.length>3);
        // MESS!!! uncertainty in the value of the [[Molar mass constant | align=`center`|M |u | 0.001 kg/mol = 1 g/mol | align=`center`|defined | — |- | [[Planck constant | align=`center`|h | 6.626 068 96(33){{e|–34 J s | align=`center`|5.0{{e|–8 | −0.9996 |- | [[Fine structure constant | align=`center`|α | 7.297 352 5376(50){{e|–3 | align=`center`|6.8{{e|–10 | 0.0269 |- | Avogadro constant | align=`center`|N |A | 6.022 141 79(30){{e|23 mol −1 | align=`
//        System.out.println(wiki);
//        assertTrue(wiki.contains("serum half-life that averages 3.8 hours"));
//        assertTrue(text.contains("serum half-life that averages 3.8 hours"));
    }

    private String join(String[] phrases) {
            String s="";
        for (String phrase : phrases) {
            s += phrase + ",\n";
        }
        return s;
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
//        String url = "file:///Users/me/dev/ai/nlp/qa/wikiphrases/enwiki-latest-pages-articles.xml.bz2";
//        String url = "file:///Users/me/dev/ai/nlp/qa/wikiphrases/wiki-plaintext-de/AA/wiki_00.bz2";
//        String url = "file:///Users/me/dev/ai/nlp/qa/wikiphrases/wiki-plaintext-en/AA/wiki_00.bz2";
        String url = "file:///Users/me/dev/ai/nlp/qa/wikiphrases/wiki-plaintext-en/AA/wiki_00";
        WikiXMLParser parser = WikiXMLParserFactory.getSAXParser(new URL(url));
//        parser.setPageCallback(new org.elasticsearch.river.wikipedia.PageCallback(new WikipediaTestRiver()));
//        parser.setPlainWikiXml(true);

        parser.setPageCallback(new TestPlainPageCallback());
//        parser.setPageCallback(new TestPageCallback());
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

}
