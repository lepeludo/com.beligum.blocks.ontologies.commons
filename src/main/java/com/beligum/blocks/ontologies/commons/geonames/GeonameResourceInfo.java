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

package com.beligum.blocks.ontologies.commons.geonames;

import com.beligum.blocks.index.ifaces.ResourceProxy;
import com.beligum.blocks.rdf.RdfFactory;
import com.beligum.blocks.rdf.ifaces.RdfClass;
import com.beligum.blocks.utils.RdfTools;
import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;
import java.util.List;
import java.util.Locale;

/**
 * Created by bram on 3/12/16.
 */
public class GeonameResourceInfo extends AbstractGeoname
{
    //-----CONSTANTS-----
    //special value for 'lang' that maps to external documentation
    private static final String LINK_LANGUAGE = "link";

    //-----VARIABLES-----
    private String toponymName;
    private List<GeonameLangValue> alternateName;

    //temp values...
    private transient boolean triedLink;
    private transient URI cachedLink;

    //-----CONSTRUCTORS-----
    public GeonameResourceInfo()
    {
    }

    //-----PUBLIC METHODS-----
    @Override
    public String getDescription()
    {
        return null;
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
    @Override
    public URI getImage()
    {
        return null;
    }

    //-----PROTECTED METHODS-----
    //see http://stackoverflow.com/questions/11872914/write-only-properties-with-jackson
    @JacksonInject(RESOURCE_TYPE_INJECTABLE)
    private void setResourceType(URI resourceType)
    {
        this.resourceType = resourceType;
    }
    @JsonProperty
    private void setName(String name)
    {
        this.name = name;
    }
    @JsonProperty
    private void setToponymName(String toponymName)
    {
        this.toponymName = toponymName;
    }
    @JsonProperty
    private void setGeonameId(String geonameId)
    {
        this.geonameId = geonameId;
    }
    @JsonProperty
    private void setAlternateName(List<GeonameLangValue> alternateName)
    {
        this.alternateName = alternateName;
    }
    @JsonIgnore
    private String getToponymName()
    {
        return toponymName;
    }
    @JsonIgnore
    private String getGeonameId()
    {
        return geonameId;
    }
    @JsonIgnore
    private List<GeonameLangValue> getAlternateName()
    {
        return alternateName;
    }
    @JsonIgnore
    private boolean isTriedLink()
    {
        return triedLink;
    }
    @JsonIgnore
    private URI getCachedLink()
    {
        return cachedLink;
    }

    //-----PRIVATE METHODS-----
    private URI findExternalLink()
    {
        if (!this.triedLink) {
            if (this.alternateName != null) {
                for (GeonameLangValue val : this.alternateName) {
                    if (val != null && val.getLang() != null && val.getLang().equals(LINK_LANGUAGE)) {
                        this.cachedLink = URI.create(val.getValue());
                        //we stop at first sight of a link
                        break;
                    }
                }
            }

            this.triedLink = true;
        }

        return this.cachedLink;
    }
}
