package femr.business.services.system.api;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.typesafe.config.Config;
import femr.business.services.core.IUserService;
import femr.business.services.core.api.IAuthService;
import femr.common.dtos.CurrentUser;
import femr.common.dtos.ServiceResponse;
import femr.data.daos.core.IUserRepository;
import femr.data.models.core.IUser;
import femr.util.calculations.dateUtils;
import femr.util.encryptions.IPasswordEncryptor;
import org.joda.time.DateTime;
import org.joda.time.Days;
import play.Logger;

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

    @Inject
    public AuthService(IUserService userService, IPasswordEncryptor passwordEncryptor, IUserRepository userRepository, Config configuration) {
        this.userService = userService;
        this.passwordEncryptor = passwordEncryptor;
        this.userRepository = userRepository;
        this.configuration = configuration;
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

            long timeout = Long.parseLong(configuration.getString("sessionTimeout"));
            CurrentUser currentUserDTO = new CurrentUser(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRoles(),
                null,
                timeout
            );

            response.setResponseObject(currentUserDTO);
        }

        return response;
    }


    public ServiceResponse<String> getSignedToken(CurrentUser user){

        String secret = configuration.getString("play.http.secret.key");
        ServiceResponse<String> response = new ServiceResponse<>();

        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            String token = JWT.create()
                .withIssuer("fEMR")
                .withClaim("user_id", user.getId())
                .withExpiresAt(Date.from(ZonedDateTime.now(ZoneId.systemDefault()).plusMinutes(user.getTimeout2()).toInstant()))
                .sign(algorithm);

            response.setResponseObject(token);
        }
        catch (UnsupportedEncodingException e){
            Logger.error("Error getting signed token: Unsupported character encoding in secret");
            response.addError("", "Unsupported character encoding in secret");
        }

        return response;
    }
}
