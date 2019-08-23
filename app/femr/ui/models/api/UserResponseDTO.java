package femr.ui.models.api;

import java.util.ArrayList;
import java.util.List;

public class UserResponseDTO {
    private int id;
    private String firstName;
    private String lastName;
    private String email;
    private List<String> roles = new ArrayList<>();
    private boolean requiresPasswordReset;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public boolean isRequiresPasswordReset() {
        return requiresPasswordReset;
    }

    public void setRequiresPasswordReset(boolean requiresPasswordReset) {
        this.requiresPasswordReset = requiresPasswordReset;
    }
}
