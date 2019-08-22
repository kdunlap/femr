package femr.util.filters.jwt;

import femr.business.wrappers.sessions.jwt.IVerifiedJwt;
import play.libs.typedmap.TypedKey;

public class Attrs {
    public static final TypedKey<IVerifiedJwt> VERIFIED_JWT = TypedKey.<IVerifiedJwt>create("verifiedJwt");
}