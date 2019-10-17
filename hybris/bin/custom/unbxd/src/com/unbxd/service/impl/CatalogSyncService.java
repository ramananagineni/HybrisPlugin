package com.unbxd.service.impl;

import com.unbxd.client.ConnectionManager;
import com.unbxd.client.Unbxd;
import com.unbxd.client.feed.DataType;
import com.unbxd.client.feed.FeedClient;
import com.unbxd.client.feed.FeedClientFactory;
import com.unbxd.client.feed.FeedProduct;
import com.unbxd.client.feed.exceptions.FeedUploadException;
import com.unbxd.client.feed.response.FeedResponse;
import com.unbxd.constants.UnbxdConstants;
import de.hybris.platform.util.Config;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class CatalogSyncService {

    private FeedClientFactory feedClientFactory;

    public void syncCatalogProduct(){

        Unbxd.configure(Config.getParameter(UnbxdConstants.SITE_KEY), Config.getParameter(UnbxdConstants.API_KEY), Config.getParameter(UnbxdConstants.SECRET_KEY));
        FeedClient feedClient = null;
        try {
            feedClient = Unbxd.getFeedClient();

        //getFeedClientFactory().getFeedClient(Config.getParameter(UnbxdConstants.SITE_KEY), Config.getParameter(UnbxdConstants.SECRET_KEY), true);

        feedClient.addSchema("code", DataType.TEXT);
        feedClient.addSchema("name", DataType.TEXT);

        Map<String, Object> pid1 = new HashMap<String, Object>();
        pid1.put("code", "new");
        pid1.put("name", "new product");
//        pid1.put("title", "Nike Men's Flex 2015 Running Shoe"); //Title of the product
//        pid1.put("color", "black"); //A custom field
//        pid1.put("brand", "Adidas");
//        pid1.put("category", "Sports Shoes");
//        pid1.put("price", 1195);

        //feedClient.addProduct(new FeedProduct("sku id1",pid1));
        //FeedResponse response= feedClient.push(true);

            String url = feedClient.getFeedUrl()+"full";


            HttpClientBuilder builder = HttpClientBuilder.create();
            HttpClient httpClient1 = builder.build();

            //CloseableHttpClient httpClient = HttpClients.custom().setConnectionTimeToLive(1, TimeUnit.MINUTES).setConnectionManager(ConnectionManager.getConnectionManager()).build();
            //CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(ConnectionManager.getConnectionManager()).build();
            File file = new File("/Users/i313831/Documents/temp.json");
            HttpPost post = new HttpPost(url);
            post.addHeader("Authorization", Config.getParameter(UnbxdConstants.SECRET_KEY));
            post.addHeader("Content-Type", ContentType.TEXT_PLAIN.toString());
            MultipartEntityBuilder entity = MultipartEntityBuilder.create();
            entity.addPart("file", new FileBody(file));
            post.setEntity(entity.build());
            //post.setEntity(new FileEntity(file));

            HttpResponse response = httpClient1.execute(post);


            System.out.println(response.toString());
        } /*catch (FeedUploadException e) {
            e.printStackTrace();
        } */catch (Exception e){
            e.printStackTrace();
        }
    }

    //comment1

    public FeedClientFactory getFeedClientFactory() {
        return feedClientFactory;
    }

    public void setFeedClientFactory(FeedClientFactory feedClientFactory) {
        this.feedClientFactory = feedClientFactory;
    }
}
