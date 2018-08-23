package org.opencb.commons.datastore.core.result;

public class Error {

    private int code;

    /**
     * example: USER_NOT_FOUND, ...
     */
    private String name;
    private String description;

    public Error() {
    }

    public Error(int code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Error{");
        sb.append("code=").append(code);
        sb.append(", name='").append(name).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public int getCode() {
        return code;
    }

    public Error setCode(int code) {
        this.code = code;
        return this;
    }

    public String getName() {
        return name;
    }

    public Error setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Error setDescription(String description) {
        this.description = description;
        return this;
    }
}
