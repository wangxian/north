<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.Date" %>
<html>
<head>
  <title>测试</title>
</head>
<body>
当前时间：<%= new Date() %> <br/>
user: <%= request.getAttribute("user") %><br/>
user: ${user}<br/>
</body>
</html>
