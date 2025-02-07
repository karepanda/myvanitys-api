package com.api.myvanitys.domain.model;


import com.api.myvanitys.domain.valueobject.EntityId;
import lombok.NonNull;

public class Category {

    private final EntityId id;

    private final String name;

    public Category(@NonNull EntityId id, @NonNull String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
