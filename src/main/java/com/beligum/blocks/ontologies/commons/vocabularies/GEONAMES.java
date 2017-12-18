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

package com.beligum.blocks.ontologies.commons.vocabularies;

import com.beligum.blocks.rdf.ifaces.RdfClass;
import com.beligum.blocks.rdf.ifaces.RdfProperty;
import com.beligum.blocks.rdf.ifaces.RdfVocabulary;
import com.beligum.blocks.rdf.ontology.RdfClassImpl;
import com.beligum.blocks.rdf.ontology.RdfPropertyImpl;
import com.beligum.blocks.rdf.ontology.vocabularies.AbstractRdfVocabulary;
import com.beligum.blocks.rdf.ontology.vocabularies.XSD;

import java.net.URI;

/**
 * Created by bram on 2/28/16.
 */
public final class GEONAMES extends AbstractRdfVocabulary
{
    //-----VARIABLES-----

    //-----SINGLETON-----
    public static final RdfVocabulary INSTANCE = new GEONAMES();
    private GEONAMES()
    {
        super(URI.create("http://www.geonames.org/ontology#"), "geonames");
    }

    //-----PUBLIC FUNCTIONS-----

    //-----ENTRIES-----
    /**
     * The class of OWL individuals
     * Note: this is actually an OwlClass (that subclasses rdfs:Class)
     */
    public static final RdfClass Feature = new RdfClassImpl("Feature", INSTANCE, gen.com.beligum.blocks.ontologies.commons.messages.blocks.ontologies.commons.Entries.GEONAMES_title_Feature, gen.com.beligum.blocks.ontologies.commons.messages.blocks.ontologies.commons.Entries.GEONAMES_label_Feature, null);

    public static final RdfProperty name = new RdfPropertyImpl("name", INSTANCE, gen.com.beligum.blocks.ontologies.commons.messages.blocks.ontologies.commons.Entries.GEONAMES_title_name, gen.com.beligum.blocks.ontologies.commons.messages.blocks.ontologies.commons.Entries.GEONAMES_label_name, XSD.STRING);

    public static final RdfProperty officialName = new RdfPropertyImpl("officialName", INSTANCE, gen.com.beligum.blocks.ontologies.commons.messages.blocks.ontologies.commons.Entries.GEONAMES_title_officialName, gen.com.beligum.blocks.ontologies.commons.messages.blocks.ontologies.commons.Entries.GEONAMES_label_officialName, XSD.STRING);

    public static final RdfProperty alternateName = new RdfPropertyImpl("alternateName", INSTANCE, gen.com.beligum.blocks.ontologies.commons.messages.blocks.ontologies.commons.Entries.GEONAMES_title_alternateName, gen.com.beligum.blocks.ontologies.commons.messages.blocks.ontologies.commons.Entries.GEONAMES_label_alternateName, XSD.STRING);

}
