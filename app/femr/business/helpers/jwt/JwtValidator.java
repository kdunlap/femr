package femr.business.helpers.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.typesafe.config.Config;
import femr.common.dtos.jwt.IVerifiedJwt;
import femr.common.dtos.jwt.VerifiedJwt;
import play.Logger;
import play.libs.F;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.UnsupportedEncodingException;

@Singleton
public class JwtValidator implements IJwtValidator {
    private JWTVerifier verifier;

    @Inject
    public JwtValidator(Config configuration) throws UnsupportedEncodingException {
        String secret = configuration.getString("play.http.secret.key");
        String issuer = "fe.mr";
        if(configuration.hasPath("jwt.issuer")) {
            issuer = configuration.getString("jwt.issuer");
        }

        Algorithm algorithm = Algorithm.HMAC256(secret);
        verifier = JWT.require(algorithm)
            .withIssuer(issuer)
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
            Logger.error("Error validating Jwt: Invalid Signature or claim");
            return F.Either.Left(Error.ERR_INVALID_SIGNATURE_OR_CLAIM);
        }
    }
}
