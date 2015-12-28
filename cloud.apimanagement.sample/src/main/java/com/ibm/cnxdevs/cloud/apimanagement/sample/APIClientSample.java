/*
* © Copyright IBM Corp. 2015
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at:
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
* implied. See the License for the specific language governing
* permissions and limitations under the License.
*/

package com.ibm.cnxdevs.cloud.apimanagement.sample;

import java.io.IOException;
import java.net.URI;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;

import com.ibm.cnxdevs.cloud.apimanagement.sample.util.Prop;
import com.ibm.cnxdevs.cloud.apimanagement.sample.util.SSLUtil;


/*
 * Sample demonstrate usage of appkey and apim-session-id headers.
 * Before executing ensure
 * 1. You have updated right value for AppKey variable
 * 2. You have update right value for username and password variables (credentials)
 */
public class APIClientSample {
	
	public String ServerUrl = Prop.serverUrl;
	public String ApiUrl = ServerUrl + "/profiles/serviceconfigs";
	public static final String SessionIdHeader = "apim-session-id";
	// Update right appkey
	public static final String AppKey = Prop.appkey;
	public static String sessionId = "";
	HttpClient httpclient;
	
	public static void main(String[] args) {
		// change the values of below variables with right username and password.
		String username = Prop.username;
		String password = Prop.password;

	
		APIClientSample sampleApp = new APIClientSample();
		for (int i = 0; i < 12; i++) {
			sampleApp.apiRequests(username, password);
		}

	}
	

	public void apiRequests(String username, String password){
		try {
			prepareHttpClient(username, password);
			HttpGet getMethod = new HttpGet();
			getMethod.setHeader("Authorization", "Basic " + createAuthHeader(username, password));
			getMethod.setHeader("appkey", AppKey);
			if(!(sessionId.isEmpty())){
				getMethod.setHeader(SessionIdHeader, sessionId);
			}
			getMethod.setURI(new URI(ApiUrl));
			HttpResponse response = httpclient.execute(getMethod);
			System.out.println("Server response was "+response.getStatusLine().getStatusCode() );
			if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
				Header[] allHeaders = response.getAllHeaders();
				for (int i = 0; i < allHeaders.length; i++) {
					if(allHeaders[i].getName().equals(SessionIdHeader)){
						sessionId = allHeaders[i].getValue();
						System.out.println("session id was "+sessionId);
					}
				}
			} else if (response.getStatusLine().getStatusCode() == 429 || response.getStatusLine().getStatusCode() == 529){
				System.out.println("Client app throttled, wait for a minute");
				Thread.sleep(60000);
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	private HttpClient prepareHttpClient(String username, String password){
		PoolingClientConnectionManager cm = new PoolingClientConnectionManager();
		httpclient = new DefaultHttpClient(cm);
		SSLUtil.wrapHttpClient((DefaultHttpClient) httpclient);
		return httpclient;
	}
	
	
	private static String createAuthHeader(String username, String password) {
		String authString = username + ":" + password;
		byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
		String authStringEnc = new String(authEncBytes);
		return authStringEnc;
	}
}
