package com.beligum.blocks.ontologies.commons.wikidata;

public class GenericModelSubject
{
    private String subject;
    private String objectLabel;
    private String objectLanguage;
    private String predicate;

    public GenericModelSubject(String subject, String predicate, String objectLabel, String objectLanguage)
    {
        this.subject = subject;
        this.objectLabel = objectLabel;
        this.objectLanguage = objectLanguage;
        this.predicate = predicate;
    }

    public String getSubject()
    {
        return subject;
    }
    public String getObjectLabel()
    {
        return objectLabel;
    }
    public String getObjectLanguage()
    {
        return objectLanguage;
    }
    public String getPredicate()
    {
        return predicate;
    }
}
