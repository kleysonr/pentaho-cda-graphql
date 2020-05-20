# Pentaho CDA datasource for GraphQL

```java
package com.agroneural.pentaho.cda.graphql;

import org.pentaho.reporting.engine.classic.core.util.TypedTableModel;

public class TestClient {

	public static void main(String[] args) throws Exception {

//		String query = "" +
//		"mutation {" +
//		"	signup(email:\"t.eng@gmail.com\", name: \"Alessandro Dias\", uuid:\"123456\", userTypeId: \"5e335c873e8d40676928656d\", application: xyz) {" +
//		"		status," +
//		"		success," +
//		"		message" +
//		"	}" +
//		"}";
//		
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

		TypedTableModel r = graphqlDatasource.run(query, "123456");
		
	}

}
```
