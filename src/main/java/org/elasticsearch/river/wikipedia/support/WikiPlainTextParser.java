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
 * For internal use only -- Used by the {@link org.elasticsearch.river.wikipedia.support.WikiPage} class.
 * Can also be used as a stand alone class to parse wiki formatted text.
 *
 * @author Delip Rao
 */
public class WikiPlainTextParser extends WikiTextParser{

    private boolean hasLinks=false;

    public WikiPlainTextParser(String wtext) {
        super();
        wikiText = wtext;
    }

    public String getText() {
        return wikiText;
    }

    public ArrayList<String> getCategories() {
        return pageCats;
    }

    public ArrayList<String> getLinks() {
        if(pageLinks==null && hasLinks)parseLinks();
        return pageLinks;
    }

    public String getPlainText() {
        String text = wikiText;
        text = replaceEnglishAbbrevations(text);
        text = replaceGermanAbbrevations(text);
        return text;
    }

}
