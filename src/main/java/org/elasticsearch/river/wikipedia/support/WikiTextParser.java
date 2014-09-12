/*
 * Licensed to ElasticSearch and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. ElasticSearch licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *//*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.river.wikipedia.support;


import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * For internal use only -- Used by the {@link WikiPage} class.
 * Can also be used as a stand alone class to parse wiki formatted text.
 *
 * @author Delip Rao
 */
public class WikiTextParser {

    private String wikiText = null;
    private ArrayList<String> pageCats = null;
    private ArrayList<String> pageLinks = null;
    private boolean redirect = false;
    private String redirectString = null;
    private static Pattern redirectPattern =
            Pattern.compile("#REDIRECT", Pattern.CASE_INSENSITIVE);
    private static Pattern redirectPage =
            Pattern.compile("#REDIRECT\\s+\\[\\[(.*?)\\]\\]", Pattern.CASE_INSENSITIVE);
    private static Pattern redirectPattern2 =
            Pattern.compile("#WEITERLEITUNG", Pattern.CASE_INSENSITIVE);
    private static Pattern redirectPage2 =
            Pattern.compile("#WEITERLEITUNG\\s+\\[\\[(.*?)\\]\\]", Pattern.CASE_INSENSITIVE);
    private boolean stub = false;
    private boolean disambiguation = false;
    private static Pattern stubPattern = Pattern.compile("\\-stub\\}\\}");
    // the first letter of pages is case-insensitive
    private static Pattern disambCatPattern =
            Pattern.compile("\\{\\{[Dd]isambig(uation)?\\}\\}");
    private static Pattern disambCatPattern2 =
            Pattern.compile("\\{\\{[Bb]egriffsklärung\\}\\}");
    private InfoBox infoBox = null;
    private boolean category;
    private boolean template;

    public WikiTextParser(String wtext) {
        wikiText = wtext;
        checkRedirect();
        checkRedirectDE();
        Matcher matcher = stubPattern.matcher(wikiText);
        stub = matcher.find();
        matcher = disambCatPattern.matcher(wikiText);
        disambiguation = matcher.find();
        matcher = disambCatPattern2.matcher(wikiText);
        disambiguation = disambiguation || matcher.find();
    }

    private void checkRedirect() {
        Matcher matcher = redirectPage.matcher(wikiText);
        Matcher redirectmatcher = redirectPattern.matcher(wikiText);
        if (redirectmatcher.find()) {
            redirect = true;
            if (matcher.find() && matcher.groupCount() == 1)
                redirectString = matcher.group(1);
        }
    }

    private void checkRedirectDE() {
        Matcher matcher = redirectPage2.matcher(wikiText);
        Matcher redirectmatcher = redirectPattern2.matcher(wikiText);
        if (redirectmatcher.find()) {
            redirect = true;
            if (matcher.find() && matcher.groupCount() == 1)
                redirectString = matcher.group(1);
        }
    }

    public boolean isRedirect() {
        return redirect;
    }

    public boolean isStub() {
        return stub;
    }

    public String getRedirectText() {
        return redirectString;
    }

    public String getText() {
        return wikiText;
    }

    public ArrayList<String> getCategories() {
        if (pageCats == null) parseCategories();
        return pageCats;
    }

    public ArrayList<String> getLinks() {
        if (pageLinks == null) parseLinks();
        return pageLinks;
    }

    private void parseCategories() {
        pageCats = new ArrayList<String>();
        Pattern catPattern = Pattern.compile("\\[\\[[Cc]ategory:(.*?)\\]\\]", Pattern.MULTILINE);
        Matcher matcher = catPattern.matcher(wikiText);
        while (matcher.find()) {
            String[] temp = matcher.group(1).split("\\|");
            pageCats.add(temp[0]);
        }
    }

    private void parseLinks() {
        pageLinks = new ArrayList<String>();
        Pattern catPattern = Pattern.compile("\\[\\[(.*?)\\]\\]", Pattern.MULTILINE);
        Matcher matcher = catPattern.matcher(wikiText);
        while (matcher.find()) {
            String[] temp = matcher.group(1).split("\\|");
            if (temp == null || temp.length == 0) continue;
            String link = temp[0];
            if (link.contains(":") == false) {
                pageLinks.add(link);
            }
        }
    }

//    static WikiClean cleaner;

    public String getPlainText() {
//        if (cleaner == null)
//            cleaner = new WikiCleanBuilder().build();
        String text = wikiText;
//        text = stripCite(text);
//        text = text.replaceAll("\\[.*?\\]", " ");
//        if (1 > 0)
//            return text;
        text = text.replaceAll("(?s)==\\s*Siehe auch.*", "");
        text = text.replaceAll("(?s)==\\s*Literatur.*", "");
        text = text.replaceAll("(?s)==\\s*Einzelnachweise.*", "");
        text = text.replaceAll("(?s)==\\s*Weblinks.*", "");
        text = text.replaceAll("(?s)==\\s*See also.*", "");
        text = text.replaceAll("(?s)==\\s*External links.*", "");
        text = text.replaceAll("(?s)==\\s*References.*", "");
        text = text.replace("&amp;", "&");
        text = text.replace("&gt;", ">");
        text = text.replace("&lt;", "<");
        text = text.replace("&ndash;", "-");
        text = text.replace("&nbsp;", " ");
        text = text.replace("&quot;", "`");
//        text = text.replaceAll("\\* (\\d\\d\\. \\w+ \\d\\d\\d\\d)", "geboren $1");
//        text = text.replaceAll("\\† (\\d\\d\\. \\w+ \\d\\d\\d\\d)", "† gestorben $1");
//        text = text.replaceAll("\\* (\\d\\d \\w+ \\d\\d\\d\\d)", "born $1");
//        text = text.replaceAll("\\† (\\d\\d \\w+ \\d\\d\\d\\d)", "† died $1");
//        text = text.replaceAll("(\\d\\d \\w+ \\d\\d\\d\\d) – (\\d\\d \\w+ \\d\\d\\d\\d)", "born $1 died $2");
        text = text.replace("fmt=commas|", "");
        text = text.replaceAll("<ref.*?</ref>", " ");
        text = text.replaceAll("</?.*?>", " ");
        text = text.replaceAll("(?s)<!--.*?-->", "");// (?s) == "single-line" mode :  Pattern.DOTALL  . match \n !!
//        text = text.replaceAll("\\{\\{e|(.*?)\\}\\}", " $1 ");
//        text = text.replaceAll("\\{\\{val|(.*?)\\}\\}", " $1 ");
//        text = text.replaceAll("\\{\\{sub|(\\w*?)\\}\\}", " $1 ");

        try {
            text = stripCite(text, 0);
        } catch (Exception e) {
        }
//        text = text.replaceAll("\\{\\{.*?\\}\\}", " ");
//        text = text.replaceAll("\\{.*?\\}", " ");
//        text = text.replaceAll("(?s)\\{\\{.*?\\}\\}", " ");
//        text = cleaner.clean(text);
//        System.out.println(text);
        text = text.replaceAll("\\[\\[\\w+:[^\\]]+\\]\\]", " ");// [[File:...]]
        // to do [[File:Lugi Gallean2.jpg|right|thumb|[[Italian-American]] anarchist [[Luigi Galleani]]. His followers, known as Galleanists, carried out a series of bombings
        // Pattern.compile( static
        text = text.replaceAll("\\[\\[[^\\[]+\\|(.*?)\\]\\]", "$1");// super ... AGGRESSIVE!!!
        text = text.replaceAll("\\[\\[(.*?)\\]\\]", "$1");
        text = text.replaceAll("\\[.*?\\]", " ");
        text = text.replaceAll("\\'+", "");
        text = text.replaceAll("&nbsp;", " ");
//        text = text.replaceAll("\n", " ");
//        text = text.replaceAll("}", " "); //don't hide bug
        text = text.replaceAll("\\s+", " ");
        text = text.replaceAll("p\\.\\s?\\d+", "");
        text = replaceGermanAbbrevations(text);
        text = replaceEnglishAbbrevations(text);
        text = text.replaceAll("([a-z])\\. ", "$1.\r\n");
        return text;
    }

