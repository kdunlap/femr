package femr.common.dtos.jwt;

public class UserTokens {

    private Integer userId;
    private String authToken;
    private String refreshToken;

    public UserTokens(Integer userId, String authToken, String refreshToken) {
        this.userId = userId;
        this.authToken = authToken;
        this.refreshToken = refreshToken;
    }

    public Integer getUserId() {
        return userId;
    }

    public String getAuthToken() {
        return authToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}
