package com.medcore.hms.pharmacy.entity;

import com.medcore.hms.common.entity.BaseEntity;
import com.medcore.hms.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "stock_transaction",
        indexes = {
                @Index(name = "idx_stocktx_medicine", columnList = "medicine_id"),
                @Index(name = "idx_stocktx_batch", columnList = "batch_id"),
                @Index(name = "idx_stocktx_type", columnList = "transaction_type"),
                @Index(name = "idx_stocktx_date", columnList = "transaction_date")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockTransaction extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id")
    private MedicineBatch batch;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 30)
    private TransactionType transactionType;

    @NotNull
    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "quantity_after", nullable = false)
    private Integer quantityAfter;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by")
    private User performedBy;

    @Column(columnDefinition = "TEXT")
    private String reason;
}
