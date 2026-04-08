<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.bupt.ta.model.Job" %>
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
%>
<div class="page--top">
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

    <div class="card card-wide">
      <div class="top" style="margin-bottom:16px; display:flex; align-items:center; justify-content:flex-end;">
        <a class="btn-ghost" href="${pageContext.request.contextPath}/profile" style="white-space:nowrap;">
          <svg viewBox="0 0 24 24"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
          Profile
        </a>
      </div>

      <div class="subsection" style="border-top:none;padding-top:0;margin-top:12px;display:flex;flex-direction:column;gap:14px;">
        <form method="get" action="${pageContext.request.contextPath}/job" class="card" style="background:#fff;">
          <div style="display:flex; gap:8px; margin-bottom:10px;">
            <input type="text" name="q" value="<%= escQ %>" placeholder="Search jobs..." style="flex:1;" />
            <button type="submit" class="btn-ghost" style="white-space:nowrap;">Search</button>
          </div>
          <div style="display:flex; align-items:center; gap:8px;">
            <label for="sortBy" style="font-size:13px; white-space:nowrap;">Sort By:</label>
            <select id="sortBy" name="sortBy" onchange="this.form.submit()">
              <option value="moduleName" <%= "moduleName".equals(sortBy) ? "selected" : "" %>>Module Name</option>
              <option value="postingDate" <%= "postingDate".equals(sortBy) ? "selected" : "" %>>Posting Date</option>
              <option value="activityType" <%= "activityType".equals(sortBy) ? "selected" : "" %>>Activity Type</option>
            </select>
          </div>
        </form>

        <% if (jobs.isEmpty()) { %>
          <div class="card" style="text-align:center;">
            <p style="color:#6b7280;">No matching jobs found</p>
            <p style="color:#94a3b8; font-size:13px; margin-top:4px;">Try adjusting your search criteria</p>
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
            <div class="card" style="background:#fff;">
              <div style="display:flex; justify-content:space-between; align-items:flex-start; gap:8px; margin-bottom:10px;">
                <h3 style="margin:0; font-size:18px; color:#111827;"><%= moduleEsc %></h3>
                <span style="background:#dbeafe;color:#1d4ed8;border-radius:999px;padding:3px 10px;font-size:12px;white-space:nowrap;"><%= actEsc %></span>
              </div>

              <div style="margin-bottom:10px;">
                <p style="font-size:12px; color:#6b7280; margin-bottom:6px;">Required Skills:</p>
                <div style="display:flex; flex-wrap:wrap; gap:6px;">
                  <%
                    if (skills.isEmpty()) {
                  %>
                    <span style="font-size:12px; color:#94a3b8;">No required skills</span>
                  <%
                    } else {
                      for (String s : skills) {
                        if (s == null || s.isEmpty()) continue;
                        String esc = s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
                  %>
                    <span style="padding:3px 8px;background:#f3f4f6;color:#374151;border-radius:6px;font-size:12px;"><%= esc %></span>
                  <%
                      }
                    }
                  %>
                </div>
              </div>

              <p style="color:#4b5563; font-size:13px; margin:0 0 12px 0; line-height:1.45;">
                <%= descEsc %>
              </p>

              <div style="display:flex; justify-content:space-between; align-items:center; gap:10px;">
                <div style="font-size:12px; color:#6b7280; line-height:1.4;">
                  <div>Deadline: <%= deadlineEsc.isEmpty() ? "TBC" : deadlineEsc %></div>
                  <div><%= numTaEsc.isEmpty() ? "1" : numTaEsc %> positions</div>
                </div>
                <a href="${pageContext.request.contextPath}/job?id=<%= jobId %>" class="btn-primary" style="padding:8px 14px; border-radius:8px; color:#fff; text-decoration:none; font-size:13px;">
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

