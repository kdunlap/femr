## Json Web Tokens

In an effort to start modernizing the front end of fEMR, authentication via Json Web Tokens (JWT) was added. The long term goal is to make the front and back ends of the application completely separate.

The existing cookie based authentication was left in tact. 

Any endpoints that authenticate via jwt are all namespaced to `/api/` in the url structure

So far this feature is treated as a proof of concept meant to see if any hurdles existing with Play Framework and the intended implementation for front end logic.

### TODO

- create specific exceptions and return errors as json 
- clean up code and add testing
- add a better audit trail of login and refreshing
- store token expiration or issue date?
- make sure this implementation is secure and without loop holes before use in production
- better hash out the rules for revoking refresh tokens - should there be any automatic reason? like detecting a compromised token somehow?
- Should the user be able to log in from multiple locations? Right now this will break things
    - would need to put tokens in a new table to allow multiple per user
    - some thought needs to go here

### Concepts

- **authentication token** - a short lived jwt which authorizes the user to protected endpoints of the api 
- **refresh token** - a longer lived token stored in the database which is used to refresh the shorter lived authentication tokens. The idea here is we have some sort of intrusion detection and are able to revoke access to the API by removing the refresh token.


### Endpoints

#### POST `/api/login`

Will issue the user both an authentication token and a refresh token

- Post Body
    - username
    - password
- Response
    - ```
        {
            "userId": 3,
            "token": "",
            "refreshToken": ""
        }
        ```
    - token - short lived authentication token. Only valid for /api endpoints
    
    
#### POST `/api/refresh`


#### POST `/api/logout`

*required auth token

#### GET `/api/user`

*requires auth token

Will return basic info for the current user