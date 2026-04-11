<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.nio.charset.StandardCharsets" %>
<%@ page import="com.bupt.ta.model.User" %>
<%@ page import="com.bupt.ta.model.Job" %>
<%@ page import="com.bupt.ta.model.Application" %>
<%
    User currentUser = (User) session.getAttribute("user");
    List<Job> jobs = (List<Job>) request.getAttribute("jobs");
    List<Application> applications = (List<Application>) request.getAttribute("applications");
    if (jobs == null) jobs = java.util.Collections.emptyList();
    if (applications == null) applications = java.util.Collections.emptyList();
    @SuppressWarnings("unchecked")
    Map<String, String> cvByUserId = (Map<String, String>) request.getAttribute("cvByUserId");
    if (cvByUserId == null) cvByUserId = java.util.Collections.emptyMap();
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>MO Dashboard</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            background: #f6f8fb;
            margin: 0;
            padding: 0;
            color: #1f2937;
        }
        .container {
            max-width: 1100px;
            margin: 30px auto;
            padding: 0 20px 40px;
        }
        .header {
            background: #ffffff;
            border-radius: 12px;
            padding: 20px 24px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.06);
            margin-bottom: 24px;
        }
        .header h1 {
            margin: 0 0 8px;
            font-size: 28px;
        }
        .header p {
            margin: 0;
            color: #6b7280;
        }
        .stats {
            display: flex;
            gap: 16px;
            margin-bottom: 24px;
            flex-wrap: wrap;
        }
        .card {
            background: #ffffff;
            border-radius: 12px;
            padding: 18px 20px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.06);
            flex: 1;
            min-width: 220px;
        }
        .card h3 {
            margin: 0 0 10px;
            font-size: 16px;
            color: #6b7280;
        }
        .card .value {
            font-size: 28px;
            font-weight: bold;
        }
        .section {
            background: #ffffff;
            border-radius: 12px;
            padding: 20px 24px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.06);
            margin-bottom: 24px;
        }
        .section h2 {
            margin-top: 0;
            margin-bottom: 16px;
            font-size: 22px;
        }
        table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 8px;
        }
        th, td {
            text-align: left;
            padding: 12px 10px;
            border-bottom: 1px solid #e5e7eb;
            vertical-align: top;
        }
        th {
            background: #f9fafb;
            font-weight: 600;
        }
        .tag {
            display: inline-block;
            padding: 4px 10px;
            border-radius: 999px;
            font-size: 12px;
            background: #eef2ff;
            color: #3730a3;
            margin-right: 6px;
            margin-bottom: 4px;
        }
        .muted {
            color: #6b7280;
        }
        .topbar {
            margin-top: 10px;
        }
        .logout-link {
            display: inline-block;
            margin-top: 10px;
            color: #2563eb;
            text-decoration: none;
        }
        .empty {
            color: #6b7280;
            padding: 8px 0;
        }
    </style>
</head>
<body>
<div class="container">

    <div class="header">
        <h1>Module Organiser Dashboard</h1>
        <p>
            Welcome,
            <strong><%= currentUser != null ? currentUser.name : "MO User" %></strong>
        </p>
        <div class="topbar">
            <a class="logout-link" href="<%= request.getContextPath() %>/logout">Logout</a>
        </div>
    </div>

    <div class="stats">
        <div class="card">
            <h3>Total Jobs</h3>
            <div class="value"><%= jobs.size() %></div>
        </div>
        <div class="card">
            <h3>Total Applications</h3>
            <div class="value"><%= applications.size() %></div>
        </div>
        <div class="card">
            <h3>Pending Applications</h3>
            <div class="value">
                <%
                    int pending = 0;
                    for (Application a : applications) {
                        if ("Pending".equalsIgnoreCase(a.status)) pending++;
                    }
                    out.print(pending);
                %>
            </div>
        </div>
    </div>

    <div class="section">
        <h2>Posted Jobs</h2>
        <% if (jobs.isEmpty()) { %>
            <div class="empty">No jobs available.</div>
        <% } else { %>
            <table>
                <thead>
                <tr>
                    <th>Module</th>
                    <th>Code</th>
                    <th>Activity Type</th>
                    <th>Required Skills</th>
                    <th>Post Date</th>
                    <th>Deadline</th>
                </tr>
                </thead>
                <tbody>
                <% for (Job job : jobs) { %>
                    <tr>
                        <td>
                            <strong><%= job.getModuleName() %></strong><br>
                            <span class="muted"><%= job.getDescription() %></span>
                        </td>
                        <td><%= job.getModuleCode() %></td>
                        <td><%= job.getActivityType() %></td>
                        <td>
                            <%
                                List<String> skills = job.getRequiredSkills();
                                if (skills != null && !skills.isEmpty()) {
                                    for (String s : skills) {
                            %>
                                <span class="tag"><%= s %></span>
                            <%
                                    }
                                } else {
                            %>
                                <span class="muted">None</span>
                            <%
                                }
                            %>
                        </td>
                        <td><%= job.getPostDate() == null ? "" : job.getPostDate() %></td>
                        <td><%= job.getApplicationDeadline() == null ? "" : job.getApplicationDeadline() %></td>
                    </tr>
                <% } %>
                </tbody>
            </table>
        <% } %>
    </div>

    <div class="section">
        <h2>Incoming Applications</h2>
        <% if (applications.isEmpty()) { %>
            <div class="empty">No applications available.</div>
        <% } else { %>
            <table>
                <thead>
                <tr>
                    <th>Application ID</th>
                    <th>User ID</th>
                    <th>Module</th>
                    <th>Role</th>
                    <th>Application Date</th>
                    <th>Status</th>
                    <th>CV</th>
                    <th>Feedback</th>
                </tr>
                </thead>
                <tbody>
                <% for (Application app : applications) { %>
                    <tr>
                        <td><%= app.id %></td>
                        <td><%= app.userId %></td>
                        <td>
                            <strong><%= app.moduleName %></strong><br>
                            <span class="muted"><%= app.moduleCode %></span>
                        </td>
                        <td><%= app.role %></td>
                        <td><%= app.applicationDate %></td>
                        <td><%= app.status %></td>
                        <td>
                            <%
                                String cvName = cvByUserId.get(app.userId);
                                if (cvName != null) {
                                    String q = URLEncoder.encode(app.userId, StandardCharsets.UTF_8);
                            %>
                                <a href="<%= request.getContextPath() %>/cv?userId=<%= q %>" target="_blank" rel="noopener">Open CV</a>
                            <%
                                } else {
                            %>
                                <span class="muted">No CV</span>
                            <%
                                }
                            %>
                        </td>
                        <td><%= app.feedback == null ? "" : app.feedback %></td>
                    </tr>
                <% } %>
                </tbody>
            </table>
        <% } %>
    </div>

</div>
</body>
</html>