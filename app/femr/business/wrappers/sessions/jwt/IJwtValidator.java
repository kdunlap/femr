package femr.business.wrappers.sessions.jwt;

import play.libs.F;

public interface IJwtValidator {

    enum Error {
        ERR_INVALID_SIGNATURE_OR_CLAIM
    }

    F.Either<Error, IVerifiedJwt> verify(String token);
}
