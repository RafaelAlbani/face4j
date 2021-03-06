package com.face4j.facebook.http;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;

import com.face4j.facebook.exception.FacebookException;
import com.face4j.facebook.util.JSONToObjectTransformer;
import com.google.appengine.api.urlfetch.*;

public class URLFetchAPICaller implements APICallerInterface {

	public String getData(String url, NameValuePair[] nameValuePairs) throws FacebookException {

		URLFetchService fetchService = URLFetchServiceFactory.getURLFetchService();
		URL fetchURL = null;

		HTTPResponse response = null;
		String responseString = null;
		String constructedParams = null;

		try {

			if (nameValuePairs != null) {
				constructedParams = constructParams(nameValuePairs);

				if (url.contains("?")) {
					url = url.concat("&" + constructedParams);
				} else {
					url = url.concat("?" + constructedParams);
				}
			}

			fetchURL = new URL(url);
			response = fetchService.fetch(fetchURL);

			int statusCode = response.getResponseCode();
			if (statusCode != HttpStatus.SC_OK) {
				// FacebookError error = new FacebookError(statusCode,
				// "I guess you are not permitted to access this url. HTTP status code:"+statusCode, null);
				responseString = new String(response.getContent());
				throw new FacebookException(JSONToObjectTransformer.getError(responseString, statusCode));
			}
			responseString = new String(response.getContent());
		} catch (HttpException e) {
			throw new FacebookException("Http Exception while calling facebook!", e);
		} catch (IOException e) {
			throw new FacebookException("IO Exception while calling facebook!", e);
		}

		// if response string contains accessToken=xxx remove it!
		// responseString = Util.replaceAccessToken(responseString, nameValuePairs);

		return responseString;
	}

	/**
	 * @param url
	 * @param nameValuePairs
	 * @return
	 * @throws FacebookException
	 */
	public String postData(String url, NameValuePair[] nameValuePairs) throws FacebookException {

		String content = null;
		String constructedParams = null;
		int statusCode = 0;
		HttpURLConnection connection = null;
		try {
			URL posturl = new URL(url);
			connection = (HttpURLConnection) posturl.openConnection();
			connection.setDoOutput(true);
			connection.setRequestMethod("POST");
			// connection.setConnectTimeout(10000);
			// connection.setReadTimeout(10000);

			OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());

			constructedParams = constructParams(nameValuePairs);

			writer.write(constructedParams);
			writer.close();

			statusCode = connection.getResponseCode();
			if (statusCode != HttpURLConnection.HTTP_OK) {
				// FacebookError error = new FacebookError(statusCode,
				// "I guess you are not permitted to access this url. HTTP status code:"+statusCode, null);
				content = getResponse(connection);
				throw new FacebookException(JSONToObjectTransformer.getError(content, statusCode));
			} else {
				content = getResponse(connection);
			}
		} catch (MalformedURLException e) {
			throw new FacebookException("Malformed URL Exception while calling facebook!", e);
		} catch (IOException e) {
			throw new FacebookException("IOException while calling facebook!", e);
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}

		return content;

	}

	private String getResponse(HttpURLConnection connection) throws IOException {
		String content;
		// Get Response
		InputStream is = connection.getInputStream();
		BufferedReader rd = new BufferedReader(new InputStreamReader(is));
		String line;
		StringBuilder response = new StringBuilder();
		while ((line = rd.readLine()) != null) {
			response.append(line);
			response.append('\r');
		}
		rd.close();
		content = response.toString();
		return content;
	}

	/*
	 * public String deleteData(String url, NameValuePair[] nameValuePairs) throws FacebookException {
	 * 
	 * String content = null; String constructedParams = null; int statusCode = 0; HttpURLConnection
	 * connection = null; try {
	 * 
	 * constructedParams = constructParams(nameValuePairs);
	 * 
	 * 
	 * 
	 * URL posturl = new URL(url+"/?"+constructedParams); connection = (HttpURLConnection)
	 * posturl.openConnection(); connection.setRequestProperty( "Content-Type",
	 * "application/x-www-form-urlencoded" ); connection.setDoOutput(true);
	 * connection.setRequestMethod("DELETE"); // connection.setConnectTimeout(10000); //
	 * connection.setReadTimeout(10000);
	 * 
	 * //connection.connect();
	 * 
	 * //System.out.println(connection.getContent());
	 * 
	 * OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
	 * 
	 * writer.write(""); writer.close();
	 * 
	 * statusCode = connection.getResponseCode(); if (statusCode != HttpURLConnection.HTTP_OK) {
	 * content = getResponse(connection); throw new
	 * FacebookException(JSONToObjectTransformer.getError(content, statusCode));
	 * 
	 * } else { content = getResponse(connection);
	 * 
	 * } } catch (MalformedURLException e) { throw new
	 * FacebookException("Malformed URL Exception while calling facebook!", e); } catch (IOException
	 * e) { throw new FacebookException("IOException while calling facebook!", e); } finally { if
	 * (connection != null) { connection.disconnect(); } }
	 * 
	 * return content;
	 * 
	 * }
	 */

	public String deleteData(String url, NameValuePair[] nameValuePairs) throws FacebookException {
		String content = null;
		String constructedParams = null;
		int statusCode = 0;

		URLFetchService fetchService = URLFetchServiceFactory.getURLFetchService();
		URL posturl = null;
		constructedParams = constructParams(nameValuePairs);

		try {
			posturl = new URL(url + "?" + constructedParams);
		} catch (MalformedURLException e) {
		}

		try {
			HTTPResponse response = fetchService.fetch(new HTTPRequest(posturl, HTTPMethod.DELETE));

			statusCode = response.getResponseCode();

			if (statusCode != HttpURLConnection.HTTP_OK) {
				content = new String(response.getContent());
				throw new FacebookException(JSONToObjectTransformer.getError(content, statusCode));
			} else {
				content = new String(response.getContent());
			}

		} catch (IOException e) {
		}

		return content;
	}

	private String constructParams(NameValuePair[] nameValuePairs) {

		StringBuilder builder = null;
		String constructedParams = null;

		for (NameValuePair nameValuePair : nameValuePairs) {
			if (nameValuePair != null && nameValuePair.getName() != null && nameValuePair.getValue() != null) {
				if (builder != null) {
					try {
						builder.append("&" + nameValuePair.getName() + "=" + URLEncoder.encode(nameValuePair.getValue(), "UTF-8"));
					} catch (UnsupportedEncodingException e) {
						// TODO: Catch error
					}
				} else {
					builder = new StringBuilder();
					try {
						builder.append(nameValuePair.getName() + "=" + URLEncoder.encode(nameValuePair.getValue(), "UTF-8"));
					} catch (UnsupportedEncodingException e) {
						// TODO: Catch error
					}
				}
			}
		}

		if (builder != null) {
			constructedParams = builder.toString();
		}

		return constructedParams;
	}

}