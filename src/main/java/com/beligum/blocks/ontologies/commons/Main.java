package com.beligum.blocks.ontologies.commons;

import com.beligum.blocks.exceptions.RdfInitializationException;
import com.beligum.blocks.rdf.RdfFactory;

public class Main extends com.beligum.blocks.rdf.ontologies.Main
{
    //-----CONSTANTS-----

    //-----MEMBERS-----

    //-----CONSTRUCTORS-----
    @Override
    protected void create(RdfFactory rdfFactory) throws RdfInitializationException
    {
        //in this module, the sameAs ontologies of the main pages are present, so attach them to main:Page
        rdfFactory.register(Page)
                  .isSameAs(DBR.Web_page)
                  .isSameAs(SCHEMA.WebPage);
    }


    //-----PUBLIC METHODS-----
}