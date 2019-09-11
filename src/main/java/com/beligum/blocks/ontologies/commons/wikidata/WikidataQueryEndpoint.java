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

import com.beligum.base.cache.Cache;
import com.beligum.base.cache.EhCacheAdaptor;
import com.beligum.base.server.R;
import com.beligum.base.utils.Logger;
import com.beligum.base.utils.json.Json;
import com.beligum.blocks.index.ifaces.ResourceProxy;
import com.beligum.blocks.ontologies.commons.WB;
import com.beligum.blocks.ontologies.commons.persistance.CachePersistor;
import com.beligum.blocks.rdf.RdfFactory;
import com.beligum.blocks.rdf.ifaces.RdfClass;
import com.beligum.blocks.rdf.ifaces.RdfEndpoint;
import com.beligum.blocks.rdf.ifaces.RdfOntologyMember;
import com.beligum.blocks.rdf.ifaces.RdfProperty;
import com.beligum.blocks.rdf.ontologies.RDFS;
import com.beligum.blocks.utils.RdfTools;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.glassfish.jersey.client.ClientConfig;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.*;

import static com.beligum.blocks.ontologies.commons.config.CacheKeys.WIKIDATA_CACHED_RESULTS;

//import com.beligum.blocks.endpoints.ifaces.AutocompleteSuggestion;
//import com.beligum.blocks.endpoints.ifaces.ResourceInfo;

/**
 * Created by Bram on 6/01/17.
 * <p>
 * Handles queries to Wikidata / Wikipedia.
 */
public class WikidataQueryEndpoint implements RdfEndpoint
{
    private final String WIKIPEDIPrefix = "https://";
    private final String WIKIPEDIA_API_URI = ".wikipedia.org/w/api.php";
    private final String WIKIDATA_PUBLIC_URI = "http://www.wikidata.org/entity/";
    private final String action = "action";
    private final String search = "search";
    private AbstractWikidata.Type wikiType;
    private String[] wikidataInstancesOff;
    private RdfProperty[] cachedLabelProps;
    //this will use sql  to "cache" the wikidata label and basic model.
    private final boolean USESSQL = true;
    //this disables fetching wikidata online (if the wikidata item is not found in the sql database it will not try to get it
    //from the sparql endpoint.
    private final boolean DISABLEFETCHONLINE = R.configuration().getBoolean("disable-wiki-online");


    /**
     *
     * @param wikiType
     * @param wikidataInstancesOff : class of which the results should be an instance of (e.g. human = Q5). Use overloaded constructor if it should be null.
     *                             Using wikidataInstancesOff will cause an extra sparql query so will be slower.
     */
    public WikidataQueryEndpoint(AbstractWikidata.Type wikiType, String... wikidataInstancesOff)

    {
        this.wikiType = wikiType;
        this.wikidataInstancesOff = wikidataInstancesOff;
    }

    /**
     * @param wikiType
     */
    public WikidataQueryEndpoint(AbstractWikidata.Type wikiType)
    {
        this.wikiType = wikiType;
    }

    @Override
    public boolean isExternal()
    {
        return true;
    }

