<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <base href="<%=basePath%>">
    
    <title>My JSP 'index.jsp' starting page</title>
	<meta http-equiv="pragma" content="no-cache"> 
	<meta http-equiv="cache-control" content="no-cache">
	<meta http-equiv="expires" content="0">    
	<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
	<meta http-equiv="description" content="This is my page">
	<!--
	<link rel="stylesheet" type="text/css" href="styles.css">
	-->
  </head>

 
  <script type="text/javascript">
  	function go(num){
  		if(num==1){
  			window.location.href="http://123.56.237.81:8007/SanYi/Portal/IndexHeZhangQuan.htm#";
  		}else if(num==2){
  			window.location.href="getQzjbInfo.do";
  		}else if(num==3){
  			window.location.href="rlist.do";
  		}else if(num==4){
  			window.location.href="login.jsp";
  		}else if(num==5){
  			window.location.href="getQzjbByOpenid.do?openid=1001";
  		}else if(num==6){
  			window.location.href="getRiverInfo.do";
  		}else if(num==7){
  			window.location.href="riverScoreTask.do";
  		}
  	}
  </script>
  <body>
  <%System.out.println("welome"); %>
    <h1 align="center">后端接口测试数据预览</h1>
     <input type="button" onclick="go(7)" value="启动服务">
    <!-- <input type="button" onclick="go(1)" value="监测数据">&nbsp;&nbsp;
     <input type="button" onclick="go(2)" value="测试接口数据一(举报圈数据)">&nbsp;&nbsp;
       <input type="button" onclick="go(5)" value="测试接口数据二（用户举报数据）">&nbsp;&nbsp;
       <input type="button" onclick="go(6)" value="测试接口数据三（河道资料信息）">&nbsp;&nbsp;
        <input type="button" onclick="go(3)" value="测试接口数据四（用户信息）">&nbsp;&nbsp;
       <input type="button" onclick="go(4)" value="测试接口数据五（用户登录）">&nbsp;&nbsp;
      
       <form action="http://localhost:8080/pjriver/sendQzjbInfo.do" method="post">
      		<h5>群众举报接口测试</h5>
      		<table>
      			<tr><td>河道编号：</td><td><input name="rid" value="1002"></td><td>rid</td><tr>
      			<tr><td>上报内容：</td><td><input name="taskcon" value="测试数据-上报内容-EE"></td><td>taskcon</td><tr>
      			<tr><td>地址：</td><td><input name="address" value="DD"></td><td>address</td><tr>
      			<tr><td>上报人：</td><td><input name="repname" value="AA"></td><td>repname</td><tr>
      			<tr><td>电话：</td><td><input name="reptel" value="18368195765"></td><td>reptel</td><tr>
      			<tr><td>事件类型：</td><td><input name="inctype" value="1"></td><td>inctype</td><tr>
      			<tr><td>图片地址：</td><td><input name="imgurl" value="imgurl/img/url"></td><td>imgurl</td><tr>
      			<tr><td>详细地址：</td><td><input name="detail" value="fafafafafad"></td><td>detail</td><tr>
      			<tr><td>办理状态：</td><td><input name="tstate" value="1"></td><td>tstate</td><tr>
      			<tr><td>定位地点：</td><td><input name="repgeom" value="119.7713 29.43331"></td>repgeom</tr>
      			<tr><td colspan="3">点击上报，次数据将保存至服务器，谨慎操作</td><tr>
      			<tr><td colspan="3" align="center"><input style="background-color: blue" type="submit" value="上报"></td></tr>
      		</table>
      		
      </form>
       -->
  </body>
</html>