    private String replaceEnglishAbbrevations(String text) {
        text = text.replace(" abbr.", " abbreviation");
        text = text.replace(" Acad.", " Academy");
        text = text.replace(" A.D.", " anno Domini");
        text = text.replace(" alt.", " altitude");
        text = text.replace(" a.m.", " ante meridiem");
        text = text.replace(" AM.", " amplitude modulation");
        text = text.replace(" Assn.", " Association");
        text = text.replace(" at.", " atomic");
        text = text.replace(" no.", " number");
        text = text.replace(" wt.", " weight");
        text = text.replace(" Aug.", " August");
        text = text.replace(" Ave.", " Avenue");
        text = text.replace(" b.", " born");
        text = text.replace(" b.p.", " boiling point");
        text = text.replace(" C.", " Celsius");
        text = text.replace(" c.", " circa");
        text = text.replace(" cal.", " calorie");
        text = text.replace(" Capt.", " Captain");
        text = text.replace(" cent.", " century, centuries");
        text = text.replace(" cm.", " centimeter");
        text = text.replace(" co.", " county");
        text = text.replace(" Col.", " Colonel");
        text = text.replace(" Comdr.", " Commander");
        text = text.replace(" Corp.", " Corporation");
        text = text.replace(" Cpl.", " Corporal");
        text = text.replace(" cu.", " cubic");
        text = text.replace(" d.", " died");
        text = text.replace(" D.C.", " District of Columbia");
        text = text.replace(" Dec.", " December");
        text = text.replace(" dept.", " department");
        text = text.replace(" dist.", " district");
        text = text.replace(" div.", " division");
        text = text.replace(" Dr.", " doctor");
        text = text.replace(" ed.", " edited, edition, editor");
        text = text.replace(" est.", " established; estimated");
        text = text.replace(" et al.", " et alii");
        text = text.replace(" F.", " Fahrenheit");
        text = text.replace(" Feb.", " February");
        text = text.replace(" ft.", " foot, feet");
        text = text.replace(" gal.", " gallon");
        text = text.replace(" Gen.", " General, Genesis");
        text = text.replace(" Gov.", " governor");
        text = text.replace(" grad.", " graduated, graduated at");
        text = text.replace(" H.", " hour");
        text = text.replace(" Hon.", " the Honorable");
        text = text.replace(" hr.", " hour");
        text = text.replace(" i.e.", " id est");
        text = text.replace(" in.", " inch");
        text = text.replace(" inc.", " incorporated");
        text = text.replace(" Inst.", " Institute, Institution");
        text = text.replace(" Jan.", " January");
        text = text.replace(" Jr.", " Junior");
        text = text.replace(" K.", " Kelvin");
        text = text.replace(" kg.", " kilogram");
        text = text.replace(" km.", " kilometer");
        text = text.replace(" lat.", " latitude");
        text = text.replace(" Lib.", " Library");
        text = text.replace(" long.", " longitude");
        text = text.replace(" Lt.", " Lieutenant");
        text = text.replace(" Ltd.", " Limited");
        text = text.replace(" mg.", " milligram");
        text = text.replace(" mi.", " mile");
        text = text.replace(" min.", " minute");
        text = text.replace(" mm.", " millimeter");
        text = text.replace(" mph.", " miles per hour");
        text = text.replace(" Mr.", " Mister");
        text = text.replace(" Mrs.", " Mistress");
        text = text.replace(" mt.", " Mount");
        text = text.replace(" mts.", " mountains");
        text = text.replace(" Mus.", " Museum");
        text = text.replace(" NE.", " northeast");
        text = text.replace(" no.", " number");
        text = text.replace(" Nov.", " November");
        text = text.replace(" Oct.", " October");
        text = text.replace(" Op.", " Opus");
        text = text.replace(" oz.", " ounce");
        text = text.replace(" pl.", " plural");
        text = text.replace(" pop.", " population");
        text = text.replace(" pseud.", " pseudonym");
        text = text.replace(" pt.", " part");
        text = text.replace(" pt.", " pint");
        text = text.replace(" pub.", " published");
        text = text.replace(" qt.", " quart");
        text = text.replace(" Rev.", " Revelation; the Reverend");
        text = text.replace(" rev.", " revised");
        text = text.replace(" rpm.", " revolution per minute");
        text = text.replace(" sec.", " second; secant");
        text = text.replace(" Sept.", " September");
        text = text.replace(" Ser.", " Series");
        text = text.replace(" Sgt.", " Sergeant");
        text = text.replace(" sq.", " square");
        text = text.replace(" Sr.", " Senior");
        text = text.replace(" Univ.", " University");
        text = text.replace(" vol.", " volume");
        text = text.replace(" vs.", " versus");
        text = text.replace(" wt.", " weight");
        text = text.replace(" yd.", " yard");
        text = text.replace(" Acad.", " Academy ");
        text = text.replace(" abbrev.", " abbreviation ");
        text = text.replace(" abbrev.", " abbreviation ");
        text = text.replace(" abbrev.", " abbreviation ");
        text = text.replace(" abl.", " ablative");
        text = text.replace(" absol.", " absolute");
        text = text.replace(" Abstr.", " Abstract");
        text = text.replace(" abstr.", " abstract");
        text = text.replace(" acc.", " accusative");
        text = text.replace(" Acct.", " Account");
        text = text.replace(" acct.", " account");
        text = text.replace(" ad.", " adaptation of");
        text = text.replace(" Add.", " Addenda");
        text = text.replace(" add.", " addenda");
        text = text.replace(" adj.", " adjective");
        text = text.replace(" Adv.", " Advance");
        text = text.replace(" adv.", " advance");
        text = text.replace(" adv.", " adverb");
        text = text.replace(" advb.", " adverbial");
        text = text.replace(" Advt.", " advertisement");
        text = text.replace(" advt.", " advertisement");
        text = text.replace(" Afr.", " Africa");
        text = text.replace(" afr.", " african");
        text = text.replace(" Agric.", " Agriculture");
        text = text.replace(" agric.", " agriculture");
        text = text.replace(" Alb.", " Albanian");
        text = text.replace(" alb.", " albanian");
        text = text.replace(" alt.", " alternative");
        text = text.replace(" Amer.", " American");
        text = text.replace(" amer.", " american");
        text = text.replace(" Anat.", " Anatomy");
        text = text.replace(" anat.", " anatomic");
        text = text.replace(" Anc.", " Ancient");
        text = text.replace(" anc.", " ancient");
        text = text.replace(" Ann.", " Annals");
        text = text.replace(" ann.", " annals");
        text = text.replace(" Anthrop.", " Anthropology");
        text = text.replace(" anthrop.", " anthropology");
        text = text.replace(" Antiq.", " Antiquities");
        text = text.replace(" antiq.", " antiquities");
        text = text.replace(" aphet.", " aphetic");
        text = text.replace(" app.", " apparently");
        text = text.replace(" Appl.", " Applied");
        text = text.replace(" appl.", " applied");
        text = text.replace(" approx.", " approximately");
        text = text.replace(" Applic.", " Application");
        text = text.replace(" applic.", " application");
        text = text.replace(" appos.", " appositive");
        text = text.replace(" Arab.", " Arabic");
        text = text.replace(" arab.", " arab");
        text = text.replace(" Aram.", " Aramaic");
        text = text.replace(" aram.", " aramaic");
        text = text.replace(" Arch.", " Architecture");
        text = text.replace(" arch.", " architecture");
        text = text.replace(" Arm.", " Armenian");
        text = text.replace(" arm.", " armenian");
        text = text.replace(" assoc.", " association");
        text = text.replace(" Astr.", " Astronomy");
        text = text.replace(" astr.", " astronomy");
        text = text.replace(" Astrol.", " Astrology");
        text = text.replace(" astrol.", " astrology");
        text = text.replace(" Astron.", " Astronomy");
        text = text.replace(" astron.", " astronomy");
        text = text.replace(" Astronaut.", " Astronautic");
        text = text.replace(" astronaut.", " astronautic");
        text = text.replace(" attrib.", " attributive");
        text = text.replace(" Austral.", " Australian");
        text = text.replace(" austral.", " australian");
        text = text.replace(" Autobiogr.", " Autobiography");
        text = text.replace(" autobiogr.", " autobiography");
        text = text.replace(" Bibliogr.", " Bibliography");
        text = text.replace(" bibliogr.", " bibliography");
        text = text.replace(" Biochem.", " Biochemistry");
        text = text.replace(" biochem.", " biochemistry");
        text = text.replace(" Biol.", " Biology");
        text = text.replace(" biol.", " biology");
        text = text.replace(" Bk.", " Book");
        text = text.replace(" bk.", " book");
        text = text.replace(" Bot.", " Botany");
        text = text.replace(" bot.", " botany");
        text = text.replace(" Brit.", " Britain");
        text = text.replace(" brit.", " britain");
        text = text.replace(" Bulg.", " Bulgarian");
        text = text.replace(" bulg.", " bulgarian");
        text = text.replace(" Bull.", " Bulletin");
        text = text.replace(" bull.", " bulletin");
        text = text.replace(" ca.", " circa");
        text = text.replace(" c.a.", " circa");
        text = text.replace(" Cal.", " Calendar");
        text = text.replace(" cal.", " calendar");
        text = text.replace(" Cambr.", " Cambridge");
        text = text.replace(" cambr.", " cambridge");
        text = text.replace(" Canad.", " Canadian");
        text = text.replace(" canad.", " canadian");
        text = text.replace(" Catal.", " Catalogue");
        text = text.replace(" catal.", " catalogue");
        text = text.replace(" Celt.", " Celtic");
        text = text.replace(" celt.", " celtic");
        text = text.replace(" Cent.", " Century");
        text = text.replace(" cent.", " century");
        text = text.replace(" Ch.", " Church");
        text = text.replace(" ch.", " church");
        text = text.replace(" Chem.", " Chemistry");
        text = text.replace(" chem.", " chemistry");
        text = text.replace(" Chr.", " Christian");
        text = text.replace(" chr.", " christian");
        text = text.replace(" Chron.", " Chronicle");
        text = text.replace(" chron.", " chronicle");
        text = text.replace(" Chronol.", " Chronology");
        text = text.replace(" chronol.", " chronology");
        text = text.replace(" Cinemat.", " Cinematologic");
        text = text.replace(" cinemat.", " cinematologic");
        text = text.replace(" cl.", " classical");
        text = text.replace(" Clin.", " Clinical");
        text = text.replace(" clin.", " clinical");
        text = text.replace(" cogn.", " cognate");
        text = text.replace(" Col.", " Colonel");
        text = text.replace(" col.", " colonel");
        text = text.replace(" Coll.", " Collection");
        text = text.replace(" coll.", " collection");
        text = text.replace(" collect.", " collective");
        text = text.replace(" colloq.", " colloquial");
        text = text.replace(" Comb.", " Combinations");
        text = text.replace(" comb.", " combinations");
        text = text.replace(" comb.", " combined");
        text = text.replace(" Comm.", " Commercial usage");
        text = text.replace(" comm.", " commercial usage");
        text = text.replace(" Communic.", " Communications");
        text = text.replace(" communic.", " communications");
        text = text.replace(" comp.", " compound");
        text = text.replace(" Compan.", " Companion");
        text = text.replace(" compan.", " companion");
        text = text.replace(" compar.", " comparative");
        text = text.replace(" compl.", " complement");
        text = text.replace(" Compl.", " Complete");
        text = text.replace(" compl.", " complete");
        text = text.replace(" Conc.", " Concise");
        text = text.replace(" conc.", " concise");
        text = text.replace(" concr.", " concrete");
        text = text.replace(" Conf.", " Conference");
        text = text.replace(" conf.", " conference");
        text = text.replace(" Congr.", " Congress");
        text = text.replace(" congr.", " congress");
        text = text.replace(" conj.", " conjunction");
        text = text.replace(" cons.", " consonant");
        text = text.replace(" const.", " construction");
        text = text.replace(" contr.", " contrast ");
        text = text.replace(" Contrib.", " Contribution");
        text = text.replace(" contrib.", " contribution");
        text = text.replace(" Corr.", " Correspondence");
        text = text.replace(" corr.", " corr");// ...
        text = text.replace(" corresp.", " corresponding ");
        text = text.replace(" cpd.", " compound");
        text = text.replace(" Crit.", " criticism");
        text = text.replace(" crit.", " critic");// al ...
        text = text.replace(" Cryst.", " Crystallography");
        text = text.replace(" cryst.", " crystallography");
        text = text.replace(" Cycl.", " Cyclopaedia");
        text = text.replace(" cycl.", " cyclopaedia");
        text = text.replace(" Cytol.", " Cytology");
        text = text.replace(" cytol.", " cytology");
        text = text.replace(" c.", " c");
        text = text.replace(" Da.", " Danish");
        text = text.replace(" da.", " danish");
        text = text.replace(" dat.", " dative");
        text = text.replace(" Deb.", " Debate");
        text = text.replace(" deb.", " debate");
        text = text.replace(" def.", " definite");
        text = text.replace(" dem.", " demonstrative");
        text = text.replace(" Dep.", " Department");
        text = text.replace(" dep.", " department");
        text = text.replace(" deriv.", " derivative");
        text = text.replace(" derog.", " derogatory");
        text = text.replace(" Descr.", " Description");
        text = text.replace(" descr.", " description");
        text = text.replace(" Devel.", " Development");
        text = text.replace(" devel.", " development");
        text = text.replace(" Diagn.", " Diagnosis");
        text = text.replace(" diagn.", " diagnosis");
        text = text.replace(" dial.", " dialect");
        text = text.replace(" Dict.", " Dictionary");
        text = text.replace(" Dict.", " dictionary");
        text = text.replace(" dict.", " dictionary");
        text = text.replace(" dim.", " diminutive");
        text = text.replace(" Dis.", " Disease");
        text = text.replace(" dis.", " disease");
        text = text.replace(" Diss.", " Dissertation");
        text = text.replace(" diss.", " dissertation");
        text = text.replace(" Du.", " Dutch");
        text = text.replace(" du.", " dutch");
        text = text.replace(" eg.", " exempli gratia");
        text = text.replace(" e.g.", " exempli gratia");
        text = text.replace(" e.t.c.", " et cetera");
        text = text.replace(" Ecol.", " Ecology");
        text = text.replace(" ecol.", " ecologic");
        text = text.replace(" Econ.", " Economics");
        text = text.replace(" econ.", " economic");
        text = text.replace(" ed.", " edition");
        text = text.replace(" Educ.", " Education");
        text = text.replace(" educ.", " education");
        text = text.replace(" Electr.", " Electricity");
        text = text.replace(" electr.", " electric");
        text = text.replace(" Electron.", " Electronic");
        text = text.replace(" electron.", " electronic");
        text = text.replace(" Elem.", " Element");
        text = text.replace(" elem.", " element");
        text = text.replace(" ellipt.", " elliptical");
        text = text.replace(" Embryol.", " Embryology");
        text = text.replace(" embryol.", " embryology");
        text = text.replace(" Encycl.", " Encyclopaedia");
        text = text.replace(" encycl.", " encyclopaedia");
        text = text.replace(" Eng.", " England");
        text = text.replace(" eng.", " england");
        text = text.replace(" Engin.", " Engineering");
        text = text.replace(" engin.", " engineering");
        text = text.replace(" Ent.", " Entomology");
        text = text.replace(" ent.", " entomology");
        text = text.replace(" Entomol.", " Entomology");
        text = text.replace(" entomol.", " entomology");
        text = text.replace(" erron.", " erroneous");
        text = text.replace(" esp.", " especially");
        text = text.replace(" Ess.", " Essay");
        text = text.replace(" ess.", " essay");
        text = text.replace(" et al.", " et al");
        text = text.replace(" etc.", " et cetera");
        text = text.replace(" etym.", " etymology");
        text = text.replace(" euphem.", " euphemistically");
        text = text.replace(" Exam.", " Examination");
        text = text.replace(" exam.", " examination");
        text = text.replace(" exc.", " except");
        text = text.replace(" Exerc.", " Exercise");
        text = text.replace(" exerc.", " exercise");
        text = text.replace(" Exper.", " Experiment");
        text = text.replace(" exper.", " experiment");
        text = text.replace(" Explor.", " Exploration");
        text = text.replace(" explor.", " exploration");
        text = text.replace(" E.", " East");
        text = text.replace(" e.", " east");
        text = text.replace(" fem.", " feminine");
        text = text.replace(" fig.", " figurative");
        text = text.replace(" Finn.", " Finnish");
        text = text.replace(" finn.", " finnish");
        text = text.replace(" Found.", " Foundation");
        text = text.replace(" found.", " foundation");
        text = text.replace(" Fr.", " French");
        text = text.replace(" fr.", " french");
        text = text.replace(" freq.", " frequent");
        text = text.replace(" Fund.", " Fundamental");
        text = text.replace(" fund.", " fundamental");
        text = text.replace(" F.", " French");
        text = text.replace(" f.", " f");
//        text = text.replace(" f."," french");
//        text = text.replace(" f."," female");
        text = text.replace(" G.", " German");
        text = text.replace(" g.", " german");
        text = text.replace(" Gael.", " Gaelic");
        text = text.replace(" gael.", " gaelic");
        text = text.replace(" Gaz.", " Gazette");
        text = text.replace(" gaz.", " gazette");
        text = text.replace(" Gen.", " General");
        text = text.replace(" gen.", " general");
        text = text.replace(" gen.", " genitive");
        text = text.replace(" Geogr.", " Geography");
        text = text.replace(" geogr.", " geography");
        text = text.replace(" Geol.", " Geology");
        text = text.replace(" geol.", " geology");
        text = text.replace(" Geom.", " Geometry");
        text = text.replace(" geom.", " geometry");
        text = text.replace(" Geomorphol.", " Geomorphology");
        text = text.replace(" geomorphol.", " geomorphology");
        text = text.replace(" Ger.", " German");
        text = text.replace(" ger.", " german");
        text = text.replace(" Gloss.", " Glossary");
        text = text.replace(" gloss.", " glossary");
        text = text.replace(" Gmc.", " Germanic");
        text = text.replace(" gmc.", " germanic");
        text = text.replace(" Goth.", " Gothic");
        text = text.replace(" goth.", " gothic");
        text = text.replace(" Govt.", " Government");
        text = text.replace(" govt.", " government");
        text = text.replace(" Gr.", " Great");
        text = text.replace(" gr.", " great");
        text = text.replace(" Gr.", " Greek");
        text = text.replace(" gr.", " greek");
        text = text.replace(" Gram.", " Grammar");
        text = text.replace(" gram.", " grammar");
        text = text.replace(" Gt.", " Great");
        text = text.replace(" gt.", " great");
        text = text.replace(" Heb.", " Hebrew");
        text = text.replace(" heb.", " hebrew");
        text = text.replace(" Her.", " Heraldry");
        text = text.replace(" her.", " heraldry");
        text = text.replace(" Herb.", " among herbalists");
        text = text.replace(" herb.", " among herbalists");
        text = text.replace(" Hind.", " Hindustani");
        text = text.replace(" hind.", " hindustani");
        text = text.replace(" hist.", " historical");
        text = text.replace(" Hist.", " History");
        text = text.replace(" hist.", " history");
        text = text.replace(" Histol.", " Histology");
        text = text.replace(" histol.", " histology");
        text = text.replace(" Hort.", " Horticulture");
        text = text.replace(" hort.", " horticulture");
        text = text.replace(" Househ.", " Household");
        text = text.replace(" househ.", " household");
        text = text.replace(" Housek.", " Housekeeping");
        text = text.replace(" housek.", " housekeeping");
        text = text.replace(" i.e.", " ie");
        text = text.replace(" Icel.", " Icelandic");
        text = text.replace(" icel.", " icelandic");
        text = text.replace(" id.", " idem");
        text = text.replace(" Illustr.", " Illustration");
        text = text.replace(" illustr.", " illustration");
        text = text.replace(" imit.", " imitative");
        text = text.replace(" Immunol.", " Immunology");
        text = text.replace(" immunol.", " immunology");
        text = text.replace(" imp.", " imperative");
        text = text.replace(" impers.", " impersonal");
        text = text.replace(" impf.", " imperfect");
        text = text.replace(" Ind.", " Indian");
        text = text.replace(" ind.", " indian");
        text = text.replace(" ind.", " indicative");
        text = text.replace(" indef.", " indefinite");
        text = text.replace(" inf.", " infinitive");
        text = text.replace(" infl.", " influenced");
        text = text.replace(" Inorg.", " Inorganic");
        text = text.replace(" inorg.", " inorganic");
        text = text.replace(" Ins.", " Insurance");
        text = text.replace(" ins.", " insurance");
        text = text.replace(" Inst.", " Institute");
        text = text.replace(" inst.", " instance");
//        text = text.replace(" int."," interjection");
//        text = text.replace(" intr."," intransitive");
        text = text.replace(" Introd.", " Introduction");
        text = text.replace(" introd.", " introduction");
        text = text.replace(" Ir.", " Irish");
        text = text.replace(" ir.", " irish");
        text = text.replace(" irreg.", " irregular");
        text = text.replace(" It.", " Italian");
        text = text.replace(" it.", " italian");
        text = text.replace(" Jap.", " Japanese");
        text = text.replace(" jap.", " japanese");
        text = text.replace(" Jrnl.", " Journal");
        text = text.replace(" jrnl.", " journal");
        text = text.replace(" Jun.", " Junior");
        text = text.replace(" jun.", " junior");
        text = text.replace(" Knowl.", " Knowledge");
        text = text.replace(" knowl.", " knowledge");
        text = text.replace(" l.", " l");
        text = text.replace(" lang.", " language");
        text = text.replace(" Lect.", " Lecture");
        text = text.replace(" lect.", " lecture");
        text = text.replace(" Less.", " Lesson");
        text = text.replace(" less.", " lesson");
        text = text.replace(" Let.", " Letter");
        text = text.replace(" let.", " letter");
        text = text.replace(" lit.", " literal");
        text = text.replace(" Mag.", " Magazine");
        text = text.replace(" mag.", " magazine");
        text = text.replace(" Magn.", " Magnetic");
        text = text.replace(" magn.", " magnetic");
        text = text.replace(" Man.", " Manual");
        text = text.replace(" man.", " manual");
        text = text.replace(" Managem.", " Management");
        text = text.replace(" managem.", " management");
        text = text.replace(" Manch.", " Manchester");
        text = text.replace(" manch.", " manchester");
        text = text.replace(" Manuf.", " Manufacture");
        text = text.replace(" manuf.", " manufacture");
        text = text.replace(" Mar.", " Marine");
        text = text.replace(" mar.", " marine");
        text = text.replace(" masc.", " masculine");
        text = text.replace(" Math.", " Mathematics");
        text = text.replace(" math.", " mathematics");
        text = text.replace(" Mech.", " Mechanics");
        text = text.replace(" mech.", " mechanics");
        text = text.replace(" Med.", " Medicine");
        text = text.replace(" med.", " medicine");
        text = text.replace(" Mem.", " Memoir");
        text = text.replace(" mem.", " memoir");
        text = text.replace(" Meteorol.", " Meteorology");
        text = text.replace(" meteorol.", " meteorology");
        text = text.replace(" Mics.", " Miscellany");
        text = text.replace(" misc.", " miscellaneous");
        text = text.replace(" midl.", " midland ");
        text = text.replace(" Mil.", " military");
        text = text.replace(" mil.", " military");
        text = text.replace(" Min.", " Mineralogy");
        text = text.replace(" min.", " mineralogy");
        text = text.replace(" Mineral.", " Mineralogy");
        text = text.replace(" mineral.", " mineralogy");
        text = text.replace(" mod.", " modern");
        text = text.replace(" Mus.", " Music");
        text = text.replace(" mus.", " music");
        text = text.replace(" Myst.", " Mystery");
        text = text.replace(" myst.", " mystery");
        text = text.replace(" Mythol.", " Mythology");
        text = text.replace(" mythol.", " mythology");
        text = text.replace(" m.", " masculine");
        text = text.replace(" N.Amer.", " North America");
        text = text.replace(" n.amer.", " north america");
        text = text.replace(" n.q.", " no quotations");
        text = text.replace(" Narr.", " Narrative");
        text = text.replace(" narr.", " narrative");
        text = text.replace(" Nat.", " Natural");
        text = text.replace(" nat.", " natural");
        text = text.replace(" neut.", " neuter");
        text = text.replace(" No.", " Number");
        text = text.replace(" no.", " number");
        text = text.replace(" nom.", " nominative");
        text = text.replace(" Norw.", " Norwegian");
        text = text.replace(" norw.", " norwegian");
        text = text.replace(" Nucl.", " Nuclear");
        text = text.replace(" nucl.", " nuclear");
        text = text.replace(" n.", " n");
        text = text.replace(" obj.", " object");
        text = text.replace(" obl.", " oblique");
        text = text.replace(" occas.", " occasional");
        text = text.replace(" opp.", " opposed ");
        text = text.replace(" Opt.", " Optics");
        text = text.replace(" opt.", " opticical");
        text = text.replace(" Org.", " Organic");
        text = text.replace(" org.", " organic");
        text = text.replace(" orig.", " original");
        text = text.replace(" Ornith.", " Ornithology");
        text = text.replace(" ornith.", " ornithology");
        text = text.replace(" Outl.", " Outline");
        text = text.replace(" outl.", " outline");
        text = text.replace(" Oxf.", " Oxford");
        text = text.replace(" oxf.", " oxford");
        text = text.replace(" p.", " page");
        text = text.replace(" Palaeogr.", " Palaeography");
        text = text.replace(" palaeogr.", " palaeography");
        text = text.replace(" Palaeont.", " Palaeontology");
        text = text.replace(" palaeont.", " palaeontology");
        text = text.replace(" pass.", " passive");
        text = text.replace(" Path.", " Pathology");
        text = text.replace(" path.", " pathology");
        text = text.replace(" perh.", " perhaps ");
        text = text.replace(" Pers.", " Persian");
        text = text.replace(" pers.", " person");
        text = text.replace(" pf.", " perfect");
        text = text.replace(" Pg.", " Portuguese");
        text = text.replace(" pg.", " portuguese");
        text = text.replace(" Pharm.", " Pharma");//cology
        text = text.replace(" pharm.", " pharma");
        text = text.replace(" Philol.", " Philology");
        text = text.replace(" philol.", " philology");
        text = text.replace(" Philos.", " Philosophy");
        text = text.replace(" philos.", " philosophy");
        text = text.replace(" phonet.", " phonetic");
        text = text.replace(" Photogr.", " Photography");
        text = text.replace(" photogr.", " photography");
        text = text.replace(" phr.", " phrase");
        text = text.replace(" Phys.", " physical");
        text = text.replace(" phys.", " physical");
        text = text.replace(" Physiol.", " Physiology");
        text = text.replace(" physiol.", " physiologic");
        text = text.replace(" Pict.", " Picture");
        text = text.replace(" pict.", " picture");
        text = text.replace(" pl.", " place");
        text = text.replace(" poet.", " poetic");
        text = text.replace(" Pol.", " Politic");
        text = text.replace(" pol.", " politic");
//        text = text.replace(" Pol."," Polish");
//        text = text.replace(" pol."," polish");
        text = text.replace(" Polit.", " Politics");
        text = text.replace(" polit.", " politics");
        text = text.replace(" pop.", " popular");
        text = text.replace(" Porc.", " Porcelain");
        text = text.replace(" porc.", " porcelain");
        text = text.replace(" poss.", " possessive");
        text = text.replace(" Pott.", " Pottery");
        text = text.replace(" pott.", " pottery");
        text = text.replace(" ppl.", " poeple");
        text = text.replace(" pple.", " participle");
        text = text.replace(" pr.", " present");
        text = text.replace(" Pr.", " Provencal");
        text = text.replace(" pr.", " provencal");
        text = text.replace(" Pract.", " Practice");
        text = text.replace(" pract.", " practice");
        text = text.replace(" prec.", " preceding ");
        text = text.replace(" pred.", " predicative");
        text = text.replace(" pref.", " prefix");
        text = text.replace(" prep.", " preposition");
        text = text.replace(" pres.", " present");
        text = text.replace(" Princ.", " Principle");
        text = text.replace(" princ.", " principle");
        text = text.replace(" priv.", " privative");
        text = text.replace(" prob.", " probably");
        text = text.replace(" Probl.", " Problem");
        text = text.replace(" probl.", " problem");
        text = text.replace(" Proc.", " Proceedings");
        text = text.replace(" proc.", " proceedings");
        text = text.replace(" pron.", " pronoun");
        text = text.replace(" pronunc.", " pronunciation");
        text = text.replace(" prop.", " properly");
        text = text.replace(" Pros.", " Prosody");
        text = text.replace(" pros.", " prosody");
        text = text.replace(" Prov.", " Provencal");
        text = text.replace(" prov.", " provencal");
        text = text.replace(" Psych.", " Psychology");
        text = text.replace(" psych.", " psychology");
        text = text.replace(" Psychol.", " Psychology");
        text = text.replace(" psychol.", " psychology");
        text = text.replace(" Publ.", " Publications");
        text = text.replace(" publ.", " publications");
        text = text.replace(" Q.", " Quarterly");
        text = text.replace(" q.", " quarterly");
        text = text.replace(" q.v.", " quod vide");
        text = text.replace(" R.", " Royal");
        text = text.replace(" r.", " royal");
        text = text.replace("Ch.", "Church");
        text = text.replace(" Radiol.", " Radiology");
        text = text.replace(" radiol.", " radiology");
        text = text.replace(" Rec.", " Record");
        text = text.replace(" rec.", " record");
        text = text.replace(" redupl.", " reduplicating");
        text = text.replace(" Ref.", " Reference");
        text = text.replace(" ref.", " reference");
        text = text.replace(" refash.", " refashioned");
        text = text.replace(" refl.", " reflexive");
        text = text.replace(" Reg.", " Register");
        text = text.replace(" reg.", " regular");
        text = text.replace(" rel.", " related to");
        text = text.replace(" Reminisc.", " Reminiscence");
        text = text.replace(" reminisc.", " reminiscence");
        text = text.replace(" Rep.", " Report");
        text = text.replace(" rep.", " report");
        text = text.replace(" repr.", " representative");
        text = text.replace(" Res.", " Research");
        text = text.replace(" res.", " research");
        text = text.replace(" Rev.", " Review");
        text = text.replace(" rev.", " review");
        text = text.replace(" rev.", " revised");
        text = text.replace(" Rhet.", " Rhetoric");
        text = text.replace(" rhet.", " rhetoric");
        text = text.replace(" Rom.", " Roman");
        text = text.replace(" rom.", " roman");
        text = text.replace(" Rum.", " Rumanian");
        text = text.replace(" rum.", " rumanian");
        text = text.replace(" Russ.", " Russian");
        text = text.replace(" russ.", " russian");
        text = text.replace(" S.Afr.", " South Africa");
        text = text.replace(" s.afr.", " south africa");
        text = text.replace(" s.v.", " sub voce");
        text = text.replace(" sb.", " substantive");
        text = text.replace(" sc.", " scilicet");
        text = text.replace(" Sc.", " Scottish");
        text = text.replace(" sc.", " scottish");
        text = text.replace(" Scand.", " Scandinavia");
        text = text.replace(" scand.", " scandinavia");
        text = text.replace(" Sch.", " School");
        text = text.replace(" sch.", " school");
        text = text.replace(" Scotl.", " Scotland");
        text = text.replace(" scotl.", " scotland");
        text = text.replace(" Sel.", " Selection");
        text = text.replace(" sel.", " selection");
        text = text.replace(" Ser.", " Series");
        text = text.replace(" ser.", " series");
        text = text.replace(" sing.", " singular");
        text = text.replace(" Sk.", " Sketch");
        text = text.replace(" sk.", " sketch");
        text = text.replace(" Skr.", " Sanskrit");
        text = text.replace(" skr.", " sanskrit");
        text = text.replace(" Soc.", " Society");
        text = text.replace(" soc.", " society");
        text = text.replace(" Sociol.", " Sociology");
        text = text.replace(" sociol.", " sociology");
        text = text.replace(" Sp.", " Sp");
//        text = text.replace(" Sp."," Spanish");
//        text = text.replace(" sp."," spanish");
//        text = text.replace(" Sp."," Speech");
//        text = text.replace(" sp."," speech");
//        text = text.replace(" sp."," spelling");
        text = text.replace(" spec.", " specifically");
        text = text.replace(" Spec.", " Specimen");
        text = text.replace(" spec.", " specimen");
        text = text.replace(" St.", " Saint");
        text = text.replace(" st.", " saint");
        text = text.replace(" Stand.", " Standard");
        text = text.replace(" stand.", " standard");
        text = text.replace(" Stanf.", " Stanford");
        text = text.replace(" stanf.", " stanford");
        text = text.replace(" str.", " Street");
        text = text.replace(" str.", " street");
        text = text.replace(" Struct.", " Structure");
        text = text.replace(" struct.", " structure");
        text = text.replace(" Stud.", " Studies");
        text = text.replace(" stud.", " studies");
        text = text.replace(" subj.", " subject");
        text = text.replace(" subord.", " subordinate");
        text = text.replace(" subseq.", " subsequent");
        text = text.replace(" subst.", " substantively");
        text = text.replace(" suff.", " suffix");
        text = text.replace(" superl.", " superlative");
        text = text.replace(" Suppl.", " Supplement");
        text = text.replace(" suppl.", " supplement");
        text = text.replace(" Surg.", " Surgery");
        text = text.replace(" surg.", " surgery");
        text = text.replace(" Sw.", " Swedish");
        text = text.replace(" sw.", " swedish");
        text = text.replace(" syll.", " syllable");
        text = text.replace(" Syr.", " Syrian");
        text = text.replace(" syr.", " syrian");
        text = text.replace(" Syst.", " System");
        text = text.replace(" syst.", " system");
        text = text.replace(" techn.", " technical");
        text = text.replace(" Technol.", " Technology");
        text = text.replace(" technol.", " technology");
        text = text.replace(" Telegr.", " Telegraphy");
        text = text.replace(" telegr.", " telegraphy");
        text = text.replace(" Teleph.", " Telephony");
        text = text.replace(" teleph.", " telephony");
        text = text.replace(" Theatr.", " Theatre");
        text = text.replace(" theatr.", " theatre");
        text = text.replace(" Theol.", " Theology");
        text = text.replace(" theol.", " theology");
        text = text.replace(" Theoret.", " Theoretical");
        text = text.replace(" theoret.", " theoretical");
        text = text.replace(" tr.", " translated");
        text = text.replace(" Trans.", " Transactions");
        text = text.replace(" trans.", " transactions");
        text = text.replace(" trans.", " transitive");
        text = text.replace(" transf.", " transferred");
        text = text.replace(" transl.", " translation");
        text = text.replace(" Trav.", " Travel");
        text = text.replace(" trav.", " travel");
        text = text.replace(" Treas.", " Treasury");
        text = text.replace(" treas.", " treasury");
        text = text.replace(" Treat.", " Treatise");
        text = text.replace(" treat.", " treatise");
        text = text.replace(" Treatm.", " Treatment");
        text = text.replace(" treatm.", " treatment");
        text = text.replace(" trig.", " Trigonometry");
        text = text.replace(" trig.", " trigonometry");
        text = text.replace(" trop.", " Tropical");
        text = text.replace(" trop.", " tropical");
        text = text.replace(" turk.", " Turkish");
        text = text.replace(" turk.", " turkish");
        text = text.replace(" typogr.", " Typography");
        text = text.replace(" typogr.", " typography");
        text = text.replace(" ult.", " ultimate");
        text = text.replace(" Univ.", " University");
        text = text.replace(" univ.", " university");
        text = text.replace(" unkn.", " unknown");
        text = text.replace(" usu.", " usually");
        text = text.replace(" var.", " variable");
        text = text.replace(" vulg.", " vulgar");
        text = text.replace(" w.", " with");
        text = text.replace(" wd.", " word");
        text = text.replace(" Wks.", " Works");
        text = text.replace(" wks.", " works");
        text = text.replace(" Yrs.", " Years");
        text = text.replace(" yrs.", " years");
        text = text.replace(" Zool.", " Zoology");
        text = text.replace(" zool.", " zoology");
        return text;
    }

