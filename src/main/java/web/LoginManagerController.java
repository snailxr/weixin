package web;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import login.MyClientConnectionManager;
import md5.MD5;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.apache.http.Consts;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageDecoder;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

@Controller
public class LoginManagerController {
	// 登录地址
	public static final String LOGIN_URL = "https://mp.weixin.qq.com/cgi-bin/login?lang=zh_CN";

	@RequestMapping("/weixin/main")
	public String weixinFormLogin(HttpServletRequest request, HttpServletResponse response) throws ClientProtocolException, IOException {
		String username = request.getParameter("username");
		String pwd = request.getParameter("pwd");
		String imageCode = request.getParameter("imagecode");
		login(username, pwd, imageCode, request);
		return "send_message_page";
	}

	@RequestMapping("/index")
	public String indexPage() throws ClientProtocolException, IOException {
		return "login";
	}

	@RequestMapping("/weixin/verficationCode")
	public void getLoginInfo(HttpServletRequest request, HttpServletResponse response) throws ClientProtocolException, IOException {
		String temp = null;
		HttpGet httpGet = getHttpGetImage("https://mp.weixin.qq.com/cgi-bin/verifycode?username=h380050543@126.com&r=1378432610850");
		DefaultHttpClient httpClient = new DefaultHttpClient();
		httpClient = (DefaultHttpClient) (new MyClientConnectionManager().getSSLInstance(httpClient));
		HttpResponse httpresponse = httpClient.execute(httpGet);
		HeaderIterator headItr = httpresponse.headerIterator("Set-Cookie");
		String sig = null;
		while (headItr.hasNext()) {
			temp = headItr.next().toString();
			sig = temp.substring(12);
		}
		if (sig != null) {
			request.getSession().setAttribute("SIG", sig);
		}
		HttpEntity entity = httpresponse.getEntity();
		response.setContentType("image/jpeg;charset=utf-8");
		ServletOutputStream output = response.getOutputStream();
		InputStream is = null;
		is = entity.getContent();
		JPEGImageDecoder decoder = JPEGCodec.createJPEGDecoder(is);
		BufferedImage image = decoder.decodeAsBufferedImage();
		JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(output);
		encoder.encode(image);
		httpGet.abort();
	}

	@RequestMapping("/weixin/getFriends")
	public void getFriendsById(HttpServletRequest request, HttpServletResponse response) throws ClientProtocolException, IOException {
		String groupId = request.getParameter("id");
		String messageUrl = "https://mp.weixin.qq.com/cgi-bin/contactmanage?t=user/index&pagesize=1000&pageidx=0&type=0&groupid=" + groupId
				+ "&token=" + request.getSession().getAttribute("TOKEN") + "&lang=zh_CN";
		Map<String, Object> m = new HashMap<String, Object>();
		Map<String, Object> friends;
		m.put("flag", true);
		outputJson(response, m);
	}

	@RequestMapping("/weixin/sendMeg")
	private void sendMegs(HttpServletRequest request, HttpServletResponse response) throws ClientProtocolException, IOException {
		String ids = request.getParameter("ids");
		String content = request.getParameter("content");
		String textOrImage = request.getParameter("textOrImage");
		String imageId = request.getParameter("imageId");
		System.out.println("------------------------->" + imageId);
		String id[] = ids.split(",");
		Map<String, Object> m = new HashMap<String, Object>();
		for (int i = 0; i < id.length; i++) {
			String fakeId = id[i];
			if (textOrImage.equals("text")) {
				sendAMsg(fakeId, content, (String) request.getSession().getAttribute("TOKEN"), request);
				m.put("flag", true);
			} else {
				if (sendAMsgImage(fakeId, content, imageId, (String) request.getSession().getAttribute("TOKEN"), request)) {
					m.put("flag", true);
				} else {
					m.put("flag", false);
				}
			}
		}
		outputJson(response, m);
	}

	private void setCookieToHttpClient(DefaultHttpClient httpClient, HttpServletRequest request) {
		CookieStore cookieStore = (CookieStore) request.getSession().getAttribute("COOKIE_STORE");
		httpClient.setCookieStore(cookieStore);
	}

