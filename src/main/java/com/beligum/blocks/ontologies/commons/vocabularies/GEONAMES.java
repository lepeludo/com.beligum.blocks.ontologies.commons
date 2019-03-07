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

import com.beligum.blocks.exceptions.RdfInitializationException;
import com.beligum.blocks.rdf.*;
import com.beligum.blocks.rdf.ifaces.RdfClass;
import com.beligum.blocks.rdf.ifaces.RdfNamespace;
import com.beligum.blocks.rdf.ifaces.RdfProperty;
import com.beligum.blocks.rdf.ontologies.XSD;
import gen.com.beligum.blocks.ontologies.commons.messages.blocks.ontologies.commons;

/**
 * Created by bram on 2/28/16.
 */
public final class GEONAMES extends RdfOntologyImpl
{
    //-----CONSTANTS-----
    public static final RdfNamespace NAMESPACE = new RdfNamespaceImpl("http://www.geonames.org/ontology#", "geonames");

    //-----MEMBERS-----
    public static final RdfClass Feature = RdfFactory.newProxyClass("Feature");
    public static final RdfProperty name = RdfFactory.newProxyProperty("name");
    public static final RdfProperty officialName = RdfFactory.newProxyProperty("officialName");
    public static final RdfProperty alternateName = RdfFactory.newProxyProperty("alternateName");
    // TODO

    //-----CONSTRUCTORS-----
    @Override
    protected void create(RdfFactory rdfFactory) throws RdfInitializationException
    {
        rdfFactory.register(Feature)
                  .title(commons.Entries.GEONAMES_title_Feature)
                  .label(commons.Entries.GEONAMES_label_Feature);

        rdfFactory.register(name)
                  .title(commons.Entries.GEONAMES_title_name)
                  .label(commons.Entries.GEONAMES_label_name)
                  .dataType(XSD.string);

        rdfFactory.register(officialName)
                  .title(commons.Entries.GEONAMES_title_officialName)
                  .label(commons.Entries.GEONAMES_label_officialName)
                  .dataType(XSD.string);

        rdfFactory.register(alternateName)
                  .title(commons.Entries.GEONAMES_title_alternateName)
                  .label(commons.Entries.GEONAMES_label_alternateName)
                  .dataType(XSD.string);
    }

    //-----PUBLIC METHODS-----
    @Override
    public RdfNamespace getNamespace()
    {
        return NAMESPACE;
    }
    @Override
    public boolean isPublic()
    {
        return false;
    }
}
