<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.bupt.ta.model.Job" %>
<%@ page import="com.bupt.ta.service.ai.AiFeatureOutput" %>
<%@ page import="java.util.List" %>
<!doctype html>
<html lang="en">
<head>
  <title>Job Portal - TA Recruitment</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/app.css" />
</head>
<body>
<%
  List<Job> jobs = (List<Job>) request.getAttribute("jobs");
  if (jobs == null) jobs = new java.util.ArrayList<>();
  String q = (String) request.getAttribute("q");
  if (q == null) q = "";
  String sortBy = (String) request.getAttribute("sortBy");
  if (sortBy == null || sortBy.isEmpty()) sortBy = "postingDate";
  String escQ = q.replace("&", "&amp;").replace("\"", "&quot;").replace("<", "&lt;").replace(">", "&gt;");
  AiFeatureOutput aiRec = (AiFeatureOutput) request.getAttribute("aiRecommendation");
%>
<div class="page--top fade-in">
  <div class="container--third">
    <!-- Header -->
    <div class="page-header page-header--sm">
      <div class="logo-wrap">
        <div class="logo logo--sm">
          <svg viewBox="0 0 24 24"><path d="M22 10v6M2 10l10-5 10 5-10 5z"/><path d="M6 12v5c3 3 9 3 12 0v-5"/></svg>
        </div>
      </div>
      <h1 class="page-title">TA Job Portal</h1>
      <p class="page-subtitle">Browse available TA positions and view role details</p>
    </div>

    <div class="card" style="padding:1.5rem;">
      <div class="top-bar" style="margin-bottom:.75rem;">
        <span></span>
        <a class="btn-ghost" href="${pageContext.request.contextPath}/profile">
          <svg viewBox="0 0 24 24"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
          Profile
        </a>
      </div>

      <div class="job-list">
        <form method="get" action="${pageContext.request.contextPath}/job" class="search-panel">
          <div class="search-row">
            <input type="text" name="q" value="<%= escQ %>" placeholder="Search jobs..." />
            <button type="submit" class="btn-ghost">Search</button>
          </div>
          <div class="sort-row">
            <label for="sortBy">Sort By:</label>
            <select id="sortBy" name="sortBy" onchange="this.form.submit()">
              <option value="moduleName" <%= "moduleName".equals(sortBy) ? "selected" : "" %>>Module Name</option>
              <option value="postingDate" <%= "postingDate".equals(sortBy) ? "selected" : "" %>>Posting Date</option>
              <option value="activityType" <%= "activityType".equals(sortBy) ? "selected" : "" %>>Activity Type</option>
            </select>
          </div>
        </form>

        <!-- AI Recommendation button -->
        <div style="margin:.75rem 0 1rem;">
          <a href="${pageContext.request.contextPath}/job?aiRec=1<%= q.isEmpty() ? "" : "&q=" + java.net.URLEncoder.encode(q, "UTF-8") %>&sortBy=<%= sortBy %>"
             class="btn-ai" id="aiRecBtn">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="14" r="4"/><path d="M12 2a4 4 0 0 1 4 4c0 1.95-1.4 3.58-3.25 3.93"/><path d="M8.24 9.93A4 4 0 0 1 12 2"/><path d="M12 18v4"/><path d="M8 22h8"/>
            </svg>
            AI Smart Recommendation
          </a>
        </div>

        <% if (aiRec != null) { %>
          <div class="ai-panel <%= aiRec.isSuccess() ? "ai-panel--ok" : "ai-panel--err" %>">
            <div class="ai-panel-header">
              <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="14" r="4"/><path d="M12 2a4 4 0 0 1 4 4c0 1.95-1.4 3.58-3.25 3.93"/><path d="M8.24 9.93A4 4 0 0 1 12 2"/>
              </svg>
              <span>AI Recommendation</span>
              <% if (aiRec.isSuccess() && !aiRec.getModel().isEmpty()) { %>
                <span class="ai-model-tag"><%= aiRec.getModel() %></span>
              <% } %>
            </div>
            <div class="ai-panel-body">
              <% if (aiRec.isSuccess()) { %>
                <div class="ai-text"><%= aiRec.getText().replace("\n", "<br/>") %></div>
              <% } else { %>
                <div class="ai-error"><%= aiRec.getErrorMessage() %></div>
              <% } %>
            </div>
          </div>
        <% } %>

        <% if (jobs.isEmpty()) { %>
          <div class="empty-state">
            <p class="app-name">No matching jobs found</p>
            <p class="page-subtitle">Try adjusting your search criteria</p>
          </div>
        <% } else { %>
          <%
            for (Job job : jobs) {
              List<String> skills = job.getRequiredSkills();
              if (skills == null) skills = new java.util.ArrayList<>();
              String jobId = job.getId() == null ? "" : job.getId();
              String module = job.getModuleName() == null ? "" : job.getModuleName();
              String act = job.getActivityType() == null ? "" : job.getActivityType();
              String desc = job.getDescription() == null ? "" : job.getDescription();
              String deadline = job.getApplicationDeadline() == null ? "" : job.getApplicationDeadline();
              String numTa = job.getNumberOfTAs() == null ? "" : job.getNumberOfTAs();
              String moduleEsc = module.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
              String actEsc = act.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
              String descEsc = desc.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
              String deadlineEsc = deadline.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
              String numTaEsc = numTa.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
          %>
            <div class="job-card">
              <div class="job-card-header">
                <h3 class="job-card-title"><%= moduleEsc %></h3>
                <span class="chip"><%= actEsc %></span>
              </div>

              <div class="skill-wrap">
                <p class="job-meta" style="margin-bottom:.375rem;">Required Skills:</p>
                <div style="display:flex;flex-wrap:wrap;gap:.375rem;">
                  <%
                    if (skills.isEmpty()) {
                  %>
                    <span class="page-subtitle">No required skills</span>
                  <%
                    } else {
                      for (String s : skills) {
                        if (s == null || s.isEmpty()) continue;
                        String esc = s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
                  %>
                    <span class="chip--skill"><%= esc %></span>
                  <%
                      }
                    }
                  %>
                </div>
              </div>

              <p class="job-desc"><%= descEsc %></p>

              <div class="job-footer">
                <div class="job-meta">
                  <div>Deadline: <%= deadlineEsc.isEmpty() ? "TBC" : deadlineEsc %></div>
                  <div><%= numTaEsc.isEmpty() ? "1" : numTaEsc %> positions</div>
                </div>
                <a href="${pageContext.request.contextPath}/job?id=<%= jobId %>" class="btn btn-primary btn-sm">
                  View Details
                </a>
              </div>
            </div>
          <%
            }
          %>
        <% } %>
      </div>
    </div>
  </div>
</div>
</body>
</html>