    private String replaceGermanAbbrevations(String text) {
        text = text.replace("Abb.", "Abbildung");
        text = text.replace("Abf.", "Abfahrt");
        text = text.replace("Abk.", "Abkürzung");
        text = text.replace("Abs.", "Absender");
        text = text.replace("Abt.", "Abteilung");
        text = text.replace("abzgl.", "abzüglich");
        text = text.replace("Adr.", "Adresse");
        text = text.replace(" am.", "amerikanisch");
        text = text.replace("amtl.", "amtlich");
        text = text.replace("Anh.", "Anhang");
        text = text.replace("Ank.", "Ankunft");
        text = text.replace("Anl.", "Anlage");
        text = text.replace("Anm.", "Anmerkung");
        text = text.replace("Aufl.", "Auflage");
        text = text.replace(" b.", "bis/bei");
        text = text.replace("Bd.", "Band");
        text = text.replace(" bes.", " besonders");
        text = text.replace("Best.-Nr.", "Bestellnummer");
        text = text.replace("Betr.", "Betreff");
        text = text.replace("Bez.", "Bezeichnung/Beziehung");
        text = text.replace("Bhf.", "Bahnhof");
        text = text.replace("bzgl.", "bezüglich");
        text = text.replace("bzw.", "beziehungsweise");
        text = text.replace(" ca.", "circa");
        text = text.replace("Chr.", "Christus");
//        text = text.replace("...l.", "lich");
        text = text.replace("d.Ä.", "der Ältere");
        text = text.replace("dgl.", "dergleichen, desgleichen");
        text = text.replace("d.h.", "das heißt");
        text = text.replace("Dipl.", "Diplom");
        text = text.replace("Ing.", "Ingenieur");
        text = text.replace("Kfm.", "Kaufmann");
        text = text.replace("Dir.", "Direktor");
        text = text.replace("d.J.", "der Jüngere");
        text = text.replace("Dr. med.", "Doktor der Medizin");
        text = text.replace("Dr. phil.", "Doktor der Philosophie");
        text = text.replace("Dr.", "Doktor");
        text = text.replace(" med.", "Medizin");
        text = text.replace(" phil.", "Philosophie");
        text = text.replace(" dt.", " deutsch");
        text = text.replace("Dtzd.", "Dutzend");
        text = text.replace("e.h.", "ehrenhalber");
        text = text.replace("ehem.", "ehemals");
        text = text.replace("eigtl.", "eigentlich");
        text = text.replace("einschl.", "einschließlich");
        text = text.replace("entspr.", "entsprechend");
        text = text.replace(" erb.", " erbaut");
        text = text.replace(" erw.", " erweitert");
        text = text.replace("Erw.", "Erwachsene");
        text = text.replace(" ev.", " evangelisch");
        text = text.replace("evtl.", "eventuell");
//        text = text.replace("e.Wz.", "eingetragenes Warenzeichen");
        text = text.replace("exkl.", "exklusive");
        text = text.replace("Fa.", "Firma");
        text = text.replace("Fam.", "Familie");
        text = text.replace("F.f.", "Fortsetzung folgt");
        text = text.replace("Fr.", "Frau");
        text = text.replace("Frl.", "Fräulein");
        text = text.replace("frz.", "französisch");
        text = text.replace("gg.", "gegen");
        text = text.replace(" geb.", " geboren");
        text = text.replace("Gebr.", "Gebrüder");
        text = text.replace(" gedr.", " gedruckt");
        text = text.replace(" gegr.", " gegründet");
        text = text.replace(" gek.", " gekürzt");
        text = text.replace("Ges.", "Gesellschaft");
        text = text.replace(" gesch.", " geschieden");
        text = text.replace(" gest.", " gestorben");
        text = text.replace(" gez.", " gezeichnet");
        text = text.replace("ggf.", "gegebenfalls");
        text = text.replace("ggfs.", "gegebenfalls");
        text = text.replace("ggs.", "Gegensatz");
        text = text.replace("Ggs.", "Gegensatz");
        text = text.replace("Hbf.", "Hauptbahnhof");
        text = text.replace("hpts.", "hauptsächlich");
        text = text.replace("Hptst.", "Hauptstadt");
        text = text.replace("Hr.", "Herr");
        text = text.replace("Hrn.", "Herrn");
        text = text.replace("i.A.", "im Auftrag");
        text = text.replace("i.b.", "im besonderen");
        text = text.replace("i.H.", "im Hause");
        text = text.replace("i.J.", "im Jahre");
        text = text.replace("Ing.", "Ingnieur");
        text = text.replace("Inh.", "Inhaber");
        text = text.replace("Inh.", "Inhalt");
        text = text.replace("inkl.", "inklusive");
        text = text.replace("i.R.", "im Ruhestand");
        text = text.replace("i.V.", "in Vertretung");
        text = text.replace("i.V.", "im Vorjahr");
        text = text.replace(" jew.", " jeweils");
        text = text.replace(" jem.", " jemand");
        text = text.replace(" jmd.", " jemand");
        text = text.replace("jhrl.", "jährlich");
        text = text.replace("jährl.", "jährlich");
        text = text.replace("Kap.", "Kapitel");
        text = text.replace("kath.", "katholisch");
        text = text.replace("Kfm.", "Kaufmann");
        text = text.replace("kfm.", "kaufmännisch");
        text = text.replace("Kfz.", "Kraftfahrzeug");
        text = text.replace("kgl.", "königlich");
        text = text.replace(" kl.", " kleine");
        text = text.replace(" gr.", " große");
        text = text.replace(" Kl.", " Klein");
        text = text.replace(" Gr.", " Groß");
//        text = text.replace("Kl.", "Klasse");
        text = text.replace("k.o.", "knockout");
//        text = text.replace("<!-- l.", "links");
//        text = text.replace("<!-- l", "Liter");
        text = text.replace(" led.", " ledig");
        text = text.replace("Mio.", "Millionen");
        text = text.replace("möbl.", "möbliert");
        text = text.replace("Mrd.", "Milliarde");
        text = text.replace("mtl.", "monatlich");
        text = text.replace("MwSt.", "Mehrwertsteuer");
        text = text.replace("MWSt.", "Mehrwertsteuer");
//        text = text.replace("<!-- N.", "Nord -->");
        text = text.replace("näml.", "nämlich");
        text = text.replace("n.Chr.", "nach Christus");
        text = text.replace("Nr.", "Nummer");
//        text = text.replace("<!-- O.", "Osten");
//        text = text.replace("<!-- o.", "oben");
        text = text.replace("o.ä.", "oder ähnliches");
        text = text.replace("Obb.", "Oberbayern");
        text = text.replace(" od.", " oder");
        text = text.replace(" orig.", " original");
        text = text.replace("o.g.", "oben genannt");
        text = text.replace("österr.", "österreichisch");
        text = text.replace("Pfd.", "Pfund");
        text = text.replace("Pkw.", "Personenkraftwagen");
        text = text.replace("Pl.", "Platz");
        text = text.replace("qkm.", "Quadratkilometer");
        text = text.replace("km.", "Kilometer");
//        text = text.replace("sec.", "sekunde");// sekular etc
        text = text.replace("sec.", "second");
        text = text.replace("qm.", "Quadratmeter");
        text = text.replace("Reg.", "Regierung");
        text = text.replace("Bez.", "Bezirk");
        text = text.replace("r.k.", "römisch-katholisch");
        text = text.replace("röm.", "römisch");
        text = text.replace("röm.-kath.", "römisch-katholisch");
        text = text.replace(" S.", " Seite");
//        text = text.replace(" s.", "sich");
        text = text.replace("s.a.", "siehe auch");
        text = text.replace("Sa.", "Samstag");
        text = text.replace("schles.", "schlesisch");
        text = text.replace("schwäb.", "schwäbisch");
        text = text.replace("schweiz.", "schweizerisch");
        text = text.replace("s.o.", "siehe oben");
        text = text.replace("So.", "Sonntag");
        text = text.replace(" sog.", " so genannt");
        text = text.replace("St.", "Sankt");
        text = text.replace("St.", "Stück");
        text = text.replace("Str.", "Straße");
        text = text.replace("s.u.", "siehe unten");
        text = text.replace("südd.", "süddeutsch");
        text = text.replace("tägl.", "täglich");
        text = text.replace("admin.", "administrativ");
        text = text.replace(" u.", " und");
        text = text.replace("u.a.", "und andere");
        text = text.replace("u.a.", "unter anderem");
        text = text.replace("u.ä.", "und ähnlich");
        text = text.replace("u.Ä.", "und Ähnliches");
        text = text.replace("u.a.m.", "unter andere(s) mehr");
        text = text.replace("usw.", "und so weiter");
        text = text.replace("u.s.w.", "und so weiter");
        text = text.replace("u.s.f.", "und so fort");
        text = text.replace("usf.", "und so fort");
        text = text.replace("u.v.a", "und viele andere");
        text = text.replace("u.v.m", "und vieles mehr");
        text = text.replace("u.U.", "unter Umständen");
        text = text.replace("v.Chr.", "vor Christus");
        text = text.replace("Verf.", "Verfasser");
        text = text.replace("verh.", "verheiratet");
        text = text.replace("verw.", "verwitwet");
        text = text.replace("vgl.", "vergleiche");
        text = text.replace("vorm.", "vormals");
        text = text.replace("z.B.", "zum Beispiel");
        text = text.replace(" z.", " zu");
        text = text.replace("Zi.", "Zimmer");
//        text = text.replace("Zg.", "Zug");
        text = text.replace(" zur.", " zurück");
        text = text.replace(" zus.", " zusammen");
        text = text.replace("z.T.", "zum Teil");
        text = text.replace("zzgl.", "zuzüglich");
        text = text.replace("z.Z.", "zur Zeit");
        return text;
    }

