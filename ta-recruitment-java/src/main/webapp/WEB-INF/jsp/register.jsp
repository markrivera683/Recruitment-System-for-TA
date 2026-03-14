<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<html>
<head>
  <title>Register - TA Recruitment</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/app.css" />
</head>
<body>
<div class="card">
  <h1>TA Recruitment</h1>
  <h2>Register</h2>
  <% String error = (String) request.getAttribute("error"); if (error != null) { %>
  <div class="alert"><%= error %></div>
  <% } %>
  <form method="post" action="${pageContext.request.contextPath}/register">
    <label>Name</label>
    <input name="name" required />

    <label>Student ID</label>
    <input name="studentId" />

    <label>Email</label>
    <input name="email" type="email" required />

    <label>Password</label>
    <input name="password" type="password" required />

    <label>Confirm Password</label>
    <input name="confirm" type="password" required />

    <button type="submit">Create account</button>
  </form>
  <div class="row">
    <a href="${pageContext.request.contextPath}/login">Back to login</a>
  </div>
</div>
</body>
</html>
