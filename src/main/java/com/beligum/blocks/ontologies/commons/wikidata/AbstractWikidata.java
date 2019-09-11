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

import com.beligum.base.utils.Logger;
import com.beligum.blocks.index.ifaces.ResourceProxy;
import com.beligum.blocks.rdf.RdfFactory;
import com.beligum.blocks.rdf.ifaces.RdfClass;
import com.beligum.blocks.utils.RdfTools;
import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;
import java.util.Locale;

/**
 * Created by Bram on 6/01/17.
 */
public abstract class AbstractWikidata implements ResourceProxy
{
    //-----CONSTANTS-----
    protected static final String RESOURCE_TYPE_INJECTABLE = "resourceType";
    //the custom wikidata prefix
    protected static final String WIKIDATAPREFIX = "http://www.wikidata.org/entity/";
    //-----CONSTRUCTORS-----
    public enum Type
    {
        //we consider "thing" the most general
        THING(WikidataSuggestion.class);

        public Class<? extends ResourceProxy> suggestionClass;
        Type(Class<? extends ResourceProxy> suggestionClass)
        {
            this.suggestionClass = suggestionClass;
        }
    }

    //-----VARIABLES-----
    protected URI resourceType;
    protected URI uri;
    protected Locale language;
    protected String label;
    protected String description;
    protected String wikidataId;
    protected URI image;
    private transient RdfClass cachedRdfClass;

    //-----PUBLIC METHODS-----

    @Override
    public URI getUri()
    {
        return RdfTools.createRelativeResourceId(this.getTypeOf(), this.wikidataId);
    }
    @Override
    public RdfClass getTypeOf()
    {
        return this.getCachedRdfClass();
    }
    @Override
    public String getResource()
    {
        return this.getUri().toString();
    }
    @Override
    public String getDescription()
    {
        return description;
    }
    @Override
    public String getLabel()
    {
        return label;
    }
    @JsonProperty
    @Override
    public Locale getLanguage()
    {
        return language;
    }

    public void setDescription(String description)
    {
        Logger.warn("description :"+description);

        this.description = description;
    }

    public void setLabel(String label)
    {
        Logger.warn("label :"+label);

        this.label = label;
    }

    public void setLanguage(Locale language)
    {
        this.language = language;
    }


    @JacksonInject(RESOURCE_TYPE_INJECTABLE)
    public void setResourceType(URI resourceType)
    {
        this.resourceType = resourceType;
    }
    @JsonProperty
    public void setUri(URI uri)
    {
        //normalize the uri to correspond with the framework
        this.uri = uri;
    }

    public URI getResourceType() {
        return resourceType;
    }

    public void setWikidataId(String wikidataId)
    {
        this.wikidataId = wikidataId;
    }
    public String getWikidataId()
    {
        return wikidataId;
    }
    public static URI toWikidataUri(String wikidataId)
    {
        return URI.create(WIKIDATAPREFIX + wikidataId);
    }


    //-----PROTECTED METHODS-----

    //-----PRIVATE METHODS-----
    @JsonIgnore
    private RdfClass getCachedRdfClass()
    {
        if (this.cachedRdfClass == null && this.resourceType != null) {
            this.cachedRdfClass = RdfFactory.getClass(this.resourceType);
        }
        return this.cachedRdfClass;
    }
}
