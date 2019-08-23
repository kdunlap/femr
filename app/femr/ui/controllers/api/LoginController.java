package femr.ui.controllers.api;

import com.google.inject.Inject;
import femr.business.services.core.api.IAuthService;
import femr.common.dtos.CurrentUser;
import femr.common.dtos.ServiceResponse;
import femr.ui.models.api.AuthResponseDTO;
import femr.ui.models.sessions.CreateViewModel;
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

            // TODO - can we implement a refresh token here?

            CurrentUser user = response.getResponseObject();
            ServiceResponse<String> tokenResponse = authService.getSignedToken(user);

            // TODO - maybe this should be more specific?
            if(tokenResponse.hasErrors()) throw new RuntimeException();

            AuthResponseDTO authResponseDTO = new AuthResponseDTO(user.getId(), tokenResponse.getResponseObject());

            return ok(Json.toJson(authResponseDTO));
        }
    }

    public Result logout(){

        return ok("implement me");
    }

    public Result refresh(){

        return ok("implement me");
    }
}
