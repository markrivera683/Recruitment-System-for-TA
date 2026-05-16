<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.bupt.ta.model.Job" %>
<%@ page import="java.util.List" %>
<!doctype html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>Job Portal - TA Recruitment</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/app.css" />
</head>
<body>
<%
  List<Job> jobs = (List<Job>) request.getAttribute("jobs");
  if (jobs == null) jobs = new java.util.ArrayList<>();
  String q = (String) request.getAttribute("q");
  if (q == null) q = "";
  String sortBy = (String) request.getAttribute("sortBy");
  if (sortBy == null || sortBy.isEmpty()) sortBy = "postingDate";
  String escQ = q.replace("&", "&amp;").replace("\"", "&quot;").replace("<", "&lt;").replace(">", "&gt;");
  String ctx = request.getContextPath();
%>
<div class="page--top fade-in">
  <div class="layout-xl">

    <div class="page-header-row">
      <div class="header-left">
        <div class="logo">
          <svg viewBox="0 0 24 24"><path d="M22 10v6M2 10l10-5 10 5-10 5z"/><path d="M6 12v5c3 3 9 3 12 0v-5"/></svg>
        </div>
        <div>
          <h1 class="page-title">TA Job Portal</h1>
          <p class="page-subtitle">Browse available TA positions and view role details</p>
        </div>
      </div>
      <a class="link-pill" href="<%= ctx %>/profile">
        <svg viewBox="0 0 24 24"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
        Profile
      </a>
    </div>

    <form method="get" action="<%= ctx %>/job" class="app-summary-row">
      <div class="job-search-wrap">
        <div class="search-panel">
          <div class="search-row">
            <input type="text" name="q" value="<%= escQ %>" placeholder="Search jobs..." />
            <button type="submit" class="btn-ghost">Search</button>
          </div>
        </div>
      </div>
      <div class="filter-panel filter-panel--sort">
        <div class="filter-panel-label">Sort by</div>
        <div class="sort-row sort-row--panel">
          <select id="sortBy" name="sortBy" onchange="this.form.submit()" aria-label="Sort jobs by">
            <option value="moduleName" <%= "moduleName".equals(sortBy) ? "selected" : "" %>>Module Name</option>
            <option value="postingDate" <%= "postingDate".equals(sortBy) ? "selected" : "" %>>Posting Date</option>
            <option value="activityType" <%= "activityType".equals(sortBy) ? "selected" : "" %>>Activity Type</option>
            <option value="favorited" <%= "favorited".equals(sortBy) ? "selected" : "" %>>Favorited</option>
          </select>
        </div>
      </div>
    </form>

    <div class="job-ai-row">
      <button type="button" class="btn-ai" id="aiRecBtn">
        <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <circle cx="12" cy="14" r="4"/><path d="M12 2a4 4 0 0 1 4 4c0 1.95-1.4 3.58-3.25 3.93"/><path d="M8.24 9.93A4 4 0 0 1 12 2"/><path d="M12 18v4"/><path d="M8 22h8"/>
        </svg>
        AI Smart Recommendation
      </button>
    </div>

    <div id="aiRecPanel" class="ai-panel ai-panel--ok" style="display:none;margin-bottom:1.25rem;">
      <div class="ai-panel-header">
        <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <circle cx="12" cy="14" r="4"/><path d="M12 2a4 4 0 0 1 4 4c0 1.95-1.4 3.58-3.25 3.93"/><path d="M8.24 9.93A4 4 0 0 1 12 2"/>
        </svg>
        <span>AI Recommendation</span>
        <span class="ai-model-tag" id="aiRecModel"></span>
      </div>
      <div class="ai-panel-body">
        <p class="ai-stream-hint" id="aiRecHint" style="display:none;margin:0 0 .5rem;font-size:.8125rem;color:#64748b;">Streaming…</p>
        <div id="aiRecMd" class="ai-md"></div>
        <div id="aiRecErr" class="ai-error" style="display:none;"></div>
      </div>
    </div>

    <div class="app-grid job-portal-grid">
        <% if (jobs.isEmpty()) { %>
          <div class="empty-state">
            <p class="app-name"><%= "favorited".equals(sortBy) ? "No favorited jobs yet" : "No matching jobs found" %></p>
            <p class="page-subtitle"><%= "favorited".equals(sortBy) ? "Save jobs from the detail page to see them here" : "Try adjusting your search criteria" %></p>
          </div>
        <% } else { %>
          <%
            for (Job job : jobs) {
              List<String> skills = job.getRequiredSkills();
              if (skills == null) skills = new java.util.ArrayList<>();
              String jobId = job.getId() == null ? "" : job.getId();
              String module = job.getModuleName() == null ? "" : job.getModuleName();
              String act = job.getActivityType() == null ? "" : job.getActivityType();
              String desc = job.getDescription() == null ? "" : job.getDescription();
              String deadline = job.getApplicationDeadline() == null ? "" : job.getApplicationDeadline();
              String numTa = job.getNumberOfTAs() == null ? "" : job.getNumberOfTAs();
              String moduleEsc = module.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
              String actEsc = act.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
              String descEsc = desc.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
              String deadlineEsc = deadline.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
              String numTaEsc = numTa.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
          %>
            <div class="job-card">
              <div class="job-card-header">
                <h3 class="job-card-title"><%= moduleEsc %></h3>
                <span class="chip"><%= actEsc %></span>
              </div>

              <div class="skill-wrap">
                <p class="job-meta" style="margin-bottom:.375rem;">Required Skills:</p>
                <div style="display:flex;flex-wrap:wrap;gap:.375rem;">
                  <%
                    if (skills.isEmpty()) {
                  %>
                    <span class="page-subtitle">No required skills</span>
                  <%
                    } else {
                      for (String s : skills) {
                        if (s == null || s.isEmpty()) continue;
                        String esc = s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
                  %>
                    <span class="chip--skill"><%= esc %></span>
                  <%
                      }
                    }
                  %>
                </div>
              </div>

              <p class="job-desc"><%= descEsc %></p>

              <div class="job-footer">
                <div class="job-meta">
                  <div>Deadline: <%= deadlineEsc.isEmpty() ? "TBC" : deadlineEsc %></div>
                  <div><%= numTaEsc.isEmpty() ? "1" : numTaEsc %> positions</div>
                </div>
                <a href="${pageContext.request.contextPath}/job?id=<%= jobId %>" class="btn btn-primary btn-sm">
                  View Details
                </a>
              </div>
            </div>
          <%
            }
          %>
        <% } %>
    </div>

  </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/marked@12.0.2/marked.min.js"></script>
