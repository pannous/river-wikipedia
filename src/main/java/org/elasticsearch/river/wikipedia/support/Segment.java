package org.elasticsearch.river.wikipedia.support;

/**
 * Copyright 2013 Pannous GmbH
 * <p/>
 * User: info@pannous.com / me
 * Date: 10/9/14
 * Time: 2:39 PM
 */
public class Segment {
    public final WikiPage page;
    public final String topic;
    public final String subTopic;
    public final String text;
    public final String subSubTopic;
    public final String image;

    public Segment(WikiPage currentPage, String currentSegment, String currentSubSegment, String currentSubSubSegment, String currentWikitext, String currentImage) {
        this.page = currentPage;
        this.topic= currentSegment==null?"":currentSegment;
        this.subTopic = currentSubSegment==null?"":currentSubSegment;
        this.subSubTopic = currentSubSubSegment==null?"":currentSubSubSegment;
        this.text = currentWikitext==null?"":currentWikitext;
        this.image = currentImage==null?"":currentImage;
    }
}
