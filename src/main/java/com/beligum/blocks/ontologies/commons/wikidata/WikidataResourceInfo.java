/*
 * Copyright 2017 Republic of Reinvention bvba. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.beligum.blocks.ontologies.commons.wikidata;

import com.beligum.base.utils.toolkit.StringFunctions;
import com.beligum.blocks.rdf.RdfFactory;
import com.beligum.blocks.utils.RdfTools;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

public class WikidataResourceInfo extends AbstractWikidata
{

    private static final String LINK_LANGUAGE = "link";
    //temp values...
    private transient boolean triedLink;
    private transient URI cachedLink;
    private URI link;

    //-----CONSTRUCTORS-----
    public WikidataResourceInfo()
    {
    }

    //-----PUBLIC METHODS-----
    @Override
    public String getLabel()
    {

        return label;
    }

    @Override
    public URI getImage()
    {
        //return a Wikimedia Commons image link for the wikidata item (if one exists). Return null if you don't want an image.
        //        return this.image;
        return null;
    }
    public void setImage(String image) throws URISyntaxException
    {
        this.image = new URI(image);
    }
    @Override
    public Locale getLanguage()
    {
        return this.language;
    }
    @Override
    public boolean isExternal()
    {
        return true;
    }
    @Override
    public URI getParentUri()
    {
        return null;
    }

    public void setLanguage(Locale language)
    {
        this.language = language;
    }

    //-----PROTECTED METHODS-----
    public void setResourceType(URI resourceType)
    {
        this.resourceType = resourceType;
    }

    private boolean isTriedLink()
    {
        return triedLink;
    }
    private URI getCachedLink()
    {
        return cachedLink;
    }
    public static String getLinkLanguage()
    {
        return LINK_LANGUAGE;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }
    public void setTriedLink(boolean triedLink)
    {
        this.triedLink = triedLink;
    }
    public void setCachedLink(URI cachedLink)
    {
        this.cachedLink = cachedLink;
    }
    public URI getLink()
    {
        return link;
    }
    public void setLink(URI link)
    {
        this.link = link;
    }
}
