package femr.business.wrappers.sessions.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.typesafe.config.Config;
import play.Logger;
import play.libs.F;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.UnsupportedEncodingException;

@Singleton
public class JwtValidator implements IJwtValidator {
    private String secret;
    private JWTVerifier verifier;

    @Inject
    public JwtValidator(Config config) throws UnsupportedEncodingException {
        this.secret = config.getString("play.http.secret.key");

        Algorithm algorithm = Algorithm.HMAC256(secret);
        verifier = JWT.require(algorithm)
            .withIssuer("fEMR")
            .build();
    }

    @Override
    public F.Either<Error, IVerifiedJwt> verify(String token) {
        try {
            DecodedJWT jwt = verifier.verify(token);
            VerifiedJwt verifiedJwt = new VerifiedJwt(jwt);
            return F.Either.Right(verifiedJwt);
        }
        catch (JWTVerificationException exception) {
            //Invalid signature/claims
            Logger.error("f=JwtValidator, event=verify, exception=JWTVerificationException, msg={}",
                exception.getMessage());
            return F.Either.Left(Error.ERR_INVALID_SIGNATURE_OR_CLAIM);
        }
    }
}
