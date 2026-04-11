<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>Register — TA Recruitment</title>
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
      <h1 class="page-title">Register as TA</h1>
      <p class="page-subtitle">Create your applicant account</p>
    </div>

    <!-- Card -->
    <div class="card">
      <% String error = (String) request.getAttribute("error"); if (error != null) { %>
        <div class="alert alert-error"><%= error %></div>
      <% } %>

      <form class="form" method="post" action="${pageContext.request.contextPath}/register">
        <div class="field">
          <label for="name">Full Name</label>
          <input id="name" name="name" type="text" placeholder="John Doe" required />
        </div>

        <div class="field">
          <label for="studentId">Student ID</label>
          <input id="studentId" name="studentId" type="text" placeholder="123456789" />
        </div>

        <div class="field">
          <label for="email">Email</label>
          <input id="email" name="email" type="email" placeholder="student@university.edu" required />
        </div>

        <div class="field">
          <label for="password">Password</label>
          <input id="password" name="password" type="password" placeholder="Enter your password" required />
        </div>

        <div class="field">
          <label for="confirm">Confirm Password</label>
          <input id="confirm" name="confirm" type="password" placeholder="Confirm your password" required />
        </div>

        <button type="submit" class="btn btn-primary" style="margin-top:.5rem;">Register</button>
      </form>

      <div class="card-footer" style="text-align:center;">
        <a class="back-link" href="${pageContext.request.contextPath}/login" style="justify-content:center;">
          <svg viewBox="0 0 24 24"><polyline points="15 18 9 12 15 6"/></svg>
          Back to login
        </a>
      </div>
    </div>

  </div>
</div>
</body>
</html>
