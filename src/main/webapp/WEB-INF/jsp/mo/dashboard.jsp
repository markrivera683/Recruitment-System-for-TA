<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.nio.charset.StandardCharsets" %>
<%@ page import="com.bupt.ta.model.User" %>
<%@ page import="com.bupt.ta.model.Job" %>
<%@ page import="com.bupt.ta.model.Application" %>
<%@ page import="com.bupt.ta.model.MoWorkloadSnapshot" %>
<%@ page import="com.bupt.ta.model.TaWorkloadStats" %>
<%
    String ctx = request.getContextPath();
    if (ctx == null) ctx = "";
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

    @SuppressWarnings("unchecked")
    Map<String, MoWorkloadSnapshot> workloadSnapshots =
            (Map<String, MoWorkloadSnapshot>) request.getAttribute("workloadSnapshots");
    if (workloadSnapshots == null) workloadSnapshots = java.util.Collections.emptyMap();

    Boolean aiEnabledObj = (Boolean) request.getAttribute("aiEnabled");
    boolean aiEnabled = aiEnabledObj != null && aiEnabledObj.booleanValue();

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
            max-width: 1280px;
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
            width: 100%;
            margin-bottom: 8px;
        }

        /* ── Pending application cards ── */
        .pending-apps {
            display: flex;
            flex-direction: column;
            gap: 16px;
            margin-bottom: 28px;
        }
        .pending-card {
            border: 1px solid #e5e7eb;
            border-radius: 14px;
            overflow: hidden;
            background: #fff;
        }
        .pending-card-header {
            display: flex;
            flex-wrap: wrap;
            align-items: flex-start;
            justify-content: space-between;
            gap: 12px;
            padding: 16px 18px;
            background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
            border-bottom: 1px solid #e5e7eb;
        }
        .pending-card-title {
            margin: 0;
            font-size: 17px;
            font-weight: 700;
            color: #0f172a;
        }
        .pending-card-sub {
            margin: 4px 0 0;
            font-size: 13px;
            color: #64748b;
        }
        .pending-card-meta {
            display: flex;
            flex-wrap: wrap;
            gap: 8px;
            align-items: center;
        }
        .pending-card-body {
            padding: 16px 18px 18px;
            display: grid;
            grid-template-columns: minmax(0, 1fr) minmax(0, 1.4fr);
            gap: 18px;
        }
        .pending-side {
            display: flex;
            flex-direction: column;
            gap: 12px;
        }
        .pending-links {
            display: flex;
            flex-wrap: wrap;
            gap: 8px;
        }
        .pending-id {
            font-size: 11px;
            color: #94a3b8;
            word-break: break-all;
        }
        .workload-chips {
            display: flex;
            flex-wrap: wrap;
            gap: 8px;
        }
        .workload-chip {
            display: inline-flex;
            align-items: center;
            gap: 6px;
            padding: 6px 12px;
            border-radius: 999px;
            font-size: 12px;
            font-weight: 600;
            background: #f1f5f9;
            color: #334155;
            border: 1px solid #e2e8f0;
        }
        .workload-chip strong {
            font-size: 14px;
            color: #0f172a;
        }
        .workload-chip.is-warn {
            background: #fffbeb;
            border-color: #fcd34d;
            color: #92400e;
        }
        .workload-chip.is-warn strong {
            color: #b45309;
        }
        .mo-ai-advice {
            border: 1px solid #dbeafe;
            border-radius: 12px;
            background: #f8fafc;
            overflow: hidden;
        }
        .mo-ai-advice-head {
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 10px;
            padding: 10px 14px;
            background: #eff6ff;
            border-bottom: 1px solid #dbeafe;
        }
        .mo-ai-advice-title {
            font-size: 13px;
            font-weight: 700;
            color: #1e40af;
            margin: 0;
        }
        .rec-badge {
            display: inline-block;
            padding: 4px 10px;
            border-radius: 999px;
            font-size: 11px;
            font-weight: 700;
            letter-spacing: 0.02em;
            text-transform: uppercase;
        }
        .rec-badge.rec-approve { background: #dcfce7; color: #166534; }
        .rec-badge.rec-reject { background: #fee2e2; color: #991b1b; }
        .rec-badge.rec-caution { background: #fef3c7; color: #92400e; }
        .mo-ai-advice-body {
            padding: 12px 14px 14px;
        }
        .mo-ai-advice-loading {
            color: #64748b;
            font-size: 13px;
            display: flex;
            align-items: center;
            gap: 8px;
        }
        .mo-ai-advice-loading::before {
            content: "";
            width: 14px;
            height: 14px;
            border: 2px solid #cbd5e1;
            border-top-color: #2563eb;
            border-radius: 50%;
            animation: mo-spin 0.8s linear infinite;
        }
        @keyframes mo-spin { to { transform: rotate(360deg); } }
        .mo-ai-advice-error {
            color: #b91c1c;
            font-size: 13px;
        }
        .mo-ai-advice-md {
            line-height: 1.55;
            font-size: 13px;
            color: #334155;
            word-wrap: break-word;
            overflow-wrap: anywhere;
            white-space: normal;
        }
        .mo-ai-advice-md h2 {
            font-size: 12px;
            font-weight: 700;
            text-transform: uppercase;
            letter-spacing: 0.04em;
            color: #64748b;
            margin: 14px 0 6px;
            padding-bottom: 4px;
            border-bottom: 1px solid #e2e8f0;
        }
        .mo-ai-advice-md h2:first-child { margin-top: 0; }
        .mo-ai-advice-md p { margin: 0 0 8px; }
        .mo-ai-advice-md ul, .mo-ai-advice-md ol {
            margin: 0 0 8px;
            padding-left: 18px;
        }
        .mo-ai-advice-md li { margin-bottom: 4px; }
        .mo-ai-advice-md strong { color: #0f172a; }
        .mo-ai-disabled {
            font-size: 12px;
            color: #64748b;
            padding: 10px 12px;
            background: #f8fafc;
            border-radius: 8px;
            border: 1px dashed #cbd5e1;
        }
        .decision-form {
            margin-top: auto;
            padding-top: 12px;
            border-top: 1px solid #f1f5f9;
        }
        .decision-actions {
            display: flex;
            gap: 8px;
            flex-wrap: wrap;
        }
        .section-subtitle {
            font-size: 15px;
            font-weight: 600;
            color: #475569;
            margin: 0 0 12px;
        }
        .app-status-pill {
            display: inline-block;
            padding: 3px 10px;
            border-radius: 999px;
            font-size: 12px;
            font-weight: 600;
        }
        .app-status-pending { background: #fef3c7; color: #92400e; }
        .app-status-accepted { background: #dcfce7; color: #166534; }
        .app-status-rejected { background: #fee2e2; color: #991b1b; }
        .app-status-withdrawn { background: #f3f4f6; color: #4b5563; }
        .table-compact td, .table-compact th {
            font-size: 13px;
        }
        .table-compact .col-id {
            max-width: 120px;
            word-break: break-all;
            font-size: 11px;
            color: #64748b;
        }

        @media (max-width: 900px) {
            .pending-card-body {
                grid-template-columns: 1fr;
            }
            .form-grid {
                grid-template-columns: 1fr;
            }
            .decision-form input[type="text"] {
                width: 100%;
            }
            .table-scroll {
                display: block;
                overflow-x: auto;
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
        <% } else {
            int pendingReviewCount = 0;
            for (Application a : applications) {
                if ("Pending".equalsIgnoreCase(a.status)) pendingReviewCount++;
            }
        %>

        <% if (pendingReviewCount > 0) { %>
            <h3 class="section-subtitle">Pending review (<%= pendingReviewCount %>)</h3>
            <div class="pending-apps">
            <% for (Application app : applications) {
                if (!"Pending".equalsIgnoreCase(app.status)) continue;
                MoWorkloadSnapshot ws = workloadSnapshots.get(app.id);
                int acceptedCount = ws != null ? ws.acceptedCount : 0;
                int pendingCount = ws != null ? ws.pendingCount : 0;
                int potentialLoad = ws != null ? ws.potentialLoadIfApprove : 0;
                boolean highLoad = potentialLoad >= TaWorkloadStats.ASSIGNED_JOBS_WARNING_THRESHOLD;
                String applicantQuery = URLEncoder.encode(app.userId, StandardCharsets.UTF_8);
                String cvName = cvByUserId.get(app.userId);
                String displayName = ws != null && ws.applicantName != null ? ws.applicantName : app.userId;
            %>
                <article class="pending-card">
                    <div class="pending-card-header">
                        <div>
                            <h3 class="pending-card-title"><%= app.moduleName %></h3>
                            <p class="pending-card-sub">
                                <%= displayName %> &middot; <%= app.moduleCode %> &middot; <%= app.role %>
                                &middot; Applied <%= app.applicationDate %>
                            </p>
                        </div>
                        <div class="pending-card-meta">
                            <span class="app-status-pill app-status-pending">Pending</span>
                        </div>
                    </div>
                    <div class="pending-card-body">
                        <div class="pending-side">
                            <div class="pending-id">App <%= app.id %></div>
                            <div class="pending-links">
                                <% if (cvName != null) { %>
                                    <a class="btn btn-primary btn-link" href="<%= ctx %>/cv?userId=<%= applicantQuery %>" target="_blank" rel="noopener">Open CV</a>
                                <% } else { %>
                                    <span class="muted">No CV uploaded</span>
                                <% } %>
                                <a class="btn btn-primary btn-link" href="<%= ctx %>/mo/applicant-profile?userId=<%= applicantQuery %>">View Profile</a>
                            </div>
                            <% if (ws != null) { %>
                            <div class="workload-chips">
                                <span class="workload-chip">Accepted <strong><%= acceptedCount %></strong></span>
                                <span class="workload-chip">Pending <strong><%= pendingCount %></strong></span>
                                <span class="workload-chip<%= highLoad ? " is-warn" : "" %>">Potential load <strong><%= potentialLoad %></strong></span>
                            </div>
                            <% } %>
                            <form class="decision-form" method="post" action="<%= ctx %>/mo">
                                <input type="hidden" name="appId" value="<%= app.id %>" />
                                <input type="text" name="feedback" placeholder="Optional feedback for applicant" />
                                <div class="decision-actions">
                                    <button class="btn btn-success" type="submit" name="action" value="approveApp">Approve</button>
                                    <button class="btn btn-danger" type="submit" name="action" value="rejectApp">Reject</button>
                                </div>
                            </form>
                        </div>
                        <div class="pending-side">
                            <% if (aiEnabled) { %>
                            <div class="mo-ai-advice" id="mo-ai-<%= app.id %>" data-app-id="<%= app.id %>">
                                <div class="mo-ai-advice-head">
                                    <p class="mo-ai-advice-title">AI workload advice</p>
                                    <span class="rec-badge-slot"></span>
                                </div>
                                <div class="mo-ai-advice-body">
                                    <div class="mo-ai-advice-loading">Analyzing workload…</div>
                                    <div class="mo-ai-advice-md" style="display:none;"></div>
                                </div>
                            </div>
                            <% } else { %>
                            <div class="mo-ai-disabled">AI advice is disabled (LM_ENABLED=false).</div>
                            <% } %>
                        </div>
                    </div>
                </article>
            <% } %>
            </div>
        <% } %>

            <h3 class="section-subtitle">Processed applications</h3>
            <div class="table-scroll">
            <table class="table-compact">
                <thead>
                <tr>
                    <th>Module</th>
                    <th>Applicant</th>
                    <th>Role</th>
                    <th>Date</th>
                    <th>Status</th>
                    <th>CV</th>
                    <th>Profile</th>
                    <th>Feedback</th>
                </tr>
                </thead>
                <tbody>
                <% boolean anyProcessed = false;
                   for (Application app : applications) {
                    if ("Pending".equalsIgnoreCase(app.status)) continue;
                    anyProcessed = true;
                    String st = app.status == null ? "" : app.status.trim();
                    String stClass = "app-status-pending";
                    if ("Accepted".equalsIgnoreCase(st)) stClass = "app-status-accepted";
                    else if ("Rejected".equalsIgnoreCase(st)) stClass = "app-status-rejected";
                    else if ("Withdrawn".equalsIgnoreCase(st)) stClass = "app-status-withdrawn";
                    String applicantQuery = URLEncoder.encode(app.userId, StandardCharsets.UTF_8);
                    String cvName = cvByUserId.get(app.userId);
                %>
                    <tr>
                        <td>
                            <strong><%= app.moduleName %></strong><br>
                            <span class="muted"><%= app.moduleCode %></span>
                        </td>
                        <td class="col-id"><%= app.userId %></td>
                        <td><%= app.role %></td>
                        <td><%= app.applicationDate %></td>
                        <td><span class="app-status-pill <%= stClass %>"><%= app.status %></span></td>
                        <td>
                            <% if (cvName != null) { %>
                                <a href="<%= ctx %>/cv?userId=<%= applicantQuery %>" target="_blank" rel="noopener">CV</a>
                            <% } else { %>
                                <span class="muted">—</span>
                            <% } %>
                        </td>
                        <td>
                            <a class="btn btn-primary btn-link" href="<%= ctx %>/mo/applicant-profile?userId=<%= applicantQuery %>">Profile</a>
                        </td>
                        <td><%= app.feedback == null || app.feedback.isEmpty() ? "—" : app.feedback %></td>
                    </tr>
                <% }
                   if (!anyProcessed) { %>
                    <tr><td colspan="8" class="empty">No processed applications yet.</td></tr>
                <% } %>
                </tbody>
            </table>
            </div>
        <% } %>
    </div>

</div>
<% if (aiEnabled) { %>
<script src="https://cdn.jsdelivr.net/npm/marked@12.0.2/marked.min.js"></script>
<script src="<%= ctx %>/static/js/ai-stream.js"></script>
<script>
(function () {
  'use strict';
  var ctx = '<%= ctx %>';
  var maxConcurrent = 2;
  var queue = [];
  var active = 0;

  document.querySelectorAll('.mo-ai-advice[data-app-id]').forEach(function (panel) {
    queue.push(panel.getAttribute('data-app-id'));
  });

  function pumpQueue() {
    while (active < maxConcurrent && queue.length > 0) {
      var appId = queue.shift();
      active++;
      startAdviceStream(appId, function () {
        active--;
        pumpQueue();
      });
    }
  }

  function applyRecommendationBadge(panel, text) {
    if (!panel || !text) return;
    var slot = panel.querySelector('.rec-badge-slot');
    if (!slot) return;
    var m = text.match(/\*\*(Approve|Reject|Caution)\*\*/i);
    if (!m) {
      m = text.match(/Recommendation[^\n]*\n+\s*(Approve|Reject|Caution)/i);
    }
    if (!m) return;
    var label = m[1];
    var badge = document.createElement('span');
    badge.className = 'rec-badge rec-' + label.toLowerCase();
    badge.textContent = label;
    slot.innerHTML = '';
    slot.appendChild(badge);
  }

  function startAdviceStream(appId, done) {
    var panel = document.getElementById('mo-ai-' + appId);
    if (!panel) {
      done();
      return;
    }
    var loadingEl = panel.querySelector('.mo-ai-advice-loading');
    var mdEl = panel.querySelector('.mo-ai-advice-md');
    var text = '';
    var url = ctx + '/api/ai/stream?feature=moWorkloadAdvice&applicationId=' + encodeURIComponent(appId);

    TaAiStream.consume(url, {
      onDelta: function (delta) {
        text += delta;
        if (loadingEl) loadingEl.style.display = 'none';
        if (mdEl) {
          mdEl.style.display = 'block';
          TaAiStream.renderMarkdown(mdEl, text, typeof marked !== 'undefined' ? marked : null);
        }
      },
      onDone: function () {
        if (!text && loadingEl) {
          loadingEl.textContent = 'No advice returned.';
          loadingEl.style.display = 'flex';
        } else {
          applyRecommendationBadge(panel, text);
        }
        done();
      },
      onError: function (err) {
        if (loadingEl) {
          loadingEl.className = 'mo-ai-advice-error';
          loadingEl.textContent = err || 'AI advice failed.';
          loadingEl.style.display = 'block';
        }
        done();
      }
    });
  }

  if (queue.length > 0) {
    pumpQueue();
  }
})();
</script>
<% } %>
</body>
</html>