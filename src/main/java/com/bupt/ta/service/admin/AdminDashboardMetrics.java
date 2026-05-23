package com.bupt.ta.service.admin;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Aggregated metrics for the admin dashboard and AI analytics. */
public class AdminDashboardMetrics {
    public int totalUsers;
    public int activeUsers;
    public int inactiveUsers;
    public int totalJobs;
    public int totalApplications;
    public int highWorkloadTaCount;

    public final Map<String, Integer> usersByRole = new LinkedHashMap<>();
    public final Map<String, Integer> appsByStatus = new LinkedHashMap<>();
    public final List<SeriesPoint> monthlyApplications = new ArrayList<>();
    public final List<SeriesPoint> monthlyApplicantPool = new ArrayList<>();
    public final List<SeriesPoint> monthlyNewApplicants = new ArrayList<>();
    public final List<SeriesPoint> topModules = new ArrayList<>();
    public final Map<String, Integer> appsByModule = new LinkedHashMap<>();

    public static final class SeriesPoint {
        public final String label;
        public final int value;

        public SeriesPoint(String label, int value) {
            this.label = label;
            this.value = value;
        }
    }
}
