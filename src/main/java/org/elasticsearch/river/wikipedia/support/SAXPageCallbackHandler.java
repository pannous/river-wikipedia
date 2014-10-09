/*
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

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A Wrapper class for the PageCallbackHandler
 *
 * @author Jason Smith
 */
public class SAXPageCallbackHandler extends DefaultHandler {

    private PageCallbackHandler pageHandler;
    private WikiPage currentPage;
    private String currentTag;

    private String currentWikitext;
    private String currentTitle;
    private String currentID;
    private String currentSegment;
    private String currentSubSegment;
    private String currentSubSubSegment;
    private String image;

    public SAXPageCallbackHandler(PageCallbackHandler ph) {
        pageHandler = ph;
    }

    public void startElement(String uri, String name, String qName, Attributes attr) {
        currentTag = qName;
        if (qName.equals("page")) {
            currentPage = new WikiPage();
            currentWikitext = "";
            currentTitle = attr.getValue("title");
            currentID = attr.getValue("pageid");
        }
        if (qName.equals("doc")) {
            currentPage = new WikiPage();
            currentWikitext = "";
            currentTitle = attr.getValue("title");
            currentSegment = "";
            currentSubSegment = "";
            currentSubSubSegment="";
            currentID = attr.getValue("id");
            image = "";
//            currentURL = attr.getValue("url");
        }
        // START of segment = SAVE OLD!!
        if (qName.equals("h1") || qName.equals("h2") || qName.equals("h3")) {
//            if(currentSegment==null) currentSegment = "description";
            if (currentWikitext != null && currentWikitext.length() > 5)
                currentPage.addSegment(new Segment(currentPage, currentSegment, currentSubSegment,currentSubSubSegment, currentWikitext, image));
            currentWikitext = "";// getText() ONLY THROUGH subSegments !!!!
            if(qName.equals("h1") || qName.equals("h2")) currentSegment = "";
            if(qName.equals("h1") || qName.equals("h2")||qName.equals("h3"))currentSubSegment= "";
            currentSubSubSegment= "";
        }
        if (currentTitle == null) currentTitle = "";
        if (currentID == null) currentID = "";
    }

    public void endElement(String uri, String name, String qName) {
        if (qName.equals("page")) {
            currentPage.setTitle(currentTitle);
            currentPage.setID(currentID);
            currentPage.setWikiText(currentWikitext, false);
            pageHandler.process(currentPage);
        }
        if (qName.equals("doc")) {
            currentPage.setTitle(currentTitle);
            currentPage.setID(currentID);
            currentPage.setWikiText(currentWikitext, true);// PLAIN TEXT!
//            currentPage.setRedirect(); ... !!!
            pageHandler.process(currentPage);
        }
        if (qName.equals("mediawiki")) {
            // TODO hasMoreElements() should now return false
        }
    }

    public void characters(char ch[], int start, int length) {
        if(currentTag.equals("image")) {
            image = new String(ch, start, length);
            currentPage.getImages().add(image);
            currentTag = "doc";
        }
        if (currentTag.equals("title")) {
            currentTitle = currentTitle.concat(new String(ch, start, length));
        }
        // TODO: To avoid looking at the revision ID, only the first ID is taken.
        // I'm not sure how big the block size is in each call to characters(),
        // so this may be unsafe.
        else if ((currentTag.equals("id")) && (currentID.length() == 0)) {
            currentID = new String(ch, start, length);
        } else if (currentTag.equals("doc")) {
            currentWikitext = currentWikitext.concat(new String(ch, start, length));
        } else if (currentTag.equals("h1")) {
            currentSegment = new String(ch, start, length).trim();
        } else if (currentTag.equals("h2")) {
//            if (currentSegment == null || currentSegment.length() == 0)
            currentSegment = new String(ch, start, length).trim();
//            else
//                currentSubSegment = new String(ch, start, length);
            currentTag = "doc"; // hack to continue wikiText
        } else if (currentTag.equals("h3")) {
//            currentSegment = currentSubSegment;
//            currentSubSegment += new String(ch, start, length);
//            currentSegment = new String(ch, start, length).trim();
            currentSubSegment= new String(ch, start, length).trim();
            currentTag = "doc";
        } else if (currentTag.equals("h4")||currentTag.equals("h5")) {
//            currentSegment = currentSubSegment;
//            currentSubSegment += new String(ch, start, length).trim();
            currentSubSubSegment = new String(ch, start, length).trim();
            currentTag = "doc";
        } else if (currentTag.equals("text")) {
            currentWikitext = currentWikitext.concat(new String(ch, start, length));
        } else if (currentTag.equals("rev")) {
            currentWikitext = currentWikitext.concat(new String(ch, start, length));
        }
    }
}
