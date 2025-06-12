package org.opencb.commons.datastore.mongodb;

import com.mongodb.client.ClientSession;
import com.mongodb.client.TransactionBody;
import org.bson.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opencb.commons.datastore.core.DataResult;
import org.opencb.commons.datastore.core.DataStoreServerAddress;
import org.opencb.commons.datastore.core.QueryOptions;

import java.util.ArrayList;
import java.util.List;

public class TransactionTest {

    private MongoDataStoreManager mongoDataStoreManager;
    private MongoDataStore mongoDataStore;

    @Before
    public void setUp() throws Exception {
        List<DataStoreServerAddress> dataStoreServerAddressList = new ArrayList<>(1);
        dataStoreServerAddressList.add(new DataStoreServerAddress("127.0.0.1", 27017));
//        dataStoreServerAddressList.add(new DataStoreServerAddress("127.0.0.1", 27018));
//        dataStoreServerAddressList.add(new DataStoreServerAddress("127.0.0.1", 27019));
        mongoDataStoreManager = new MongoDataStoreManager(dataStoreServerAddressList);
        mongoDataStoreManager.get("test").getDb().drop();
        mongoDataStore = mongoDataStoreManager.get("test", MongoDBConfiguration.builder()
                .setReplicaSet("rs-test").init().build());
    }

    @After
    public void tearDown() throws Exception {
        mongoDataStoreManager.close("test");
    }

    @Test
    @Ignore
    public void testGet() throws Exception {
        ClientSession clientSession = mongoDataStore.startSession();

        mongoDataStore.createCollection("foo");
        mongoDataStore.createCollection("bar");

        TransactionBody txnBody = new TransactionBody<DataResult>() {
            public DataResult execute() {
                MongoDBCollection coll1 = mongoDataStore.getCollection("foo");
                MongoDBCollection coll2 = mongoDataStore.getCollection("bar");

                for (int i = 0; i < 1000; i++) {
                    coll1.insert(clientSession, new Document("abc", i), QueryOptions.empty());
                    coll2.insert(clientSession, new Document("xyz", i + 100), QueryOptions.empty());
//                    coll2.insert(clientSession, new Document("abc", i + 100), QueryOptions.empty());
                }

                return DataResult.empty();
            }
        };

        mongoDataStore.commitSession(clientSession, txnBody);
    }


}
