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
import java.util.Random;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.util.EntityUtils;

import com.ibm.cnxdevs.cloud.apimanagement.sample.util.Prop;
import com.ibm.cnxdevs.cloud.apimanagement.sample.util.SSLUtil;


/*
 * Sample demonstrates usage App registration api to register internal applications
 * It includes sample code for registering application, fetching details like application id and appkey
 * Before executing ensure
 * 1. You have update right value for username and password variables (credentials). 
 * 	  These should belong to user register with developer or admin role.
 */

public class AppRegistrationSample {
	public String ServerUrl = Prop.serverUrl;
	public String AppRegistrationURL = "/api/bss/resource/servicecomponent";
	public String RegistrationUrl = ServerUrl + AppRegistrationURL;
	HttpClient httpclient;
	
	public static void main(String[] args) {
		// change the values of below variables with right username and password, works with developer or admin user type
		String username = Prop.username;
		String password = Prop.password;

		AppRegistrationSample sampleApp = new AppRegistrationSample();
		sampleApp.prepareHttpClient(username, password);
		sampleApp.registerApplication("appstest"+new Random().nextInt(100), username, password);
	}
	
	public void registerApplication(String applicationName, String username, String password) {
		String appId = "";
		String appKey = "";
		try {
			HttpPost postMethod = new HttpPost();
			postMethod.setURI(new URI(RegistrationUrl));
			postMethod.setHeader("Authorization", "Basic " + createAuthHeader(username, password));
			String registerAppContent = createPostBody(applicationName);
			HttpEntity appEntity = new ByteArrayEntity(registerAppContent.getBytes("UTF-8"));
			postMethod.setEntity(appEntity);
			HttpResponse response = httpclient.execute(postMethod);
			System.out.println(" Response Code : "+ response.getStatusLine().getStatusCode() + ", Message : "+ response.getStatusLine().getReasonPhrase());
			String result = EntityUtils.toString(response.getEntity()).trim();
			System.out.println(" response appid ::: " + result);
			appId = result.substring(8, result.length() - 1).trim();
			System.out.println("Application id of the new registered app whose name is "+ applicationName + " is " + appId);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			HttpGet getMethod = new HttpGet();
			getMethod.setURI(new URI(RegistrationUrl));
			getMethod.setHeader("Authorization", "Basic " + createAuthHeader(username, password));
			HttpResponse response = httpclient.execute(getMethod);	
			String result = EntityUtils.toString(response.getEntity()).trim();
			System.out.println("AppKey of the new registered app is "+extractAppKey(result, appId));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private String createPostBody(String applicationName) {
		return "{\"ServiceComponent\": {\"Description\": \"" + applicationName + "\",\"DisplayName\": \"" + applicationName +"\",\"ServiceComponentAttributeSet\":[{\"LongValue\": 1,\"BooleanValue\":true,\"StringValue\":\"\",\"Name\":\"basic_auth_flow_supported\",\"DisplayPrecedence\":0,\"DisplayName\":\"basic_auth_flow_supported\",\"Type\":\"BOOLEAN\"}]}}";
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

	public String extractAppKey(String response, String appid){
		int position = response.indexOf(appid)+1;
		response = response.substring(position+appid.length());
		position = response.indexOf(",");
		response = response.substring(0,position);
		response = response.substring(("\"Name\":\"").length(),response.length()-1);
		return response;
	}

}
