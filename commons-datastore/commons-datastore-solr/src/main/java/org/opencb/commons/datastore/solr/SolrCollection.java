/*
 * Copyright 2015-2017 OpenCB
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

package org.opencb.commons.datastore.solr;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.opencb.commons.datastore.core.ComplexTypeConverter;
import org.opencb.commons.datastore.core.QueryResult;
import org.opencb.commons.datastore.core.result.FacetQueryResult;
import org.opencb.commons.datastore.core.result.FacetQueryResultItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SolrCollection {

    private SolrClient solrClient;
    private String collection;

    private Logger logger;

    SolrCollection(String collection, SolrClient solrClient) {
        this.solrClient = solrClient;
        this.collection = collection;

        logger = LoggerFactory.getLogger(SolrCollection.class);
    }

    public <S> QueryResult<S> query(SolrQuery solrQuery, Class<S> clazz) throws IOException, SolrServerException {
        logger.debug("Executing Solr query: {}", solrQuery.toString());
        StopWatch stopWatch = StopWatch.createStarted();
        QueryResponse solrResponse = solrClient.query(collection, solrQuery);
        List<S> solrResponseBeans = solrResponse.getBeans(clazz);
        int dbTime = (int) stopWatch.getTime(TimeUnit.MILLISECONDS);

        return new QueryResult<>("", dbTime, solrResponseBeans.size(), solrResponse.getResults().getNumFound(), "", "", solrResponseBeans);
    }

    public <S, T> QueryResult<T> query(SolrQuery solrQuery, Class<S> clazz, ComplexTypeConverter<T, S> converter)
            throws IOException, SolrServerException {
        logger.debug("Executing Solr query: {}", solrQuery.toString());
        StopWatch stopWatch = StopWatch.createStarted();
        QueryResponse solrResponse = solrClient.query(collection, solrQuery);
        List<S> solrResponseBeans = solrResponse.getBeans(clazz);
        int dbTime = (int) stopWatch.getTime(TimeUnit.MILLISECONDS);

        List<T> results = new ArrayList<>(solrResponseBeans.size());
        for (S s: solrResponseBeans) {
            results.add(converter.convertToDataModelType(s));
        }

        return new QueryResult<>("", dbTime, results.size(), solrResponse.getResults().getNumFound(), "", "", results);
    }

    public FacetQueryResult facet(SolrQuery solrQuery) throws IOException, SolrServerException {
        return facet(solrQuery, null, null);
    }

    public FacetQueryResult facet(SolrQuery solrQuery, Map<String, String> alias, Map<String, List<String>> exclude) throws IOException,
            SolrServerException {
        logger.debug("Executing Solr facet: {}", solrQuery.toString());
        StopWatch stopWatch = StopWatch.createStarted();
        QueryResponse query = solrClient.query(collection, solrQuery);
        FacetQueryResultItem resultItem = SolrFacetToFacetQueryResultItemConverter.convert(query, alias, exclude);
        int dbTime = (int) stopWatch.getTime(TimeUnit.MILLISECONDS);

        return new FacetQueryResult("Faceted data from Solr", dbTime, resultItem.getFacetFields().size(), Collections.emptyList(), null,
                resultItem, solrQuery.getQuery());
    }

    public long count(SolrQuery solrQuery) throws IOException, SolrServerException {
        // Only count, no results
        solrQuery.setRows(0);

        logger.debug("Solr count: {}", solrQuery.toString());
        QueryResponse solrResponse = solrClient.query(collection, solrQuery);
        return solrResponse.getResults().getNumFound();
    }
}
