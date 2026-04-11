package com.bupt.ta.model;

import java.util.ArrayList;
import java.util.List;

/** Per-TA stats; accepted/rejected lists hold one line per application of that status. */
public class TaWorkloadStats {

    /** Accepted applications count as assigned jobs; warn when count is at or above this value. */
    public static final int ASSIGNED_JOBS_WARNING_THRESHOLD = 3;

    public int total;
    public int pending;
    public int accepted;
    public int rejected;
    public final List<String> acceptedPositions = new ArrayList<>();
    public final List<String> rejectedPositions = new ArrayList<>();

    /** Same layout for accepted or rejected rows (module, code, role, date). */
    public static String formatAcceptedLine(Application application) {
        if (application == null) {
            return "";
        }
        String mod = application.moduleName != null ? application.moduleName.trim() : "";
        String code = application.moduleCode != null ? application.moduleCode.trim() : "";
        String role = application.role != null ? application.role.trim() : "";
        String date = application.applicationDate != null ? application.applicationDate.trim() : "";
        StringBuilder sb = new StringBuilder();
        sb.append(mod.isEmpty() ? "(Unknown module)" : mod);
        if (!code.isEmpty()) {
            sb.append(" [").append(code).append("]");
        }
        if (!role.isEmpty()) {
            sb.append(" - ").append(role);
        }
        if (!date.isEmpty()) {
            sb.append(" | ").append(date);
        }
        return sb.toString();
    }
}
