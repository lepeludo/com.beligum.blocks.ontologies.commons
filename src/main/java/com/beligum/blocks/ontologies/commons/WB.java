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

package com.beligum.blocks.ontologies.commons;

import com.beligum.blocks.exceptions.RdfInitializationException;
import com.beligum.blocks.rdf.RdfFactory;
import com.beligum.blocks.rdf.RdfNamespaceImpl;
import com.beligum.blocks.rdf.RdfOntologyImpl;
import com.beligum.blocks.rdf.ifaces.RdfClass;
import com.beligum.blocks.rdf.ifaces.RdfNamespace;

import static gen.com.beligum.blocks.ontologies.commons.messages.blocks.ontologies.commons.Entries.WB_label_Item;

/**
 * Created by bram on 2/28/16.
 */
public final class WB extends RdfOntologyImpl
{
    //the wikibase namespace, used by wikidata.
    public static final RdfNamespace NAMESPACE = new RdfNamespaceImpl("http://wikiba.se/ontology#", "WB");
    //the wikibase main class http://wikiba.se/ontology#Item
    public static final RdfClass Item = RdfFactory.newProxyClass("Item");

    //-----CONSTRUCTORS-----
    @Override
    protected void create(RdfFactory rdfFactory) throws RdfInitializationException
    {
        rdfFactory.register(Item)
                  .label(WB_label_Item);
    }

    //-----PUBLIC METHODS-----
    @Override
    public RdfNamespace getNamespace()
    {
        return NAMESPACE;
    }
    @Override
    protected boolean isPublicOntology()
    {
        return false;
    }

    //-----VARIABLES-----


}