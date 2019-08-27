package femr.business.services.core.api;

import femr.common.dtos.CurrentUser;
import femr.common.dtos.ServiceResponse;
import femr.common.dtos.jwt.UserTokens;

public interface IAuthService {

    ServiceResponse<CurrentUser> authenticateUser(String email, String password, String ipAddress);

    ServiceResponse<UserTokens> createUserTokens(CurrentUser user);

    ServiceResponse<Integer> logoutUser(Integer userId);

    ServiceResponse<UserTokens> refreshUserTokens(String refreshToken);
}