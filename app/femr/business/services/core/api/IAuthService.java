package femr.business.services.core.api;

import femr.common.dtos.CurrentUser;
import femr.common.dtos.ServiceResponse;

import java.io.UnsupportedEncodingException;

public interface IAuthService {

    ServiceResponse<CurrentUser> authenticateUser(String email, String password, String ipAddress);
    ServiceResponse<String> getSignedToken(CurrentUser user);
}