    public InfoBox getInfoBox() {
        //parseInfoBox is expensive. Doing it only once like other parse* methods
        if (infoBox == null)
            infoBox = parseInfoBox();
        return infoBox;
    }

    private InfoBox parseInfoBox() {
        String INFOBOX_CONST_STR = "{{Infobox";
        int startPos = wikiText.indexOf(INFOBOX_CONST_STR);
        if (startPos < 0) return null;
        int bracketCount = 2;
        int endPos = startPos + INFOBOX_CONST_STR.length();
        for (; endPos < wikiText.length(); endPos++) {
            switch (wikiText.charAt(endPos)) {
                case '}':
                    bracketCount--;
                    break;
                case '{':
                    bracketCount++;
                    break;
                default:
            }
            if (bracketCount == 0) break;
        }
        String infoBoxText = wikiText.substring(startPos, endPos + 1);
        try {
            infoBoxText = stripCite(infoBoxText, 0); // strip clumsy {{cite}} tags
        } catch (Exception e) {
        }
        // strip any html formatting
        infoBoxText = infoBoxText.replaceAll("&gt;", ">");
        infoBoxText = infoBoxText.replaceAll("&lt;", "<");
        infoBoxText = infoBoxText.replaceAll("<ref.*?>.*?</ref>", " ");
        infoBoxText = infoBoxText.replaceAll("</?.*?>", " ");
        return new InfoBox(infoBoxText);
    }

