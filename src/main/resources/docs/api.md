# API Endpoints

This document describes the endpoints for this app

---

##  Start Google OAuth2 Login

**Endpoint:**\
Trigger Google Login:
"http://localhost:8080/oauth2/authorization/google" 

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
Frontend should hold onto email to send with next call.\
Verification token is sent to email and upon typing and submitting code,\
We call:
[http://localhost:8080/api/auth/verify-token](http://localhost:8080/api/auth/verify-token)

Send Json:
```json
{
  "email": "email",
  "token": "inputted-token"
}

```

Upon success we redirect to FRONTEND_URL




