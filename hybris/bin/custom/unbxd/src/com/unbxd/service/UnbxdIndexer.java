package com.unbxd.service;

import com.unbxd.client.ConfigException;
import com.unbxd.client.Unbxd;
import com.unbxd.client.feed.DataType;
import com.unbxd.client.feed.FeedClient;
import com.unbxd.client.feed.FeedProduct;
import com.unbxd.client.feed.exceptions.FeedUploadException;
import com.unbxd.client.feed.response.FeedResponse;
import com.unbxd.constants.UnbxdConstants;
import de.hybris.platform.core.PK;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.solrfacetsearch.config.*;
import de.hybris.platform.solrfacetsearch.config.exceptions.FieldValueProviderException;
import de.hybris.platform.solrfacetsearch.indexer.exceptions.IndexerException;
import de.hybris.platform.solrfacetsearch.indexer.spi.Exporter;
import de.hybris.platform.solrfacetsearch.provider.IdentityProvider;
import de.hybris.platform.solrfacetsearch.solr.Index;
import de.hybris.platform.solrfacetsearch.solr.SolrSearchProvider;
import de.hybris.platform.solrfacetsearch.solr.SolrSearchProviderFactory;
import de.hybris.platform.solrfacetsearch.solr.exceptions.SolrServiceException;
import de.hybris.platform.util.Config;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Required;

import java.util.*;
import java.util.stream.Collectors;



public class UnbxdIndexer implements BeanFactoryAware {
    private static final Logger LOG = Logger.getLogger("solrIndexThreadLogger");
    private SolrSearchProviderFactory solrSearchProviderFactory;
    private UnbxdDocumentFactory unbxdDocumentFactory;
    private BeanFactory beanFactory;

    public UnbxdIndexer() {
    }

    public SolrSearchProviderFactory getSolrSearchProviderFactory() {
        return this.solrSearchProviderFactory;
    }

    @Required
    public void setSolrSearchProviderFactory(SolrSearchProviderFactory solrSearchProviderFactory) {
        this.solrSearchProviderFactory = solrSearchProviderFactory;
    }

    public UnbxdDocumentFactory getSolrDocumentFactory() {
        return this.unbxdDocumentFactory;
    }

    @Required
    public void setUnbxdDocumentFactory(UnbxdDocumentFactory unbxdDocumentFactory) {
        this.unbxdDocumentFactory = unbxdDocumentFactory;
    }

    protected BeanFactory getBeanFactory() {
        return this.beanFactory;
    }

    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    public Collection<FeedProduct> indexItems(Collection<ItemModel> items, FacetSearchConfig facetSearchConfig, IndexedType indexedType) throws IndexerException, InterruptedException {
        if (items == null) {
            return Collections.emptyList();
        } else {
            IndexConfig indexConfig = facetSearchConfig.getIndexConfig();
            SolrConfig solrConfig = facetSearchConfig.getSolrConfig();
            Collection<FeedProduct> documents = new ArrayList(items.size());
            Iterator var8 = items.iterator();

            while(var8.hasNext()) {
                ItemModel itemModel = (ItemModel)var8.next();
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }

                try {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Indexing item with PK " + itemModel.getPk());
                    }

                    FeedProduct solrDocument = this.unbxdDocumentFactory.createInputDocument(itemModel, indexConfig, indexedType);
                    documents.add(solrDocument);
                } catch (RuntimeException | FieldValueProviderException var11) {
                    String message = "Failed to index item with PK " + itemModel.getPk() + ": " + var11.getMessage();
                    this.handleError(indexConfig, indexedType, message, var11);
                }
            }


