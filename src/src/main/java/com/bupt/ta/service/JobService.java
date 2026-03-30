package com.bupt.ta.service;

import com.bupt.ta.model.Job;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
                StandardCharsets.UTF_8
            );

            for (String obj : splitTopLevelObjects(json)) {
                Job job = new Job();
                job.setId(extractString(obj, "id"));
                job.setModuleName(extractString(obj, "moduleName"));
                job.setDescription(extractString(obj, "description"));
                job.setRequiredSkills(extractStringArray(obj, "requiredSkills"));

                jobs.add(job);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return jobs;
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
                if (depth == 0) start = i;
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
        if (!m.find()) return values;

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
}