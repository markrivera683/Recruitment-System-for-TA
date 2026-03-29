<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.bupt.ta.model.Roles" %>
<%@ page import="com.bupt.ta.model.User" %>
<%@ page import="java.util.List" %>
<%
  @SuppressWarnings("unchecked")
  List<User> users = (List<User>) request.getAttribute("users");
  if (users == null) users = java.util.Collections.emptyList();

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
        <div class="card-content"><div class="stat-value">0</div></div>
      </article>
      <article class="card">
        <div class="card-header">
          <h2 class="card-title">Total Applications</h2>
          <div class="icon-wrap bg-purple" aria-hidden="true">&#128221;</div>
        </div>
        <div class="card-content"><div class="stat-value">0</div></div>
      </article>
    </section>

    <section class="card">
      <div class="card-header"><h2 class="card-title">TA Workload Overview</h2></div>
      <div class="card-content">
        <p class="tooltip-small" style="margin-top:0">Sample preview (no backend data yet).</p>
        <table class="table">
          <thead>
            <tr><th>TA Name</th><th>Assigned Jobs</th><th>Status</th></tr>
          </thead>
          <tbody>
            <tr><td>Alice Johnson</td><td>5</td><td><span class="badge badge-closed">Overloaded</span></td></tr>
            <tr><td>Bob Smith</td><td>3</td><td><span class="badge badge-open">Normal</span></td></tr>
            <tr><td>Carol Williams</td><td>1</td><td><span class="badge badge-active">Low Workload</span></td></tr>
            <tr><td>David Brown</td><td>4</td><td><span class="badge badge-open">Normal</span></td></tr>
            <tr><td>Emma Davis</td><td>6</td><td><span class="badge badge-closed">Overloaded</span></td></tr>
            <tr><td>Frank Miller</td><td>2</td><td><span class="badge badge-active">Low Workload</span></td></tr>
            <tr><td>Grace Wilson</td><td>3</td><td><span class="badge badge-open">Normal</span></td></tr>
            <tr><td>Henry Moore</td><td>1</td><td><span class="badge badge-active">Low Workload</span></td></tr>
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
