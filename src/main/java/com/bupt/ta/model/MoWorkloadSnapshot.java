package com.bupt.ta.model;

import java.util.ArrayList;
import java.util.List;

/** Per-application workload context for MO approval AI advice. */
public class MoWorkloadSnapshot {

    public String applicationId;
    public String userId;
    public String applicantName;
    public String targetModuleName;
    public String targetModuleCode;
    public String targetRole;
    public String targetWorkloadHours;

    public int acceptedCount;
    public int pendingCount;
    /** acceptedCount + pendingCount (includes the target pending application). */
    public int potentialLoadIfApprove;
    public int warningThreshold = TaWorkloadStats.ASSIGNED_JOBS_WARNING_THRESHOLD;

    public final List<String> acceptedPositions = new ArrayList<>();
    public final List<String> pendingPositions = new ArrayList<>();
    /** Parallel hints for accepted/pending lines (moduleCode → hours or "unknown"). */
    public final List<String> acceptedHoursHints = new ArrayList<>();
    public final List<String> pendingHoursHints = new ArrayList<>();
}
