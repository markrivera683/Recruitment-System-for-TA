<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>CV unavailable — TA Recruitment</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/app.css" />
</head>
<body>
<%
  String raw = (String) request.getAttribute("cvErrorMessage");
  if (raw == null) raw = "We could not open this CV.";
  String esc = raw.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
          .replace("\"", "&quot;");
%>
<div class="page--top fade-in">
  <div class="layout-wide">
    <div class="card" style="padding:2rem 2.5rem;max-width:32rem;margin:2rem auto;">
      <h1 class="page-title" style="font-size:1.35rem;">CV cannot be displayed</h1>
      <p class="page-subtitle" style="margin-top:.75rem;"><%= esc %></p>
      <div style="margin-top:1.5rem;display:flex;flex-wrap:wrap;gap:.5rem;">
        <a class="btn btn-primary" href="${pageContext.request.contextPath}/profile">Back to profile</a>
        <a class="link-pill" href="${pageContext.request.contextPath}/mo">MO dashboard</a>
        <a class="link-pill" href="${pageContext.request.contextPath}/job">Job list</a>
      </div>
    </div>
  </div>
</div>
</body>
</html>
