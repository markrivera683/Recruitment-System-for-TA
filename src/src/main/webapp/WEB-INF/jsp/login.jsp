<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<html>
<head>
  <title>Login - TA Recruitment</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/app.css" />
</head>
<body>
<div class="card">
  <h1>TA Recruitment</h1>
  <h2>Login</h2>
  <% String error = (String) request.getAttribute("error"); if (error != null) { %>
    <div class="alert"><%= error %></div>
  <% } %>
  <form method="post" action="${pageContext.request.contextPath}/login">
    <label>Email</label>
    <input name="email" type="email" required />

    <label>Password</label>
    <input name="password" type="password" required />

    <button type="submit">Log in</button>
  </form>
  <div class="row">
    <a href="${pageContext.request.contextPath}/register">Create account</a>
    <a href="${pageContext.request.contextPath}/forgot-password">Forgot password?</a>
  </div>
</div>
</body>
</html>
