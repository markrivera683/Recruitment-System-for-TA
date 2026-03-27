<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<html>
<head>
  <title>Forgot Password - TA Recruitment</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/app.css" />
</head>
<body>
<div class="card">
  <h1>Forgot password</h1>
  <% String msg = (String) request.getAttribute("message"); if (msg != null) { %>
    <div class="info"><%= msg %></div>
  <% } %>
  <form method="post" action="${pageContext.request.contextPath}/forgot-password">
    <label>Email</label>
    <input name="email" type="email" required />
    <button type="submit">Request reset</button>
  </form>
  <div class="row">
    <a href="${pageContext.request.contextPath}/login">Back to login</a>
  </div>
</div>
</body>
</html>
