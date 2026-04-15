package com.newsaggregator.domain.model;

import java.util.Objects;

public class Category {
    private final CategoryId id;
    private final String name;
    private final String color;
    private final String icon;

    public Category(CategoryId id, String name, String color, String icon) {
        this.id = Objects.requireNonNull(id, "ID darf nicht null sein");
        this.name = Objects.requireNonNull(name, "Name darf nicht null sein");
        this.color = color != null ? color : "#667eea";
        this.icon = icon != null ? icon : "label";
    }

    public static Category create(String name, String color, String icon) {
        return new Category(CategoryId.generate(), name, color, icon);
    }

    public CategoryId getId() { return id; }
    public String getName() { return name; }
    public String getColor() { return color; }
    public String getIcon() { return icon; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return Objects.equals(id, category.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", color='" + color + '\'' +
                ", icon='" + icon + '\'' +
                '}';
    }
}
