<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.bupt.ta.model.Job" %>
<%@ page import="com.bupt.ta.model.User" %>
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
  User currentUser = (User) session.getAttribute("user");
  String errMsg = request.getParameter("err");
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

      <% if (errMsg != null && !errMsg.trim().isEmpty()) { %>
        <div class="alert" style="margin-top:12px; background:#fef3c7; color:#92400e; border:1px solid #fcd34d;">
          <%= errMsg.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;") %>
        </div>
      <% } %>

      <% if (job == null) { %>
        <div style="margin-top:16px;">
          <div class="alert">Job Not Found</div>
        </div>
      <% } else { %>
        <h1 style="margin:18px 0 4px 0; font-size:22px;">
          <%= job.getModuleName() == null ? "" : job.getModuleName().replace("&","&amp;").replace("<","&lt;").replace(">","&gt;") %>
        </h1>
        <% if (job.getModuleCode() != null && !job.getModuleCode().isEmpty()) { %>
          <p style="margin:0 0 14px 0; color:#6b7280; font-size:13px; border-bottom:1px solid #eef0f6; padding-bottom:14px;">
            Module Code: <strong><%= job.getModuleCode().replace("&","&amp;").replace("<","&lt;").replace(">","&gt;") %></strong>
            &nbsp;&bull;&nbsp;
            Activity: <strong><%= job.getActivityType() == null ? "" : job.getActivityType().replace("&","&amp;").replace("<","&lt;").replace(">","&gt;") %></strong>
          </p>
        <% } %>

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

        <!-- Apply section -->
        <% if (currentUser != null) { %>
        <div class="subsection" style="border-top:1px solid #eef0f6; padding-top:20px;">
          <form method="post" action="<%= ctx %>/applications">
            <input type="hidden" name="jobId"      value="<%= job.getId() == null ? "" : job.getId() %>" />
            <input type="hidden" name="moduleName" value="<%= job.getModuleName() == null ? "" : job.getModuleName().replace("\"","&quot;") %>" />
            <input type="hidden" name="moduleCode" value="<%= job.getModuleCode() == null ? "" : job.getModuleCode().replace("\"","&quot;") %>" />
            <input type="hidden" name="role"       value="<%= job.getActivityType() == null ? "Teaching Assistant" : job.getActivityType().replace("\"","&quot;") %>" />
            <button type="submit" class="btn-primary" style="padding:10px 28px; font-size:15px;">
              Apply Now
            </button>
          </form>
        </div>
        <% } else { %>
        <div class="subsection" style="border-top:1px solid #eef0f6; padding-top:20px;">
          <p style="color:#6b7280; font-size:14px;">
            <a href="<%= ctx %>/login">Log in</a> to apply for this position.
          </p>
        </div>
        <% } %>

      <% } %>
    </div>
  </div>
</div>
</body>
</html>
