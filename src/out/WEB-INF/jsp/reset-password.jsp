<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<html>
<head>
  <title>Reset Password - TA Recruitment</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/app.css" />
</head>
<body>
<div class="card">
  <h1>Reset password</h1>
  <% String msg = (String) request.getAttribute("message"); if (msg != null) { %>
    <div class="info"><%= msg %></div>
  <% } %>
  <form method="post" action="${pageContext.request.contextPath}/reset-password">
    <label>New Password</label>
    <input name="password" type="password" required />
    <label>Confirm Password</label>
    <input name="confirm" type="password" required />
    <button type="submit">Set new password</button>
  </form>
  <div class="row">
    <a href="${pageContext.request.contextPath}/login">Back to login</a>
  </div>
</div>
</body>
</html>
