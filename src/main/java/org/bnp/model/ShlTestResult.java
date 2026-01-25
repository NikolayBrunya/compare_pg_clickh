package org.bnp.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class ShlTestResult {
    private long employeeId;
    private String departmentId;
    private LocalDate testDate;
    private int score;
    private int cognitiveScore;
    private int behavioralScore;
    private String region;
    private String position;
    private int teamworkScore;
    private int stressResistanceScore;
    private int leadershipPotential;
    private int learningAgility;
    private String testVersion;
    private int testDurationMinutes;
    private boolean isPassed;
    private String railwayDirection;
}
