package com.agroneural.pentaho.cda.graphql;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
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
	private TypedTableModel createCdaModel(Map<String, Class<String>> columns) {

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

	public TypedTableModel readJSONObject(JSONObject data) throws JSONException {

		Map<String, Serializable> resultset = new LinkedHashMap<String, Serializable>();
		Map<String, Class<String>> _columns = new LinkedHashMap<String, Class<String>>();

		Iterator<?> res = data.keys();

		while(res.hasNext()) {

			String resKey = res.next().toString();
			String resValue = data.get(resKey).toString();

			if (this.debug) {
				System.out.println("[DEBUG] GraphqlDatasourceX: key: "+ resKey + " value: " + resValue);
			}

			resultset.put(resKey, resValue);
			_columns.put(resKey, String.class);

		}

		TypedTableModel model = createCdaModel(_columns);

		List<Object> row = new ArrayList<Object>();

		for (String key : resultset.keySet()) {

			String value = resultset.get(key).toString();

			if(value.length() == 0) { value = ""; }

			row.add(new String(value));
		}

		model.addRow(row.toArray());

		return model;

	}

	public TypedTableModel readJSONArray(JSONArray data) throws JSONException {

		Map<String, Class<String>> _columns = new LinkedHashMap<String, Class<String>>();
		Map<String, Serializable> resultset = new LinkedHashMap<String, Serializable>();
		TypedTableModel model = null;

		if (data.length() > 0) {

			// Read the first object in the array to discover the columns and
			// create the CDA model.
			JSONObject item = data.getJSONObject(0);

			Iterator<?> res = item.keys();

			while(res.hasNext()) {

				String resKey = res.next().toString();
				String resValue = item.get(resKey).toString();

				if (this.debug) {
					System.out.println("[DEBUG] GraphqlDatasource: key: "+ resKey + " value: " + resValue);
				}

				resultset.put(resKey, resValue);
				_columns.put(resKey, String.class);

			}

			model = createCdaModel(_columns);

			List<Object> row = new ArrayList<Object>();

			for (String key : resultset.keySet()) {

				String value = resultset.get(key).toString();

				if(value.length() == 0) { value = ""; }

				row.add(new String(value));
			}

			model.addRow(row.toArray());

			// Read the others objects in the array to include in the
			// CDA model.
			for (int i = 1; i < data.length(); i++) {

				item = data.getJSONObject(i);

				row = new ArrayList<Object>();
				res = item.keys();

				while(res.hasNext()) {

					String key = res.next().toString();
					String value = item.get(key).toString();

					if (this.debug) {
						System.out.println("[DEBUG] GraphqlDatasource: key: "+ key + " value: " + value);
					}

					if(value.length() == 0) { value = ""; }

					row.add(new String(value));
	
				}

				model.addRow(row.toArray());

			}

		}

		return model;

	}

	public TypedTableModel run(String query) throws JSONException, ClientProtocolException, IOException {

		return run(query, null);

	}

	public TypedTableModel run(String query, String token) throws JSONException, ClientProtocolException, IOException {

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
			model = readJSONObject((JSONObject) data.get(dataNodeName));

		else if (data.get(dataNodeName) instanceof JSONArray)
			model = readJSONArray((JSONArray) data.get(dataNodeName));

		return model;

	}

}
