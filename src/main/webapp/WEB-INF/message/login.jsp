<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ include file="../../common/header.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html;charset=UTF-8">
<title>微信发送页面</title>
</head>
<script type="text/javascript">
	function clear() {
		$("#image").attr("src", "<c:url value="/weixin/verficationCode.html"/>");
	}
</script>
<body>
	<form id="sendMessageForm" action="<c:url value="/weixin/main.html"/>"
		method="post">
		<table>
			<tr>
				<td colspan="3">用户名:<input type="text" id="username"
					name="username">
				</td>
			</tr>
			<tr>
				<td colspan="3">密&nbsp;&nbsp;码:<input type="password" id="pwd"
					name="pwd">
				</td>
			</tr>
			<tr>
				<td>验证码:<input type="text" id="imagecode" size="2"
					name="imagecode">
				</td>
				<td><a href="javascript:clear()" style="text-decoration: none;">
						<img id="image"
						src="<c:url value="/weixin/verficationCode.html"/>" width="100"
						height="40" />
				</a></td>
			</tr>
			<tr>
			</tr>
			<tr>
				<td></td>
				<td style="padding-left: 57px"><input type="submit" value="登录">
				</td>
			</tr>
		</table>
	</form>
</body>
</html>