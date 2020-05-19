# Pentaho CDA datasource for GraphQL

```java
package com.agroneural.pentaho.cda.graphql;

import java.util.HashMap;
import java.util.Map;

import org.pentaho.reporting.engine.classic.core.util.TypedTableModel;

public class TestClient {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub

//		String query = "" +
//		"mutation {" +
//		"	signup(email:\"t.eng@gmail.com\", name: \"Alessandro Dias\", uuid:\"123456\", userTypeId: \"5e335c873e8d40676928656d\", application: xyz) {" +
//		"		status," +
//		"		success," +
//		"		message" +
//		"	}" +
//		"}";
//		
//		Map columns = new HashMap();
//		
//		// Set the output fields from the query - use only String.class
//		columns.put("status", String.class);
//		columns.put("success", String.class);
//		columns.put("message", String.class);

		
		String query = "" +
		"{" +
		"	appVersion(appName: \"im4adm\") {" +
		"		appName," +
		"	    version," +
		"	    forceUpdate," +
		"	    url" +
		"	}" +
		"}";
		
		Map columns = new HashMap();
		
		// Set the output fields from the query - use only String.class
		columns.put("appName", String.class);
		columns.put("version", String.class);
		columns.put("forceUpdate", String.class);
		columns.put("url", String.class);
		
		GraphqlDatasource graphqlDatasource = new GraphqlDatasource("http://localhost:8080/", true);

		TypedTableModel r = graphqlDatasource.run(columns, query, "123456");
		
	}

}
```
