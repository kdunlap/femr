package femr.ui.controllers.api;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import femr.business.services.core.ISessionService;
import femr.business.services.core.IUserService;
import femr.business.wrappers.sessions.jwt.IJwtHelper;
import femr.business.wrappers.sessions.jwt.IVerifiedJwt;
import femr.business.wrappers.sessions.jwt.VerifiedJwt;
import femr.common.dtos.CurrentUser;
import femr.common.dtos.ServiceResponse;
import femr.ui.models.sessions.CreateViewModel;
import femr.util.filters.jwt.Attrs;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import java.io.UnsupportedEncodingException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Optional;

public class SessionsController extends Controller {

    private final FormFactory formFactory;
    private final ISessionService sessionsService;
    private final IUserService userService;
    private final IJwtHelper jwtHelper;

    @Inject
    public SessionsController(FormFactory formFactory, ISessionService sessionsService, IUserService userService, IJwtHelper jwtHelper) {

        this.formFactory = formFactory;
        this.sessionsService = sessionsService;
        this.userService = userService;
        this.jwtHelper = jwtHelper;
    }

    @Inject
    private Config config;

    public Result generateSignedToken() throws UnsupportedEncodingException {
        CurrentUser currentUser = this.sessionsService.retrieveCurrentUserSession();
        return ok("signed token: " + getSignedToken(currentUser.getId()));
    }

    public Result login() throws UnsupportedEncodingException {

        final Form<CreateViewModel> createViewModelForm = formFactory.form(CreateViewModel.class);
        CreateViewModel viewModel = createViewModelForm.bindFromRequest().get();
        ServiceResponse<CurrentUser> response = sessionsService.createSession(viewModel.getEmail(), viewModel.getPassword(), request().remoteAddress());

        if (!response.hasErrors()) {

            // TODO - do the same stuff as the regular front end login - centralize this logic
            // TODO - visit logs in new JWT code and make more like fEMR logs
            // TODO - log invalid attempts in DB
            // TODO - can we implement a refresh token here?

            // TODO - use actual objects and jackson here
            ObjectNode result = Json.newObject();
            ObjectNode data = Json.newObject();
            data.put("access_token", getSignedToken(response.getResponseObject().getId()));
            result.put("data", data );

            return ok(result);
        } else {
            Logger.error("login failed: {}", response.getErrors());
        }

        return forbidden();
    }

    private String getSignedToken(Integer userId) throws UnsupportedEncodingException {

        String secret = config.getString("play.http.secret.key");
        int sessionLength = config.getInt("sessionTimeout");
        // TODO - this should probably not be in the controller?
        // TODO - control expires at via config

        Algorithm algorithm = Algorithm.HMAC256(secret);
        return JWT.create()
            .withIssuer("fEMR")
            .withClaim("user_id", userId)
            .withExpiresAt(Date.from(ZonedDateTime.now(ZoneId.systemDefault()).plusMinutes(sessionLength).toInstant()))
            .sign(algorithm);
    }

    public Result requiresJwt() {
        Optional<IVerifiedJwt> oVerifiedJwt = request().attrs().getOptional(Attrs.VERIFIED_JWT);
        return oVerifiedJwt.map(jwt -> {
                Logger.debug(jwt.toString());
                return ok("access granted via filter until: ".concat(jwt.getExpiresAt().toString()));
            })
            .orElse(forbidden("eh, no verified jwt found"));
    }
}
