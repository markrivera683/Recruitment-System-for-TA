package com.bupt.ta.service;

import com.bupt.ta.model.Job;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.time.LocalDate;
import java.util.UUID;
import java.util.stream.Collectors;

/*
功能：
读取 JSON
返回 job list
根据 id 查 job
*/
public class JobService {

    private final String jobsJsonPath;

    public JobService(String jobsJsonPath) {
        this.jobsJsonPath = jobsJsonPath;
    }

    public List<Job> getAllJobs() {
        List<Job> jobs = new ArrayList<>();

        try {
            String json = new String(
                    Files.readAllBytes(Paths.get(jobsJsonPath)),
                    StandardCharsets.UTF_8);

            for (String obj : splitTopLevelObjects(json)) {
                Job job = new Job();
                job.setId(extractString(obj, "id"));
                job.setModuleName(extractString(obj, "moduleName"));
                job.setModuleCode(extractString(obj, "moduleCode"));
                job.setActivityType(extractString(obj, "activityType"));
                job.setDescription(extractString(obj, "description"));
                job.setRequiredSkills(extractStringArray(obj, "requiredSkills"));
                job.setPostDate(extractString(obj, "postDate"));
                job.setApplicationDeadline(firstNonEmpty(
                        extractString(obj, "applicationDeadline"),
                        extractString(obj, "deadline"),
                        ""));
                job.setDuration(firstNonEmpty(
                        extractString(obj, "duration"),
                        "One semester"));
                job.setNumberOfTAs(firstNonEmpty(
                        extractString(obj, "numberOfTAs"),
                        "2"));
                job.setStatus(firstNonEmpty(extractString(obj, "status"), "Published"));
                job.setCreatedByMoId(extractString(obj, "createdByMoId"));
                job.setCreatedAt(extractString(obj, "createdAt"));
                job.setPublishedAt(extractString(obj, "publishedAt"));
                job.setWorkloadHours(extractString(obj, "workloadHours"));
                List<String> schedule = extractStringArray(obj, "schedule");
                if (schedule.isEmpty()) {
                    schedule = buildDefaultSchedule(job.getActivityType());
                }
                job.setSchedule(schedule);

                jobs.add(job);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return jobs;
    }

    /** Remove one job by id and persist {@code jobs.json}. */
    public void deleteJobById(String id) throws IOException {
        if (id == null || id.isEmpty()) {
            return;
        }
        List<Job> jobs = getAllJobs();
        boolean changed = false;
        for (Iterator<Job> it = jobs.iterator(); it.hasNext();) {
            Job j = it.next();
            if (id.equals(j.getId())) {
                it.remove();
                changed = true;
                break;
            }
        }
        if (changed) {
            writeJobs(jobs);
        }
    }

    private void writeJobs(List<Job> jobs) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");
        for (int i = 0; i < jobs.size(); i++) {
            if (i > 0) {
                sb.append(",\n");
            }
            sb.append(jobToJson(jobs.get(i)));
        }
        sb.append("\n]");
        Files.write(Paths.get(jobsJsonPath), sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    private static String jobToJson(Job job) {
        StringBuilder sb = new StringBuilder();
        sb.append("  {\n");
        sb.append("    \"id\": \"").append(esc(job.getId())).append("\",\n");
        sb.append("    \"moduleName\": \"").append(esc(job.getModuleName())).append("\",\n");
        sb.append("    \"moduleCode\": \"").append(esc(job.getModuleCode())).append("\",\n");
        sb.append("    \"activityType\": \"").append(esc(job.getActivityType())).append("\",\n");
        sb.append("    \"requiredSkills\": ");
        sb.append(skillsToJson(job.getRequiredSkills()));
        sb.append(",\n");
        sb.append("    \"description\": \"").append(esc(job.getDescription())).append("\",\n");
        sb.append("    \"postDate\": \"").append(esc(job.getPostDate())).append("\",\n");
        sb.append("    \"applicationDeadline\": \"").append(esc(job.getApplicationDeadline())).append("\",\n");
        sb.append("    \"duration\": \"").append(esc(job.getDuration())).append("\",\n");
        sb.append("    \"numberOfTAs\": \"").append(esc(job.getNumberOfTAs())).append("\",\n");
        sb.append("    \"schedule\": ").append(skillsToJson(job.getSchedule())).append("\n");
        sb.append("  }");
        sb.append("    \"status\": \"").append(esc(firstNonEmpty(job.getStatus(), "Published"))).append("\",\n");
        sb.append("    \"createdByMoId\": \"").append(esc(job.getCreatedByMoId())).append("\",\n");
        sb.append("    \"createdAt\": \"").append(esc(job.getCreatedAt())).append("\",\n");
        sb.append("    \"publishedAt\": \"").append(esc(job.getPublishedAt())).append("\",\n");
        sb.append("    \"workloadHours\": \"").append(esc(job.getWorkloadHours())).append("\",\n");
        return sb.toString();
    }

    private static String skillsToJson(List<String> skills) {
        StringBuilder sb = new StringBuilder("[");
        if (skills != null) {
            for (int i = 0; i < skills.size(); i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append("\"").append(esc(skills.get(i))).append("\"");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private static String esc(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    public Job getJobById(String id) {
        for (Job job : getAllJobs()) {
            if (job.getId() != null && job.getId().equals(id)) {
                return job;
            }
        }
        return null;
    }

    private static List<String> splitTopLevelObjects(String json) {
        List<String> objects = new ArrayList<>();
        int depth = 0;
        int start = -1;
        boolean inString = false;
        boolean escaped = false;

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (inString) {
                if (escaped) {
                    escaped = false;
                } else if (c == '\\') {
                    escaped = true;
                } else if (c == '"') {
                    inString = false;
                }
                continue;
            }
            if (c == '"') {
                inString = true;
            } else if (c == '{') {
                if (depth == 0)
                    start = i;
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0 && start >= 0) {
                    objects.add(json.substring(start, i + 1));
                    start = -1;
                }
            }
        }
        return objects;
    }

    private static String extractString(String objJson, String key) {
        Pattern p = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\"");
        Matcher m = p.matcher(objJson);
        return m.find() ? unescapeJson(m.group(1)) : "";
    }

    private static List<String> extractStringArray(String objJson, String key) {
        List<String> values = new ArrayList<>();
        Pattern p = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\\[(.*?)\\]", Pattern.DOTALL);
        Matcher m = p.matcher(objJson);
        if (!m.find())
            return values;

        Matcher itemMatcher = Pattern.compile("\"((?:\\\\.|[^\"])*)\"").matcher(m.group(1));
        while (itemMatcher.find()) {
            values.add(unescapeJson(itemMatcher.group(1)));
        }
        return values;
    }

    private static String unescapeJson(String s) {
        return s.replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t");
    }

    private static String firstNonEmpty(String... values) {
        for (String v : values) {
            if (v != null && !v.trim().isEmpty()) {
                return v.trim();
            }
        }
        return "";
    }

    private static List<String> buildDefaultSchedule(String activityType) {
        List<String> slots = new ArrayList<>();
        String t = activityType == null ? "" : activityType.toLowerCase();
        if (t.contains("lab")) {
            slots.add("Monday 14:00 - 16:00");
            slots.add("Wednesday 14:00 - 16:00");
        } else if (t.contains("tutorial")) {
            slots.add("Tuesday 10:00 - 11:30");
            slots.add("Thursday 10:00 - 11:30");
        } else if (t.contains("invigil")) {
            slots.add("Exam period (dates announced by module leader)");
        } else {
            slots.add("Schedule to be confirmed");
        }
        return slots;
    }

    public List<Job> listPublishedJobs() {
        return getAllJobs().stream()
                .filter(j -> !"Draft".equalsIgnoreCase(firstNonEmpty(j.getStatus(), "Published")))
                .collect(Collectors.toList());
    }

    public Job createJob(Job job) throws IOException {
        List<Job> jobs = getAllJobs();
        String today = LocalDate.now().toString();

        if (job.getId() == null || job.getId().trim().isEmpty()) {
            job.setId(UUID.randomUUID().toString());
        }
        if (job.getStatus() == null || job.getStatus().trim().isEmpty()) {
            job.setStatus("Draft");
        }
        if (job.getCreatedAt() == null || job.getCreatedAt().trim().isEmpty()) {
            job.setCreatedAt(today);
        }
        if ("Published".equalsIgnoreCase(job.getStatus())) {
            if (job.getPostDate() == null || job.getPostDate().trim().isEmpty()) {
                job.setPostDate(today);
            }
            if (job.getPublishedAt() == null || job.getPublishedAt().trim().isEmpty()) {
                job.setPublishedAt(today);
            }
        }
        jobs.add(job);
        writeJobs(jobs);
        return job;
    }

    public boolean publishJob(String jobId, String moId) throws IOException {
        if (jobId == null || jobId.trim().isEmpty())
            return false;
        List<Job> jobs = getAllJobs();
        boolean changed = false;
        String today = LocalDate.now().toString();

        for (Job j : jobs) {
            if (jobId.trim().equals(j.getId())) {
                j.setStatus("Published");
                if (j.getPostDate() == null || j.getPostDate().trim().isEmpty()) {
                    j.setPostDate(today);
                }
                if (j.getPublishedAt() == null || j.getPublishedAt().trim().isEmpty()) {
                    j.setPublishedAt(today);
                }
                if (j.getCreatedByMoId() == null || j.getCreatedByMoId().trim().isEmpty()) {
                    j.setCreatedByMoId(moId == null ? "" : moId);
                }
                changed = true;
                break;
            }
        }
        if (changed)
            writeJobs(jobs);
        return changed;
    }
}