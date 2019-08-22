package femr.business.wrappers.sessions.jwt;

import play.libs.F;
import play.mvc.Http;
import play.mvc.Result;

import java.util.function.Function;

public interface IJwtHelper {
    Result verify(Http.Request request, Function<F.Either<IJwtValidator.Error, IVerifiedJwt>, Result> f);
}
