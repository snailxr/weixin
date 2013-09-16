
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ include file="../../common/header.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" 

"http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=GB18030">
<title>微信发送页面</title>
</head>
<script type="text/javascript">
	$(function() {
		$('#groups').datagrid({
			onClickRow : function(rowInex, rowData) {
				var queryParams = $('#friends').datagrid('options').queryParams;
				queryParams.groupId = rowData.id;
				$('#friends').datagrid('reload');
			}
		});
	});
	function sendMessage() {
		var rows = $("#friends").datagrid('getChecked');
		var content = $("#messageContext").val();
		var textOrImage = $("input[name='textOrImage']:checked").val();
		var imageId = $("#imageId").val();
		$('#sendButton').attr('disabled', 'disabled');
		if (rows.length > 0) {
			var ids = [];
			for ( var i = 0; i < rows.length; i++) {
				ids.push(rows[i].fakeId);
			}
			if (textOrImage == 'image' && ($('#imageId').val() == null || $('#imageId').val() == '')) {
				alert('发送图文消息时,图文id不能为空');
				$('#sendButton').removeAttr('disabled');
			} else {
				$.ajax({
					type : "POST",
					dataType : "JSON",
					url : "<c:url value="/weixin/sendMeg.html"/>",
					data : {
						ids : ids.join(','),
						content : content,
						textOrImage : textOrImage,
						imageId : imageId
					},
					success : function(data) {
						if (data.flag) {
							alert('发送成功');
							$('#messageContext').val('');
							$('#sendButton').removeAttr('disabled');
						} else {
							alert('发送失败，图片id不正确');
							$('#messageContext').val('');
							$('#sendButton').removeAttr('disabled');
						}
					}
				});
			}
		} else {
			alert('请选择要发送的好友');
			$('#sendButton').removeAttr('disabled');
		}
	}
</script>
<body class="easyui-layout">
	<!--1.1 egion="north"，指明高度，可以自适应-->
	<div region="north" style="height: 80px;">
		<center>
			<h1>发送微信信息</h1>
		</center>
	</div>

	<!--1.2 region="west",必须指明宽度-->
	<div region="west" title="导航菜单" split="true" style="width: 220px">
		<table id="groups" class="easyui-datagrid"
			style="width: auto; height: auto"
			data-options="singleSelect:true,collapsible:true,url:'<c:url value="/weixin/groups.html"/>',method:'get'">
			<thead>
				<tr>
					<th data-options="field:'id',width:210,hidden:true">ID</th>
					<th data-options="field:'name',width:210,align:'center'">好友分组</th>
				</tr>
			</thead>
		</table>
	</div>

	<!--1.3region="center",这里的宽度和高度都是由周边决定，不用设置-->
	<div region="center">
		<div id="cc" class="easyui-layout" fit="true">
			<div region="north" split="true" style="height: 400px;">
				<div fit="true" style="padding-left: 250px; padding-top: 15px">
					<form id="sendMessageForm" method="post">
						<table>
							<tr>
								<td colspan="3"><textarea id="messageContext" rows="10"
										cols="40"></textarea></td>
							</tr>
							<tr>
								<td><input type="radio" name="textOrImage" value="text"
									checked>文字 <input type="radio" name="textOrImage"
									value="image">图文&nbsp;</td>
								<td>图文id<input type="text" id="imageId" name="imageId"
									size="8">
								</td>
								<td><input id="sendButton" type="button"
									onclick="javascript:sendMessage()" value="发送"></td>
							</tr>
						</table>
					</form>
				</div>
			</div>
		</div>
	</div>

	<!--1.4 region="east",必须指明宽度-->
	<div region="east" style="width: 280px;">
		<table id="friends" class="easyui-datagrid"
			style="width: auto; height: auto"
			data-options="rownumbers:true,singleSelect:false,url:'<c:url value="/weixin/friends.html"/>',method:'get'">
			<thead>
				<tr>
					<th data-options="field:'fakeId',checkbox:true"></th>
					<th data-options="field:'nickName',width:100">昵称</th>
					<th data-options="field:'remarkName',width:100">备注名称</th>
				</tr>
			</thead>
		</table>
	</div>

	<!--1.5 region="south"，指明高度，可以自适应-->
	<div region="south" style="height: 50px;">
		<center>
			<h3></h3>
		</center>
	</div>
</body>
</html>
