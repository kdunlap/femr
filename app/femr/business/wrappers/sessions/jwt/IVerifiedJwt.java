package femr.business.wrappers.sessions.jwt;

import java.util.Date;

public interface IVerifiedJwt {
    String getHeader();
    String getPayload();
    String getIssuer();
    Date getExpiresAt();
}
