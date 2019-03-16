package com.beligum.blocks.ontologies.commons;

import com.beligum.blocks.config.Settings;
import com.beligum.blocks.exceptions.RdfInitializationException;
import com.beligum.blocks.rdf.RdfFactory;
import com.beligum.blocks.rdf.RdfOntologyImpl;
import com.beligum.blocks.rdf.ifaces.RdfNamespace;

import static com.beligum.blocks.rdf.ontologies.Local.Page;

public class Local extends RdfOntologyImpl
{
    //-----CONSTANTS-----
    public static final RdfNamespace NAMESPACE = Settings.instance().getRdfMainOntologyNamespace();

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
    @Override
    public RdfNamespace getNamespace()
    {
        return NAMESPACE;
    }
    @Override
    protected boolean isPublicOntology()
    {
        return true;
    }
}