<script src="<%= ctx %>/static/js/ai-stream.js"></script>
<script>
(function () {
  var ctx = '<%= ctx %>';
  var btn = document.getElementById('aiRecBtn');
  var panel = document.getElementById('aiRecPanel');
  var mdEl = document.getElementById('aiRecMd');
  var errEl = document.getElementById('aiRecErr');
  var modelEl = document.getElementById('aiRecModel');
  var hint = document.getElementById('aiRecHint');
  if (!btn || !window.TaAiStream) return;
  btn.addEventListener('click', function () {
    var qInput = document.querySelector('.app-summary-row input[name="q"]');
    var sortSel = document.getElementById('sortBy');
    var q = qInput ? qInput.value : '';
    var sortBy = sortSel ? sortSel.value : 'postingDate';
    panel.style.display = 'block';
    panel.className = 'ai-panel ai-panel--ok';
    errEl.style.display = 'none';
    errEl.textContent = '';
    mdEl.innerHTML = '';
    modelEl.textContent = '';
    hint.style.display = 'block';
    btn.disabled = true;
    var text = '';
    var params = new URLSearchParams();
    params.set('feature', 'recommendation');
    params.set('q', q);
    params.set('sortBy', sortBy);
    TaAiStream.consume(ctx + '/api/ai/stream?' + params.toString(), {
      onMeta: function (m) { modelEl.textContent = m || ''; },
      onDelta: function (d) {
        text += d;
        TaAiStream.renderMarkdown(mdEl, text, typeof marked !== 'undefined' ? marked : null);
      },
      onDone: function () {
        hint.style.display = 'none';
        btn.disabled = false;
      },
      onError: function (e) {
        hint.style.display = 'none';
        errEl.style.display = 'block';
        errEl.textContent = e;
        panel.className = 'ai-panel ai-panel--err';
        btn.disabled = false;
      }
    });
  });
})();
</script>
</body>
</html>
