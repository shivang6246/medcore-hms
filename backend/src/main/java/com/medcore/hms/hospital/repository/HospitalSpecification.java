package com.medcore.hms.hospital.repository;

import com.medcore.hms.hospital.entity.Hospital;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specification factory for dynamic Hospital query composition.
 *
 * <p>Supports keyword search across multiple columns, active status filtering,
 * and city name filtering — all combinable in a single query.
 *
 * <p><b>Join strategy:</b> Address is joined with {@code LEFT JOIN} so that
 * hospitals without an address record are still returned when no city filter
 * or search is applied.
 */
public class HospitalSpecification {

    private HospitalSpecification() {
        // Utility class — no instantiation
    }

    /**
     * Builds a composite {@link Specification} for filtering hospitals.
     *
     * @param search   keyword matched case-insensitively against: name, registrationNumber,
     *                 licenseNumber, email, city (OR semantics)
     * @param isActive exact match on the {@code isActive} flag; {@code null} = no filter
     * @param city     partial, case-insensitive match on city name; {@code null} = no filter
     * @return a composed Specification (AND across all active predicates)
     */
    public static Specification<Hospital> filterHospitals(String search, Boolean isActive, String city) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // ---- Active status filter ----
            if (isActive != null) {
                predicates.add(cb.equal(root.get("isActive"), isActive));
            }

            // ---- City filter (LEFT JOIN — hospitals without address still returned) ----
            if (city != null && !city.isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.join("address", JoinType.LEFT).get("city")),
                        "%" + city.trim().toLowerCase() + "%"
                ));
            }

            // ---- Keyword search across multiple columns ----
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";

                Predicate nameLike    = cb.like(cb.lower(root.get("name")), pattern);
                Predicate regLike     = cb.like(cb.lower(root.get("registrationNumber")), pattern);
                Predicate licLike     = cb.like(cb.lower(root.get("licenseNumber")), pattern);
                Predicate emailLike   = cb.like(cb.lower(root.get("email")), pattern);
                Predicate cityLike    = cb.like(cb.lower(root.join("address", JoinType.LEFT).get("city")), pattern);

                predicates.add(cb.or(nameLike, regLike, licLike, emailLike, cityLike));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
