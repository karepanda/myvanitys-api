package com.api.myvanitys.domain.model;


import com.api.myvanitys.domain.valueobject.EntityId;
import lombok.Getter;

@Getter
public class Category {

    private final EntityId id;

    private final String name;

    public Category(EntityId id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        Category category = (Category) object;

        return id.equals(category.id);
    }

    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
