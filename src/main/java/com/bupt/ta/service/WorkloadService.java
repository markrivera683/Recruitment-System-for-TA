package com.bupt.ta.service;

import com.bupt.ta.model.Application;
import com.bupt.ta.model.Job;
import com.bupt.ta.model.MoWorkloadSnapshot;
import com.bupt.ta.model.Roles;
import com.bupt.ta.model.TaWorkloadStats;
import com.bupt.ta.model.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Computes TA workload stats for admin dashboards and MO approval advice. */
public final class WorkloadService {

    public Map<String, TaWorkloadStats> buildTaWorkloadStats(List<User> users, List<Application> appList) {
        Map<String, TaWorkloadStats> taWorkload = new HashMap<>();
        if (users != null) {
            for (User user : users) {
                if (user != null && Roles.TA.equals(user.role) && user.id != null && !user.id.trim().isEmpty()) {
                    taWorkload.put(user.id.trim(), new TaWorkloadStats());
                }
            }
        }
        if (appList == null) {
            return taWorkload;
        }
        for (Application app : appList) {
            if (app == null || app.userId == null) {
                continue;
            }
            String applicantId = app.userId.trim();
            if (applicantId.isEmpty()) {
                continue;
            }
            TaWorkloadStats row = taWorkload.get(applicantId);
            if (row == null) {
                continue;
            }
            row.total++;
            String raw = app.status == null ? "" : app.status.trim();
            if ("Accepted".equalsIgnoreCase(raw)) {
                row.accepted++;
                row.acceptedPositions.add(TaWorkloadStats.formatAcceptedLine(app));
            } else if ("Rejected".equalsIgnoreCase(raw)) {
                row.rejected++;
                row.rejectedPositions.add(TaWorkloadStats.formatAcceptedLine(app));
            } else {
                row.pending++;
            }
        }
        return taWorkload;
    }

    public MoWorkloadSnapshot buildSnapshotForApplication(
            String applicationId,
            List<Application> applications,
            List<Job> jobs,
            String applicantName) {
        if (applicationId == null || applicationId.trim().isEmpty() || applications == null) {
            return null;
        }
        Application target = null;
        for (Application app : applications) {
            if (app != null && applicationId.trim().equals(app.id)) {
                target = app;
                break;
            }
        }
        if (target == null || target.userId == null) {
            return null;
        }
        String status = target.status == null ? "" : target.status.trim();
        if (!"Pending".equalsIgnoreCase(status)) {
            return null;
        }

        Map<String, String> hoursByModuleCode = buildHoursByModuleCode(jobs);
        MoWorkloadSnapshot snapshot = new MoWorkloadSnapshot();
        snapshot.applicationId = target.id;
        snapshot.userId = target.userId.trim();
        snapshot.applicantName = applicantName != null && !applicantName.trim().isEmpty()
                ? applicantName.trim() : snapshot.userId;
        snapshot.targetModuleName = target.moduleName != null ? target.moduleName.trim() : "";
        snapshot.targetModuleCode = target.moduleCode != null ? target.moduleCode.trim() : "";
        snapshot.targetRole = target.role != null ? target.role.trim() : "";
        snapshot.targetWorkloadHours = resolveHours(snapshot.targetModuleCode, hoursByModuleCode);

        String userId = snapshot.userId;
        for (Application app : applications) {
            if (app == null || app.userId == null || !userId.equals(app.userId.trim())) {
                continue;
            }
            String raw = app.status == null ? "" : app.status.trim();
            String line = TaWorkloadStats.formatAcceptedLine(app);
            String code = app.moduleCode != null ? app.moduleCode.trim() : "";
            String hours = resolveHours(code, hoursByModuleCode);
            if ("Accepted".equalsIgnoreCase(raw)) {
                snapshot.acceptedCount++;
                snapshot.acceptedPositions.add(line);
                snapshot.acceptedHoursHints.add(hours);
            } else if ("Pending".equalsIgnoreCase(raw)) {
                snapshot.pendingCount++;
                snapshot.pendingPositions.add(line);
                snapshot.pendingHoursHints.add(hours);
            }
        }
        snapshot.potentialLoadIfApprove = snapshot.acceptedCount + snapshot.pendingCount;
        return snapshot;
    }

    public Map<String, MoWorkloadSnapshot> buildSnapshotsForPendingApplications(
            List<Application> applications,
            List<Job> jobs,
            Map<String, String> applicantNamesByUserId) {
        Map<String, MoWorkloadSnapshot> out = new HashMap<>();
        if (applications == null) {
            return out;
        }
        for (Application app : applications) {
            if (app == null || app.id == null) {
                continue;
            }
            String status = app.status == null ? "" : app.status.trim();
            if (!"Pending".equalsIgnoreCase(status)) {
                continue;
            }
            String name = null;
            if (applicantNamesByUserId != null && app.userId != null) {
                name = applicantNamesByUserId.get(app.userId.trim());
            }
            MoWorkloadSnapshot snapshot = buildSnapshotForApplication(app.id, applications, jobs, name);
            if (snapshot != null) {
                out.put(app.id, snapshot);
            }
        }
        return out;
    }

    private static Map<String, String> buildHoursByModuleCode(List<Job> jobs) {
        Map<String, String> map = new HashMap<>();
        if (jobs == null) {
            return map;
        }
        for (Job job : jobs) {
            if (job == null || job.getModuleCode() == null) {
                continue;
            }
            String code = job.getModuleCode().trim();
            if (code.isEmpty()) {
                continue;
            }
            String hours = job.getWorkloadHours();
            if (hours != null && !hours.trim().isEmpty()) {
                map.put(code.toUpperCase(), hours.trim());
            }
        }
        return map;
    }

    private static String resolveHours(String moduleCode, Map<String, String> hoursByModuleCode) {
        if (moduleCode == null || moduleCode.trim().isEmpty()) {
            return "unknown";
        }
        String hours = hoursByModuleCode.get(moduleCode.trim().toUpperCase());
        return hours != null && !hours.isEmpty() ? hours : "unknown";
    }
}
