<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.bupt.ta.model.ApplicantProfile" %>
<%@ page import="com.bupt.ta.model.User" %>
<%@ page import="com.bupt.ta.model.EducationEntry" %>
<%@ page import="com.bupt.ta.service.ProfileService" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<!doctype html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>Applicant Profile — TA Recruitment</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/app.css" />
</head>
<body>
<%
  ApplicantProfile p = (ApplicantProfile) request.getAttribute("profile");
  User u = (User) request.getAttribute("user");
  Boolean editableObj = (Boolean) request.getAttribute("editable");
  boolean editable = editableObj == null || editableObj;
  if (p == null) p = new ApplicantProfile();
  @SuppressWarnings("unchecked")
  List<EducationEntry> eduList = (List<EducationEntry>) request.getAttribute("educationList");
  if (eduList == null || eduList.isEmpty()) {
    eduList = new java.util.ArrayList<>();
    eduList.add(new EducationEntry());
  }
  String def = "";
  @SuppressWarnings("unchecked")
  Map<String, String> fieldErrors = (Map<String, String>) request.getAttribute("fieldErrors");
  if (fieldErrors == null) fieldErrors = new java.util.LinkedHashMap<>();
%>
<div class="page--top fade-in">
  <div class="layout-wide">

    <!-- Horizontal header row (matches ProfilePage.tsx) -->
    <div class="page-header-row">
      <div class="header-left">
        <div class="logo">
          <svg viewBox="0 0 24 24"><path d="M22 10v6M2 10l10-5 10 5-10 5z"/><path d="M6 12v5c3 3 9 3 12 0v-5"/></svg>
        </div>
        <div>
          <h1 class="page-title">Applicant Profile</h1>
          <p class="page-subtitle">Create or edit your TA profile</p>
        </div>
      </div>
      <div style="display:flex;gap:.5rem;align-items:center;flex-wrap:wrap;">
        <% if (!editable) { %>
        <a class="link-pill" href="${pageContext.request.contextPath}/profile?edit=1">
          Edit
        </a>
        <% } %>
        <a class="link-pill" href="${pageContext.request.contextPath}/job">
          <svg viewBox="0 0 24 24"><path d="M9 18V5a2 2 0 0 1 2-2h10"/><path d="M9 18a2 2 0 0 0 2 2h10"/><path d="M3 11h6"/><path d="M3 15h6"/><path d="M3 7h6"/></svg>
          Job List
        </a>
        <a class="link-pill" href="${pageContext.request.contextPath}/logout">
          <svg viewBox="0 0 24 24"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>
          Logout
        </a>
      </div>
    </div>

    <!-- Card -->
    <div class="card" style="padding:1.5rem 2.5rem;">

      <% if (!editable) { %>
        <div class="alert" style="margin-bottom:.875rem;">Profile is in view mode. Click <strong>Edit</strong> to update your information.</div>
      <% } %>
      <% String infoMessage = (String) request.getAttribute("infoMessage"); if (infoMessage != null) { %>
        <div class="alert alert-info"><%= infoMessage %></div>
      <% } %>

      <form class="form" method="post"
            action="${pageContext.request.contextPath}/profile"
            enctype="multipart/form-data"
            novalidate
            data-editable="<%= editable ? "1" : "0" %>">

        <div class="grid-2col">
          <!-- Left Column: Personal Information -->
          <div style="display:flex;flex-direction:column;gap:1rem;">
            <h3 class="section-heading">Personal Information</h3>

        <div class="field">
          <label for="fullName">Full Name</label>
          <input id="fullName" name="fullName" type="text" placeholder="John Doe"
                 value="<%= p.fullName != null ? p.fullName : def %>" required />
          <% if (fieldErrors.get("fullName") != null) { %><div class="field-error"><%= fieldErrors.get("fullName") %></div><% } %>
        </div>

        <div class="grid-2col" style="gap:.75rem 1rem;">
          <div class="field">
            <label for="gender">Gender</label>
            <select id="gender" name="gender" required>
              <option value="">Select</option>
              <option value="Male"   <%= "Male".equals(p.gender)   ? "selected" : "" %>>Male</option>
              <option value="Female" <%= "Female".equals(p.gender) ? "selected" : "" %>>Female</option>
              <option value="Other"  <%= "Other".equals(p.gender)  ? "selected" : "" %>>Other</option>
            </select>
            <% if (fieldErrors.get("gender") != null) { %><div class="field-error"><%= fieldErrors.get("gender") %></div><% } %>
          </div>
          <div class="field">
            <label for="degree">Degree</label>
            <input id="degree" name="degree" type="text" placeholder="e.g. Masters"
                   value="<%= p.degree != null ? p.degree : def %>" required />
            <% if (fieldErrors.get("degree") != null) { %><div class="field-error"><%= fieldErrors.get("degree") %></div><% } %>
          </div>
        </div>

        <div class="grid-2col" style="gap:.75rem 1rem;">
          <div class="field">
            <label for="major">Major</label>
            <input id="major" name="major" type="text" placeholder="Computer Science"
                   value="<%= p.major != null ? p.major : def %>" required />
            <% if (fieldErrors.get("major") != null) { %><div class="field-error"><%= fieldErrors.get("major") %></div><% } %>
          </div>
          <div class="field">
            <label for="studentId">Student ID</label>
            <input id="studentId" name="studentId" type="text" placeholder="123456789"
                   value="<%= p.studentId != null ? p.studentId : def %>" required
                   pattern="[A-Za-z0-9_-]{4,30}" />
            <% if (fieldErrors.get("studentId") != null) { %><div class="field-error"><%= fieldErrors.get("studentId") %></div><% } %>
          </div>
        </div>

        <div class="grid-2col" style="gap:.75rem 1rem;">
          <div class="field">
            <label for="phone">Phone</label>
            <input id="phone" name="phone" type="text" placeholder="+44 7700 000000"
                   value="<%= p.phone != null ? p.phone : def %>" required />
            <% if (fieldErrors.get("phone") != null) { %><div class="field-error"><%= fieldErrors.get("phone") %></div><% } %>
          </div>
          <div class="field">
            <label for="email">Email</label>
            <input id="email" name="email" type="email" placeholder="student@university.edu"
                   value="<%= p.email != null ? p.email : def %>" required />
            <% if (fieldErrors.get("email") != null) { %><div class="field-error"><%= fieldErrors.get("email") %></div><% } %>
          </div>
        </div>
        <div class="field">
          <label for="idCard">National ID</label>
          <input id="idCard" name="idCard" type="text" placeholder="ID number"
                 value="<%= p.idCard != null ? p.idCard : def %>" required
                 pattern="[A-Za-z0-9]{8,30}" />
          <% if (fieldErrors.get("idCard") != null) { %><div class="field-error"><%= fieldErrors.get("idCard") %></div><% } %>
        </div>
          </div>

          <!-- Right Column: Education, Skills, CV -->
          <div style="display:flex;flex-direction:column;gap:1rem;">
            <h3 class="section-heading">Qualifications &amp; Availability</h3>
        <table class="edu-table" id="edu-table">
          <thead>
            <tr>
              <th>School</th>
              <th>Degree</th>
              <th>Major</th>
              <th>Period</th>
              <th></th>
            </tr>
          </thead>
          <tbody id="edu-body">
            <% for (int i = 0; i < eduList.size(); i++) { EducationEntry e = eduList.get(i); %>
            <tr>
              <td>
                <input name="edu_school" type="text" placeholder="University"
                       value="<%= e.school != null ? e.school : def %>" required />
                <% if (fieldErrors.get("edu_school_" + i) != null) { %><div class="field-error"><%= fieldErrors.get("edu_school_" + i) %></div><% } %>
              </td>
              <td>
                <input name="edu_degree" type="text" placeholder="B.Sc."
                       value="<%= e.degree != null ? e.degree : def %>" required />
                <% if (fieldErrors.get("edu_degree_" + i) != null) { %><div class="field-error"><%= fieldErrors.get("edu_degree_" + i) %></div><% } %>
              </td>
              <td>
                <input name="edu_major"  type="text" placeholder="CS"
                       value="<%= e.major  != null ? e.major  : def %>" required />
                <% if (fieldErrors.get("edu_major_" + i) != null) { %><div class="field-error"><%= fieldErrors.get("edu_major_" + i) %></div><% } %>
              </td>
              <td>
                <input name="edu_period" type="text" placeholder="2020-2024"
                       value="<%= e.period != null ? e.period : def %>" required />
                <% if (fieldErrors.get("edu_period_" + i) != null) { %><div class="field-error"><%= fieldErrors.get("edu_period_" + i) %></div><% } %>
              </td>
              <td style="text-align:center;vertical-align:middle;">
                <button type="button" onclick="removeEduRow(this)"
                        class="btn-withdraw"
                        title="Remove row">&times;</button>
              </td>
            </tr>
            <% } %>
          </tbody>
        </table>
        <% if (fieldErrors.get("education") != null) { %><div class="field-error"><%= fieldErrors.get("education") %></div><% } %>
        <button id="add-edu-btn" type="button" onclick="addEduRow()"
                class="upload-area" style="margin-top:.5rem;">+ Add education row</button>

        <p class="section-title" style="margin-top:1.5rem;">Courses &amp; Availability</p>

        <div class="field">
          <label for="courses">Courses Completed <span style="font-weight:400;color:#94a3b8;">(one per line)</span></label>
          <textarea id="courses" name="courses" placeholder="eg: Data Structures"
                    required><%= p.courses != null ? p.courses : def %></textarea>
          <% if (fieldErrors.get("courses") != null) { %><div class="field-error"><%= fieldErrors.get("courses") %></div><% } %>
        </div>

        <div class="field">
          <label for="freeTime">Availability</label>
          <textarea id="freeTime" name="freeTime"
                    placeholder="eg: Mon 14:00-17:00"
                    required><%= p.freeTime != null ? p.freeTime : (p.availability != null ? p.availability : def) %></textarea>
          <% if (fieldErrors.get("freeTime") != null) { %><div class="field-error"><%= fieldErrors.get("freeTime") %></div><% } %>
        </div>

        <!-- Skills -->
        <p class="section-title">Skills</p>

        <div class="field">
          <label for="skills">Skills</label>
          <textarea id="skills" name="skills"
                    placeholder="eg: Python"
                    required><%= p.skills != null ? p.skills : def %></textarea>
          <% if (fieldErrors.get("skills") != null) { %><div class="field-error"><%= fieldErrors.get("skills") %></div><% } %>
        </div>

        <!-- CV Upload -->
        <p class="section-title">Curriculum Vitae</p>

        <div class="field">
          <% if (p.cvFileName != null && !p.cvFileName.isEmpty()) { %>
            <div class="alert alert-success" style="display:flex;align-items:center;gap:.5rem;margin-bottom:.5rem;">
              <svg style="width:14px;height:14px;fill:none;stroke:currentColor;stroke-width:2;
                          stroke-linecap:round;stroke-linejoin:round;" viewBox="0 0 24 24">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                <polyline points="14 2 14 8 20 8"/>
              </svg>
              <span>Current: <%= p.cvFileName %></span>
              <a href="${pageContext.request.contextPath}/cv" target="_blank"
                 class="link-sm" style="margin-left:auto;">View</a>
            </div>
            <label style="display:flex;align-items:center;gap:.4rem;font-size:.82rem;color:#64748b;margin-bottom:.5rem;">
              <input id="deleteCv" name="deleteCv" value="1" type="checkbox" style="width:auto;height:auto;" />
              Delete current CV on save
            </label>
          <% } %>
          <label for="cv" style="cursor:pointer;">
            <div class="upload-area" id="upload-label">
              <svg viewBox="0 0 24 24"><polyline points="16 16 12 12 8 16"/><line x1="12" y1="12" x2="12" y2="21"/><path d="M20.39 18.39A5 5 0 0 0 18 9h-1.26A8 8 0 1 0 3 16.3"/></svg>
              <span id="cv-name"><%= (p.cvFileName != null && !p.cvFileName.isEmpty()) ? "Replace CV" : "Upload CV" %></span>
            </div>
            <input id="cv" name="cv" type="file" accept=".pdf,.doc,.docx"
                   style="display:none;"
                   onchange="document.getElementById('cv-name').textContent = this.files[0] ? this.files[0].name : 'Upload CV';" />
          </label>
          <p class="page-subtitle" style="margin-top:.25rem;">PDF, DOC or DOCX &mdash; max 10 MB</p>
          <% if (fieldErrors.get("cv") != null) { %><div class="field-error"><%= fieldErrors.get("cv") %></div><% } %>
        </div>

        <% if (editable) { %>
        <div class="save-bar">
          <button type="submit" class="btn btn-primary">Save Profile</button>
        </div>
        <% } %>

        </div><!-- /right column -->
        </div><!-- /grid-2col -->
      </form>
    </div><!-- /card -->
  </div><!-- /layout-wide -->