    private String stripCite(String text, int depth) throws Exception {
        int startPos = text.indexOf("{{Info");
        if (startPos < 0)
            startPos = text.indexOf("{{cite");
        if (startPos < 0)
            startPos = text.indexOf("{{Cite");
        if (startPos < 0)
            startPos = text.indexOf("{{Nav");
        if (startPos < 0)
            startPos = text.indexOf("{{");
        if (startPos < 0)
            startPos = text.indexOf("{|");
        if (startPos < 0) return text;
        int length = text.length();
        int bracketCount = 2;
        if (text.substring(startPos, startPos + 2).equals("{|"))
            bracketCount = 1;
        int endPos = startPos + 2;

        char last = '.';
        for (; endPos < length; endPos++) {
            char cha = text.charAt(endPos);
            switch (cha) {
                case '=':
                    if (last == '=') bracketCount = 0;// Something went wrong: stop here!!
                    break;
                case '}':
                    bracketCount--;
                    break;
                case '{':
                    bracketCount++;
                    break;
                default:
            }
            last = cha;
            if (bracketCount == 0) break;
        }
        if (startPos == 0) startPos = 1;
        try {
            String substring = text.substring(endPos + 1);
            String text0 = text.substring(0, startPos - 1) + substring;
            if (text0.length() < length && depth < 100)
                return stripCite(text0, depth + 1);
        } catch (Exception e) {
        }
        return text;
    }

