/*
 * Copyright (c) 2015, Christian Motika. Dedicated to Sara.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 * all contributors, this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, an acknowledgment to all contributors, this list of conditions
 * and the following disclaimer in the documentation and/or other materials
 * provided with the distribution.
 * 
 * 3. Neither the name Delphino CryptSecure nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * 4. Free or commercial forks of CryptSecure are permitted as long as
 *    both (a) and (b) are and stay fulfilled: 
 *    (a) This license is enclosed.
 *    (b) The protocol to communicate between CryptSecure servers
 *        and CryptSecure clients *MUST* must be fully conform with 
 *        the documentation and (possibly updated) reference 
 *        implementation from cryptsecure.org. This is to ensure 
 *        interconnectivity between all clients and servers. 
 * 
 * THIS SOFTWARE IS PROVIDED BY THE CONTRIBUTORS �AS IS� AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, 
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, 
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 */
package org.cryptsecure;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.util.Log;

/**
 * The HttpStringRequest class is responsible for making HTTP GET and POST
 * requests in order to communicate with a HTTP server.
 * 
 * @author Christian Motika
 * @since 1.2
 * @date 08/23/2015
 * 
 */
public class HttpStringRequest {

	/** The listener for the server response. */
	private OnResponseListener listener;

	// -------------------------------------------------------------------------

	/**
	 * The listener interface for receiving onResponse events. The class that is
	 * interested in processing a onResponse event implements this interface,
	 * and the object created with that class is registered with a component
	 * using the component's <code>addOnResponseListener<code> method. When
	 * the onResponse event occurs, that object's appropriate
	 * method is invoked.
	 * 
	 * @see OnResponseEvent
	 */
	public interface OnResponseListener {

		/**
		 * Response of the server as String.
		 * 
		 * @param response
		 *            the response
		 */
		void response(String response);
	}

	// -------------------------------------------------------------------------

	/**
	 * Sets the on response listener.
	 * 
	 * @param responseListener
	 *            the new on response listener
	 */
	public void setOnResponseListener(OnResponseListener responseListener) {
		listener = responseListener;
	}

	// -------------------------------------------------------------------------

	/**
	 * Instantiates a new http get string request.
	 * 
	 * @param context
	 *            the context
	 * @param url
	 *            the url
	 * @param responseListener
	 *            the response listener
	 */
	public HttpStringRequest(Context context, String url,
			OnResponseListener responseListener) {
		Log.d("communicator", "GET REQUEST: url=" + url);

		listener = responseListener;
		(new UrlThread(context, url, false)).start();
	}

	// -------------------------------------------------------------------------

	/**
	 * Instantiates a new http get or post string request.
	 * 
	 * @param context
	 *            the context
	 * @param url
	 *            the url
	 * @param responseListener
	 *            the response listener
	 */
	public HttpStringRequest(Context context, String url, boolean postRequest,
			OnResponseListener responseListener) {
		Log.d("communicator", "POST REQUEST: url=" + url);

		listener = responseListener;
		(new UrlThread(context, url, postRequest)).start();
	}

	// -------------------------------------------------------------------------

	/**
	 * The inner class UrlThread is used to do the HTTP request in a separate
	 * background thread asynchronously and NOT in the UI thread.
	 */
	class UrlThread extends Thread {

		/** The post request. */
		boolean postRequest = false;

		/** The context. */
		Context context;

		/** The url. */
		String url;

		// ------------------------------------------------

		/**
		 * Instantiates a new url thread.
		 * 
		 * @param context
		 *            the context
		 * @param url
		 *            the url
		 */
		public UrlThread(Context context, String url, boolean postRequest) {
			super();
			this.context = context;
			this.url = url;
			this.postRequest = postRequest;
		}

		// ------------------------------------------------

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Thread#run()
		 */
		public void run() {
			String response;
			try {
				response = getData(context, url, postRequest);
				listener.response(response);
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	// -------------------------------------------------------------------------

	/**
	 * Gets the data method should NOT be used directly as it blocks the current
	 * thread until data is received. It is used internally by the UrlThread.
	 * 
	 * @param context
	 *            the context
	 * @param url
	 *            the url
	 * @return the data
	 * @throws ClientProtocolException
	 *             the client protocol exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static String getData(Context context, String url,
			boolean postRequest) throws ClientProtocolException, IOException {
		// Create a new HttpClient
		HttpClient httpclient = new DefaultHttpClient();
		String returnValue = "";

		if (!postRequest) {
			HttpGet request = new HttpGet();
			try {
				URI website = new URI(url);
				request.setURI(website);
				HttpResponse response = httpclient.execute(request);
				returnValue = getContentAsString(response);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			String urlWithParams[] = url.split("\\?");
			boolean hasUrl = (urlWithParams != null)
					&& (urlWithParams.length > 0);
			if (!hasUrl) {
				return "";
			}
			String postUrl = urlWithParams[0];
			boolean hasParams = urlWithParams.length > 1;

			//postUrl = "http://www.cryptsecure.com/";
			
			Log.d("communicator", "POST REQUEST: URL=" + postUrl);

			HttpPost request = new HttpPost();
			try {
				URI website = new URI(postUrl);
				request.setURI(website);

				if (hasParams) {
					String urlValues[] = urlWithParams[1].split("&");
					int valuesCnt = urlValues.length;

					Log.d("communicator", "POST REQUEST: valuesCnt="
							+ valuesCnt);

					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
							valuesCnt);

					for (int i = 0; i < valuesCnt; i++) {
						String nameAndValue = urlValues[i];
						if (nameAndValue.contains("=")) {
							String[] values = nameAndValue.split("=");
							String name = values[0];
							String value = Utility.urlDecode(values[1]);
							Log.d("communicator", "POST REQUEST: PAIR[" + i
									+ "] name=" + name + ", val=" + value);
							nameValuePairs.add(new BasicNameValuePair(name,
									value));
						}
					}

//					nameValuePairs.add(new BasicNameValuePair("cmd",
//							"test"));

					
					try {
						HttpEntity entity = new UrlEncodedFormEntity(
								nameValuePairs);
						request.setEntity(entity);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				HttpResponse response = httpclient.execute(request);
				Log.d("communicator",
						"POST RESPONSE: response=" + response.toString());

				// returnValue = getContent(response);
				returnValue = getContentAsString(response);

				Log.d("communicator", "POST RESPONSE: returnValue="
						+ returnValue);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return returnValue;
	}

	// -------------------------------------------------------------------------

	/**
	 * Convert the content of a HTTP response into a string.
	 * 
	 * @param response
	 *            the response
	 * @return the content as string
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private static String getContentAsString(HttpResponse response)
			throws IOException {
		String returnString = "";
		HttpEntity httpEntity = response.getEntity();

		InputStream inputStream = httpEntity.getContent();
		InputStreamReader is = new InputStreamReader(inputStream, "ISO-8859-1"); // "UTF-8");

		if (is != null) {
			Writer writer = new StringWriter();

			char[] buffer = new char[1024];
			try {
				Reader reader = new BufferedReader(is);
				int n;
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
				}
			} finally {
				is.close();
			}

			returnString = writer.toString().replace("\n", "");
		}

		return returnString;
	}

	// -------------------------------------------------------------------------

}
