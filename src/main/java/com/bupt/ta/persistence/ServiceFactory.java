package com.bupt.ta.persistence;

import com.bupt.ta.service.ApplicationService;
import com.bupt.ta.service.AuditService;
import com.bupt.ta.service.AuthService;
import com.bupt.ta.service.FavoriteService;
import com.bupt.ta.service.JobService;
import com.bupt.ta.service.NotificationService;
import com.bupt.ta.service.PasswordResetService;
import com.bupt.ta.service.ProfileService;
import com.bupt.ta.service.RecentlyViewedService;
import com.bupt.ta.service.WorkloadService;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Wires services from JSON data files under {@code WEB-INF/data} and CV storage directory.
 */
public final class ServiceFactory {

    public static final String SERVLET_CONTEXT_KEY = "com.bupt.ta.ServiceFactory";

    private final Path dataDir;
    private final Path cvDataDir;
    private final AuthService authService;
    private final ProfileService profileService;
    private final ApplicationService applicationService;
    private final JobService jobService;
    private final FavoriteService favoriteService;
    private final RecentlyViewedService recentlyViewedService;
    private final WorkloadService workloadService;
    private final AuditService auditService;
    private final NotificationService notificationService;
    private final PasswordResetService passwordResetService;

    public ServiceFactory(Path dataDir, Path cvDataDir) throws IOException {
        this.dataDir = dataDir;
        this.cvDataDir = cvDataDir;
        Files.createDirectories(dataDir);
        Files.createDirectories(cvDataDir);

        this.authService = new AuthService(dataDir);
        this.profileService = new ProfileService(dataDir);
        this.applicationService = new ApplicationService(dataDir);
        this.jobService = new JobService(dataDir.resolve("jobs.json").toString());
        this.favoriteService = new FavoriteService(dataDir);
        this.recentlyViewedService = new RecentlyViewedService(dataDir);
        this.workloadService = new WorkloadService();
        this.auditService = new AuditService(dataDir);
        this.notificationService = new NotificationService();
        this.passwordResetService = new PasswordResetService(dataDir, authService, notificationService);
    }

    public static ServiceFactory fromServletContext(ServletContext ctx) throws IOException {
        String dataPath = ctx.getRealPath("/WEB-INF/data");
        String cvPath = ctx.getRealPath("/WEB-INF/data/cv");
        Path dataDir = dataPath != null ? Paths.get(dataPath) : Paths.get("data");
        Path cvDir = cvPath != null ? Paths.get(cvPath) : dataDir.resolve("cv");
        return new ServiceFactory(dataDir, cvDir);
    }

    public static ServiceFactory forTests(Path dataDir, Path cvDir) throws IOException {
        return new ServiceFactory(dataDir, cvDir);
    }

    public Path getDataDir() {
        return dataDir;
    }

    public Path getCvDataDir() {
        return cvDataDir;
    }

    public AuthService getAuthService() {
        return authService;
    }

    public ProfileService getProfileService() {
        return profileService;
    }

    public ApplicationService getApplicationService() {
        return applicationService;
    }

    public JobService getJobService() {
        return jobService;
    }

    public FavoriteService getFavoriteService() {
        return favoriteService;
    }

    public RecentlyViewedService getRecentlyViewedService() {
        return recentlyViewedService;
    }

    public WorkloadService getWorkloadService() {
        return workloadService;
    }

    public AuditService getAuditService() {
        return auditService;
    }

    public NotificationService getNotificationService() {
        return notificationService;
    }

    public PasswordResetService getPasswordResetService() {
        return passwordResetService;
    }
}
