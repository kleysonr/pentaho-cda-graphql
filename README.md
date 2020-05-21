# Pentaho CDA datasource for GraphQL

## Simple query execution
```java
package com.agroneural.pentaho.cda.graphql;

import org.pentaho.reporting.engine.classic.core.util.TypedTableModel;

public class TestClient {

	public static void main(String[] args) throws Exception {

		// String query = "" +
		// "mutation {" +
		// "	signup(email:\"t.eng@gmail.com\", name: \"Alessandro Dias\", uuid:\"123456\", userTypeId: \"5e335c873e8d40676928656d\", application: xyz) {" +
		// "		status," +
		// "		success," +
		// "		message" +
		// "	}" +
		// "}";

		String query = "" +
		"{" +
		"	appVersion(appName: \"im4adm\") {" +
		"		appName," +
		"	    version," +
		"	    forceUpdate," +
		"	    url" +
		"	}" +
		"}";

		GraphqlDatasource graphqlDatasource = new GraphqlDatasource("http://localhost:8080/", true);

		TypedTableModel r = graphqlDatasource.run(query, null, "123456");

	}

}
```

## Saving a token in the MongoDB to be validated by the endpoint and allow the request
```java
import org.pentaho.reporting.engine.classic.core.util.TypedTableModel;

import java.util.UUID;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.bson.conversions.Bson;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;

import com.example.pentaho.cda.MongoDatasource;
import com.agroneural.pentaho.cda.graphql.GraphqlDatasource;

// Set the databaseName and collectionName
String DATABASE_NAME = "im4db";
String COLLECTION_NAME = "onetime_tokens";
String JNDI_NAME = "java:/comp/env/mongodb/MongoClient";

// Get database and collection
MongoDatasource mongoDS = new MongoDatasource(JNDI_NAME, DATABASE_NAME, true);
MongoCollection collection = mongoDS.getDb().getCollection(COLLECTION_NAME);

// Generate a new document _id and a new token
ObjectId _id = new ObjectId();
String token = UUID.randomUUID().toString();

// Upsert document
Bson filter = new BasicDBObject("_id", _id);

Document doc = new Document();
doc.put("token", token);

Document query = new Document("$set", doc);

UpdateOptions options = new UpdateOptions().upsert(true);
UpdateResult res = collection.updateOne(filter, query, options);

System.out.println("Inserted token id: " + res.getUpsertedId());

// GraphQL query
String gqlQuery = "" +
"mutation {" +
"    signup(email:\"t.eng@gmail.com\", name: \"Alessandro Dias\", uuid:\"123456\", userTypeId: \"5e335c873e8d40676928656d\", application: im4) {" +
"		status," +
"		success," +
"		message" +
"	}" +
"}";

// Execute query
GraphqlDatasource graphqlDatasource = new GraphqlDatasource("http://localhost:8080/", true);

return graphqlDatasource.run(gqlQuery, token);
```