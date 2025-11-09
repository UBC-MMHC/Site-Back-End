# API Endpoints

This document describes the endpoints for this app

---

##  Start Google OAuth2 Login

**Endpoint:**\
Trigger Google Login:
"http://localhost:8080/oauth2/authorization/google"
Upon successful call, JWTToken is stored in cookies and page redirected to FRONTEND_URL

## Start Username, Password Login

### FOR FUTURE
**Endpoint:**\
User Login: (Post Mapping)
[http://localhost:8080/api/auth/login](http://localhost:8080/api/auth/login-email)\
Send Json: 
```json
{ 
    "email": "email"
}
```
After login, user gets redirected to verification page.\
**Frontend** should **hold onto email** to send with next call.


Verification token is sent to email and upon typing and submitting verification code,\
Verify Token:  (Post Mapping)
[http://localhost:8080/api/auth/verify-token](http://localhost:8080/api/auth/verify-token)

Send Json:
```json
{
  "email": "email",
  "token": "inputted-token"
}

```
Upon successful call, JWTToken is stored in cookies and page redirected to FRONTEND_URL




