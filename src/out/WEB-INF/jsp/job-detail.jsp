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
%>

<div class="page--top">
  <div class="container--third">
    <div class="card card-wide">
      <!-- Header -->
      <div class="top">
        <a class="btn-ghost" href="${pageContext.request.contextPath}/job" style="white-space:nowrap;">
          <svg viewBox="0 0 24 24"><polyline points="15 18 9 12 15 6"/><path d="M20 12H9"/></svg>
          Back to Job List
        </a>
      </div>

      <% if (job == null) { %>
        <div style="margin-top:16px;">
          <div class="alert">Job Not Found</div>
        </div>
      <% } else { %>
        <h1 style="margin:18px 0 14px 0; font-size:22px; border-bottom:1px solid #eef0f6; padding-bottom:14px;">
          <%= job.getModuleName() == null ? "" : job.getModuleName() %>
        </h1>

        <div class="subsection" style="margin-bottom:28px;">
          <h2 style="margin:0 0 12px 0;">Job Description</h2>
          <p style="margin:0; line-height:1.5; color:#444;">
            <%= job.getDescription() == null ? "" : job.getDescription() %>
          </p>
        </div>

        <div class="subsection">
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
            <span style="color:#6b7280;font-size:12px;">No required skills.</span>
          <% } %>
        </div>
      <% } %>
    </div>
  </div>
</div>
</body>
</html>

