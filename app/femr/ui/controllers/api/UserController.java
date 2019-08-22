package femr.ui.controllers.api;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import femr.business.services.core.ISessionService;
import femr.business.services.core.IUserService;
import femr.business.wrappers.sessions.jwt.IJwtHelper;
import femr.business.wrappers.sessions.jwt.IVerifiedJwt;
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

public class UserController extends Controller {

    private final FormFactory formFactory;
    private final ISessionService sessionsService;
    private final IUserService userService;
    private final IJwtHelper jwtHelper;

    @Inject
    public UserController(FormFactory formFactory, ISessionService sessionsService, IUserService userService, IJwtHelper jwtHelper) {

        this.formFactory = formFactory;
        this.sessionsService = sessionsService;
        this.userService = userService;
        this.jwtHelper = jwtHelper;
    }

    @Inject
    private Config config;


    public Result get() {

        // TODO - use actual objects and jackson here
        ObjectNode result = Json.newObject();
        ObjectNode user = Json.newObject();
        user.put("user_id", 3);
        result.put("data", user);

        return ok(result);
    }

}
