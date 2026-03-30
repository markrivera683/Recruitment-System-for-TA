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

<div class="card card-wide">
  <!-- Header -->
  <div class="top">
    <a class="btn-secondary btn-inline" href="${pageContext.request.contextPath}/job">Back to Job List</a>
  </div>

  <% if (job == null) { %>
    <div style="margin-top:16px;">
      <div class="alert">Job Not Found</div>
    </div>
  <% } else { %>
    <h1 style="margin:0 0 14px 0; font-size:22px; border-bottom:1px solid #eef0f6; padding-bottom:14px;">
      <%= job.getModuleName() == null ? "" : job.getModuleName() %>
    </h1>

    <div class="subsection">
      <h2>Job Description</h2>
      <p style="margin:0; line-height:1.5; color:#444;">
        <%= job.getDescription() == null ? "" : job.getDescription() %>
      </p>
    </div>

    <div class="subsection">
      <h2>Requirements</h2>
      <div style="display:flex; flex-wrap:wrap; gap:8px; margin-top:10px;">
        <%
          List<String> skills = job.getRequiredSkills();
          boolean hasSkills = skills != null && !skills.isEmpty();
          if (hasSkills) {
            for (String skill : skills) {
              if (skill == null || skill.isEmpty()) continue;
              String esc = skill
                .replace("&", "&amp;")
                .replace("\"", "&quot;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("'", "&#39;");
        %>
              <span style="background:#f3f4f6;border:1px solid #e5e7eb;padding:4px 8px;border-radius:8px;font-size:12px;color:#374151;">
                <%= esc %>
              </span>
        <%
            }
          } else {
        %>
            <span style="color:#6b7280;font-size:12px;">No required skills.</span>
        <%
          }
        %>
      </div>
    </div>
  <% } %>
</div>
</body>
</html>

