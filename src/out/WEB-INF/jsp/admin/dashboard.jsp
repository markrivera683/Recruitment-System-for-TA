<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.bupt.ta.model.Roles" %>
<%@ page import="com.bupt.ta.model.User" %>
<%@ page import="com.bupt.ta.model.Application" %>
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
  int totalJobs = moduleMap.size();
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
    <div style="display:flex; align-items:center; justify-content:space-between; flex-wrap:wrap; gap:1rem;">
      <div>
        <h1 class="title">TA Recruitment System</h1>
        <p class="subtitle">Admin Dashboard</p>
        <div class="header-actions">
          <a href="<%= ctx %>/profile">Profile</a>
          <span style="color:#d1d5db">|</span>
          <a href="<%= ctx %>/logout">Logout</a>
        </div>
      </div>
      <div class="profile">
        <div style="text-align:right;">
          <p style="font-weight:600; margin:0;"><%= adminName %></p>
          <p style="font-size:0.75rem; color:#6b7280; margin:0.25rem 0 0;"><%= adminEmail %></p>
        </div>
        <div class="avatar" aria-hidden="true"><%= avatarLetter %></div>
      </div>
    </div>
  </header>

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
        <div class="card-content"><div class="stat-value"><%= totalJobs %></div></div>
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
                <td><span class="badge badge-active">Active</span></td>
                <td>
                  <button type="button" class="button" onclick="alert('Deactivate user: <%= u.id %>')">Deactivate</button>
                  <button type="button" class="button button-danger" onclick="alert('Delete user: <%= u.id %>')">Delete</button>
                </td>
              </tr>
              <% } %>
            </tbody>
          </table>
          <% } %>
        </div>

        <div id="jobs" class="tab-content">
          <p class="tooltip-small">Sample jobs (prototype).</p>
          <table class="table">
            <thead><tr><th>Job Title</th><th>Course</th><th>Status</th><th>Actions</th></tr></thead>
            <tbody>
              <tr><td>Grading Assistant</td><td>CS 101</td><td><span class="badge badge-open">Open</span></td><td>
                <button type="button" class="button" onclick="alert('Edit job 1')">Edit</button>
                <button type="button" class="button button-danger" onclick="alert('Delete job 1')">Delete</button>
              </td></tr>
              <tr><td>Lab Instructor</td><td>CS 201</td><td><span class="badge badge-filled">Filled</span></td><td>
                <button type="button" class="button" onclick="alert('Edit job 2')">Edit</button>
                <button type="button" class="button button-danger" onclick="alert('Delete job 2')">Delete</button>
              </td></tr>
              <tr><td>Office Hours Support</td><td>CS 301</td><td><span class="badge badge-open">Open</span></td><td>
                <button type="button" class="button" onclick="alert('Edit job 3')">Edit</button>
                <button type="button" class="button button-danger" onclick="alert('Delete job 3')">Delete</button>
              </td></tr>
              <tr><td>Project Mentor</td><td>CS 401</td><td><span class="badge badge-open">Open</span></td><td>
                <button type="button" class="button" onclick="alert('Edit job 4')">Edit</button>
                <button type="button" class="button button-danger" onclick="alert('Delete job 4')">Delete</button>
              </td></tr>
              <tr><td>Tutorial Leader</td><td>CS 102</td><td><span class="badge badge-closed">Closed</span></td><td>
                <button type="button" class="button" onclick="alert('Edit job 5')">Edit</button>
                <button type="button" class="button button-danger" onclick="alert('Delete job 5')">Delete</button>
              </td></tr>
            </tbody>
          </table>
        </div>

        <div id="export" class="tab-content">
          <p class="tooltip-small">Export system data in your preferred format</p>
          <button type="button" class="button button-primary" onclick="alert('Exporting CSV...')">Export CSV</button>
          <button type="button" class="button button-success" onclick="alert('Exporting Excel...')">Export Excel</button>
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
