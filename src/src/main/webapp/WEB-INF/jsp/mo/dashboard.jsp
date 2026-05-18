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
    @SuppressWarnings("unchecked")
    List<Job> jobs = (List<Job>) request.getAttribute("jobs");
    @SuppressWarnings("unchecked")
    List<Application> applications = (List<Application>) request.getAttribute("applications");
    if (jobs == null) jobs = java.util.Collections.emptyList();
    if (applications == null) applications = java.util.Collections.emptyList();

    @SuppressWarnings("unchecked")
    Map<String, String> cvByUserId = (Map<String, String>) request.getAttribute("cvByUserId");
    if (cvByUserId == null) cvByUserId = java.util.Collections.emptyMap();

    String moMessage = (String) request.getAttribute("moMessage");
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
            max-width: 1160px;
            margin: 30px auto;
            padding: 0 20px 40px;
        }
        .header {
            background: #ffffff;
            border-radius: 12px;
            padding: 20px 24px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.06);
            margin-bottom: 20px;
        }
        .header h1 {
            margin: 0 0 8px;
            font-size: 28px;
        }
        .header p {
            margin: 0;
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

        .notice {
            margin: 0 0 18px;
            padding: 10px 12px;
            border: 1px solid #bbf7d0;
            background: #f0fdf4;
            color: #166534;
            border-radius: 8px;
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
            padding: 10px 8px;
            border-bottom: 1px solid #e5e7eb;
            vertical-align: top;
            font-size: 14px;
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
        .empty {
            color: #6b7280;
            padding: 8px 0;
        }

        .form-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 12px 16px;
        }
        .form-grid .full {
            grid-column: 1 / -1;
        }
        .field label {
            display: block;
            font-size: 13px;
            font-weight: 600;
            margin-bottom: 4px;
            color: #374151;
        }
        .field input,
        .field textarea {
            width: 100%;
            box-sizing: border-box;
            border: 1px solid #d1d5db;
            border-radius: 8px;
            padding: 8px 10px;
            font-size: 14px;
            background: #fff;
        }
        .field textarea {
            min-height: 88px;
            resize: vertical;
        }
        .btn-row {
            margin-top: 14px;
            display: flex;
            gap: 10px;
            flex-wrap: wrap;
        }
        .btn {
            border: 1px solid #d1d5db;
            border-radius: 8px;
            padding: 8px 12px;
            cursor: pointer;
            background: #fff;
            color: #111827;
            font-size: 13px;
        }
        .btn-primary {
            background: #2563eb;
            color: #fff;
            border-color: #2563eb;
        }
        .btn-success {
            background: #059669;
            color: #fff;
            border-color: #059669;
        }
        .btn-danger {
            background: #dc2626;
            color: #fff;
            border-color: #dc2626;
        }
        .btn:disabled {
            opacity: 0.6;
            cursor: not-allowed;
        }
        .btn-link {
            display: inline-block;
            text-decoration: none;
            margin-bottom: 6px;
        }

        .status-pill {
            display: inline-block;
            padding: 3px 10px;
            border-radius: 999px;
            font-size: 12px;
            font-weight: 600;
        }
        .status-draft {
            background: #fef3c7;
            color: #92400e;
        }
        .status-published {
            background: #dcfce7;
            color: #166534;
        }

        .decision-form input[type="text"] {
            width: 180px;
            margin-bottom: 6px;
        }

        @media (max-width: 900px) {
            .form-grid {
                grid-template-columns: 1fr;
            }
            .decision-form input[type="text"] {
                width: 100%;
            }
            table {
                display: block;
                overflow-x: auto;
                white-space: nowrap;
            }
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

    <% if (moMessage != null && !moMessage.trim().isEmpty()) { %>
        <div class="notice"><%= moMessage %></div>
    <% } %>

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

    <!-- Create / Publish Job -->
    <div class="section">
        <h2>Create / Publish Job</h2>
        <form method="post" action="<%= request.getContextPath() %>/mo">
            <input type="hidden" name="action" value="createJob" />

            <div class="form-grid">
                <div class="field">
                    <label>Module Name *</label>
                    <input type="text" name="moduleName" required />
                </div>
                <div class="field">
                    <label>Module Code *</label>
                    <input type="text" name="moduleCode" required />
                </div>

                <div class="field">
                    <label>Activity Type</label>
                    <input type="text" name="activityType" placeholder="Lab Assistant / Tutorial Support" />
                </div>
                <div class="field">
                    <label>Required Skills</label>
                    <input type="text" name="requiredSkills" placeholder="Java, SQL, Communication" />
                </div>

                <div class="field full">
                    <label>Description *</label>
                    <textarea name="description" required></textarea>
                </div>

                <div class="field">
                    <label>Application Deadline</label>
                    <input type="date" name="applicationDeadline" />
                </div>
                <div class="field">
                    <label>Number of TAs</label>
                    <input type="number" name="numberOfTAs" min="1" value="1" />
                </div>

                <div class="field">
                    <label>Duration</label>
                    <input type="text" name="duration" value="One semester" />
                </div>
                <div class="field">
                    <label>Workload</label>
                    <input type="text" name="workloadHours" placeholder="e.g. 4h/week" />
                </div>
            </div>

            <div class="btn-row">
                <button class="btn" type="submit" name="publishNow" value="0">Save Draft</button>
                <button class="btn btn-success" type="submit" name="publishNow" value="1">Publish</button>
            </div>
        </form>
    </div>

    <!-- Posted Jobs -->
    <div class="section">
        <h2>Job Management</h2>
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
                    <th>Status</th>
                    <th>Actions</th>
                </tr>
                </thead>
                <tbody>
                <% for (Job job : jobs) {
                    String st = (job.getStatus() == null || job.getStatus().trim().isEmpty()) ? "Published" : job.getStatus().trim();
                %>
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
                        <td>
                            <% if ("Draft".equalsIgnoreCase(st)) { %>
                                <span class="status-pill status-draft">Draft</span>
                            <% } else { %>
                                <span class="status-pill status-published">Published</span>
                            <% } %>
                        </td>
                        <td>
                            <% if ("Draft".equalsIgnoreCase(st)) { %>
                                <form method="post" action="<%= request.getContextPath() %>/mo" style="display:inline;">
                                    <input type="hidden" name="action" value="publishJob" />
                                    <input type="hidden" name="jobId" value="<%= job.getId() %>" />
                                    <button class="btn btn-primary" type="submit">Publish</button>
                                </form>
                            <% } else { %>
                                <span class="muted">Published</span>
                            <% } %>
                        </td>
                    </tr>
                <% } %>
                </tbody>
            </table>
        <% } %>
    </div>

    <!-- Incoming Applications -->
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
                    <th>Profile</th>
                    <th>Feedback</th>
                    <th>Decision</th>
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
                        <td>
                            <%
                                String applicantQuery = URLEncoder.encode(app.userId, StandardCharsets.UTF_8);
                            %>
                            <a class="btn btn-primary btn-link" href="<%= request.getContextPath() %>/mo/applicant-profile?userId=<%= applicantQuery %>">View Profile</a>
                        </td>
                        <td><%= app.feedback == null ? "" : app.feedback %></td>
                        <td>
                            <% if ("Pending".equalsIgnoreCase(app.status)) { %>
                                <form class="decision-form" method="post" action="<%= request.getContextPath() %>/mo">
                                    <input type="hidden" name="appId" value="<%= app.id %>" />
                                    <input type="text" name="feedback" placeholder="Optional feedback" />
                                    <div>
                                        <button class="btn btn-success" type="submit" name="action" value="approveApp">Approve</button>
                                        <button class="btn btn-danger" type="submit" name="action" value="rejectApp">Reject</button>
                                    </div>
                                </form>
                            <% } else { %>
                                <span class="muted">Processed</span>
                            <% } %>
                        </td>
                    </tr>
                <% } %>
                </tbody>
            </table>
        <% } %>
    </div>

</div>
</body>
</html>