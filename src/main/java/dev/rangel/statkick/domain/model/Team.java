package dev.rangel.statkick.domain.model;

public class Team {
    private final Long id;
    private final String name;

    public Team(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
}