<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>Login — TA Recruitment</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/app.css" />
</head>
<body>
<div class="page fade-in">
  <div class="container">

    <!-- Header -->
    <div class="page-header">
      <div class="logo-wrap">
        <div class="logo">
          <svg viewBox="0 0 24 24"><path d="M22 10v6M2 10l10-5 10 5-10 5z"/><path d="M6 12v5c3 3 9 3 12 0v-5"/></svg>
        </div>
      </div>
      <h1 class="page-title">TA Recruitment System</h1>
      <p class="page-subtitle">Sign in to your account</p>
    </div>

    <!-- Card -->
    <div class="card">
      <% String error = (String) request.getAttribute("error"); if (error != null) { %>
        <div class="alert alert-error"><%= error %></div>
      <% } %>

      <form class="form" method="post" action="${pageContext.request.contextPath}/login">
        <div class="field">
          <label for="email">Email</label>
          <input id="email" name="email" type="email" placeholder="student@university.edu" required />
        </div>

        <div class="field">
          <label for="password">Password</label>
          <input id="password" name="password" type="password" placeholder="Enter your password" required />
        </div>

        <div style="display:flex;justify-content:flex-end;padding-top:.25rem;">
          <a class="forgot-link" href="${pageContext.request.contextPath}/forgot-password">Forgot password?</a>
        </div>

        <button type="submit" class="btn btn-primary">Sign in</button>
      </form>

      <div class="card-footer">
        <p class="card-footer-text">
          Don't have an account?
          <a href="${pageContext.request.contextPath}/register" style="font-weight:600;">Register here</a>
        </p>
      </div>
    </div>

  </div>
</div>
</body>
</html>