</div>

<script>
function addEduRow() {
  var tbody = document.getElementById('edu-body');
  var tr = document.createElement('tr');
  tr.innerHTML =
    '<td><input name="edu_school" type="text" placeholder="University" required /></td>' +
    '<td><input name="edu_degree" type="text" placeholder="B.Sc." required /></td>' +
    '<td><input name="edu_major"  type="text" placeholder="CS" required /></td>' +
    '<td><input name="edu_period" type="text" placeholder="2020-2024" required /></td>' +
    '<td style="text-align:center;vertical-align:middle;">' +
    '<button type="button" onclick="removeEduRow(this)" ' +
    'class="btn-withdraw" ' +
    'title="Remove row">&times;</button></td>';
  tbody.appendChild(tr);
}
function removeEduRow(btn) {
  var tbody = document.getElementById('edu-body');
  if (tbody.rows.length > 1) {
    btn.closest('tr').remove();
  }
}

(function () {
  var form = document.querySelector('form[data-editable]');
  if (!form || form.getAttribute('data-editable') === '1') return;
  form.querySelectorAll('input, textarea, select, button').forEach(function (el) {
    var isAllowedButton = el.id === 'add-edu-btn';
    if (isAllowedButton) {
      el.style.display = 'none';
      return;
    }
    if (el.type === 'button') {
      el.style.display = 'none';
      return;
    }
    if (el.type === 'file') {
      el.disabled = true;
      var upload = document.getElementById('upload-label');
      if (upload) {
        upload.style.opacity = '.6';
        upload.style.cursor = 'not-allowed';
      }
      return;
    }
    if (el.type === 'checkbox') {
      el.disabled = true;
      return;
    }
    el.setAttribute('disabled', 'disabled');
  });
})();
</script>
</body>
</html>
