<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.bupt.ta.model.Roles" %>
<%@ page import="com.bupt.ta.model.User" %>
<%@ page import="com.bupt.ta.model.Application" %>
<%@ page import="com.bupt.ta.model.Job" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.LinkedHashMap" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Collections" %>
<%
  @SuppressWarnings("unchecked")
  List<User> users = (List<User>) request.getAttribute("users");
  if (users == null) users = java.util.Collections.emptyList();
  @SuppressWarnings("unchecked")
  List<Application> applications = (List<Application>) request.getAttribute("applications");
  if (applications == null) applications = java.util.Collections.emptyList();
  @SuppressWarnings("unchecked")
  List<Job> jobList = (List<Job>) request.getAttribute("jobs");
  if (jobList == null) jobList = java.util.Collections.emptyList();

  User admin = (User) session.getAttribute("user");
  String adminName = admin != null && admin.name != null && !admin.name.isEmpty() ? admin.name : "Admin";
  String adminEmail = admin != null && admin.email != null ? admin.email : "";
  String avatarLetter = "?";
  if (admin != null) {
    if (admin.name != null && !admin.name.trim().isEmpty()) {
      avatarLetter = admin.name.trim().substring(0, 1).toUpperCase();
    } else if (admin.email != null && !admin.email.isEmpty()) {
      avatarLetter = admin.email.substring(0, 1).toUpperCase();
    }
  }

  int totalTa = 0;
  for (User u : users) {
    if (Roles.TA.equals(u.role)) totalTa++;
  }
  int totalApplications = applications.size();
  Map<String, Integer> statusMap = new LinkedHashMap<>();
  statusMap.put("Pending", 0);
  statusMap.put("Accepted", 0);
  statusMap.put("Rejected", 0);
  Map<String, Integer> moduleMap = new LinkedHashMap<>();
  Map<String, Integer> monthMap = new LinkedHashMap<>();
  for (Application a : applications) {
    String status = a.status == null || a.status.trim().isEmpty() ? "Pending" : a.status.trim();
    statusMap.put(status, statusMap.getOrDefault(status, 0) + 1);
    String moduleName = a.moduleName == null || a.moduleName.trim().isEmpty() ? "Unknown Module" : a.moduleName.trim();
    moduleMap.put(moduleName, moduleMap.getOrDefault(moduleName, 0) + 1);
    String month = "Unknown";
    if (a.applicationDate != null && a.applicationDate.length() >= 7) {
      month = a.applicationDate.substring(0, 7);
    }
    monthMap.put(month, monthMap.getOrDefault(month, 0) + 1);
  }
  List<Map.Entry<String, Integer>> topModules = new ArrayList<>(moduleMap.entrySet());
  Collections.sort(topModules, (a, b) -> Integer.compare(b.getValue(), a.getValue()));
  if (topModules.size() > 5) topModules = topModules.subList(0, 5);
  List<Map.Entry<String, Integer>> monthTrend = new ArrayList<>(monthMap.entrySet());
  Collections.sort(monthTrend, (a, b) -> a.getKey().compareTo(b.getKey()));
  int maxMonthCount = 1;
  for (Map.Entry<String, Integer> e : monthTrend) {
    if (e.getValue() > maxMonthCount) maxMonthCount = e.getValue();
  }
  String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>TA Recruitment System - Admin Dashboard</title>
  <link rel="stylesheet" href="<%= ctx %>/static/css/admin-dashboard.css" />