	private boolean sendAMsgImage(String fakeId, String content, String imageId, String token, HttpServletRequest request)
			throws ClientProtocolException, IOException {
		// URL 方法 结果 类型 已接收 已花费 发起程序 等候‎‎ 开始‎‎ 请求‎‎ 响应‎‎ 已读取缓存‎‎ 差距‎‎
		HttpPost post = getHttpPost("https://mp.weixin.qq.com/cgi-bin/singlesend?t=ajax-response&lang=zh_CN");
		// Referer
		post.addHeader("Referer", "https://mp.weixin.qq.com/cgi-bin/singlemsgpage?" + "token=" + token
				+ "&fromfakeid=2381890080&msgid=&source=&count=20&t=wxm-singlechat&lang=zh_CN");
		// 参数列表
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("ajax", 1 + ""));
		nvps.add(new BasicNameValuePair("appmsgid", imageId + ""));
		nvps.add(new BasicNameValuePair("content", ""));
		nvps.add(new BasicNameValuePair("error", "false"));
		nvps.add(new BasicNameValuePair("tofakeid", fakeId));
		nvps.add(new BasicNameValuePair("token", token));
		nvps.add(new BasicNameValuePair("type", "10"));
		post.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));
		DefaultHttpClient httpClient = new DefaultHttpClient();
		httpClient = (DefaultHttpClient) (new MyClientConnectionManager().getSSLInstance(httpClient));
		setCookieToHttpClient(httpClient, request);
		HttpResponse response = httpClient.execute(post);
		HttpEntity entity = response.getEntity();
		if (getImageRet(getReplyContent(entity)).get("msg").equals("system error")) {
			post.abort();
			return false;
		} else {
			post.abort();
			return true;
		}
	}

	private void sendAMsg(String fakeId, String content, String token, HttpServletRequest request) throws ClientProtocolException, IOException {
		HttpPost post = getHttpPost("https://mp.weixin.qq.com/cgi-bin/singlesend?t=ajax-response&lang=zh_CN");
		// Referer
		post.addHeader("Referer", "https://mp.weixin.qq.com/cgi-bin/singlemsgpage?" + "token=" + token
				+ "&fromfakeid=2381890080&msgid=&source=&count=20&t=wxm-singlechat&lang=zh_CN");
		// 参数列表
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("type", 1 + ""));
		nvps.add(new BasicNameValuePair("content", content));
		nvps.add(new BasicNameValuePair("error", "false"));
		nvps.add(new BasicNameValuePair("tofakeid", fakeId));
		nvps.add(new BasicNameValuePair("token", token));
		nvps.add(new BasicNameValuePair("ajax", 1 + ""));
		post.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));
		DefaultHttpClient httpClient = new DefaultHttpClient();
		httpClient = (DefaultHttpClient) (new MyClientConnectionManager().getSSLInstance(httpClient));
		setCookieToHttpClient(httpClient, request);
		HttpResponse response = httpClient.execute(post);
		post.abort();
	}

	private Map<String, Object> getAllFriends(String token, HttpServletRequest request, String groupId) throws ClientProtocolException, IOException {
		String messageUrl = "https://mp.weixin.qq.com/cgi-bin/contactmanage?t=user/index&pagesize=1000&pageidx=0&type=0&groupid=" + groupId
				+ "&token=" + token + "&lang=zh_CN";
		HttpGet httpget = getHttpGetText(messageUrl);
		DefaultHttpClient httpClient = new DefaultHttpClient();
		httpClient = (DefaultHttpClient) (new MyClientConnectionManager().getSSLInstance(httpClient));
		setCookieToHttpClient(httpClient, request);
		HttpResponse httpResponse = httpClient.execute(httpget);

		HttpEntity entity = httpResponse.getEntity();

		String reg = ".*(friendsList.*\\.contacts).*";
		Pattern pattern1 = Pattern.compile(reg);
		Matcher m = pattern1.matcher(getReplyContent(entity));
		String result = "";
		while (m.find()) {
			result = m.group(1);
		}
		if (result.length() > 0) {
			result = result.substring(result.indexOf("["), result.lastIndexOf("]") + 1);
		} else {
			return null;
		}
		JSONArray array = JSONArray.fromObject(result);
		List<Map<String, String>> friends = new ArrayList<Map<String, String>>();
		for (int i = 0; i < array.size(); i++) {
			JSONObject json = JSONObject.fromObject(array.get(i));
			Map<String, String> params = new HashMap<String, String>();
			params.put("fakeId", json.get("id") + "");
			params.put("nickName", json.get("nick_name") + "");
			params.put("remarkName", json.get("remark_name") + "");
			params.put("groupId", json.get("group_id") + "");
			friends.add(params);
		}
		Map<String, Object> jsonMap = this.convertToDataGridJasonMap(friends, array.size());
		// System.out.println("friends:"+friends);
		return jsonMap;
	}

	public Map<String, Object> getAllGroups(String token, HttpServletRequest request) throws ClientProtocolException, IOException {
		String messageUrl = "https://mp.weixin.qq.com/cgi-bin/contactmanage?t=user/index&pagesize=1000&pageidx=0&type=0&groupid=0&token=" + token
				+ "&lang=zh_CN";
		DefaultHttpClient httpClient = new DefaultHttpClient();
		httpClient = (DefaultHttpClient) (new MyClientConnectionManager().getSSLInstance(httpClient));
		setCookieToHttpClient(httpClient, request);
		HttpGet httpget = getHttpGetText(messageUrl);
		HttpResponse httpResponse = httpClient.execute(httpget);

		HttpEntity entity = httpResponse.getEntity();

		String reg = ".*(groupsList.*\\.groups).*";
		Pattern pattern1 = Pattern.compile(reg);
		Matcher m = pattern1.matcher(getReplyContent(entity));
		String result = "";
		while (m.find()) {
			result = m.group(1);
		}
		if (result.length() > 0) {
			result = result.substring(result.indexOf("["), result.lastIndexOf("]") + 1);
		} else {
			return null;
		}
		JSONArray array = JSONArray.fromObject(result);
		// System.out.println(array.size());
		List<Map<String, String>> groups = new ArrayList<Map<String, String>>();
		for (int i = 0; i < array.size(); i++) {
			JSONObject json = JSONObject.fromObject(array.get(i));
			Map<String, String> params = new HashMap<String, String>();
			params.put("id", json.get("id") + "");
			params.put("name", json.get("name") + "");
			params.put("cnt", json.get("cnt") + "");
			groups.add(params);
		}
		Map<String, Object> jsonMap = this.convertToDataGridJasonMap(groups, array.size());
		// System.out.println("groups:"+groups);
		return jsonMap;
	}

	@RequestMapping("/weixin/groups")
	public void groups(HttpServletRequest request, HttpServletResponse response) throws IOException {
		Map<String, Object> groups = getAllGroups((String) request.getSession().getAttribute("TOKEN"), request);
		outputJson(response, groups);
	}

	@RequestMapping("/weixin/friends")
	public void friends(HttpServletRequest request, HttpServletResponse response, String groupId) throws IOException {
		if (StringUtils.isBlank(groupId)) {
			groupId = "0";
		}
		Map<String, Object> friends = getAllFriends((String) request.getSession().getAttribute("TOKEN"), request, groupId);
		outputJson(response, friends);
	}

	public Map<String, Object> convertToDataGridJasonMap(List<Map<String, String>> list, int size) {
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		jsonMap.put("rows", list);
		jsonMap.put("total", size);
		return jsonMap;
	}

	public void outputJson(HttpServletResponse response, Object obj) throws IOException {
		String jsonString = JSON.toJSONStringWithDateFormat(obj, "yyyy-MM-dd HH:mm:ss", SerializerFeature.DisableCircularReferenceDetect);
		response.setContentType("text/html;charset=UTF-8");
		response.setCharacterEncoding("UTF-8");
		PrintWriter pw = response.getWriter();
		pw.write(jsonString);
		pw.flush();
		pw.close();
	}

	/**
	 * 模拟微信公众平台登录
	 * 
	 * @author hudq
	 * @param userName
	 * @param passWord
	 * @return 登录是否成功
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public boolean login(String userName, String passWord, String imageCode, HttpServletRequest request) throws ClientProtocolException, IOException {
		HttpPost httpPost = getHttpPost(LOGIN_URL);
		httpPost.addHeader("Referer", "https://mp.weixin.qq.com/");
		httpPost.addHeader("Cookie", (String) request.getSession().getAttribute("SIG"));
		// 请求头传递的参数
		List<NameValuePair> nvp = new ArrayList<NameValuePair>();
		nvp.add(new BasicNameValuePair("username", userName));

		// 进行md5加密
		MD5 md5 = new MD5();
		String str = md5.getMD5ofStr(passWord);
		str = str.toLowerCase();

		nvp.add(new BasicNameValuePair("pwd", str));
		nvp.add(new BasicNameValuePair("f", "json"));
		nvp.add(new BasicNameValuePair("imgcode", imageCode));
		httpPost.setEntity(new UrlEncodedFormEntity(nvp, Consts.UTF_8));
		DefaultHttpClient httpClient = new DefaultHttpClient();
		httpClient = (DefaultHttpClient) (new MyClientConnectionManager().getSSLInstance(httpClient));
		// 提交表单
		HttpResponse httpResponse = httpClient.execute(httpPost);
		// 获得返回的实体，即登录页面的html信息，根据实体判断是否登录成功
		HttpEntity entity = httpResponse.getEntity();
		// 获取登录成功后返回的cookie值以便后来的连接可保持登录状态
		CookieStore cookieStore = httpClient.getCookieStore();
		request.getSession().setAttribute("COOKIE_STORE", cookieStore);
		Map<String, String> result = getRet(getReplyContent(entity));
		if (result != null && "0".equals(result.get("ErrCode"))) {
			String token = result.get("token");
			request.getSession().setAttribute("TOKEN", token);
			return true;
		}
		return false;
	}

	/**
	 * @author hudq
	 * @throws IOException
	 *             暂不使用
	 */
	public void logout(HttpServletRequest request) throws IOException {
		HttpGet get = getHttpGetImage("http://mp.weixin.qq.com/cgi-bin/logout?t=wxm-logout&lang=zh_CN");
		DefaultHttpClient httpClient = new DefaultHttpClient();
		httpClient = (DefaultHttpClient) (new MyClientConnectionManager().getSSLInstance(httpClient));
		setCookieToHttpClient(httpClient, request);
		int status = httpClient.execute(get).getStatusLine().getStatusCode();
		if (status == org.apache.http.HttpStatus.SC_OK) {
			System.err.println("-----------注销登录成功-----------");
		}
	}

	/**
	 * 添加登录时的post的表头信息
	 * 
	 * @author hudq
	 * @param url
	 *            登录请求地址
	 * @return httpPost
	 */
	public HttpPost getHttpPost(String url) {
		HttpPost pmethod = new HttpPost(url);
		// 添加请求头信息
		pmethod.addHeader("Accept", "*/*");
		pmethod.addHeader("Host", "mp.weixin.qq.com");
		pmethod.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:23.0) Gecko/20100101 Firefox/23.0");
		pmethod.addHeader("Accept-Language", "zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3");
		pmethod.addHeader("Accept-Encoding", "gzip, deflate");
		pmethod.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
		pmethod.addHeader("X-Requested-With", "XMLHttpRequest");
		// Content-length在body非空时自动添加,不然会报org.apache.http.ProtocolException:
		// Content-Length header already present异常
		// pmethod.addHeader("Content-Length","82");
		pmethod.addHeader("Connection", "keep-alive");
		pmethod.addHeader("Pragma", "no-cache");
		pmethod.addHeader("Cache-Control", "no-cache");

		return pmethod;
	}

	/**
	 * get方法
	 * 
	 * @param url
	 * @param code
	 *            text表示accept为text/html png表示为image/png
	 * @return
	 */
	public HttpGet getHttpGetImage(String url) {
		HttpGet gmethod = new HttpGet(url);
		gmethod.addHeader("Connection", "keep-alive");
		gmethod.addHeader("Cache-Control", "no-cache");
		gmethod.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:23.0) Gecko/20100101 Firefox/23.0");
		gmethod.addHeader("Accept", "image/png,image/*;q=0.8,*/*;q=0.");
		return gmethod;
	}

	public HttpGet getHttpGetText(String url) {
		HttpGet gmethod = new HttpGet(url);
		gmethod.addHeader("Connection", "keep-alive");
		gmethod.addHeader("Cache-Control", "max-age=0");
		gmethod.addHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.0) ");
		gmethod.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/;q=0.8");

		return gmethod;
	}

	/**
	 * 获取内容为 text/html
	 * 
	 * @author hudq
	 * @param entity
	 *            登录请求后的返回实体
	 * @return 解析后的entity的内容
	 */
	public String getReplyContent(HttpEntity entity) {
		InputStream is = null;
		StringBuffer sb = new StringBuffer();
		try {
			// HttpEntity实体中的内容以流的形式存在
			is = entity.getContent();
			// 实体中内容可能过多，所以采用带有缓存形式的方式读取
			BufferedReader br = new BufferedReader(new InputStreamReader(is, "utf-8"));
			String temp = "";
			while ((temp = br.readLine()) != null) {
				sb.append(temp);
				System.out.println("*" + temp);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sb.toString();
	}

	/**
	 * 获取用户登陆返回的数据 用户登陆后返回的是json数据，解析成map
	 * 
	 * @author hudq
	 * @param json
	 * @return
	 */
	public Map<String, String> getRet(String json) {
		Map<String, String> param = new HashMap<String, String>();
		JSONObject jo = JSONObject.fromObject(json);
		int i = jo.get("ErrMsg").toString().lastIndexOf("=");
		String token = jo.get("ErrMsg").toString().substring(i + 1);
		param.put("Ret", jo.get("Ret").toString());
		param.put("ErrMsg", jo.get("ErrMsg").toString());
		param.put("ErrCode", jo.get("ErrCode").toString());
		param.put("token", token);
		return param;
	}

	public Map<String, String> getImageRet(String json) {
		Map<String, String> param = new HashMap<String, String>();
		JSONObject jo = JSONObject.fromObject(json);
		param.put("Ret", jo.get("ret").toString());
		param.put("msg", jo.get("msg").toString());
		return param;
	}
}
