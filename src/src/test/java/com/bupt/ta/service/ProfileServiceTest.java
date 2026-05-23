package com.bupt.ta.service;

import com.bupt.ta.model.ApplicantProfile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests profile completeness rules used before job application. */
class ProfileServiceTest {

    @Test
    void isApplicantProfileComplete_falseWhenSkillsMissing() {
        ApplicantProfile p = minimalCompleteProfile();
        p.skills = "";
        assertFalse(ProfileService.isApplicantProfileComplete(p));
    }

    @Test
    void isApplicantProfileComplete_trueWhenAllRequiredFieldsPresent() {
        assertTrue(ProfileService.isApplicantProfileComplete(minimalCompleteProfile()));
    }

    @Test
    void getByUserId_loadsStoredProfile(@TempDir Path dataDir) throws Exception {
        String json = "[{\"userId\":\"u9\",\"fullName\":\"Ann\",\"gender\":\"F\",\"degree\":\"Master\","
                + "\"major\":\"CS\",\"studentId\":\"S9\",\"idCard\":\"ID9\",\"phone\":\"1\",\"email\":\"a@t.com\","
                + "\"educationJson\":\"[{\\\"school\\\":\\\"BUPT\\\",\\\"degree\\\":\\\"BSc\\\",\\\"major\\\":\\\"CS\\\",\\\"period\\\":\\\"2020-2024\\\"}]\","
                + "\"courses\":\"CS101\",\"freeTime\":\"Mon\",\"skills\":\"Java\"}]";
        Files.write(dataDir.resolve("profiles.json"), json.getBytes(StandardCharsets.UTF_8));

        ProfileService svc = new ProfileService(dataDir);
        Optional<ApplicantProfile> p = svc.getByUserId("u9");
        assertTrue(p.isPresent());
        org.junit.jupiter.api.Assertions.assertEquals("Ann", p.get().fullName);
    }

    private static ApplicantProfile minimalCompleteProfile() {
        ApplicantProfile p = new ApplicantProfile();
        p.userId = "u1";
        p.fullName = "Test User";
        p.gender = "Male";
        p.degree = "Master";
        p.major = "Computer Science";
        p.studentId = "S001";
        p.idCard = "110000000000000000";
        p.phone = "13800000000";
        p.email = "ta@example.com";
        p.courses = "CS101\nCS102";
        p.freeTime = "Weekday evenings";
        p.skills = "Java, Python";
        p.educationJson = "[{\"school\":\"BUPT\",\"degree\":\"BSc\",\"major\":\"CS\",\"period\":\"2020-2024\"}]";
        return p;
    }
}
