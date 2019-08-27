package femr.ui.models.api;

public class AuthResponseDTO {
    private Integer userId;
    private String token;
    private String refreshToken;

    public AuthResponseDTO(Integer userId, String token, String refreshToken) {
        this.userId = userId;
        this.token = token;
        this.refreshToken = refreshToken;
    }

    public Integer getUserId() {
        return userId;
    }

    public String getToken() {
        return token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}
