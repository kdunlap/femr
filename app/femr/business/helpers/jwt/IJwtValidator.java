package femr.business.helpers.jwt;

import femr.common.dtos.jwt.IVerifiedJwt;
import play.libs.F;

public interface IJwtValidator {

    enum Error {
        ERR_INVALID_SIGNATURE_OR_CLAIM
    }

    F.Either<Error, IVerifiedJwt> verify(String token);
}
