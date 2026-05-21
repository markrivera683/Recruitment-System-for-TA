<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.bupt.ta.model.TaResumeDisplay" %>
<%@ page import="com.bupt.ta.model.ApplicantProfile" %>
<%@ page import="com.bupt.ta.model.EducationEntry" %>
<%@ page import="com.bupt.ta.model.User" %>
<%@ page import="java.util.List" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.nio.charset.StandardCharsets" %>
<%!
  private static String h(String s) {
    if (s == null) return "";
    return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
  }
  private static String hbr(String s) {
    return h(s).replace("\r\n", "\n").replace("\n", "<br/>");
  }
%>
<%
  @SuppressWarnings("unchecked")
  List<TaResumeDisplay> taResumes = (List<TaResumeDisplay>) request.getAttribute("taResumes");
  if (taResumes == null) taResumes = java.util.Collections.emptyList();
  String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>TA profiles (admin) - TA Recruitment</title>
  <link rel="stylesheet" href="<%= ctx %>/static/css/admin-dashboard.css" />
  <link rel="stylesheet" href="<%= ctx %>/static/css/app.css" />
  <style>
    .ta-resume-page { max-width: 960px; margin: 0 auto; padding: 1.5rem 1rem 3rem; }
    .ta-resume-back { margin-bottom: 1.25rem; }
    .ta-resume-hero { margin-bottom: 2rem; }
    .ta-resume-hero h1 { font-size: 1.65rem; margin: 0 0 .35rem; color: #0f172a; }
    .ta-resume-hero p { margin: 0; color: #64748b; font-size: .95rem; }
    .ta-resume-stack { display: flex; flex-direction: column; gap: 1.75rem; }
    .ta-resume-card {
      background: #fff; border-radius: 14px; border: 1px solid #e2e8f0;
      box-shadow: 0 1px 3px rgba(15, 23, 42, .06); overflow: hidden;
    }
    .ta-resume-card__head {
      padding: 1.1rem 1.35rem; background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
      border-bottom: 1px solid #e2e8f0; display: flex; flex-wrap: wrap; gap: .75rem 1.25rem;
      align-items: baseline; justify-content: space-between;
    }
    .ta-resume-card__title { font-size: 1.2rem; font-weight: 700; color: #0f172a; margin: 0; }
    .ta-resume-meta { display: flex; flex-wrap: wrap; gap: .5rem .75rem; font-size: .85rem; color: #475569; }
    .ta-resume-meta span { background: #fff; border: 1px solid #e2e8f0; border-radius: 999px; padding: .2rem .65rem; }
    .ta-resume-body { padding: 1.25rem 1.35rem 1.5rem; }
    .ta-resume-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem 1.5rem; }
    @media (max-width: 720px) { .ta-resume-grid { grid-template-columns: 1fr; } }
    .ta-resume-section { margin-bottom: 1.25rem; }
    .ta-resume-section:last-child { margin-bottom: 0; }
    .ta-resume-section h3 {
      font-size: .78rem; text-transform: uppercase; letter-spacing: .06em; color: #64748b;
      margin: 0 0 .6rem; font-weight: 600;
    }
    .ta-resume-dl { margin: 0; display: grid; grid-template-columns: auto 1fr; gap: .35rem 1rem; font-size: .9rem; align-items: start; }
    .ta-resume-dl dt { color: #94a3b8; font-weight: 500; margin: 0; }
    .ta-resume-dl dd { margin: 0; color: #1e293b; word-break: break-word; }
    .ta-resume-block {
      background: #f8fafc; border-radius: 10px; padding: .85rem 1rem; font-size: .9rem;
      color: #334155; line-height: 1.55; border: 1px solid #e2e8f0;
    }
    .ta-resume-empty { color: #94a3b8; font-style: italic; font-size: .9rem; }
    .ta-resume-table { width: 100%; border-collapse: collapse; font-size: .85rem; }
    .ta-resume-table th, .ta-resume-table td { border: 1px solid #e2e8f0; padding: .5rem .65rem; text-align: left; }
    .ta-resume-table th { background: #f1f5f9; color: #475569; font-weight: 600; }
    .ta-resume-badge { font-size: .75rem; padding: .15rem .5rem; border-radius: 6px; background: #e0f2fe; color: #0369a1; }
    .ta-resume-cv { margin-top: .75rem; }
  </style>
</head>
<body>
<div class="ta-resume-page">
  <div class="ta-resume-back">
    <a class="button button-outline" href="<%= ctx %>/admin">&larr; Back to Admin Dashboard</a>
  </div>
  <div class="ta-resume-hero">
    <h1>Registered TA profiles</h1>
    <p>Read-only copy of applicant profile fields (same data as each TA&rsquo;s profile page). <%= taResumes.size() %> TA account(s).</p>
  </div>

  <% if (taResumes.isEmpty()) { %>
    <p class="tooltip-small">No TA accounts yet.</p>
  <% } else { %>
  <div class="ta-resume-stack">
    <% for (TaResumeDisplay row : taResumes) {
         User u = row.user;
         ApplicantProfile p = row.profile;
         String uid = u.id == null ? "" : u.id;
         String cvUserParam = URLEncoder.encode(uid, StandardCharsets.UTF_8);
         String anchor = h(uid).replace(" ", "-");
    %>
    <article class="ta-resume-card" id="ta-<%= anchor %>">
      <div class="ta-resume-card__head">
        <h2 class="ta-resume-card__title"><%= h(p.fullName != null && !p.fullName.trim().isEmpty() ? p.fullName : (u.name != null ? u.name : "(no name)")) %></h2>
        <div class="ta-resume-meta">
          <span>Login: <%= h(u.email != null ? u.email : "") %></span>
          <% if (u.studentId != null && !u.studentId.trim().isEmpty()) { %>
          <span>Reg. student ID: <%= h(u.studentId) %></span>
          <% } %>
          <% if (!row.hasSavedProfile) { %>
          <span class="ta-resume-badge">No saved profile</span>
          <% } %>
        </div>
      </div>
      <div class="ta-resume-body">
        <% if (!row.hasSavedProfile) { %>
          <p class="ta-resume-empty">This account has not saved a profile in <code>profiles.json</code> yet.</p>
        <% } else { %>
        <div class="ta-resume-grid">
          <div>
            <div class="ta-resume-section">
              <h3>Personal information</h3>
              <dl class="ta-resume-dl">
                <dt>Full name</dt><dd><%= h(p.fullName) %></dd>
                <dt>Gender</dt><dd><%= h(p.gender) %></dd>
                <dt>Degree</dt><dd><%= h(p.degree) %></dd>
                <dt>Major</dt><dd><%= h(p.major) %></dd>
                <dt>Student ID (profile)</dt><dd><%= h(p.studentId) %></dd>
                <dt>National ID</dt><dd><%= h(p.idCard) %></dd>
                <dt>Phone</dt><dd><%= h(p.phone) %></dd>
                <dt>Email (profile)</dt><dd><%= h(p.email) %></dd>
              </dl>
            </div>
            <div>
              <div class="ta-resume-section">
                <h3>Education</h3>
                <% if (!row.hasEducationRows()) { %>
                  <p class="ta-resume-empty">No education rows.</p>
                <% } else { %>
                <table class="ta-resume-table">
                  <thead><tr><th>School</th><th>Degree</th><th>Major</th><th>Period</th></tr></thead>
                  <tbody>
                  <% for (EducationEntry e : row.visibleEducation) { %>
                    <tr>
                      <td><%= h(e.school) %></td>
                      <td><%= h(e.degree) %></td>
                      <td><%= h(e.major) %></td>
                      <td><%= h(e.period) %></td>
                    </tr>
                  <% } %>
                  </tbody>
                </table>
                <% } %>
              </div>
            </div>
          </div>

          <div class="ta-resume-section">
            <h3>Courses completed</h3>
            <div class="ta-resume-block"><%= hbr(p.courses) %></div>
          </div>
          <div class="ta-resume-section">
            <h3>Availability</h3>
            <div class="ta-resume-block"><%= hbr(p.freeTime != null && !p.freeTime.trim().isEmpty() ? p.freeTime : (p.availability != null ? p.availability : "")) %></div>
          </div>
          <div class="ta-resume-section">
            <h3>Skills</h3>
            <div class="ta-resume-block"><%= hbr(p.skills) %></div>
          </div>

          <% boolean legacy = (p.degreeProgramme != null && !p.degreeProgramme.trim().isEmpty())
              || (p.yearOfStudy != null && !p.yearOfStudy.trim().isEmpty())
              || (p.selfIntro != null && !p.selfIntro.trim().isEmpty());
             if (legacy) { %>
          <div class="ta-resume-section">
            <h3>Additional (legacy fields)</h3>
            <dl class="ta-resume-dl">
              <% if (p.degreeProgramme != null && !p.degreeProgramme.trim().isEmpty()) { %>
              <dt>Degree programme</dt><dd><%= h(p.degreeProgramme) %></dd>
              <% } %>
              <% if (p.yearOfStudy != null && !p.yearOfStudy.trim().isEmpty()) { %>
              <dt>Year of study</dt><dd><%= h(p.yearOfStudy) %></dd>
              <% } %>
              <% if (p.selfIntro != null && !p.selfIntro.trim().isEmpty()) { %>
              <dt>Self introduction</dt><dd><%= hbr(p.selfIntro) %></dd>
              <% } %>
            </dl>
          </div>
          <% } %>

          <div class="ta-resume-section">
            <h3>Curriculum vitae</h3>
            <% if (p.cvFileName != null && !p.cvFileName.trim().isEmpty()) { %>
              <p style="margin:0 0 .5rem;font-size:.9rem;color:#334155;">File: <strong><%= h(p.cvFileName) %></strong></p>
              <a class="button button-primary ta-resume-cv" href="<%= ctx %>/admin/cv?userId=<%= cvUserParam %>" target="_blank" rel="noopener">Open CV</a>
            <% } else { %>
              <p class="ta-resume-empty">No CV uploaded.</p>
            <% } %>
          </div>
        <% } %>
      </div>
    </article>
    <% } %>
  </div>
  <% } %>
</div>
</body>
</html>