    public boolean isDisambiguationPage() {
        return disambiguation;
    }

    public String getTranslatedTitle(String languageCode) {
        Pattern pattern = Pattern.compile("^\\[\\[" + languageCode + ":(.*?)\\]\\]$", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(wikiText);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    String[] bad_images = new String[]{"desc", "Converted", "Cc", "Wiki", "Commons-logo", "important", "go-forward", "PD-icon", "GNU", "lightblue", "right", "trademark", ".js", "Disambig", "-logo"};
    Pattern image_pattern = Pattern.compile("File:([a-zA-Z0-9_\\-\\s]*?)(.jpg|.bmp|.png|.gif)");//.gif nah

    public String getImage() {
        Matcher matcher = image_pattern.matcher(wikiText);
        a:
        while (matcher.find()) {
            String group = matcher.group(1);
            for (String badImage : bad_images) if (group.contains(badImage)) continue a;
            return group + "" + matcher.group(2);
        }
        return null;
    }

    public List<String> getImages() {
        Matcher matcher = image_pattern.matcher(wikiText);
        List<String> all = new ArrayList<String>();
        a:
        while (matcher.find()) {
            String group = matcher.group(1);
            for (String badImage : bad_images) if (group.contains(badImage)) continue a;
            all.add(group + "" + matcher.group(2));
        }
        return all;
    }
}
