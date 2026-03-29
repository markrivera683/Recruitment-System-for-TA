<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.bupt.ta.model.ApplicantProfile" %>
<%@ page import="com.bupt.ta.model.EducationEntry" %>
<%@ page import="java.util.List" %>
<!doctype html>
<html>
<head>
  <title>Profile - TA Recruitment</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/app.css" />
</head>
<body>
<%
  ApplicantProfile p = (ApplicantProfile) request.getAttribute("profile");
  List<EducationEntry> edus = (List<EducationEntry>) request.getAttribute("educationList");
  String err = (String) request.getAttribute("error");
  if (p == null) p = new ApplicantProfile();
  if (edus == null || edus.isEmpty()) {
    edus = new java.util.ArrayList<EducationEntry>();
    edus.add(new EducationEntry());
  }
%>
<div class="card card-wide">
  <div class="top">
    <h1>Create / edit profile</h1>
    <a class="btn-secondary" href="${pageContext.request.contextPath}/logout">Log out</a>
  </div>

  <% if (err != null) { %>
    <div class="alert"><%= err %></div>
  <% } %>

  <form method="post" action="${pageContext.request.contextPath}/profile" enctype="multipart/form-data">
    <div class="subsection">
      <h2>Personal information</h2>
      <label>Full name</label>
      <input name="fullName" required value="<%= p.fullName == null ? "" : p.fullName %>" />

      <label>Gender</label>
      <select name="gender">
        <option value="" <%= p.gender == null || p.gender.isEmpty() ? "selected" : "" %>>Select</option>
        <option value="Male" <%= "Male".equals(p.gender) || "\u7537".equals(p.gender) ? "selected" : "" %>>Male</option>
        <option value="Female" <%= "Female".equals(p.gender) || "\u5973".equals(p.gender) ? "selected" : "" %>>Female</option>
        <option value="Other" <%= "Other".equals(p.gender) || "\u5176\u4ed6".equals(p.gender) ? "selected" : "" %>>Other</option>
      </select>

      <label>Degree</label>
      <input name="degree" placeholder="e.g. Bachelor, Master (in progress)" value="<%= p.degree == null ? "" : p.degree %>" />

      <label>Major</label>
      <input name="major" value="<%= p.major == null ? "" : p.major %>" />

      <label>Student ID</label>
      <input name="studentId" required value="<%= p.studentId == null ? "" : p.studentId %>" />

      <label>National ID</label>
      <input name="idCard" value="<%= p.idCard == null ? "" : p.idCard %>" />

      <label>Phone</label>
      <input name="phone" type="tel" value="<%= p.phone == null ? "" : p.phone %>" />

      <label>Email</label>
      <input name="email" type="email" required value="<%= p.email == null ? "" : p.email %>" />
    </div>

    <div class="subsection">
      <h2>Education</h2>
      <p class="hint">Add multiple entries. Include school, level, major, and dates.</p>
      <div id="edu-rows">
        <% for (EducationEntry e : edus) { %>
        <div class="edu-row">
          <label>School</label>
          <input name="edu_school" value="<%= e.school == null ? "" : e.school %>" />
          <label>Degree / level</label>
          <input name="edu_degree" placeholder="e.g. Bachelor" value="<%= e.degree == null ? "" : e.degree %>" />
          <label>Major</label>
          <input name="edu_major" value="<%= e.major == null ? "" : e.major %>" />
          <label>Period</label>
          <input name="edu_period" placeholder="e.g. Sep 2021 – present" value="<%= e.period == null ? "" : e.period %>" />
        </div>
        <% } %>
      </div>
      <button type="button" class="btn-secondary btn-inline" id="add-edu">+ Add education entry</button>
    </div>

    <div class="subsection">
      <h2>Courses completed</h2>
      <label>One course per line</label>
      <textarea name="courses" rows="5" placeholder="Data structures&#10;Operating systems"><%= p.courses == null ? "" : p.courses %></textarea>
    </div>

    <div class="subsection">
      <h2>Availability</h2>
      <textarea name="freeTime" rows="3" placeholder="Days and times you are free, etc."><%= p.freeTime == null ? "" : p.freeTime %></textarea>
    </div>

    <div class="subsection">
      <h2>Skills</h2>
      <textarea name="skills" rows="4" placeholder="Languages, tools, competitions, etc."><%= p.skills == null ? "" : p.skills %></textarea>
    </div>

    <div class="subsection">
      <h2>CV</h2>
      <% if (p.cvFileName != null && !p.cvFileName.isEmpty()) { %>
        <p class="info">Current file: <a href="${pageContext.request.contextPath}/cv" target="_blank" rel="noopener"><%= p.cvFileName %></a> (open or download)</p>
      <% } %>
      <label>Upload (PDF / Word, max ~10 MB)</label>
      <input type="file" name="cv" accept=".pdf,.doc,.docx,application/pdf" />
    </div>

    <button type="submit">Save profile</button>
  </form>
</div>
<script>
(function () {
  var btn = document.getElementById('add-edu');
  if (!btn) return;
  btn.addEventListener('click', function () {
    var first = document.querySelector('#edu-rows .edu-row');
    if (!first) return;
    var row = first.cloneNode(true);
    row.querySelectorAll('input').forEach(function (el) { el.value = ''; });
    document.getElementById('edu-rows').appendChild(row);
  });
})();
</script>
</body>
</html>
