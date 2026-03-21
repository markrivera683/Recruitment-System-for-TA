<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.bupt.ta.model.ApplicantProfile" %>
<!doctype html>
<html>
<head>
  <title>Profile - TA Recruitment</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/app.css" />
</head>
<body>
<%
  ApplicantProfile p = (ApplicantProfile) request.getAttribute("profile");
%>
<div class="card">
  <div class="top">
    <h1>Applicant Profile</h1>
    <a class="btn-secondary" href="${pageContext.request.contextPath}/logout">Logout</a>
  </div>

  <form method="post" action="${pageContext.request.contextPath}/profile">
    <label>Degree / Programme</label>
    <input name="degreeProgramme" value="<%= p.degreeProgramme == null ? "" : p.degreeProgramme %>" />

    <label>Year of Study</label>
    <input name="yearOfStudy" value="<%= p.yearOfStudy == null ? "" : p.yearOfStudy %>" />

    <label>Skills</label>
    <textarea name="skills" rows="3"><%= p.skills == null ? "" : p.skills %></textarea>

    <label>Availability</label>
    <textarea name="availability" rows="2"><%= p.availability == null ? "" : p.availability %></textarea>

    <label>Self introduction</label>
    <textarea name="selfIntro" rows="3"><%= p.selfIntro == null ? "" : p.selfIntro %></textarea>

    <button type="submit">Save profile</button>
  </form>
</div>
</body>
</html>
