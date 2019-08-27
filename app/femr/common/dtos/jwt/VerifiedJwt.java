package femr.common.dtos.jwt;

import com.auth0.jwt.interfaces.DecodedJWT;
import play.libs.Json;

import java.util.Date;

public class VerifiedJwt implements IVerifiedJwt {

    private String header;
    private String payload;
    private String issuer;
    private Integer userId;
    private Date expiresAt;
    private String fullToken;

    public VerifiedJwt(DecodedJWT decodedJWT) {
        this.header = decodedJWT.getHeader();
        this.payload = decodedJWT.getPayload();
        this.issuer = decodedJWT.getIssuer();
        this.expiresAt = decodedJWT.getExpiresAt();
        this.userId = decodedJWT.getClaim("user_id").asInt();
        this.fullToken = decodedJWT.getToken();
    }

    @Override
    public String getHeader() {
        return header;
    }

    @Override
    public String getPayload() {
        return payload;
    }

    @Override
    public String getIssuer() {
        return issuer;
    }

    @Override
    public Date getExpiresAt() {
        return expiresAt;
    }

    @Override
    public Integer getUserId(){
        return userId;
    }

    @Override
    public String getFullToken() {
        return fullToken;
    }

    @Override
    public String toString() {
        return Json.toJson(this).toString();
    }
}
