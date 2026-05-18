package com.bupt.ta.model;

import java.util.List;

/**
 * Aggregated application counts for one job (matched by module name + code).
 */
public final class JobApplicationStats {
    public final int pending;
    public final int accepted;
    public final int rejected;
    public final int withdrawn;

    private JobApplicationStats(int pending, int accepted, int rejected, int withdrawn) {
        this.pending = pending;
        this.accepted = accepted;
        this.rejected = rejected;
        this.withdrawn = withdrawn;
    }

    /** Applicants still in pipeline (excludes withdrawn). */
    public int activeTotal() {
        return pending + accepted + rejected;
    }

    public static JobApplicationStats forJob(List<Application> all, String moduleName, String moduleCode) {
        int p = 0, a = 0, r = 0, w = 0;
        if (all == null) {
            return new JobApplicationStats(0, 0, 0, 0);
        }
        for (Application app : all) {
            if (!matchesJob(app, moduleName, moduleCode)) {
                continue;
            }
            String st = app.status == null ? "Pending" : app.status.trim();
            if ("Withdrawn".equalsIgnoreCase(st)) {
                w++;
            } else if ("Accepted".equalsIgnoreCase(st)) {
                a++;
            } else if ("Rejected".equalsIgnoreCase(st)) {
                r++;
            } else {
                p++;
            }
        }
        return new JobApplicationStats(p, a, r, w);
    }

    public static boolean matchesJob(Application app, String moduleName, String moduleCode) {
        if (app == null || app.moduleName == null || moduleName == null) {
            return false;
        }
        if (!moduleName.trim().equals(app.moduleName.trim())) {
            return false;
        }
        String jc = moduleCode == null ? "" : moduleCode.trim();
        String ac = app.moduleCode == null ? "" : app.moduleCode.trim();
        return jc.equals(ac);
    }

    /** Parse TA headcount from job; invalid or empty defaults to 1. */
    public static int parseCapacity(String numberOfTAs) {
        if (numberOfTAs == null || numberOfTAs.trim().isEmpty()) {
            return 1;
        }
        try {
            int n = Integer.parseInt(numberOfTAs.trim().replaceAll("[^0-9]", ""));
            return n < 1 ? 1 : n;
        } catch (NumberFormatException e) {
            return 1;
        }
    }
}
