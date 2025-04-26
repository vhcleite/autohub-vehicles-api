// Pacote: src/main/java/com/fiap/autohub/autohub_vehicles_api/infrastructure/persistence/specifications/VehicleSpecification.java
package com.fiap.autohub.autohub_vehicles_api.infrastructure.persistence.specifications;

import com.fiap.autohub.autohub_vehicles_api.infrastructure.persistence.entities.VehiclePersistenceEntity;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

public class VehicleSpecification {

    private static boolean isProvided(Object value) {
        if (value == null) return false;
        if (value instanceof String) {
            return StringUtils.hasText((String) value);
        }
        return true;
    }

    public static Specification<VehiclePersistenceEntity> hasMake(String make) {
        return (root, query, cb) -> {
            if (!isProvided(make)) {
                return null;
            }

            return cb.equal(cb.lower(root.get("make")), make.toLowerCase());
        };
    }

    public static Specification<VehiclePersistenceEntity> hasModel(String model) {
        return (root, query, cb) -> {
            if (!isProvided(model)) {
                return null;
            }
            return cb.like(cb.lower(root.get("model")), "%" + model.toLowerCase() + "%");
        };
    }

    public static Specification<VehiclePersistenceEntity> hasColor(String color) {
        return (root, query, cb) -> {
            if (!isProvided(color)) {
                return null;
            }
            return cb.equal(cb.lower(root.get("color")), color.toLowerCase());
        };
    }

    public static Specification<VehiclePersistenceEntity> hasYearGreaterThanOrEqualTo(Integer minYear) {
        return (root, query, cb) -> {
            if (!isProvided(minYear)) {
                return null;
            }
            return cb.greaterThanOrEqualTo(root.get("year"), minYear);
        };
    }

    public static Specification<VehiclePersistenceEntity> hasYearLessThanOrEqualTo(Integer maxYear) {
        return (root, query, cb) -> {
            if (!isProvided(maxYear)) {
                return null;
            }
            return cb.lessThanOrEqualTo(root.get("year"), maxYear);
        };
    }

    public static Specification<VehiclePersistenceEntity> hasPriceGreaterThanOrEqualTo(BigDecimal minPrice) {
        return (root, query, cb) -> {
            if (!isProvided(minPrice)) {
                return null;
            }
            return cb.greaterThanOrEqualTo(root.get("price"), minPrice);
        };
    }

    public static Specification<VehiclePersistenceEntity> hasPriceLessThanOrEqualTo(BigDecimal maxPrice) {
        return (root, query, cb) -> {
            if (!isProvided(maxPrice)) {
                return null;
            }
            return cb.lessThanOrEqualTo(root.get("price"), maxPrice);
        };
    }
}