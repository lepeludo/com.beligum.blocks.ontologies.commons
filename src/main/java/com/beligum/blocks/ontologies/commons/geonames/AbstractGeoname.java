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
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.net.URI;
import java.util.Locale;

/**
 * Created by bram on 3/12/16.
 */
public abstract class AbstractGeoname implements ResourceProxy
{
    //-----CONSTANTS-----
    public static final String RESOURCE_TYPE_INJECTABLE = "resourceType";

    protected static final String GEONAMES_URI_PREFIX = "http://sws.geonames.org/";
    protected static final String GEONAMES_URI_SUFFIX = "/";

    public enum Type
    {
        //Note: see the Geonames ontology mapping for these values:
        // http://www.geonames.org/ontology/mappings_v3.01.rdf
        // Especially, we want this value to match the OWL restrictions that map to http://schema.org/Country
        COUNTRY(new String[] { "A" }, new String[] {
                        //independent political entity
                        "PCLI",
                        //historical political entity
                        "PCLH"
        }, GeonameCountrySuggestion.class),

        //Note: see the Geonames ontology mapping for these values:
        // http://www.geonames.org/export/codes.html
        // http://www.geonames.org/ontology/mappings_v3.01.rdf
        // Especially, we want this value to match the OWL restrictions that map to http://schema.org/City
        CITY(new String[] { "P" }, new String[] {
                        //populated place
                        "PPL",
                        //seat of a first-order administrative division
                        "PPLA",
                        //seat of a second-order administrative division
                        "PPLA2",
                        //seat of a third-order administrative division
                        "PPLA3",
                        //seat of a fourth-order administrative division
                        "PPLA4",
                        //capital of a political entity
                        "PPLC",
                        //farm village; a populated place where the population is largely engaged in agricultural activities
                        "PPLF",
                        //seat of government of a political entity
                        "PPLG",
                        //populated locality; an area similar to a locality but with a small group of dwellings or other buildings
                        "PPLL",
                        //populated places; cities, towns, villages, or other agglomerations of buildings where people live and work
                        "PPLS",
                        //section of populated place
                        "PPLX"
        }, GeonameCitySuggestion.class);

        public String[] featureClasses;
        public String[] featureCodes;
        public Class<? extends ResourceProxy> suggestionClass;
        Type(String[] featureClasses, String[] featureCodes, Class<? extends ResourceProxy> suggestionClass)
        {
            this.featureClasses = featureClasses;
            this.featureCodes = featureCodes;
            this.suggestionClass = suggestionClass;
        }
    }

    //-----VARIABLES-----
    protected URI resourceType;
    protected String geonameId;
    protected String name;
    protected Locale language;

    private transient RdfClass cachedRdfClass;

    //-----CONSTRUCTORS-----

    //-----STATIC METHODS-----
    public static URI toGeonamesUri(String geonameId)
    {
        return URI.create(GEONAMES_URI_PREFIX + geonameId + GEONAMES_URI_SUFFIX);
    }
    public static String fromGeonamesUri(URI geonameUri)
    {
        String geonameUriStr = geonameUri.toString();
        return geonameUriStr.substring(GEONAMES_URI_PREFIX.length(), geonameUriStr.length() - GEONAMES_URI_SUFFIX.length());
    }

    //-----PUBLIC METHODS-----
    @Override
    public URI getUri()
    {
        //note that the endpoint behind this will take care of the redirection to a good external landing page
        return RdfTools.createRelativeResourceId(this.getTypeOf(), this.geonameId);
    }
    @Override
    public String getResource()
    {
        return this.getUri().toString();
    }
    @Override
    public RdfClass getTypeOf()
    {
        return this.getCachedRdfClass();
    }
    @Override
    public String getLabel()
    {
        return name;
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
    public URI getImage() { return null; }
    @Override
    public Locale getLanguage()
    {
        return language;
    }
    /**
     * We need to make this one public because the geonames webservice doesn't return the value; it's set manually after fetching it from the service
     */
    public void setLanguage(Locale language)
    {
        this.language = language;
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
