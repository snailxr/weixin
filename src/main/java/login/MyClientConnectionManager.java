package login;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;

import login.MySSLSocketFactory;


public class MyClientConnectionManager {
	public static HttpClient getSSLInstance(HttpClient httpClient){
		ClientConnectionManager ccm = httpClient.getConnectionManager(); 
		SchemeRegistry sr = ccm.getSchemeRegistry();
		sr.register(new Scheme("https", MySSLSocketFactory.getInstance(), 443));
		httpClient =  new DefaultHttpClient(ccm, httpClient.getParams());
		return httpClient;
	}
}
