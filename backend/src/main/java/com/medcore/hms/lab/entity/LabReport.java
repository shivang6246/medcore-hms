package com.medcore.hms.lab.entity;

import com.medcore.hms.common.entity.BaseEntity;
import com.medcore.hms.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

/**
 * LabReport entity representing clinical test results, remarks, report file URL, and publication timestamp.
 */
@Entity
@Table(
        name = "lab_report",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_lab_report_test", columnNames = "lab_test_id")
        },
        indexes = {
                @Index(name = "idx_labreport_test", columnList = "lab_test_id"),
                @Index(name = "idx_labreport_date", columnList = "reported_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LabReport extends BaseEntity {

    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_test_id", nullable = false, unique = true)
    private LabTest labTest;

    @NotBlank
    @Column(columnDefinition = "TEXT", nullable = false)
    private String result;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "report_file_url", length = 500)
    private String reportFileUrl;

    @Column(name = "reported_at", nullable = false)
    private LocalDateTime reportedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_by")
    private User reportedBy;
}
