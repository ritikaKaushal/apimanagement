package com.ibm.cnxdevs.cloud.apimanagement.sample;

/*
 *  This samples illustrates a way how an application can respond to Throttling event (HTTP status code 429 ).
 *  429 means "Too many requests" (https://en.wikipedia.org/wiki/List_of_HTTP_status_codes)
 *  
 *  This sample leverages Exponential backoff algorithm to increases the duration of wait between each retry attempt.
 *  To read more : https://en.wikipedia.org/wiki/Exponential_backoff
 * 
 */
import java.net.URI;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import com.ibm.cnxdevs.cloud.apimanagement.sample.util.Prop;
import com.ibm.cnxdevs.cloud.apimanagement.sample.util.SSLUtil;

public class APIThrottlingSample {
	public String serverUrl = Prop.serverUrl;
	public String requestUrl =Prop.apiUrl;
	HttpClient httpClient;

	public final int MAX_RETRY = 6;
	public final int THROTTLING_STATUS_CODE = 429;
	public final int MAX_WAIT = 60000;

	public static void main(String[] args) {
		
		String username = Prop.username;
		String password = Prop.password;
		// Ensure right credentials have been updated in properties file.

		APIThrottlingSample sampleApp = new APIThrottlingSample();
		for (int i = 0; i < 10; i++) {
			sampleApp.apiRequests(username, password);
		}
	}

	/*
	 * Method responsible for making api requests
	 * Throttling events are handled using exponential backoff algorithm for illustration.
	 */
	public void apiRequests(String username, String password) {
		try {
			   int retryCount = 0;
			   boolean retry = false;

				prepareHttpClient(username, password);
				HttpGet getMethod = new HttpGet();
			getMethod.setHeader("Authorization",
					"Basic " + createAuthHeader(username, password));
			getMethod.setURI(new URI(serverUrl.concat(requestUrl)));
			do {
				  if (retry == true) {
					  //Back off period increase exponentially with each retry attempt.
					  long waitTime=getWaitTime(retryCount);
					 System.out.println("Waiting for "+ waitTime+ "ms before Retry");
					 Thread.sleep(waitTime); 
				   }
				 HttpResponse response = httpClient.execute(getMethod);
				 getMethod.releaseConnection();
				 System.out.println("Server response was "+ response.getStatusLine().getStatusCode());
				 if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					retry = false;
				 }
				 else if (response.getStatusLine().getStatusCode() == THROTTLING_STATUS_CODE) {
					 if (retryCount <= MAX_RETRY) {
						retry = true;
					 } else {
						// Since maximum throttling window is 60 secs, program waits for 60 secs before making future calls.
						Thread.sleep(MAX_WAIT);
						retry = true;
						retryCount = 0;
					}
				 }
				 retryCount++;
			   } while (retry);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	/*
	 * Returns the next wait interval, in milliseconds
	 */
	public static long getWaitTime(int retryCount) {

	    long waitTime = ((long) Math.pow(retryCount, 2) * 1000L);
	    return waitTime;
	}
	
	private HttpClient prepareHttpClient(String username, String password) {
		PoolingClientConnectionManager cm = new PoolingClientConnectionManager();
		httpClient = new DefaultHttpClient(cm);
		SSLUtil.wrapHttpClient((DefaultHttpClient) httpClient);
		return httpClient;
	}

	private static String createAuthHeader(String username, String password) {
		String authString = username + ":" + password;
		byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
		String authStringEnc = new String(authEncBytes);
		return authStringEnc;
	}

}
