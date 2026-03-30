<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>Forgot Password — TA Recruitment</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/app.css" />
</head>
<body>
<div class="page">
  <div class="container">

    <!-- Header -->
    <div class="page-header">
      <div class="logo-wrap">
        <div class="logo">
          <svg viewBox="0 0 24 24"><path d="M22 10v6M2 10l10-5 10 5-10 5z"/><path d="M6 12v5c3 3 9 3 12 0v-5"/></svg>
        </div>
      </div>
      <h1 class="page-title">Forgot Password</h1>
      <p class="page-subtitle">Enter your email to receive a reset link</p>
    </div>

    <!-- Card -->
    <div class="card">
      <% String message = (String) request.getAttribute("message"); %>
      <% if (message != null) { %>
        <!-- Success state -->
        <div class="icon-circle icon-circle--green">
          <svg viewBox="0 0 24 24"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
        </div>
        <div style="text-align:center;">
          <h2 style="font-size:1.0625rem;font-weight:600;color:#0f172a;margin-bottom:.5rem;">Check Your Inbox</h2>
          <p style="font-size:.875rem;color:#64748b;line-height:1.6;margin-bottom:1.25rem;"><%= message %></p>
        </div>
        <div class="card-footer">
          <a class="back-link" href="${pageContext.request.contextPath}/login">
            <svg viewBox="0 0 24 24"><polyline points="15 18 9 12 15 6"/></svg>
            Back to Login
          </a>
        </div>
      <% } else { %>
        <!-- Form state -->
        <div class="icon-circle icon-circle--blue">
          <svg viewBox="0 0 24 24"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/><polyline points="22,6 12,13 2,6"/></svg>
        </div>
        <p style="font-size:.875rem;color:#64748b;text-align:center;margin-bottom:1.25rem;line-height:1.6;">
          We'll send a password reset link to your registered university email address.
        </p>
        <form class="form" method="post" action="${pageContext.request.contextPath}/forgot-password">
          <div class="field">
            <label for="email">Email Address</label>
            <input id="email" name="email" type="email" placeholder="student@university.edu" required />
          </div>
          <button type="submit" class="btn btn-primary">Send Reset Link</button>
        </form>
        <div class="card-footer">
          <a class="back-link" href="${pageContext.request.contextPath}/login">
            <svg viewBox="0 0 24 24"><polyline points="15 18 9 12 15 6"/></svg>
            Back to Login
          </a>
        </div>
      <% } %>
    </div>

  </div>
</div>
</body>
</html>
