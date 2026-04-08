<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.bupt.ta.model.Job" %>
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
%>

<div class="page--top">
  <div class="container--third">
    <div class="card card-wide">
      <!-- Header -->
      <div class="top">
        <a class="btn-ghost" href="<%= ctx %>/job" style="white-space:nowrap;">
          <svg viewBox="0 0 24 24"><polyline points="15 18 9 12 15 6"/><path d="M20 12H9"/></svg>
          Back to Job List
        </a>
      </div>

      <% if (job == null) { %>
        <div style="margin-top:16px;">
          <div class="alert">Job Not Found</div>
        </div>
      <% } else { %>
        <h1 style="margin:18px 0 8px 0; font-size:26px; color:#111827;">
          <%= job.getModuleName() == null ? "" : job.getModuleName().replace("&","&amp;").replace("<","&lt;").replace(">","&gt;") %>
        </h1>
        <div style="border-bottom:1px solid #e5e7eb; padding-bottom:14px; margin-bottom:20px;">
          <span style="display:inline-block;padding:5px 12px;background:#dbeafe;color:#1d4ed8;border-radius:999px;font-size:13px;font-weight:600;">
            <%= job.getActivityType() == null ? "" : job.getActivityType().replace("&","&amp;").replace("<","&lt;").replace(">","&gt;") %>
          </span>
        </div>

        <div class="subsection" style="margin-bottom:28px;">
          <h2 style="margin:0 0 12px 0;">Job Description</h2>
          <p style="margin:0; line-height:1.5; color:#444;">
            <%= job.getDescription() == null ? "" : job.getDescription().replace("&","&amp;").replace("<","&lt;").replace(">","&gt;") %>
          </p>
        </div>

        <div class="subsection" style="margin-bottom:28px;">
          <h2 style="margin:0 0 18px 0;">Required Skills</h2>
          <%
            List<String> skills = job.getRequiredSkills();
            boolean hasSkills = skills != null && !skills.isEmpty();
          %>
          <% if (hasSkills) { %>
            <ul style="margin:0; padding-left:0; list-style:none; font-size:14px; color:#374151;">
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
                <li style="display:flex; align-items:flex-start; gap:6px; margin-bottom:4px;">
                  <span style="color:#2563eb; margin-top:2px;">&bull;</span>
                  <span><%= esc %></span>
                </li>
              <%
                }
              %>
            </ul>
          <% } else { %>
            <span style="color:#6b7280;font-size:12px;">No required skills listed.</span>
          <% } %>
        </div>

        <div class="subsection" style="margin-bottom:28px;">
          <h2 style="margin:0 0 12px 0; display:flex; align-items:center; gap:8px;">Schedule</h2>
          <div style="background:#f9fafb;border:1px solid #e5e7eb;border-radius:8px;padding:12px;">
            <%
              List<String> schedule = job.getSchedule();
              if (schedule == null) schedule = new java.util.ArrayList<>();
              if (schedule.isEmpty()) {
            %>
              <div style="color:#6b7280; font-size:13px;">Schedule to be confirmed</div>
            <%
              } else {
                for (String slot : schedule) {
                  if (slot == null || slot.isEmpty()) continue;
            %>
              <div style="display:flex; gap:8px; color:#374151; font-size:13px; margin-bottom:6px;">
                <span style="color:#6b7280;">&#128337;</span>
                <span><%= slot.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;") %></span>
              </div>
            <%
                }
              }
            %>
            <div style="margin-top:10px; padding-top:8px; border-top:1px solid #d1d5db; color:#4b5563; font-size:12px;">
              <strong>Duration:</strong> <%= (job.getDuration() == null || job.getDuration().isEmpty()) ? "One semester" : job.getDuration().replace("&","&amp;").replace("<","&lt;").replace(">","&gt;") %>
            </div>
          </div>
        </div>

        <div class="subsection" style="margin-bottom:28px;">
          <div style="display:grid; grid-template-columns:1fr; gap:10px;">
            <div style="background:#eff6ff;border:1px solid #bfdbfe;border-radius:8px;padding:12px;">
              <div style="font-weight:700; color:#111827; margin-bottom:6px;">Positions Available</div>
              <div style="color:#374151; font-size:13px;"><%= (job.getNumberOfTAs() == null || job.getNumberOfTAs().isEmpty()) ? "1" : job.getNumberOfTAs() %> TAs needed</div>
            </div>
            <div style="background:#fff7ed;border:1px solid #fed7aa;border-radius:8px;padding:12px;">
              <div style="font-weight:700; color:#111827; margin-bottom:6px;">Application Deadline</div>
              <div style="color:#374151; font-size:13px;"><%= (job.getApplicationDeadline() == null || job.getApplicationDeadline().isEmpty()) ? "TBC" : job.getApplicationDeadline().replace("&","&amp;").replace("<","&lt;").replace(">","&gt;") %></div>
            </div>
          </div>
        </div>

        <div class="subsection" style="border-top:1px solid #e5e7eb; padding-top:16px; display:flex; gap:10px;">
          <form method="post" action="<%= ctx %>/applications" style="flex:1;">
            <input type="hidden" name="jobId"      value="<%= job.getId() == null ? "" : job.getId() %>" />
            <input type="hidden" name="moduleName" value="<%= job.getModuleName() == null ? "" : job.getModuleName().replace("\"","&quot;") %>" />
            <input type="hidden" name="moduleCode" value="<%= job.getModuleCode() == null ? "" : job.getModuleCode().replace("\"","&quot;") %>" />
            <input type="hidden" name="role"       value="<%= job.getActivityType() == null ? "Teaching Assistant" : job.getActivityType().replace("\"","&quot;") %>" />
            <button type="submit" class="btn-primary" style="width:100%; padding:10px 14px; border-radius:8px; color:#fff;">
              Apply for Job
            </button>
          </form>
          <a href="<%= ctx %>/job" class="btn-ghost" style="padding:10px 14px; border-radius:8px; white-space:nowrap;">
            Back to Job List
          </a>
        </div>
      <% } %>
    </div>
  </div>
</div>
</body>
</html>
