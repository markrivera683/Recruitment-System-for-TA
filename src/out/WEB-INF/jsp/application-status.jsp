<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.bupt.ta.model.Application" %>
<%@ page import="java.util.List" %>
<%
  @SuppressWarnings("unchecked")
  List<Application> apps = (List<Application>) request.getAttribute("applications");
  if (apps == null) apps = java.util.Collections.emptyList();
  String filter      = (String) request.getAttribute("filter");       if (filter == null) filter = "All";
  Long countPending  = (Long)   request.getAttribute("countPending");  if (countPending  == null) countPending  = 0L;
  Long countAccepted = (Long)   request.getAttribute("countAccepted"); if (countAccepted == null) countAccepted = 0L;
  Long countRejected = (Long)   request.getAttribute("countRejected"); if (countRejected == null) countRejected = 0L;
  String ctx = request.getContextPath();
%>
<!doctype html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>My Applications - TA Recruitment</title>
  <link rel="stylesheet" href="<%= ctx %>/static/css/app.css" />
  <style>
    .summary-grid{display:grid;grid-template-columns:repeat(3,1fr);gap:.5rem;margin-bottom:1.25rem}
    .summary-card{border-radius:.75rem;border:1px solid;padding:.75rem;text-align:center}
    .summary-card .stat{font-size:1.375rem;font-weight:700}
    .summary-card .lbl{font-size:.75rem;font-weight:600;margin-top:.125rem}
    .s-pending{background:#fffbeb;border-color:#fde68a;color:#b45309}
    .s-accepted{background:#f0fdf4;border-color:#bbf7d0;color:#166534}
    .s-rejected{background:#fef2f2;border-color:#fecaca;color:#991b1b}
    .filter-bar{display:flex;gap:.375rem;padding:.25rem;background:#fff;border:1px solid #e2e8f0;border-radius:.75rem;margin-bottom:1rem;box-shadow:0 1px 2px rgba(0,0,0,.04)}
    .filter-btn{flex:1;padding:.375rem .25rem;border:none;border-radius:.5rem;font-size:.75rem;font-weight:500;cursor:pointer;background:transparent;color:#64748b;font-family:inherit;text-decoration:none;text-align:center;display:inline-block;transition:background .15s,color .15s}
    .filter-btn:hover{background:#f1f5f9;color:#0f172a}
    .filter-btn.active{background:#2563eb;color:#fff;box-shadow:0 1px 3px rgba(37,99,235,.3)}
    .app-card{background:#fff;border-radius:1rem;border:1px solid #e2e8f0;box-shadow:0 1px 3px rgba(0,0,0,.05);overflow:hidden;margin-bottom:.75rem}
    .app-accent{height:4px;width:100%}
    .acc-Pending{background:#fbbf24}.acc-Accepted{background:#22c55e}.acc-Rejected{background:#f87171}
    .app-body{padding:1rem}
    .app-top{display:flex;justify-content:space-between;align-items:flex-start;gap:.75rem;margin-bottom:.5rem}
    .module-code{font-size:.75rem;font-weight:600;color:#2563eb;background:#eff6ff;border:1px solid #bfdbfe;border-radius:.375rem;padding:.125rem .5rem}
    .status-badge{display:inline-flex;align-items:center;gap:.25rem;font-size:.75rem;font-weight:700;padding:.25rem .625rem;border-radius:9999px;border:1px solid;white-space:nowrap}
    .b-Pending{background:#fffbeb;color:#b45309;border-color:#fde68a}
    .b-Accepted{background:#f0fdf4;color:#166534;border-color:#bbf7d0}
    .b-Rejected{background:#fef2f2;color:#991b1b;border-color:#fecaca}
    .app-name{font-size:.9375rem;font-weight:600;color:#0f172a;margin-bottom:.25rem}
    .app-meta{display:flex;align-items:center;gap:.375rem;font-size:.75rem;color:#64748b;margin-bottom:.25rem}
    .app-meta svg{width:13px;height:13px;flex-shrink:0;fill:none;stroke:#94a3b8;stroke-width:2;stroke-linecap:round;stroke-linejoin:round}
    .fb-toggle{margin-top:.625rem;padding-top:.625rem;border-top:1px solid #f1f5f9}
    .fb-toggle button{background:none;border:none;cursor:pointer;font-size:.75rem;font-weight:600;color:#2563eb;font-family:inherit;padding:0}
    .fb-box{margin-top:.5rem;font-size:.75rem;padding:.5rem .75rem;border-radius:.5rem;border:1px solid;display:none}
    .fb-Pending{background:#fffbeb;color:#b45309;border-color:#fde68a}
    .fb-Accepted{background:#f0fdf4;color:#166534;border-color:#bbf7d0}
    .fb-Rejected{background:#fef2f2;color:#991b1b;border-color:#fecaca}
    .empty-state{text-align:center;padding:2.5rem 1rem;background:#fff;border-radius:1rem;border:1px solid #e2e8f0}
    .empty-icon{width:48px;height:48px;background:#f1f5f9;border-radius:50%;display:flex;align-items:center;justify-content:center;margin:0 auto 1rem}
    .empty-icon svg{width:24px;height:24px;fill:none;stroke:#94a3b8;stroke-width:2;stroke-linecap:round;stroke-linejoin:round}
  </style>
</head>
<body>
<div class="page--top">
  <div class="container">

    <!-- Header -->
    <div class="page-header page-header--sm">
      <div class="logo-wrap">
        <div class="logo logo--sm">
          <svg viewBox="0 0 24 24"><path d="M22 10v6M2 10l10-5 10 5-10 5z"/><path d="M6 12v5c3 3 9 3 12 0v-5"/></svg>
        </div>
      </div>
      <h1 class="page-title">My Applications</h1>
      <p class="page-subtitle">Track your TA application statuses</p>
    </div>

    <!-- Summary cards -->
    <div class="summary-grid">
      <div class="summary-card s-pending">
        <div class="stat"><%= countPending %></div>
        <div class="lbl">Pending</div>
      </div>
      <div class="summary-card s-accepted">
        <div class="stat"><%= countAccepted %></div>
        <div class="lbl">Accepted</div>
      </div>
      <div class="summary-card s-rejected">
        <div class="stat"><%= countRejected %></div>
        <div class="lbl">Rejected</div>
      </div>
    </div>

    <!-- Filter tabs -->
    <div class="filter-bar">
      <% String[] filters = {"All","Pending","Accepted","Rejected"};
         for (String f : filters) { %>
        <a href="<%= ctx %>/applications?filter=<%= f %>"
           class="filter-btn <%= f.equals(filter) ? "active" : "" %>"><%= f %></a>
      <% } %>
    </div>

    <!-- Application cards -->
    <% if (apps.isEmpty()) { %>
      <div class="empty-state">
        <div class="empty-icon">
          <svg viewBox="0 0 24 24"><path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"/><path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"/></svg>
        </div>
        <p style="font-size:.875rem;color:#64748b;">No applications found</p>
      </div>
    <% } else {
         for (Application a : apps) {
           String st = a.status != null ? a.status : "Pending";
           String dot = st.equals("Accepted") ? "&#10003;" : st.equals("Rejected") ? "&#10007;" : "&#8987;";
    %>
      <div class="app-card">
        <div class="app-accent acc-<%= st %>"></div>
        <div class="app-body">
          <div class="app-top">
            <span class="module-code"><%= a.moduleCode != null ? a.moduleCode : "" %></span>
            <span class="status-badge b-<%= st %>"><%= dot %>&nbsp;<%= st %></span>
          </div>
          <div class="app-name"><%= a.moduleName != null ? a.moduleName : "" %></div>
          <div class="app-meta">
            <svg viewBox="0 0 24 24"><path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"/><path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"/></svg>
            <%= a.role != null ? a.role : "" %>
          </div>
          <div class="app-meta">
            <svg viewBox="0 0 24 24"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
            Applied <%= a.applicationDate != null ? a.applicationDate : "" %>
          </div>
          <% if (a.feedback != null && !a.feedback.isEmpty()) { %>
          <div class="fb-toggle">
            <button type="button" onclick="toggleFb(this)">&#9660; View feedback</button>
            <div class="fb-box fb-<%= st %>"><%= a.feedback %></div>
          </div>
          <% } %>
        </div>
      </div>
    <% } } %>

    <!-- Back link -->
    <div style="margin-top:1.5rem;text-align:center;">
      <a class="back-link" href="<%= ctx %>/profile">
        <svg viewBox="0 0 24 24"><polyline points="15 18 9 12 15 6"/></svg>
        Back to profile
      </a>
    </div>

  </div>
</div>
<script>
function toggleFb(btn) {
  var box = btn.nextElementSibling;
  var open = box.style.display === 'block';
  box.style.display = open ? 'none' : 'block';
  btn.innerHTML = open ? '&#9660; View feedback' : '&#9650; Hide feedback';
}
</script>
</body>
</html>