    @Override
    public Collection<ResourceProxy>  search(RdfOntologyMember resourceType, final String query, QueryType queryType, Locale language, int maxResults) throws IOException
    {
        try {
            Collection<ResourceProxy> retVal = this.doSearchQueryForLanguage(query, resourceType, language);
            return retVal;
        }
        catch (Exception ex) {
            throw new IOException(ex);
        }
    }
    @Override
    public ResourceProxy getResource(RdfOntologyMember resourceType, URI resourceId, Locale language) throws IOException
    {
        String wikibase_item = null;
        if (resourceId.getAuthority() != null && resourceId.getAuthority().contains("wikipedia")) {
            //from wikipedia, so it's a new one
            String title = resourceId.toString().substring(resourceId.toString().lastIndexOf('/') + 1);
            wikibase_item = this.getWikibase_item(title, language);
        }
        else {
            //should be an existing one
            wikibase_item = resourceId.toString().substring(resourceId.toString().lastIndexOf('/') + 1);
        }

        WikidataResourceInfo retVal = null;
        if (this.wikiType.equals(AbstractWikidata.Type.THING)) {
            WikidataResourceInfo wikidataresourceinfo = null;
            if(USESSQL){
                wikidataresourceinfo = CachePersistor.getInstance().getwikiresourceinfo(wikibase_item);

                if(wikidataresourceinfo == null || wikidataresourceinfo.getLabel() == null || wikidataresourceinfo.getLabel().equals(wikibase_item)){
                    //the label is the wikibase_item, this is not OK.
                    wikidataresourceinfo = null;
                }
            }
            if (!DISABLEFETCHONLINE && wikidataresourceinfo == null) {
                //dutch, english and french implemented now
                String query = "PREFIX schema: <http://schema.org/> SELECT ?dataLabel ?dataDescription ?sitelink ?pic ?lang WHERE {BIND(wd:" +
                               wikibase_item +
                               " AS ?data) { ?site" +
                        "" +
                        "" +
                        "link schema:about ?data. ?sitelink schema:isPartOf <https://en.wikipedia.org/>.?sitelink schema:inLanguage ?lang. SERVICE wikibase:label { bd:serviceParam wikibase:language \"en\". }}UNION{ ?sitelink schema:about ?data. ?sitelink schema:isPartOf <https://fr.wikipedia.org/>.?sitelink schema:inLanguage ?lang. SERVICE wikibase:label { bd:serviceParam wikibase:language \"?lang, fr\". } }UNION{ ?sitelink schema:about ?data. ?sitelink schema:isPartOf <https://nl.wikipedia.org/>.?sitelink schema:inLanguage ?lang. SERVICE wikibase:label { bd:serviceParam wikibase:language \"?lang, nl\". } } OPTIONAL{?data wdt:P18 ?pic} OPTIONAL{?data schema:description ?dataDescription .}}";
                RepositoryConnection sparqlConnection = null;
                try {
                    SPARQLRepository sparqlRepository = new SPARQLRepository("https://query.wikidata.org/sparql");
                    sparqlRepository.initialize();
                    sparqlConnection = sparqlRepository.getConnection();

                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                TupleQuery tupleQuery = null;
                try {
                    tupleQuery = sparqlConnection.prepareTupleQuery(QueryLanguage.SPARQL, query);
                }
                catch (MalformedQueryException e) {
                    e.printStackTrace();
                }
                catch (RepositoryException e) {
                    e.printStackTrace();
                }
                Map<String, TupleResult> tupleResults = new HashMap<>();
                try {
                    TupleQueryResult tupleQueryResult = tupleQuery.evaluate();
                    while (tupleQueryResult.hasNext()) {
                        BindingSet bindingSet = tupleQueryResult.next();
                        String lang = bindingSet.getValue("lang").stringValue();
                        String labelForLanguage = bindingSet.getValue("dataLabel") == null ? null : bindingSet.getValue("dataLabel").stringValue();
                        String descriptionForLanguage = bindingSet.getValue("dataDescription") == null ? null : bindingSet.getValue("dataDescription").stringValue();
                        String label = labelForLanguage;
                        String pic = bindingSet.getValue("pic") == null ? null : bindingSet.getValue("pic").stringValue();
                        String sitelink = bindingSet.getValue("sitelink") == null ? null : bindingSet.getValue("sitelink").stringValue();
                        tupleResults.put(lang, new TupleResult(
                                        lang,
                                        label,
                                        descriptionForLanguage,
                                        pic,
                                        sitelink
                        ));
                    }
                }
                catch (QueryEvaluationException e) {
                    e.printStackTrace();
                }

                //see if we got it in the proper language
                TupleResult choosenTupleResult = null;

                if (tupleResults.size() > 0) {
                    if (tupleResults.keySet().contains(language.toLanguageTag())) {
                        choosenTupleResult = tupleResults.get(language.toLanguageTag());
                    }
                    else if (tupleResults.keySet().contains(Locale.ENGLISH.toLanguageTag())) {
                        //fall back to english
                        choosenTupleResult = tupleResults.get(Locale.ENGLISH.toLanguageTag());
                    }
                    else {
                        //pick the first
                        String key = null;
                        Iterator<String> it = tupleResults.keySet().iterator();
                        if (it.hasNext()) {
                            choosenTupleResult = tupleResults.get(it.next());
                        }
                    }
                }

                retVal = new WikidataResourceInfo();
                try {
                    //try linking to wikipedia. If not exists, link to wikidata
                    if (choosenTupleResult != null && choosenTupleResult.getSitelink() != null) {
                        retVal.setLink(new URI(choosenTupleResult.getSitelink()));
                    }
                }
                catch (URISyntaxException e) {
                    e.printStackTrace();
                }

                retVal.setWikidataId(wikibase_item);
                if (choosenTupleResult != null && choosenTupleResult.getLabel() != null) {
                    retVal.setLabel(choosenTupleResult.getLabel());
                }
                else {
                    String backupLabel =null;
                    if(!DISABLEFETCHONLINE){
                         backupLabel = this.additionalTupleRequestForJustLabel(wikibase_item);
                    }
                    if(backupLabel != null){
                        retVal.setLabel(backupLabel);
                    }else{
                        //no  label
//                        retVal.setLabel(wikibase_item);
                    }
                }
                if (choosenTupleResult != null && choosenTupleResult.getImage() != null) {
                    try {
                        retVal.setImage(choosenTupleResult.getImage());
                    }
                    catch (URISyntaxException e) {
                        Logger.error("URISyntaxException for" + choosenTupleResult.getImage());
                    }
                }
                retVal.setLanguage(language);
                retVal.setResourceType(resourceType.getCurie());
                if(USESSQL){
                    if(retVal.getLabel() == null){
                        Logger.info(retVal);
                    }
                    CachePersistor.getInstance().putwikiresourceinfo(retVal);
                }
            }
            else if(wikidataresourceinfo != null){
                //from sql
                wikidataresourceinfo.setResourceType(resourceType.getCurie());
                retVal = wikidataresourceinfo;
            }
        }
            return retVal;

    }
    /**
     * Always have an english result!
     *
     * @param rdfClass
     * @param resourceId the id of the resource
     * @param language the optional language we want to load the model for
     * @return
     * @throws IOException
     */
    @Override
    public Model getExternalRdfModel(RdfClass rdfClass, URI resourceId, Locale language) throws IOException
    {
        Model retVal = null;
        boolean fromDatabase = false;
        ValueFactory factory = SimpleValueFactory.getInstance();

        if (resourceId != null && !resourceId.toString().isEmpty()) {
            CachedExternalModel cacheKey = new CachedExternalModel(rdfClass, resourceId, language);

            URI rdfUri = this.getExternalResourceId(resourceId, language);
            String[] segments = rdfUri.getPath().split("/");
            String idStr = segments[segments.length - 1];
            //the wikidata endpoint is actually https.
//            Model cachedResult = this.getCachedEntry(cacheKey);
            Model cachedResult = null;

            if (cachedResult != null) {
                retVal = cachedResult;
            } else {
//                try to get from database
                List<GenericModelSubject> wikidataModelSubjects = null;
                if (USESSQL) {
                    wikidataModelSubjects = CachePersistor.getInstance().getWikiModel(idStr);
                }
                if (cachedResult == null && wikidataModelSubjects != null && wikidataModelSubjects.size() > 0) {
                    fromDatabase = true;
                    retVal = new LinkedHashModel();
                    List<String> languages = new ArrayList<>();
                    for (GenericModelSubject wikidataModelSubject : wikidataModelSubjects) {
                        //set manually
                        IRI sub = factory.createIRI(wikidataModelSubject.getSubject());
                        IRI pred = factory.createIRI(wikidataModelSubject.getPredicate());
                        Statement nameStatement;
                        //always make  it a  literal.
//                        try {
//                            //alwa
//                            IRI obj = factory.createIRI(wikidataModelSubject.getObjectLabel());
//                            nameStatement = factory.createStatement(sub, pred, obj);
//                        } catch (Exception ex) {
                            Literal obj = null;
                            if(!StringUtils.isEmpty(wikidataModelSubject.getObjectLabel())){
                                 obj = factory.createLiteral(wikidataModelSubject.getObjectLabel(), wikidataModelSubject.getObjectLanguage());
                            }else {
                                obj = factory.createLiteral(wikidataModelSubject.getObjectLabel());
                            }
                            nameStatement = factory.createStatement(sub, pred, obj);
                            languages.add(wikidataModelSubject.getObjectLanguage());
//                        }
                        retVal.add(nameStatement);
                    }

                    if (!languages.contains("en")) {
                        Logger.error("english not found in database item!");
                        if (wikidataModelSubjects.iterator().hasNext()) {
                            GenericModelSubject statement = wikidataModelSubjects.iterator().next();
                            String subject = statement.getSubject().toString();
                            String predicate = statement.getPredicate().toString();
                            String objectLabel = statement.getObjectLabel();
                            String objectLanguage = "en";

                            IRI sub = factory.createIRI(subject);
                            IRI pred = factory.createIRI(predicate);
                            //always a literal
//                            try {
//                                IRI obj = factory.createIRI(objectLabel);
//                                nameStatement = factory.createStatement(sub, pred, obj);
//                            } catch (Exception ex) {
                                Literal obj = factory.createLiteral(objectLabel, objectLanguage);
                            Statement nameStatement = factory.createStatement(sub, pred, obj);
//                            }
                            //this is, of courseobjectLanguage, only one label. We will add the others as well.
                            if (USESSQL) {
                                CachePersistor.getInstance().putWikidataAndModel(idStr, subject, predicate, objectLabel, objectLanguage);
                            }
                            retVal.add(nameStatement);

                        }
                    }
                }
            }

            if (!DISABLEFETCHONLINE &&cachedResult == null && retVal == null) {
//                Logger.info("getting from wikidata");
                retVal = new LinkedHashModel();

                try {
                    String query = "CONSTRUCT {\n" +
                            "wd:" + idStr + " rdfs:label ?l .\n" +
                            "wd:" + idStr + " schema:description ?x .\n" +
                            "  }\n" +
                            "WHERE {\n" +
                            "wd:" + idStr + " rdfs:label ?l .\n" +
                            "wd:" + idStr + " schema:description ?x .\n" +

                            " FILTER(LANG(?l) = \"nl\" || LANG(?l) = \"en\" || LANG(?l) = \"fr\") .\n " +
                            " FILTER(LANG(?x) = \"nl\" || LANG(?x) = \"en\" || LANG(?x) = \"fr\") .\n " +

                            "}";
                    RepositoryConnection sparqlConnection = null;
                    try {
                        SPARQLRepository sparqlRepository = new SPARQLRepository("https://query.wikidata.org/sparql");
                        sparqlRepository.initialize();
                        sparqlConnection = sparqlRepository.getConnection();

                        GraphQuery graphQuery = null;
                        graphQuery = sparqlConnection.prepareGraphQuery(QueryLanguage.SPARQL, query);
                        GraphQueryResult graphQueryResult = graphQuery.evaluate();
                        Model resultModel = QueryResults.asModel(graphQueryResult);
                        List<String> languagesParsed = new ArrayList<>();

                        for (Statement statement : resultModel) {
                            //We will serialize the elements of the statements to put into a sql database for parsing only.
                            //this well speed up things and will keep us from getting banned from wikidata.
                            String subject = statement.getSubject().toString();
                            String predicate = statement.getPredicate().toString();
                            String objectLabel = ((Literal) statement.getObject()).getLabel();
                            String objectLanguage = ((Literal) statement.getObject()).getLanguage().get().toString();
                            languagesParsed.add(objectLanguage);
                            //this is, of courseobjectLanguage, only one label. We will add the others as well.
                            if (USESSQL) {
                                CachePersistor.getInstance().putWikidataAndModel(idStr, subject, predicate, objectLabel, objectLanguage);
                            }
                            retVal.add(statement);
                        }
                        if (!languagesParsed.contains("en")) {
                            //FIXME  try first
                            Logger.warn("english not found for " + resourceId);
                            Logger.warn("taking any language " + resourceId);
                            if (resultModel.iterator().hasNext()) {
                                Statement statement = resultModel.iterator().next();
                                String subject = statement.getSubject().toString();
                                String predicate = statement.getPredicate().toString();
                                String objectLabel = ((Literal) statement.getObject()).getLabel();
                                String objectLanguage = "en";

                                IRI sub = factory.createIRI(subject);
                                IRI pred = statement.getPredicate();
                                Statement nameStatement;
                                try {
                                    IRI obj = factory.createIRI(objectLabel);
                                    nameStatement = factory.createStatement(sub, pred, obj);
                                } catch (Exception ex) {
                                    Literal obj = factory.createLiteral(objectLabel, objectLanguage);
                                    nameStatement = factory.createStatement(sub, pred, obj);

                                }
                                //this is, of courseobjectLanguage, only one label. We will add the others as well.
                                if (USESSQL) {
                                    CachePersistor.getInstance().putWikidataAndModel(idStr, subject, predicate, objectLabel, objectLanguage);
                                }
                                retVal.add(nameStatement);
                            }
                        }

                        this.putCachedEntry(cacheKey, retVal);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (retVal != null) {
            IRI sub = factory.createIRI("http://www.wikidata.org/entity/" + idStr);
            IRI pred = factory.createIRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
            IRI obj = factory.createIRI("http://wikiba.se/ontology-beta#Item");
            Statement nameStatement = factory.createStatement(sub, pred, obj);
            retVal.add(nameStatement);
            }
        }
        if(retVal == null){
            retVal =  new LinkedHashModel();
            Logger.error("wikidata "+resourceId + "has no external  model. ");
        }
//        if(!fromDatabase){
//            Logger.info("from query service");
//        }else{
//            Logger.info("from sql");
//        }
        return retVal;
    }

    private String additionalTupleRequestForJustLabel(String wikibase_item){
        String retVal = null;
        String query =
                        "PREFIX schema: <http://schema.org/> \n" +
                        "SELECT ?label WHERE {\n" +
                        "  BIND(\n" +
                        "wd:" +wikibase_item+
                        "\n" +
                        "AS ?data) .\n" +
                        "     ?data rdfs:label ?label . \n" +
                        "  }";
        RepositoryConnection sparqlConnection = null;
        TupleQuery tupleQuery = null;
        try {
            SPARQLRepository sparqlRepository = new SPARQLRepository("https://query.wikidata.org/sparql");
            sparqlRepository.initialize();
            sparqlConnection = sparqlRepository.getConnection();

        }
        catch (Exception e) {
            e.printStackTrace();
        }
        try {
            tupleQuery = sparqlConnection.prepareTupleQuery(QueryLanguage.SPARQL, query);
        }
        catch (MalformedQueryException e) {
            e.printStackTrace();
        }
        Map<String, String> queriedValues = new HashMap<>();

        try {
            TupleQueryResult tupleQueryResult = tupleQuery.evaluate();
            while (tupleQueryResult.hasNext()) {
                BindingSet bs = tupleQueryResult.next();
                Value labeLValue = bs.getValue("label");
                                if(labeLValue instanceof Literal){
                                    String label = ((Literal)labeLValue).getLabel();
                                    String language = ((Literal)labeLValue).getLanguage().get();
                                    queriedValues.put(language, label);
                                }
            }
        }
        catch (QueryEvaluationException e) {
            e.printStackTrace();
        }
        if(queriedValues != null && queriedValues.keySet().size() > 0){
            for(String key : queriedValues.keySet()){
                switch (key){
                    case "en" :retVal = queriedValues.get("en"); break;
                    case "fr":retVal = queriedValues.get("fr"); break;
                    case "nl":retVal = queriedValues.get("nl"); break;
                    default:
                        retVal = queriedValues.get(queriedValues.keySet().iterator().next()); break;

                }
            }
        }
       return  retVal;

    }

    @Override
    public RdfProperty[] getLabelCandidates(RdfClass rdfClass)
    {
        if (this.cachedLabelProps == null) {
            this.cachedLabelProps = new RdfProperty[] { RDFS.label };
        }

        return this.cachedLabelProps;
    }
    @Override
    public URI getExternalResourceId(URI resourceId, Locale locale)
    {
        return AbstractWikidata.toWikidataUri(new RdfTools.RdfResourceUri(resourceId).getResourceId());
    }

    @Override
    public RdfClass getExternalClasses(RdfClass rdfClass)
    {
        return WB.Item;
    }

    private String getBaseString(Locale locale)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(this.WIKIPEDIPrefix);
        sb.append(locale.getLanguage());
        sb.append(this.WIKIPEDIA_API_URI);
        return sb.toString();
    }

    private String getWikibase_item(String title, Locale language)
    {
        String wikidataApiString = "https://www.wikidata.org/w/api.php?";
        String action = "action";
        String wbgetentities = "wbgetentities";
        String sites = "sites";
        String wikiString = language.toString() + "wiki";
        String titles = "titles";

        Client httpClient = null;
        Response response = null;
        ClientConfig config = new ClientConfig();
        httpClient = ClientBuilder.newClient(config);
        UriBuilder builder = UriBuilder.fromUri(wikidataApiString)
                                       .queryParam(action, wbgetentities)
                                       .queryParam(sites, wikiString)
                                       .queryParam("format", "json")
                                       .queryParam(titles, title);

        URI target = builder.build();
        response = httpClient.target(target).request(MediaType.APPLICATION_JSON).get();
        String wikibase_item = null;
        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            JsonNode jsonNode = null;
            try {
                jsonNode = Json.getObjectMapper().readTree(response.readEntity(String.class));
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            wikibase_item = jsonNode.get("entities").fields().next().getKey();
        }
        Logger.error("no wikibase item found for: " + title);
        return wikibase_item;
    }

    private synchronized Collection<ResourceProxy> doSearchQueryForLanguage(String query, RdfOntologyMember resourceType, Locale language) throws IOException
    {
        Set<ResourceProxy> retVal = new HashSet<>();
        Set<ResourceProxy> tempVal = new HashSet<>();
        Client httpClient = null;

        Response response = null;
        try {
            //basestring in english
            String englishBaseString = this.getBaseString(Locale.ENGLISH);

            //basestring for current language
            String baseString = this.getBaseString(language);

            ClientConfig config = new ClientConfig();
            httpClient = ClientBuilder.newClient(config);

            UriBuilder builder = UriBuilder.fromUri("https://www.wikidata.org/w/api.php?")
                                           .queryParam(action, "wbsearchentities")
                                           .queryParam("format", "json")
                                           .queryParam("language", language)
                                           .queryParam("type", "item")
                                           .queryParam("continue", "0")
                                           .queryParam("limit", "15")
                                           .queryParam(search, query);
            URI target = builder.build();

            response = httpClient.target(target).request(MediaType.APPLICATION_JSON).get();
            if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                //the request will return a jsonNode.
                // Resource Titles and links are in seperate trees with the same order.
                //response example in json : https://en.wikipedia.org/w/api.php?action=opensearch&search=tank&format=json
                JsonNode jsonNode = Json.getObjectMapper().readTree(response.readEntity(String.class));

                Map<String, ResourceProxy> suggestionMap = new HashMap<>();
                for (JsonNode iteratingNode : jsonNode.get("search")) {
                    String label = iteratingNode.get("label") == null ? "" : iteratingNode.get("label").textValue();
                    String description = iteratingNode.get("description") == null ? "" : iteratingNode.get("description").textValue();
                    String id = iteratingNode.get("id") == null ? "" : iteratingNode.get("id").textValue();
                    WikidataSuggestion autocompleteSuggestion = new WikidataSuggestion();
                    autocompleteSuggestion.setLabel(label);
                    autocompleteSuggestion.setDescription(description);
                    autocompleteSuggestion.setUri(RdfTools.createRelativeResourceId(RdfFactory.getClass(resourceType.getCurie()), id));
                    autocompleteSuggestion.setLanguage(language);
                    autocompleteSuggestion.setWikidataId(id);
                    try {
                        autocompleteSuggestion.setResourceType(new URI("crb:WikidataCountry"));
                    }
                    catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                    tempVal.add(autocompleteSuggestion);
                    suggestionMap.put(id, autocompleteSuggestion);
                }
                if (this.wikidataInstancesOff != null) {

                    //Start iterating over the entityIds. Add them to the  query.
                    StringBuilder valuesBuilder = new StringBuilder("VALUES ?values {");
                    for (String wikiClass : this.wikidataInstancesOff) {
                        valuesBuilder.append(" wd:");
                        valuesBuilder.append(wikiClass);
                    }
                    valuesBuilder.append("} ");
                    String values = valuesBuilder.toString();
                    Iterator<String> entityIdIterator = suggestionMap.keySet().iterator();
                    StringBuilder totalSelectQuery = null;
                    while (entityIdIterator.hasNext()) {
                        //this is the current wikidataId.
                        String currentIteratedId = entityIdIterator.next();
                        if (totalSelectQuery == null) {
                            totalSelectQuery = new StringBuilder();
                            totalSelectQuery.append("SELECT ?item WHERE {");
                            totalSelectQuery.append(values);
                            totalSelectQuery.append(" {BIND (wd:");
                            totalSelectQuery.append(currentIteratedId);
                            totalSelectQuery.append(" as ?item) ?item wdt:P31 ?values}");
                        }
                        else {
                            totalSelectQuery.append("UNION{ BIND (wd:" + currentIteratedId + " as ?item) ?item wdt:P31 ?values}");
                        }

                    }
                    //add a closing bracket
                    totalSelectQuery.append("}");
                    RepositoryConnection sparqlConnection = null;
                    try {
                        SPARQLRepository sparqlRepository = new SPARQLRepository("https://query.wikidata.org/sparql");
                        sparqlRepository.initialize();
                        sparqlConnection = sparqlRepository.getConnection();

                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                    TupleQuery tupleQuery = null;
                    try {
                        tupleQuery = sparqlConnection.prepareTupleQuery(QueryLanguage.SPARQL, totalSelectQuery.toString());
                    }
                    catch (MalformedQueryException e) {
                        e.printStackTrace();
                    }
                    catch (RepositoryException e) {
                        e.printStackTrace();
                    }
                    String label = null;
                    retVal = new HashSet<>();

                    try {
                        TupleQueryResult tupleQueryResult = tupleQuery.evaluate();
                        while (tupleQueryResult.hasNext()) {
                            //?item is the wikidataId, it should be the only thing we find
                            BindingSet bindingSet = tupleQueryResult.next();
                            String wikidataItemId = bindingSet.getValue("item").stringValue();
                            String qId = wikidataItemId.substring(wikidataItemId.lastIndexOf('/') + 1);
                            retVal.add(suggestionMap.get(qId));
                        }
                    }
                    catch (QueryEvaluationException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    retVal = tempVal;
                }
            }

        }
        catch (UnknownHostException uhe) {
            Logger.error("UnknownHostException thrown for " + query + ". No internet connection?");
        }
        catch (ProcessingException exception) {
            Logger.error("ProcessingException thrown for " + query + ". No internet connection?");
        }

        finally {
            if (response != null) {
                response.close();
            }
            if (httpClient != null) {
                httpClient.close();
            }
        }
        return retVal;
    }

    private synchronized Collection<ResourceProxy> getCachedEntry(CachedSearch query)
    {
        return (Collection<ResourceProxy>) this.getWikidataCache().get(query);
    }
    private synchronized void putCachedEntry(CachedSearch key, Collection<ResourceProxy> results)
    {
        this.getWikidataCache().put(key, results);
    }
    private synchronized Model getCachedEntry(CachedExternalModel externalModel)
    {
        return (Model) this.getWikidataCache().get(externalModel);
    }
    private synchronized void putCachedEntry(CachedExternalModel key, Model model)
    {
        this.getWikidataCache().put(key, model);
    }
    private synchronized Cache getWikidataCache()
    {
        if (!R.cacheManager().cacheExists(WIKIDATA_CACHED_RESULTS.name())) {
            //we instance a cache where it's entries live for one day (both from creation time as from last accessed time),
            //Overflows to disk and keep at most 1000 results
            R.cacheManager().registerCache(new EhCacheAdaptor(WIKIDATA_CACHED_RESULTS.name(), 1000, false, false, 24 * 60 * 60, 24 * 60 * 60));
        }
        return R.cacheManager().getCache(WIKIDATA_CACHED_RESULTS.name());
    }

    /**
     * This class makes sure the hashmap takes all query parameters into account while caching the resutls
     */
    private static class CachedSearch
    {
        private AbstractWikidata.Type wikidataType;
        private RdfClass resourceType;
        private String query;
        private QueryType queryType;
        private Locale language;
//        private SearchOption[] options;

        public CachedSearch(AbstractWikidata.Type wikidataType, RdfClass resourceType, String query, QueryType queryType, Locale language)
        {
            this.wikidataType = wikidataType;
            this.resourceType = resourceType;
            this.query = query;
            this.queryType = queryType;
            this.language = language;
//            this.options = options;
        }

//        @Override
//        public boolean equals(Object o)
//        {
//            if (this == o) {
//                return true;
//            }
//            if (!(o instanceof CachedSearch)) {
//                return false;
//            }
//
//            CachedSearch that = (CachedSearch) o;
//
//            if (wikidataType != that.wikidataType) {
//                return false;
//            }
//            if (resourceType != null ? !resourceType.equals(that.resourceType) : that.resourceType != null) {
//                return false;
//            }
//            if (query != null ? !query.equals(that.query) : that.query != null) {
//                return false;
//            }
//            if (queryType != that.queryType) {
//                return false;
//            }
//            if (language != null ? !language.equals(that.language) : that.language != null) {
//                return false;
//            }
//            // Probably incorrect - comparing Object[] arrays with Arrays.equals
//            return Arrays.equals(options, that.options);
//
//        }
        @Override
        public int hashCode()
        {
            int result = wikidataType != null ? wikidataType.hashCode() : 0;
            result = 31 * result + (resourceType != null ? resourceType.hashCode() : 0);
            result = 31 * result + (query != null ? query.hashCode() : 0);
            result = 31 * result + (queryType != null ? queryType.hashCode() : 0);
            result = 31 * result + (language != null ? language.hashCode() : 0);
//            result = 31 * result + Arrays.hashCode(options);
            return result;
        }
    }

    private static class CachedResource
    {
        private RdfClass resourceType;
        private URI resourceId;
        private Locale language;

        public CachedResource(RdfClass resourceType, URI resourceId, Locale language)
        {
            this.resourceType = resourceType;
            this.resourceId = resourceId;
            this.language = language;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) {
                return true;
            }
            if (!(o instanceof CachedResource)) {
                return false;
            }

            CachedResource that = (CachedResource) o;

            if (resourceType != null ? !resourceType.equals(that.resourceType) : that.resourceType != null) {
                return false;
            }
            if (resourceId != null ? !resourceId.equals(that.resourceId) : that.resourceId != null) {
                return false;
            }
            return language != null ? language.equals(that.language) : that.language == null;

        }
        @Override
        public int hashCode()
        {
            int result = resourceType != null ? resourceType.hashCode() : 0;
            result = 31 * result + (resourceId != null ? resourceId.hashCode() : 0);
            result = 31 * result + (language != null ? language.hashCode() : 0);
            return result;
        }
    }

    private static class CachedExternalModel
    {
        private RdfClass resourceType;
        private URI resourceId;
        private Locale language;

        public CachedExternalModel(RdfClass resourceType, URI resourceId, Locale language)
        {
            this.resourceType = resourceType;
            this.resourceId = resourceId;
            this.language = language;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) {
                return true;
            }
            if (!(o instanceof CachedExternalModel)) {
                return false;
            }

            CachedExternalModel that = (CachedExternalModel) o;

            if (resourceType != null ? !resourceType.equals(that.resourceType) : that.resourceType != null) {
                return false;
            }
            if (resourceId != null ? !resourceId.equals(that.resourceId) : that.resourceId != null) {
                return false;
            }
            return language != null ? language.equals(that.language) : that.language == null;

        }
        @Override
        public int hashCode()
        {
            int result = resourceType != null ? resourceType.hashCode() : 0;
            result = 31 * result + (resourceId != null ? resourceId.hashCode() : 0);
            result = 31 * result + (language != null ? language.hashCode() : 0);
            return result;
        }
    }

    private class TupleResult
    {
        private String label = null;
        private String description = null;
        private String sitelink = null;
        private String image = null;
        private String languageTag = null;

        public TupleResult(String languageTag, String label, String description, String image, String siteLink)
        {
            this.label = label;
            this.sitelink = siteLink;
            this.image = image;
            this.languageTag = languageTag;
        }
        public String getLabel()
        {
            return label;
        }
        public String getSitelink()
        {
            return sitelink;
        }
        public String getImage()
        {
            return image;
        }
        public String getLanguageTag()
        {
            return languageTag;
        }
        public String getDescription()
        {
            return description;
        }
        @Override
        public boolean equals(Object o)
        {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            TupleResult result = (TupleResult) o;

            return languageTag.equals(result.languageTag);
        }
        @Override
        public int hashCode()
        {
            return languageTag.hashCode();
        }
    }

}
