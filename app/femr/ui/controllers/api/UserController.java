package femr.ui.controllers.api;

import com.google.inject.Inject;
import femr.business.services.core.IUserService;
import femr.common.dtos.jwt.IVerifiedJwt;
import femr.common.dtos.ServiceResponse;
import femr.common.models.UserItem;
import femr.ui.models.api.UserResponseDTO;
import femr.util.filters.jwt.Attrs;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

public class UserController extends Controller {

    private final IUserService userService;

    @Inject
    public UserController(IUserService userService) {

        this.userService = userService;
    }

    public Result get() {

        // verifiedJwt should always be set here
        IVerifiedJwt verifiedJwt = request().attrs().getOptional(Attrs.VERIFIED_JWT)
            .orElseThrow(RuntimeException::new);

        ServiceResponse<UserItem> userServiceResponse = userService.retrieveUser(verifiedJwt.getUserId());
        if(userServiceResponse.hasErrors()) throw new RuntimeException();

        UserItem userItem = userServiceResponse.getResponseObject();
        UserResponseDTO userJson = new UserResponseDTO();
        userJson.setId(userItem.getId());
        userJson.setFirstName(userItem.getFirstName());
        userJson.setLastName(userItem.getLastName());
        userJson.setEmail(userItem.getEmail());
        userJson.setRoles(userItem.getRoles());
        userJson.setRequiresPasswordReset(userItem.isPasswordReset());

        return ok(Json.toJson(userJson));
    }

}