            try {
                Unbxd.configure(Config.getParameter(UnbxdConstants.SITE_KEY), Config.getParameter(UnbxdConstants.API_KEY), Config.getParameter(UnbxdConstants.SECRET_KEY));

                FeedClient feedClient = Unbxd.getFeedClient();
                feedClient.addSchema("catalogVersion", DataType.TEXT);
                feedClient.addSchema("code_string", DataType.TEXT);
                feedClient.addSchema("autosuggest_en", DataType.TEXT);
                feedClient.addSchema("collectionName_text_en_mv", DataType.TEXT);
                feedClient.addSchema("name_text_en", DataType.TEXT);
                feedClient.addSchema("configurable_boolean", DataType.TEXT);
                feedClient.addSchema("img-96Wx96H_string", DataType.TEXT);
                feedClient.addSchema("categoryPath_string_mv", DataType.TEXT);
                feedClient.addSchema("priceValue_gbp_double", DataType.TEXT);
                feedClient.addSchema("catalogId", DataType.TEXT);
                feedClient.addSchema("categoryName_text_en_mv", DataType.TEXT);
                feedClient.addSchema("allCategories_string_mv", DataType.TEXT);
                feedClient.addSchema("brand_string_mv", DataType.TEXT);
                feedClient.addSchema("pickupAvailableFlag_boolean", DataType.TEXT);
                feedClient.addSchema("url_en_string", DataType.TEXT);
                feedClient.addSchema("img-65Wx65H_string", DataType.TEXT);
                feedClient.addSchema("inStockFlag_boolean", DataType.TEXT);
                feedClient.addSchema("baseProductCode", DataType.TEXT);
                feedClient.addSchema("id", DataType.TEXT);
                feedClient.addSchema("name_sortable_en_sortabletext", DataType.TEXT);
                feedClient.addSchema("stockLevelStatus_string", DataType.TEXT);
                feedClient.addSchema("brandName_text_en_mv", DataType.TEXT);
                feedClient.addSchema("img-515Wx515H_string", DataType.TEXT);
                feedClient.addSchema("img-300Wx300H_string", DataType.TEXT);
                feedClient.addSchema("img-30Wx30H_string", DataType.TEXT);
                feedClient.addSchema("collection_string_mv", DataType.TEXT);
                feedClient.addSchema("spellcheck_en", DataType.TEXT);
                feedClient.addSchema("availableInStores_string_mv", DataType.TEXT);
                feedClient.addSchema("ean_string", DataType.TEXT);
                feedClient.addSchema("url_en_string", DataType.TEXT);
                feedClient.addSchema("itemtype_string", DataType.TEXT);
                feedClient.addSchema("spellcheck", DataType.TEXT);
                feedClient.addSchema("price_gbp_string", DataType.TEXT);
                //feedClient.addSchema("autosuggest", DataType.TEXT);
                feedClient.addSchema("pk", DataType.TEXT);
                feedClient.addSchema("gender_string_mv", DataType.TEXT);
                feedClient.addSchema("summary_text_en", DataType.TEXT);
                feedClient.addSchema("category_string_mv", DataType.TEXT);


                feedClient.addProducts(new ArrayList<>(documents));
                FeedResponse response= feedClient.push(true);
                System.out.println(response.toString());
            }
            catch (FeedUploadException | ConfigException e) {
                e.printStackTrace();
            }

            catch (Exception e){
                e.printStackTrace();
            }

