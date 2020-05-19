package com.agroneural.pentaho.cda.graphql;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.pentaho.reporting.engine.classic.core.util.TypedTableModel;

public class GraphqlDatasource {
	
	String url;
	boolean debug;
	
	public GraphqlDatasource(String url, boolean debug) throws Exception {

		this.url = url;
		this.debug = debug;
	
	}
	
	// Create CDA table model
	private TypedTableModel createCdaModel(Map columns) {
		
		TypedTableModel model = new TypedTableModel();
		
		for (Object key : columns.keySet()) {
			
			String columnName = (String) key;
			Class columnType = (Class) columns.get(key);
			
			model.addColumn(columnName, columnType);
			
			if (this.debug) {
				System.out.println("[DEBUG] GraphqlDatasource: Adding to model: " + columnName + "/" + columnType.getName());
			}
			
		}
		
		return model;
		
	}

	public CloseableHttpResponse executeGraphqlQuery(String query, String token) throws JSONException, ClientProtocolException, IOException {
		
        CloseableHttpClient client = null;
        CloseableHttpResponse response = null;

        client = HttpClients.createDefault();
        HttpPost httpPost= new HttpPost(this.url);

        if (token != null)
        	httpPost.addHeader("Authorization","Bearer " + token);
        
        httpPost.addHeader("Content-Type","application/json");
        httpPost.addHeader("Accept","application/json");
        httpPost.addHeader("X-Origin","pentaho-cda://graphqldatasource");

        JSONObject jsonObj = new JSONObject();     
        jsonObj.put("query", query);

        StringEntity entity = new StringEntity(jsonObj.toString());

        httpPost.setEntity(entity);
        response = client.execute(httpPost);

        return response;
		
	}

	public TypedTableModel readJSONObject(Map columns, JSONObject data) throws JSONException {
        
		TypedTableModel model = createCdaModel(columns);
		
        Iterator res = data.keys();
        
		List<Object> row = new ArrayList<Object>();

        while(res.hasNext()) {
        	
            String resKey = res.next().toString();
            Object resValue = data.get(resKey);

    		if (this.debug) {
    			System.out.println("[DEBUG] GraphqlDatasource: key: "+ resKey + " value: " + resValue);
    		}
    		
    		for (Object key : columns.keySet()) {
    			
    			String columnName = (String) key;
    			
    			if (columnName.equalsIgnoreCase(resKey)) {
    				row.add(new String(resValue.toString()));
    				break;
    			}
    			
    		}
    		
        }            

        model.addRow(row.toArray());
        
		return model;		

	}
	
	public TypedTableModel readJSONArray(Map columns, JSONArray data) throws JSONException {
        
		TypedTableModel model = createCdaModel(columns);
		
	    for (int i = 0; i < data.length(); i++) {
	    	
	        JSONObject item = data.getJSONObject(i);

	        Iterator res = item.keys();

	        List<Object> row = new ArrayList<Object>();
	        
	        while(res.hasNext()) {
	        	
	        	String resKey = res.next().toString();
	        	Object resValue = item.get(resKey);
	        	
	        	if (this.debug) {
	        		System.out.println("[DEBUG] GraphqlDatasource: key: "+ resKey + " value: " + resValue);
	        	}
	        	
	        	for (Object key : columns.keySet()) {
	        		
	        		String columnName = (String) key;
	        		
	        		if (columnName.equalsIgnoreCase(resKey)) {
	        			row.add(new String(resValue.toString()));
	        			break;
	        		}
	        		
	        	}
	        	
	        }            
	        
	        model.addRow(row.toArray());
	    }		
		
		return model;		

	}
	
	public TypedTableModel run(Map columns, String query) throws JSONException, ClientProtocolException, IOException {
		
		return run(columns, query, null);
	}
	
	public TypedTableModel run(Map columns, String query, String token) throws JSONException, ClientProtocolException, IOException {

		TypedTableModel model = null;
		
		CloseableHttpResponse result = executeGraphqlQuery(query, token);
		
        BufferedReader reader = new BufferedReader(new InputStreamReader(result.getEntity().getContent()));
        
        String line = null;
        StringBuilder builder = new StringBuilder();
        while((line=reader.readLine())!= null) {

            builder.append(line);

        }

		if (this.debug) {
			System.out.println("[DEBUG] GraphqlDatasource: Query result: " + builder.toString());
		}

        JSONObject jsonObject = new JSONObject(builder.toString());
        
        JSONObject data = (JSONObject) jsonObject.get("data");
        String dataNodeName = (String) data.keys().next();
        
        if (data.get(dataNodeName) instanceof JSONObject)
        	model = readJSONObject(columns, (JSONObject) data.get(dataNodeName));
        else if (data.get(dataNodeName) instanceof JSONArray)
        	model = readJSONArray(columns, (JSONArray) data.get(dataNodeName));
        
        return model;
        
	}
	
}
