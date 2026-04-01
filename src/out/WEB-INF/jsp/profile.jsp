<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.bupt.ta.model.ApplicantProfile" %>
<%@ page import="com.bupt.ta.model.User" %>
<%@ page import="com.bupt.ta.model.EducationEntry" %>
<%@ page import="com.bupt.ta.service.ProfileService" %>
<%@ page import="java.util.List" %>
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
%>
<div class="page--top">
  <div class="container">

    <!-- Header -->
    <div class="page-header page-header--sm">
      <div class="logo-wrap">
        <div class="logo logo--sm">
          <svg viewBox="0 0 24 24"><path d="M22 10v6M2 10l10-5 10 5-10 5z"/><path d="M6 12v5c3 3 9 3 12 0v-5"/></svg>
        </div>
      </div>
      <h1 class="page-title">Applicant Profile</h1>
      <p class="page-subtitle">Create or edit your TA profile</p>
    </div>

    <!-- Card -->
    <div class="card">

      <!-- Top bar: title + logout -->
      <div class="top-bar">
        <span style="font-size:.9375rem;font-weight:600;color:#0f172a;">My Profile</span>
        <div style="display:flex;gap:.5rem;align-items:center;">
          <% if (!editable) { %>
          <a class="btn-ghost" href="${pageContext.request.contextPath}/profile?edit=1" style="white-space:nowrap;">
            Edit
          </a>
          <% } %>
          <a class="btn-ghost" href="${pageContext.request.contextPath}/job" style="white-space:nowrap;">
            <svg viewBox="0 0 24 24">
              <path d="M9 18V5a2 2 0 0 1 2-2h10"/>
              <path d="M9 18a2 2 0 0 0 2 2h10"/>
              <path d="M3 11h6"/>
              <path d="M3 15h6"/>
              <path d="M3 7h6"/>
            </svg>
            Job List
          </a>
          <a class="btn-ghost" href="${pageContext.request.contextPath}/logout" style="white-space:nowrap;">
            <svg viewBox="0 0 24 24">
              <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
              <polyline points="16 17 21 12 16 7"/>
              <line x1="21" y1="12" x2="9" y2="12"/>
            </svg>
            Logout
          </a>
        </div>
      </div>

      <% String error = (String) request.getAttribute("error"); if (error != null) { %>
        <div class="alert alert-error"><%= error %></div>
      <% } %>
      <% if (!editable) { %>
        <div class="alert" style="margin-bottom:.875rem;">Profile is in view mode. Click <strong>Edit</strong> to update your information.</div>
      <% } %>

      <form class="form" method="post"
            action="${pageContext.request.contextPath}/profile"
            enctype="multipart/form-data"
            data-editable="<%= editable ? "1" : "0" %>">

        <!-- Personal Information -->
        <p class="section-title">Personal Information</p>

        <div class="field">
          <label for="fullName">Full Name</label>
          <input id="fullName" name="fullName" type="text" placeholder="John Doe"
                 value="<%= p.fullName != null ? p.fullName : def %>" required />
        </div>

        <div style="display:grid;grid-template-columns:1fr 1fr;gap:.75rem;">
          <div class="field">
            <label for="gender">Gender</label>
            <select id="gender" name="gender">
              <option value="">Select</option>
              <option value="Male"   <%= "Male".equals(p.gender)   ? "selected" : "" %>>Male</option>
              <option value="Female" <%= "Female".equals(p.gender) ? "selected" : "" %>>Female</option>
              <option value="Other"  <%= "Other".equals(p.gender)  ? "selected" : "" %>>Other</option>
            </select>
          </div>
          <div class="field">
            <label for="degree">Degree</label>
            <input id="degree" name="degree" type="text" placeholder="e.g. Masters"
                   value="<%= p.degree != null ? p.degree : def %>" />
          </div>
        </div>

        <div style="display:grid;grid-template-columns:1fr 1fr;gap:.75rem;">
          <div class="field">
            <label for="major">Major</label>
            <input id="major" name="major" type="text" placeholder="Computer Science"
                   value="<%= p.major != null ? p.major : def %>" />
          </div>
          <div class="field">
            <label for="studentId">Student ID</label>
            <input id="studentId" name="studentId" type="text" placeholder="123456789"
                   value="<%= p.studentId != null ? p.studentId : def %>" />
          </div>
        </div>

        <div style="display:grid;grid-template-columns:1fr 1fr;gap:.75rem;">
          <div class="field">
            <label for="phone">Phone</label>
            <input id="phone" name="phone" type="text" placeholder="+44 7700 000000"
                   value="<%= p.phone != null ? p.phone : def %>" />
          </div>
          <div class="field">
            <label for="email">Email</label>
            <input id="email" name="email" type="email" placeholder="student@university.edu"
                   value="<%= p.email != null ? p.email : def %>" />
          </div>
        </div>

        <!-- Education Background -->
        <p class="section-title">Education Background</p>
        <table class="edu-table" id="edu-table">
          <thead>
            <tr>
              <th style="width:32%">School</th>
              <th style="width:22%">Degree</th>
              <th style="width:24%">Major</th>
              <th style="width:18%">Period</th>
              <th style="width:4%"></th>
            </tr>
          </thead>
          <tbody id="edu-body">
            <% for (EducationEntry e : eduList) { %>
            <tr>
              <td><input name="edu_school" type="text" placeholder="University"
                         value="<%= e.school != null ? e.school : def %>" /></td>
              <td><input name="edu_degree" type="text" placeholder="B.Sc."
                         value="<%= e.degree != null ? e.degree : def %>" /></td>
              <td><input name="edu_major"  type="text" placeholder="CS"
                         value="<%= e.major  != null ? e.major  : def %>" /></td>
              <td><input name="edu_period" type="text" placeholder="2020-2024"
                         value="<%= e.period != null ? e.period : def %>" /></td>
              <td style="text-align:center;vertical-align:middle;">
                <button type="button" onclick="removeEduRow(this)"
                        style="background:none;border:none;cursor:pointer;color:#94a3b8;font-size:1rem;padding:0 .25rem;"
                        title="Remove row">&times;</button>
              </td>
            </tr>
            <% } %>
          </tbody>
        </table>
        <button id="add-edu-btn" type="button" onclick="addEduRow()"
                style="margin-top:.5rem;background:none;border:1px dashed #cbd5e1;border-radius:.5rem;
                       width:100%;padding:.5rem;font-size:.8125rem;color:#64748b;cursor:pointer;
                       font-family:inherit;transition:border-color .15s,color .15s;"
                onmouseover="this.style.borderColor='#3b82f6';this.style.color='#2563eb';"
                onmouseout="this.style.borderColor='#cbd5e1';this.style.color='#64748b';"
        >+ Add education row</button>

        <!-- Courses & Availability -->
        <p class="section-title">Courses &amp; Availability</p>

        <div class="field">
          <label for="courses">Courses Completed <span style="font-weight:400;color:#94a3b8;">(one per line)</span></label>
          <textarea id="courses" name="courses" placeholder="Data Structures&#10;Algorithms&#10;Database Systems"
                    style="min-height:5rem;"><%= p.courses != null ? p.courses : def %></textarea>
        </div>

        <div class="field">
          <label for="freeTime">Availability</label>
          <textarea id="freeTime" name="freeTime"
                    placeholder="e.g. Mondays and Wednesdays 2–5 PM, Fridays all day"
                    style="min-height:4rem;"><%= p.freeTime != null ? p.freeTime : (p.availability != null ? p.availability : def) %></textarea>
        </div>

        <!-- Skills -->
        <p class="section-title">Skills</p>

        <div class="field">
          <label for="skills">Skills</label>
          <textarea id="skills" name="skills"
                    placeholder="e.g. Python, Java, Data Structures, Algorithms"
                    style="min-height:4rem;"><%= p.skills != null ? p.skills : def %></textarea>
        </div>

        <!-- CV Upload -->
        <p class="section-title">Curriculum Vitae</p>

        <div class="field">
          <% if (p.cvFileName != null && !p.cvFileName.isEmpty()) { %>
            <div style="display:flex;align-items:center;gap:.5rem;margin-bottom:.5rem;
                        padding:.5rem .75rem;background:#f0fdf4;border:1px solid #bbf7d0;
                        border-radius:.5rem;font-size:.875rem;color:#166534;">
              <svg style="width:14px;height:14px;fill:none;stroke:currentColor;stroke-width:2;
                          stroke-linecap:round;stroke-linejoin:round;" viewBox="0 0 24 24">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                <polyline points="14 2 14 8 20 8"/>
              </svg>
              <span>Current: <%= p.cvFileName %></span>
              <a href="${pageContext.request.contextPath}/cv" target="_blank"
                 style="margin-left:auto;font-size:.8125rem;color:#2563eb;font-weight:500;">View</a>
            </div>
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
          <p style="font-size:.75rem;color:#94a3b8;margin-top:.25rem;">PDF, DOC or DOCX &mdash; max 10 MB</p>
        </div>

        <% if (editable) { %>
        <button type="submit" class="btn btn-primary" style="margin-top:.5rem;">Save Profile</button>
        <% } %>
      </form>
    </div>

  </div>
</div>

<script>
function addEduRow() {
  var tbody = document.getElementById('edu-body');
  var tr = document.createElement('tr');
  tr.innerHTML =
    '<td><input name="edu_school" type="text" placeholder="University" /></td>' +
    '<td><input name="edu_degree" type="text" placeholder="B.Sc." /></td>' +
    '<td><input name="edu_major"  type="text" placeholder="CS" /></td>' +
    '<td><input name="edu_period" type="text" placeholder="2020-2024" /></td>' +
    '<td style="text-align:center;vertical-align:middle;">' +
    '<button type="button" onclick="removeEduRow(this)" ' +
    'style="background:none;border:none;cursor:pointer;color:#94a3b8;font-size:1rem;padding:0 .25rem;" ' +
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
    el.setAttribute('disabled', 'disabled');
  });
})();
</script>
</body>
</html>
