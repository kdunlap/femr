package femr.business.services.system.api;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.typesafe.config.Config;
import femr.business.helpers.jwt.IJwtValidator;
import femr.business.services.core.IUserService;
import femr.business.services.core.api.IAuthService;
import femr.common.dtos.CurrentUser;
import femr.common.dtos.ServiceResponse;
import femr.common.dtos.jwt.IVerifiedJwt;
import femr.common.dtos.jwt.UserTokens;
import femr.data.daos.core.IUserRepository;
import femr.data.models.core.IUser;
import femr.util.calculations.dateUtils;
import femr.util.encryptions.IPasswordEncryptor;
import org.joda.time.DateTime;
import org.joda.time.Days;
import play.Logger;
import play.libs.F;

import javax.inject.Inject;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

public class AuthService implements IAuthService {

    private IUserService userService;
    private IPasswordEncryptor passwordEncryptor;
    private final IUserRepository userRepository;
    private final Config configuration;
    private IJwtValidator jwtValidator;

    @Inject
    public AuthService(IUserService userService, IPasswordEncryptor passwordEncryptor, IUserRepository userRepository, Config configuration, IJwtValidator jwtValidator) {
        this.userService = userService;
        this.passwordEncryptor = passwordEncryptor;
        this.userRepository = userRepository;
        this.configuration = configuration;
        this.jwtValidator = jwtValidator;
    }

    @Override
    public ServiceResponse<CurrentUser> authenticateUser(String email, String password, String ipAddress){

        IUser user = userService.retrieveByEmail(email);

        //set to a default IP address
        byte[] ipAddressBinary = new byte[]{0, 0, 0, 0};
        //try to get the IP address of the incoming request to create a session
        try {
            ipAddressBinary = InetAddress.getByName(ipAddress).getAddress();
        } catch (Exception ex) {
            //don't do anything because the default IP address was initialized
        }

        ServiceResponse<CurrentUser> response = new ServiceResponse<>();

        if (user == null) {

            //user doesn't exist
            response.addError("", "Invalid email or password.");

        } else if (user.getDeleted() || !passwordEncryptor.verifyPassword(password, user.getPassword())) {

            //user has been deleted or they entered a wrong password
            userRepository.createLoginAttempt(email, false, ipAddressBinary, user.getId());
            response.addError("", "Invalid email or password.");

        } else { //success!

            // update last login status
            user.setLastLogin(dateUtils.getCurrentDateTime());

            // flag user needing reset
            int daysBetweenPasswordReset = Days.daysBetween(user.getPasswordCreatedDate(), DateTime.now()).getDays();
            user.setPasswordReset(daysBetweenPasswordReset > 90);
            userService.update(user, false);

            userRepository.createLoginAttempt(email, true, ipAddressBinary, user.getId());

            CurrentUser currentUserDTO = new CurrentUser(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRoles(),
                null,
                0L
            );

            response.setResponseObject(currentUserDTO);
        }

        return response;
    }

    @Override
    public ServiceResponse<UserTokens> createUserTokens(CurrentUser user){

        String secret = configuration.getString("play.http.secret.key");
        int authTimeout = configuration.getInt("jwt.authTimeout");
        int refreshTimeout = configuration.getInt("jwt.refreshTimeout");
        String issuer = "fe.mr";
        if(configuration.hasPath("jwt.issuer")) {
            issuer = configuration.getString("jwt.issuer");
        }
        ServiceResponse<UserTokens> response = new ServiceResponse<>();

        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            String authToken = JWT.create()
                .withIssuer(issuer)
                .withClaim("user_id", user.getId())
                .withSubject(user.getEmail())
                .withIssuedAt(Date.from(ZonedDateTime.now().toInstant()))
                .withExpiresAt(Date.from(ZonedDateTime.now(ZoneId.systemDefault()).plusMinutes(authTimeout).toInstant()))
                .sign(algorithm);

            // TODO - the refresh token might not need to be straight jwt?
            Date issuedAt = Date.from(ZonedDateTime.now().toInstant());

            String refreshToken = JWT.create()
                .withIssuer(issuer)
                .withClaim("user_id", user.getId())
                .withSubject(user.getEmail())
                .withIssuedAt(issuedAt)
                .withExpiresAt(Date.from(ZonedDateTime.now(ZoneId.systemDefault()).plusMinutes(refreshTimeout).toInstant()))
                .sign(algorithm);

            // store refresh token with user
            IUser userModel = userRepository.retrieveUserById(user.getId());
            userModel.setRefreshToken(refreshToken);
            userModel.setRefreshTokenIssuedAt(new DateTime(issuedAt));
            userRepository.updateUser(userModel);

            UserTokens tokens = new UserTokens(user.getId(), authToken, refreshToken);
            response.setResponseObject(tokens);
        }
        catch (UnsupportedEncodingException e){
            Logger.error("Error getting signed token: Unsupported character encoding in secret");
            response.addError("", "Unsupported character encoding in secret");
        }

        return response;
    }

    @Override
    public ServiceResponse<UserTokens> refreshUserTokens(String refreshToken){

        F.Either<IJwtValidator.Error, IVerifiedJwt> res = jwtValidator.verify(refreshToken);

        if (res.left.isPresent()) {
            String message = res.left.get().toString();
            if(res.left.get() == IJwtValidator.Error.ERR_INVALID_SIGNATURE_OR_CLAIM){
                // error
            }
        }

        IVerifiedJwt verifiedJwt = res.right.get();

        IUser user = userRepository.retrieveUserById(verifiedJwt.getUserId());

        ServiceResponse<UserTokens> response = new ServiceResponse<>();
        int refreshTimeout = configuration.getInt("jwt.refreshTimeout");

        // check error states
        if(user.getRefreshToken() == null){
            response.addError("", "Invalid Refresh Token");
            return response;
        }
        if(!user.getRefreshToken().equals(refreshToken)){
            response.addError("", "Invalid Refresh Token");
            return response;
        }
        else if(user.getRefreshTokenIssuedAt().plusMinutes(refreshTimeout).isBeforeNow()){
            response.addError("", "Refresh Token is Expired");
            return response;
        }
        else {
            CurrentUser currentUserDTO = new CurrentUser(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRoles(),
                null,
                0L
            );

            return createUserTokens(currentUserDTO);
        }
    }

    @Override
    public ServiceResponse<Integer> logoutUser(Integer userId){

        ServiceResponse<Integer> response = new ServiceResponse<>();

        IUser userModel = userRepository.retrieveUserById(userId);
        userModel.setRefreshToken(null);
        userModel.setRefreshTokenIssuedAt(null);
        userRepository.updateUser(userModel);

        response.setResponseObject(userId);

        return response;
    }

}
