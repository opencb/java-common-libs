package org.opencb.commons.datastore.core;

public class Event {

    private Type type;
    private int code;
    private String id;
    private String name;
    private String message;

    public Event() {
    }

    public Event(Type type, String message) {
        this.type = type;
        this.message = message;
    }

    public Event(Type type, String id, String message) {
        this.type = type;
        this.id = id;
        this.message = message;
    }

    public Event(Type type, int code, String name, String message) {
        this.type = type;
        this.code = code;
        this.name = name;
        this.message = message;
    }

    public Event(Type type, int code, String id, String name, String message) {
        this.type = type;
        this.code = code;
        this.id = id;
        this.name = name;
        this.message = message;
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
        sb.append(", description='").append(message).append('\'');
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

    public String getMessage() {
        return message;
    }

    public Event setMessage(String message) {
        this.message = message;
        return this;
    }
}