            /*SolrServerMode serverMode = solrConfig.getMode();
            Exporter exporter = this.getExporter(serverMode);
            exporter.exportToUpdateIndex(documents, facetSearchConfig, indexedType);*/
            return documents;
        }
    }

    public Collection<FeedProduct> indexItems(Collection<ItemModel> items, FacetSearchConfig facetSearchConfig, IndexedType indexedType, Collection<IndexedProperty> indexedProperties) throws IndexerException, InterruptedException {
        if (items == null) {
            return Collections.emptyList();
        } else {
            IndexConfig indexConfig = facetSearchConfig.getIndexConfig();
            SolrConfig solrConfig = facetSearchConfig.getSolrConfig();
            Collection<FeedProduct> documents = new ArrayList(items.size());
            Iterator var9 = items.iterator();

            while(var9.hasNext()) {
                ItemModel itemModel = (ItemModel)var9.next();
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }

                try {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Indexing item with PK " + itemModel.getPk());
                    }

                    FeedProduct solrDocument = this.unbxdDocumentFactory.createInputDocument(itemModel, indexConfig, indexedType, indexedProperties);
                    documents.add(solrDocument);
                } catch (RuntimeException | FieldValueProviderException var12) {
                    String message = "Failed to index item with PK " + itemModel.getPk() + ": " + var12.getMessage();
                    this.handleError(indexConfig, indexedType, message, var12);
                }
            }

            try {
                FeedClient feedClient = Unbxd.getFeedClient();
                feedClient.addProducts(new ArrayList<>(documents));
            } catch (ConfigException e) {
                e.printStackTrace();
            }

            /*SolrServerMode serverMode = solrConfig.getMode();
            Exporter exporter = this.getExporter(serverMode);
            exporter.exportToUpdateIndex(documents, facetSearchConfig, indexedType);*/
            return documents;
        }
    }

    public void removeItemsByPk(Collection<PK> pks, FacetSearchConfig facetSearchConfig, IndexedType indexedType, Index index) throws IndexerException, InterruptedException {
        if (!CollectionUtils.isEmpty(pks)) {
            SolrServerMode serverMode = facetSearchConfig.getSolrConfig().getMode();
            if (serverMode == SolrServerMode.XML_EXPORT) {
                Exporter exporter = this.getExporter(serverMode);
                List<String> pkValues = (List)pks.stream().map(PK::getLongValueAsString).collect(Collectors.toList());
                exporter.exportToDeleteFromIndex(pkValues, facetSearchConfig, indexedType);
            } else {
                try {
                    SolrSearchProvider searchProvider = this.solrSearchProviderFactory.getSearchProvider(facetSearchConfig, indexedType);
                    searchProvider.deleteDocumentsByPk(index, pks);
                } catch (SolrServiceException var8) {
                    throw new IndexerException(var8.getMessage(), var8);
                }
            }

        }
    }

    public Collection<String> removeItems(Collection<ItemModel> items, FacetSearchConfig facetSearchConfig, IndexedType indexedType) throws IndexerException, InterruptedException {
        if (items == null) {
            return Collections.emptyList();
        } else {
            IndexConfig indexConfig = facetSearchConfig.getIndexConfig();
            SolrConfig solrConfig = facetSearchConfig.getSolrConfig();
            Collection<String> delIds = new ArrayList(items.size());
            Iterator var8 = items.iterator();

            while(var8.hasNext()) {
                ItemModel itemModel = (ItemModel)var8.next();
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }

                try {
                    String solrId = this.getIdentityProvider(indexedType).getIdentifier(indexConfig, itemModel);
                    delIds.add(solrId);
                } catch (RuntimeException var11) {
                    String message = "Failed to remove item with PK " + itemModel.getPk() + ": " + var11.getMessage();
                    this.handleError(indexConfig, indexedType, message, var11);
                }
            }

            try {
                FeedClient feedClient = Unbxd.getFeedClient();
                feedClient.deleteProducts(new ArrayList<>(delIds));
            } catch (ConfigException e) {
                e.printStackTrace();
            }

            /*SolrServerMode serverMode = solrConfig.getMode();
            Exporter exporter = this.getExporter(serverMode);
            exporter.exportToDeleteFromIndex(delIds, facetSearchConfig, indexedType);*/

            return delIds;
        }
    }

    protected void handleError(IndexConfig indexConfig, IndexedType indexedType, String message, Exception error) throws IndexerException {
        if (indexConfig.isIgnoreErrors()) {
            LOG.warn(message);
        } else {
            throw new IndexerException(message, error);
        }
    }

    protected IdentityProvider<ItemModel> getIdentityProvider(IndexedType indexedType) {
        return (IdentityProvider)this.beanFactory.getBean(indexedType.getIdentityProvider(), IdentityProvider.class);
    }

    protected Exporter getExporter(SolrServerMode serverMode) throws IndexerException {
        String beanName = "solr.exporter." + serverMode.toString().toLowerCase();
        return (Exporter)this.beanFactory.getBean(beanName, Exporter.class);
    }
}
