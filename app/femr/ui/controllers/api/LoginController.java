package femr.ui.controllers.api;

import com.google.inject.Inject;
import femr.business.services.core.api.IAuthService;
import femr.common.dtos.CurrentUser;
import femr.common.dtos.ServiceResponse;
import femr.common.dtos.jwt.IVerifiedJwt;
import femr.common.dtos.jwt.UserTokens;
import femr.ui.models.api.AuthResponseDTO;
import femr.ui.models.api.RefreshRequestDTO;
import femr.ui.models.sessions.CreateViewModel;
import femr.util.filters.jwt.Attrs;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

public class LoginController extends Controller {

    private final FormFactory formFactory;
    private final IAuthService authService;

    @Inject
    public LoginController(FormFactory formFactory, IAuthService authService) {

        this.formFactory = formFactory;
        this.authService = authService;
    }

    public Result login(){

        final Form<CreateViewModel> createViewModelForm = formFactory.form(CreateViewModel.class);
        CreateViewModel viewModel = createViewModelForm.bindFromRequest().get();
        ServiceResponse<CurrentUser> response = authService.authenticateUser(viewModel.getEmail(), viewModel.getPassword(), request().remoteAddress());

        if (response.hasErrors()) return forbidden();
        else{
            CurrentUser user = response.getResponseObject();
            ServiceResponse<UserTokens> tokenResponse = authService.createUserTokens(user);
            // TODO - maybe this should be more specific?
            if(tokenResponse.hasErrors()) throw new RuntimeException();

            UserTokens userTokens = tokenResponse.getResponseObject();
            AuthResponseDTO authResponseDTO = new AuthResponseDTO(
                userTokens.getUserId(),
                userTokens.getAuthToken(),
                userTokens.getRefreshToken());

            return ok(Json.toJson(authResponseDTO));
        }
    }

    public Result logout(){

        IVerifiedJwt verifiedJwt = request().attrs().getOptional(Attrs.VERIFIED_JWT)
            .orElseThrow(RuntimeException::new);

        // clear refresh token from database
        ServiceResponse<Integer> logoutResponse = authService.logoutUser(verifiedJwt.getUserId());
        if(logoutResponse.hasErrors()) throw new RuntimeException();

        return ok(Json.toJson(true));
    }

    public Result refresh(){

        final Form<RefreshRequestDTO> createViewModelForm = formFactory.form(RefreshRequestDTO.class);
        RefreshRequestDTO refreshRequestDTO = createViewModelForm.bindFromRequest().get();

        // get refresh token from post
        ServiceResponse<UserTokens> refreshResponse = authService.refreshUserTokens(refreshRequestDTO.getRefreshToken());
        if(refreshResponse.hasErrors()) throw new RuntimeException();

        UserTokens userTokens = refreshResponse.getResponseObject();
        AuthResponseDTO authResponseDTO = new AuthResponseDTO(
            userTokens.getUserId(),
            userTokens.getAuthToken(),
            userTokens.getRefreshToken());

        return ok(Json.toJson(authResponseDTO));
    }
}
