package com.jamlech.loanapp.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(nullable = false)
    private double principalAmount;
    @Column(nullable = false)
    private double interestRate;
    @Column(nullable = false)
    private int repaymentPeriod;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private LoanFrequency frequency;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private LoanStatus status;

//    private double totalPaid;
//    private boolean isFullyPaid;

@OneToMany(mappedBy = "loan", cascade = CascadeType.ALL, orphanRemoval = true)
@JsonManagedReference
    private List<RepaymentSchedule> repaymentSchedules= new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime createdAt;
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Loan loan = (Loan) o;
        return getId() != null && Objects.equals(getId(), loan.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
}
