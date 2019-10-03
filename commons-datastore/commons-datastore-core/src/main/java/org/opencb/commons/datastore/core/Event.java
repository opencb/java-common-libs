package org.opencb.commons.datastore.core;

public class Event {

    private Type type;
    private int code;
    private String id;
    private String name;
    private String description;

    public Event() {
    }

    public Event(Type type, String description) {
        this.type = type;
        this.description = description;
    }

    public Event(Type type, String id, String description) {
        this.type = type;
        this.id = id;
        this.description = description;
    }

    public Event(Type type, int code, String name, String description) {
        this.type = type;
        this.code = code;
        this.name = name;
        this.description = description;
    }

    public Event(Type type, int code, String id, String name, String description) {
        this.type = type;
        this.code = code;
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public enum Type {
        INFO,
        WARNING,
        ERROR
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Event{");
        sb.append("type=").append(type);
        sb.append(", code=").append(code);
        sb.append(", id='").append(id).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public Type getType() {
        return type;
    }

    public Event setType(Type type) {
        this.type = type;
        return this;
    }

    public int getCode() {
        return code;
    }

    public Event setCode(int code) {
        this.code = code;
        return this;
    }

    public String getId() {
        return id;
    }

    public Event setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public Event setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Event setDescription(String description) {
        this.description = description;
        return this;
    }
}