</head>
<body>
  <header class="header">
    <div class="header-row">
      <div>
        <h1 class="title">TA Recruitment System</h1>
        <p class="subtitle">Admin Dashboard</p>
        <div class="header-actions">
          <a href="<%= ctx %>/admin/ai-demo">AI Demo</a>
          <span class="header-sep">|</span>
          <a href="<%= ctx %>/profile">Profile</a>
          <span class="header-sep">|</span>
          <a href="<%= ctx %>/logout">Logout</a>
        </div>
      </div>
      <div class="profile">
        <div class="header-user">
          <p class="header-user-name"><%= adminName %></p>
          <p class="header-user-email"><%= adminEmail %></p>
        </div>
        <div class="avatar" aria-hidden="true"><%= avatarLetter %></div>
      </div>
    </div>
  </header>

  <%
    String adminMessage = (String) request.getAttribute("adminMessage");
  %>
  <% if (adminMessage != null && !adminMessage.trim().isEmpty()) { %>
  <div class="container" style="padding-bottom:0">
    <p class="admin-notice"><%= adminMessage.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;") %></p>
  </div>
  <% } %>

  <main class="container">
    <section class="grid-3">
      <article class="card">
        <div class="card-header">
          <h2 class="card-title">Total TAs</h2>
          <div class="icon-wrap bg-blue" aria-hidden="true">&#128101;</div>
        </div>
        <div class="card-content"><div class="stat-value"><%= totalTa %></div></div>
      </article>
      <article class="card">
        <div class="card-header">
          <h2 class="card-title">Total Jobs</h2>
          <div class="icon-wrap bg-green" aria-hidden="true">&#128188;</div>
        </div>
        <div class="card-content"><div class="stat-value"><%= jobList.size() %></div></div>
      </article>
      <article class="card">
        <div class="card-header">
          <h2 class="card-title">Total Applications</h2>
          <div class="icon-wrap bg-purple" aria-hidden="true">&#128221;</div>
        </div>
        <div class="card-content"><div class="stat-value"><%= totalApplications %></div></div>
      </article>
    </section>

    <section class="card">
      <div class="card-header"><h2 class="card-title">Application Status Analysis</h2></div>
      <div class="card-content">
        <p class="tooltip-small" style="margin-top:0">Real-time distribution from submitted applications.</p>
        <div class="status-grid">
          <div class="status-item">
            <p class="tooltip-small status-label">Pending</p>
            <p class="status-value"><%= statusMap.getOrDefault("Pending", 0) %></p>
          </div>
          <div class="status-item">
            <p class="tooltip-small status-label">Accepted</p>
            <p class="status-value text-green"><%= statusMap.getOrDefault("Accepted", 0) %></p>
          </div>
          <div class="status-item">
            <p class="tooltip-small status-label">Rejected</p>
            <p class="status-value text-red"><%= statusMap.getOrDefault("Rejected", 0) %></p>
          </div>
        </div>
      </div>
    </section>

    <section class="grid-2">
      <section class="card">
        <div class="card-header"><h2 class="card-title">Top Modules by Applications</h2></div>
        <div class="card-content">
          <% if (topModules.isEmpty()) { %>
            <p class="tooltip-small">No application data available.</p>
          <% } else { %>
          <table class="table">
            <thead>
              <tr><th>Module</th><th>Applications</th></tr>
            </thead>
            <tbody>
              <% for (Map.Entry<String, Integer> item : topModules) { %>
              <tr>
                <td><%= item.getKey() %></td>
                <td><span class="badge badge-open"><%= item.getValue() %></span></td>
              </tr>
              <% } %>
            </tbody>
          </table>
          <% } %>
        </div>
      </section>

      <section class="card">
        <div class="card-header"><h2 class="card-title">Monthly Application Trend</h2></div>
        <div class="card-content">
          <% if (monthTrend.isEmpty()) { %>
            <p class="tooltip-small">No trend data available.</p>
          <% } else { %>
          <div class="bar-list">
            <% for (Map.Entry<String, Integer> m : monthTrend) {
                 int width = (int) Math.round((m.getValue() * 100.0) / maxMonthCount);
            %>
            <div class="bar-row">
              <div class="bar-meta"><span><%= m.getKey() %></span><span><%= m.getValue() %></span></div>
              <div class="bar-track"><div class="bar-fill" style="width:<%= width %>%"></div></div>
            </div>
            <% } %>
          </div>
          <% } %>
        </div>
      </section>
    </section>

    <section class="card">
      <div class="card-header"><h2 class="card-title">TA Workload Overview</h2></div>
      <div class="card-content">
        <table class="table">
          <thead>
            <tr><th>TA Name</th><th>Email</th><th>Role</th></tr>
          </thead>
          <tbody>
            <% if (users.isEmpty()) { %>
            <tr><td colspan="3" class="tooltip-small">No user data available.</td></tr>
            <% } else { %>
            <% for (User u : users) { %>
            <tr>
              <td><%= u.name == null ? "-" : u.name %></td>
              <td><%= u.email == null ? "-" : u.email %></td>
              <td><span class="badge badge-active"><%= u.role == null ? "-" : u.role %></span></td>
            </tr>
            <% } %>
            <% } %>
          </tbody>
        </table>
      </div>
    </section>

    <section class="card">
      <div class="card-header"><h2 class="card-title">Management</h2></div>
      <div class="card-content">
        <div class="tab-list" role="tablist">
          <button class="tab-btn active" type="button" data-tab="users">User Management</button>
          <button class="tab-btn" type="button" data-tab="jobs">Job Management</button>
          <button class="tab-btn" type="button" data-tab="export">Data Export</button>
        </div>

        <div id="users" class="tab-content active">
          <% if (users.isEmpty()) { %>
            <p class="tooltip-small">No registered users yet.</p>
          <% } else { %>
          <table class="table">
            <thead><tr><th>Name</th><th>Role</th><th>Status</th><th>Actions</th></tr></thead>
            <tbody>
              <% for (User u : users) {
                   String r = u.role != null ? u.role : "";
              %>
              <tr>
                <td><%= u.name == null ? "" : u.name %></td>
                <td><%= r %></td>
                <td>
                  <% if (u.active) { %>
                    <span class="badge badge-active">Active</span>
                  <% } else { %>
                    <span class="badge badge-inactive">Inactive</span>
                  <% } %>
                </td>
                <td>
                  <form method="post" action="<%= ctx %>/admin/users" style="display:inline" onsubmit="return confirm('Deactivate this user? They will not be able to log in.');">
                    <input type="hidden" name="action" value="deactivate" />
                    <input type="hidden" name="userId" value="<%= u.id %>" />
                    <button type="submit" class="button" <%= u.active ? "" : "disabled" %>>Deactivate</button>
                  </form>
                  <form method="post" action="<%= ctx %>/admin/users" style="display:inline" onsubmit="return confirm('Permanently delete this user and their applications, profile, and CV files?');">
                    <input type="hidden" name="action" value="delete" />
                    <input type="hidden" name="userId" value="<%= u.id %>" />
                    <button type="submit" class="button button-danger">Delete</button>
                  </form>
                </td>
              </tr>
              <% } %>
            </tbody>
          </table>
          <% } %>
        </div>

        <div id="jobs" class="tab-content">
          <p class="tooltip-small">Jobs from <code>WEB-INF/data/jobs.json</code> (same list as applicant job browser).</p>
          <% if (jobList.isEmpty()) { %>
            <p class="tooltip-small">No jobs defined.</p>
          <% } else { %>
          <table class="table">
            <thead><tr><th>Module</th><th>Activity</th><th>Posted</th><th>Actions</th></tr></thead>
            <tbody>
              <% for (Job j : jobList) {
                   String mid = j.getId() == null ? "" : j.getId();
              %>
              <tr>
                <td><%= j.getModuleName() == null ? "-" : j.getModuleName() %></td>
                <td><%= j.getActivityType() == null ? "-" : j.getActivityType() %></td>
                <td><%= j.getPostDate() == null ? "-" : j.getPostDate() %></td>
                <td>
                  <a class="button button-outline" href="<%= ctx %>/job?id=<%= mid %>">View</a>
                  <form method="post" action="<%= ctx %>/admin/jobs" style="display:inline" onsubmit="return confirm('Delete this job from jobs.json?');">
                    <input type="hidden" name="action" value="delete" />
                    <input type="hidden" name="jobId" value="<%= mid %>" />
                    <button type="submit" class="button button-danger">Delete</button>
                  </form>
                </td>
              </tr>
              <% } %>
            </tbody>
          </table>
          <% } %>
        </div>

        <div id="export" class="tab-content">
          <p class="tooltip-small">Download CSV (UTF-8). Open in Excel if needed.</p>
          <div class="export-bar">
            <a class="button button-primary" href="<%= ctx %>/admin/export?type=users">Export users (CSV)</a>
            <a class="button button-success" href="<%= ctx %>/admin/export?type=applications">Export applications (CSV)</a>
          </div>
        </div>
      </div>
    </section>
  </main>

  <script>
    function showTab(tabName) {
      document.querySelectorAll('.tab-btn').forEach(function (btn) {
        btn.classList.toggle('active', btn.getAttribute('data-tab') === tabName);
      });
      document.querySelectorAll('.tab-content').forEach(function (c) {
        c.classList.toggle('active', c.id === tabName);
      });
    }
    document.querySelectorAll('.tab-btn').forEach(function (btn) {
      btn.addEventListener('click', function () {
        showTab(btn.getAttribute('data-tab'));
      });
    });
  </script>
</body>
</html>
