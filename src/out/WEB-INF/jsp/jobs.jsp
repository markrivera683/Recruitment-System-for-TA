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
%>

<div class="card card-wide">
  <div class="top" style="margin-bottom:16px;">
    <h1 style="margin:0;">TA Job Portal</h1>
  </div>

  <div class="subsection" style="border-top:none;padding-top:0;margin-top:12px;">
    <% if (jobs.isEmpty()) { %>
      <div class="alert">No jobs found.</div>
    <% } else { %>
      <%
        for (Job job : jobs) {
          List<String> skills = job.getRequiredSkills();
          if (skills == null) skills = new java.util.ArrayList<>();
          String jobId = job.getId() == null ? "" : job.getId();
      %>
        <div
          class="card"
          style="margin-bottom:14px; background:#fff;"
        >
          <a
            href="${pageContext.request.contextPath}/job?id=<%= jobId %>"
            style="display:block; text-decoration:none; color:inherit; padding:18px;"
          >
            <h2 style="margin:0 0 10px 0; font-size:16px; color:#111827;">
              <%= job.getModuleName() == null ? "" : job.getModuleName() %>
            </h2>

            <div style="font-size:13px; color:#6b7280; margin-bottom:6px;">Requirements</div>
            <div style="display:flex; flex-wrap:wrap; gap:8px;">
              <%
                boolean hasSkills = false;
                for (String s : skills) {
                  if (s == null || s.isEmpty()) continue;
                  hasSkills = true;
                  String esc = s
                    .replace("&", "&amp;")
                    .replace("\"", "&quot;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("'", "&#39;");
              %>
                  <span style="background:#f3f4f6; border:1px solid #e5e7eb; padding:4px 8px; border-radius:8px; font-size:12px; color:#374151;">
                    <%= esc %>
                  </span>
              <%
                }
                if (!hasSkills) {
              %>
                <span style="color:#6b7280; font-size:12px;">No required skills</span>
              <%
                }
              %>
            </div>
          </a>
        </div>
      <%
        }
      %>
    <% } %>
  </div>
</div>

</body>
</html>

