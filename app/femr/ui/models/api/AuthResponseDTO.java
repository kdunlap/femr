package femr.ui.models.api;

public class AuthResponseDTO {
    private Integer userId;
    private String token;

    public AuthResponseDTO(Integer userId, String token) {
        this.userId = userId;
        this.token = token;
    }

    public Integer getUserId() {
        return userId;
    }

    public String getToken() {
        return token;
    }
}
