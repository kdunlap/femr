package femr.util.filters.jwt;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import javax.inject.Inject;
import akka.stream.Materializer;
import femr.business.helpers.jwt.IJwtValidator;
import femr.common.dtos.jwt.IVerifiedJwt;
import play.libs.F;
import play.mvc.*;
import play.routing.HandlerDef;
import play.routing.Router;

import static play.mvc.Results.forbidden;

public class JwtFilter extends Filter {
    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String BEARER = "Bearer ";
    private static final String ROUTE_MODIFIER_NO_JWT_FILTER_TAG = "noJwtFilter";
    private static final String ERR_AUTHORIZATION_HEADER = "Authorization Header is required and not present";
    private static final String ERR_INVALID_SIGNATURE = "Invalid Signature or Claim";
    private IJwtValidator jwtValidator;

    @Inject
    public JwtFilter(Materializer mat, IJwtValidator jwtValidator) {
        super(mat);
        this.jwtValidator = jwtValidator;
    }

    @Override
    public CompletionStage<Result> apply(Function<Http.RequestHeader, CompletionStage<Result>> nextFilter, Http.RequestHeader requestHeader) {
        if (requestHeader.attrs().containsKey(Router.Attrs.HANDLER_DEF)) {
            HandlerDef handler = requestHeader.attrs().get(Router.Attrs.HANDLER_DEF);
            List<String> modifiers = handler.getModifiers();

            // only protect /api endpoints and those without the `+noJwtFilter` modifier
            if(!handler.path().startsWith("/api") || modifiers.contains(ROUTE_MODIFIER_NO_JWT_FILTER_TAG)) {

                return nextFilter.apply(requestHeader);
            }
        }

        Optional<String> authHeader =  requestHeader.getHeaders().get(HEADER_AUTHORIZATION);

        if (!authHeader.filter(ah -> ah.contains(BEARER)).isPresent()) {
            return CompletableFuture.completedFuture(forbidden(ERR_AUTHORIZATION_HEADER));
        }

        String token = authHeader.map(ah -> ah.replace(BEARER, "")).orElse("");
        F.Either<IJwtValidator.Error, IVerifiedJwt> res = jwtValidator.verify(token);

        if (res.left.isPresent()) {

            String message = res.left.get().toString();
            if(res.left.get() == IJwtValidator.Error.ERR_INVALID_SIGNATURE_OR_CLAIM){
                message = ERR_INVALID_SIGNATURE;
            }

            return CompletableFuture.completedFuture(forbidden(message));
        }

        return nextFilter.apply(requestHeader.withAttrs(requestHeader.attrs().put(Attrs.VERIFIED_JWT, res.right.get())));
    }
}
