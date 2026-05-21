<%@ page contentType="text/html;charset=UTF-8" %>
<%
  String ctx = request.getContextPath();
  if (ctx == null) ctx = "";
  response.sendRedirect(ctx + "/login");
%>
