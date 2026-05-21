<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.bupt.ta.model.Job" %>
<%@ page import="java.util.List" %>
<!doctype html>
<html lang="zh-CN">
<head>
  <title>Job preview (admin) - TA Recruitment</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/admin-dashboard.css" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/app.css" />
</head>
<body>
<%
  Job job = (Job) request.getAttribute("job");
  String ctx = request.getContextPath();
%>

<div class="page--top fade-in">
  <div class="container--third">
    <div class="card" style="padding:1.5rem;">
      <div style="margin-bottom:.75rem;">
        <a class="btn-ghost" href="<%= ctx %>/admin">
          <svg viewBox="0 0 24 24"><polyline points="15 18 9 12 15 6"/><path d="M20 12H9"/></svg>
          Back to Admin Dashboard
        </a>
      </div>

      <% if (job == null) { %>
        <div class="alert alert-error" style="margin-top:1rem;">Job Not Found</div>
      <% } else { %>
        <h1 class="detail-title">
          <%= job.getModuleName() == null ? "" : job.getModuleName().replace("&","&amp;").replace("<","&lt;").replace(">","&gt;") %>
        </h1>
        <div style="border-bottom:1px solid #e5e7eb;padding-bottom:.875rem;margin-bottom:1.25rem;">
          <span class="chip" style="font-size:.8125rem;padding:.3125rem .75rem;">
            <%= job.getActivityType() == null ? "" : job.getActivityType().replace("&","&amp;").replace("<","&lt;").replace(">","&gt;") %>
          </span>
        </div>

        <div class="detail-section">
          <h2>Overview</h2>
          <p class="page-subtitle" style="margin:0;">
            <strong>Module code:</strong>
            <%= job.getModuleCode() == null || job.getModuleCode().isEmpty() ? "—" : job.getModuleCode().replace("&","&amp;").replace("<","&lt;").replace(">","&gt;") %>
            &nbsp;·&nbsp;
            <strong>Posted:</strong>
            <%= job.getPostDate() == null || job.getPostDate().isEmpty() ? "—" : job.getPostDate().replace("&","&amp;").replace("<","&lt;").replace(">","&gt;") %>
          </p>
        </div>

        <div class="detail-section">
          <h2>Job Description</h2>
          <p><%= job.getDescription() == null ? "" : job.getDescription().replace("&","&amp;").replace("<","&lt;").replace(">","&gt;") %></p>
        </div>

        <div class="detail-section">
          <h2>Required Skills</h2>
          <%
            List<String> skills = job.getRequiredSkills();
            boolean hasSkills = skills != null && !skills.isEmpty();
          %>
          <% if (hasSkills) { %>
            <ul class="skill-list">
              <%
                for (String skill : skills) {
                  if (skill == null || skill.isEmpty()) continue;
                  String esc = skill
                    .replace("&", "&amp;")
                    .replace("\"", "&quot;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("'", "&#39;");
              %>
                <li>
                  <span class="bullet">&bull;</span>
                  <span><%= esc %></span>
                </li>
              <%
                }
              %>
            </ul>
          <% } else { %>
            <span class="page-subtitle">No required skills listed.</span>
          <% } %>
        </div>

        <div class="detail-section">
          <h2>Schedule</h2>
          <div class="info-box">
            <%
              List<String> schedule = job.getSchedule();
              if (schedule == null) schedule = new java.util.ArrayList<String>();
              if (schedule.isEmpty()) {
            %>
              <div class="page-subtitle">Schedule to be confirmed</div>
            <%
              } else {
                for (String slot : schedule) {
                  if (slot == null || slot.isEmpty()) continue;
            %>
              <div class="app-meta" style="margin-bottom:.375rem;">
                <span>&#128337;</span>
                <span><%= slot.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;") %></span>
              </div>
            <%
                }
              }
            %>
            <div style="margin-top:.625rem;padding-top:.5rem;border-top:1px solid #d1d5db;" class="job-meta">
              <strong>Duration:</strong> <%= (job.getDuration() == null || job.getDuration().isEmpty()) ? "One semester" : job.getDuration().replace("&","&amp;").replace("<","&lt;").replace(">","&gt;") %>
            </div>
          </div>
        </div>

        <div class="detail-section">
          <div class="grid-2col" style="gap:.625rem;">
            <div class="info-box info-box--blue">
              <div class="info-label">Positions Available</div>
              <div class="info-value"><%= (job.getNumberOfTAs() == null || job.getNumberOfTAs().isEmpty()) ? "1" : job.getNumberOfTAs() %> TAs needed</div>
            </div>
            <div class="info-box info-box--amber">
              <div class="info-label">Application Deadline</div>
              <div class="info-value"><%= (job.getApplicationDeadline() == null || job.getApplicationDeadline().isEmpty()) ? "TBC" : job.getApplicationDeadline().replace("&","&amp;").replace("<","&lt;").replace(">","&gt;") %></div>
            </div>
          </div>
        </div>

        <p class="tooltip-small" style="margin-top:1.25rem;">This page mirrors the applicant job detail content (read-only).</p>
      <% } %>
    </div>
  </div>
</div>
</body>
</html>
