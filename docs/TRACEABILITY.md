# Requirements Traceability Matrix

Maps Product Backlog user stories (US-01 – US-35) to implementation status, source code, and automated tests.

**Legend:** Done | Partial | Missing


| ID    | Story                                  | Priority | Status  | Implementation                                                    | Tests                                                                    |
| ----- | -------------------------------------- | -------- | ------- | ----------------------------------------------------------------- | ------------------------------------------------------------------------ |
| US-01 | TA Registration                        | Must     | Done    | `RegisterServlet`, `AuthService.register`, `ProfileService`       | `RegisterServletTest`, `AuthServiceTest`, `UserLifecycleIntegrationTest` |
| US-02 | TA Login                               | Must     | Done    | `LoginServlet`, `AuthService.verifyCredentials`                   | `LoginServletTest`, `AuthServiceTest`                                    |
| US-03 | TA Logout                              | Must     | Done    | `LogoutServlet`                                                   | `LogoutServletTest`                                                      |
| US-04 | Access Control for Applicant Features  | Must     | Partial | Login redirect on protected pages; `ensureTa()` added in Phase 2  | `BaseServletTest`, `ApplicationServletTest`                              |
| US-05 | Request Password Reset                 | Should   | Partial | `ForgotPasswordServlet` — token + email in Phase 3                | —                                                                        |
| US-06 | Complete Password Reset                | Should   | Partial | `ResetPasswordServlet` — full flow in Phase 3                     | —                                                                        |
| US-07 | Create applicant profile               | Must     | Done    | `ProfileServlet`, `ProfileService.save`                           | `ProfileServiceTest`                                                     |
| US-08 | Edit TA Profile                        | Should   | Done    | `ProfileServlet` POST                                             | `ProfileServiceTest`                                                     |
| US-09 | View Profile                           | Should   | Done    | `ProfileServlet` GET, `MoApplicantProfileServlet`                 | `ProfileServiceTest`, `MoApplicantProfileServlet` (manual)               |
| US-10 | Delete Profile                         | Could    | Missing | No self-service profile delete                                    | —                                                                        |
| US-11 | Upload CV                              | Must     | Done    | `ProfileServlet` multipart upload                                 | `ProfileServiceTest`                                                     |
| US-12 | Replace CV                             | Should   | Done    | Upload overwrites `cvFileName`                                    | `ProfileServiceTest`                                                     |
| US-13 | View CV                                | Should   | Done    | `CvDownloadServlet`                                               | —                                                                        |
| US-14 | Delete CV                              | Could    | Partial | Delete action in `ProfileServlet`                                 | —                                                                        |
| US-15 | Browse available jobs                  | Must     | Done    | `JobServlet`, `JobService.listPublishedJobs`                      | `JobServiceTest`, `JobListFiltersTest`                                   |
| US-16 | Search and filter jobs                 | Must     | Done    | `JobListFilters`, `JobServlet` query params                       | `JobListFiltersTest`, `JobServiceTest`                                   |
| US-17 | View job details                       | Must     | Done    | `JobServlet?id=`, `job-detail.jsp`                                | `JobServiceTest`                                                         |
| US-18 | Sort job listings                      | Should   | Done    | `JobListFilters.sortJobs`                                         | `JobListFiltersTest`                                                     |
| US-19 | Save favorite jobs                     | Should   | Done    | `FavoriteService`, `JobServlet`                                   | `FavoriteServiceTest`                                                    |
| US-20 | View recently viewed jobs              | Should   | Done    | `RecentlyViewedService` (max 5)                                   | `RecentlyViewedServiceTest`                                              |
| US-21 | Apply for job                          | Must     | Done    | `ApplicationServlet` POST                                         | `ApplicationServiceTest`, `ApplicationFlowIntegrationTest`               |
| US-22 | Check application status               | Should   | Done    | `ApplicationServlet` GET filters                                  | `ApplicationServiceTest`                                                 |
| US-23 | Applications Manage (priority reorder) | Should   | Missing | No drag/reorder UI or field                                       | —                                                                        |
| US-24 | Identify missing skills                | Could    | Done    | `MissingSkillService`, `AiStreamServlet`                          | `MissingSkillServiceTest`                                                |
| US-25 | AI Job Recommendation                  | Could    | Done    | `RecommendationService`, `AiStreamServlet`                        | `RecommendationServiceTest`                                              |
| US-26 | Post job                               | Must     | Done    | `MoServlet` createJob/publishJob                                  | `MoServletTest`, `JobServiceTest`                                        |
| US-27 | Select applicants                      | Must     | Partial | `MoServlet` approve/reject; email in Phase 3; MO scope in Phase 2 | `MoServletTest`, `ApplicationServiceTest`                                |
| US-28 | View Applicant List                    | Must     | Partial | `MoServlet` dashboard; MO isolation in Phase 2                    | `MoServletTest`                                                          |
| US-29 | Skill matching (MO)                    | Could    | Partial | AI via admin demo; MO-side match via workload advice              | `SkillMatchServiceTest`                                                  |
| US-30 | View TA workload                       | Should   | Done    | `AdminServlet`, `TaWorkloadStats`                                 | `TaWorkloadStatsTest`, `WorkloadServiceTest`                             |
| US-31 | Balance TA workload                    | Should   | Partial | `WorkloadAdviceService`, MO AI stream                             | `WorkloadAdviceServiceTest`                                              |
| US-32 | Manage users                           | Must     | Partial | `AdminUserServlet` deactivate/delete; create MO in Phase 3        | `AdminUserServletTest`, `AuthServiceTest`                                |
| US-33 | Manage job postings                    | Should   | Partial | Admin delete + view; MO edit in Phase 3                           | `JobServiceTest`                                                         |
| US-34 | Export recruitment data                | Could    | Done    | `AdminExportServlet` CSV                                          | —                                                                        |
| US-35 | Override application status            | Should   | Done    | `AdminApplicationServlet`, `AdminApplicationsByStatusServlet`     | `ApplicationServiceTest`                                                 |


## Must-have completion summary


| Status  | Count (Must only)                                             |
| ------- | ------------------------------------------------------------- |
| Done    | 10                                                            |
| Partial | 4 (US-04, US-27, US-28, US-32)                                |
| Missing | 0 pure Must gaps; US-21 deadline/CV rules enforced in Phase 3 |


## Related documentation

- Persistence: JSON files in `WEB-INF/data/` via `FileStore` and `JobService` (no database).
- Gap details: [GAP_ANALYSIS.md](GAP_ANALYSIS.md)
- Requirements: [REQUIREMENTS.md](REQUIREMENTS.md)
- Test plan: [TEST_PLAN.md](TEST_PLAN.md)

