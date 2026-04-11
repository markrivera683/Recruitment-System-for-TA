<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.bupt.ta.model.Job" %>
<%@ page import="com.bupt.ta.service.ai.AiFeatureOutput" %>
<%@ page import="java.util.List" %>
<!doctype html>
<html lang="en">
<head>
  <title>Job Details - TA Recruitment</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/app.css" />
</head>
<body>
<%
  Job job = (Job) request.getAttribute("job");
  String ctx = request.getContextPath();
  AiFeatureOutput aiMatch = (AiFeatureOutput) request.getAttribute("aiSkillMatch");
  AiFeatureOutput aiGap   = (AiFeatureOutput) request.getAttribute("aiMissingSkills");
%>

<div class="page--top fade-in">
  <div class="container--third">
    <div class="card" style="padding:1.5rem;">
      <!-- Header -->
      <div style="margin-bottom:.75rem;">
        <a class="btn-ghost" href="<%= ctx %>/job">
          <svg viewBox="0 0 24 24"><polyline points="15 18 9 12 15 6"/><path d="M20 12H9"/></svg>
          Back to Job List
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
              if (schedule == null) schedule = new java.util.ArrayList<>();
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

        <!-- AI Skill Analysis -->
        <div class="detail-section">
          <h2>AI Skill Analysis</h2>
          <% if (aiMatch == null && aiGap == null) { %>
            <a href="<%= ctx %>/job?id=<%= job.getId() %>&aiAnalyze=1" class="btn-ai">
              <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="14" r="4"/><path d="M12 2a4 4 0 0 1 4 4c0 1.95-1.4 3.58-3.25 3.93"/><path d="M8.24 9.93A4 4 0 0 1 12 2"/><path d="M12 18v4"/><path d="M8 22h8"/>
              </svg>
              Analyze My Skill Match
            </a>
          <% } else { %>
            <div class="ai-panel <%= aiMatch != null && aiMatch.isSuccess() ? "ai-panel--ok" : "ai-panel--err" %>" style="margin-bottom:1rem;">
              <div class="ai-panel-header">
                <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
                <span>Skill Match</span>
                <% if (aiMatch != null && aiMatch.isSuccess() && !aiMatch.getModel().isEmpty()) { %>
                  <span class="ai-model-tag"><%= aiMatch.getModel() %></span>
                <% } %>
              </div>
              <div class="ai-panel-body">
                <% if (aiMatch != null && aiMatch.isSuccess()) { %>
                  <div class="ai-text"><%= aiMatch.getText().replace("\n", "<br/>") %></div>
                <% } else { %>
                  <div class="ai-error"><%= aiMatch != null ? aiMatch.getErrorMessage() : "No result" %></div>
                <% } %>
              </div>
            </div>
            <div class="ai-panel <%= aiGap != null && aiGap.isSuccess() ? "ai-panel--warn" : "ai-panel--err" %>">
              <div class="ai-panel-header">
                <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
                <span>Skill Gaps</span>
                <% if (aiGap != null && aiGap.isSuccess() && !aiGap.getModel().isEmpty()) { %>
                  <span class="ai-model-tag"><%= aiGap.getModel() %></span>
                <% } %>
              </div>
              <div class="ai-panel-body">
                <% if (aiGap != null && aiGap.isSuccess()) { %>
                  <div class="ai-text"><%= aiGap.getText().replace("\n", "<br/>") %></div>
                <% } else { %>
                  <div class="ai-error"><%= aiGap != null ? aiGap.getErrorMessage() : "No result" %></div>
                <% } %>
              </div>
            </div>
            <div style="margin-top:.75rem;">
              <a href="<%= ctx %>/job?id=<%= job.getId() %>&aiAnalyze=1" class="btn-ai btn-ai--sm">
                <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 4v6h6"/><path d="M3.51 15a9 9 0 1 0 2.13-9.36L1 10"/></svg>
                Re-analyze
              </a>
            </div>
          <% } %>
        </div>

        <div class="detail-actions">
          <form method="post" action="<%= ctx %>/applications">
            <input type="hidden" name="jobId"      value="<%= job.getId() == null ? "" : job.getId() %>" />
            <input type="hidden" name="moduleName" value="<%= job.getModuleName() == null ? "" : job.getModuleName().replace("\"","&quot;") %>" />
            <input type="hidden" name="moduleCode" value="<%= job.getModuleCode() == null ? "" : job.getModuleCode().replace("\"","&quot;") %>" />
            <input type="hidden" name="role"       value="<%= job.getActivityType() == null ? "Teaching Assistant" : job.getActivityType().replace("\"","&quot;") %>" />
            <button type="submit" class="btn btn-primary">Apply for Job</button>
          </form>
          <a href="<%= ctx %>/job" class="btn-ghost" style="padding:.625rem .875rem;">
            Back to Job List
          </a>
        </div>
      <% } %>
    </div>
  </div>
</div>
</body>
</html>
