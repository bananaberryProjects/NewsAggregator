package com.newsaggregator.domain.model;

import java.util.Objects;
import java.util.UUID;

public class CategoryId {
    private final UUID value;

    public CategoryId(UUID value) {
        this.value = Objects.requireNonNull(value, "ID-Wert darf nicht null sein");
    }

    public static CategoryId generate() {
        return new CategoryId(UUID.randomUUID());
    }

    public static CategoryId of(String id) {
        return new CategoryId(UUID.fromString(id));
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CategoryId that = (CategoryId) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
