package ar.com.edenor.ocp.security.client;

import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.message.Message;
import org.apache.cxf.rs.security.oauth2.client.AbstractAuthSupplier;
import org.apache.cxf.rs.security.oauth2.client.OAuthClientUtils;
import org.apache.cxf.rs.security.oauth2.common.ClientAccessToken;
import org.apache.cxf.rs.security.oauth2.common.OAuthError;
import org.apache.cxf.rs.security.oauth2.provider.OAuthJSONProvider;
import org.apache.cxf.rs.security.oauth2.provider.OAuthServiceException;
import org.apache.cxf.transport.http.auth.HttpAuthSupplier;

import javax.ws.rs.client.ResponseProcessingException;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;

public class GeocallBearerAuthSupplier extends AbstractAuthSupplier implements HttpAuthSupplier {
    private WebClient accessTokenServiceClient;
    private boolean refreshEarly;

    public GeocallBearerAuthSupplier(String type) {
        super("bearer");
    }

    @Override
    public boolean requiresRequestCaching() {
        return true;
    }

    @Override
    public String getAuthorization(AuthorizationPolicy authPolicy, URI currentURI, Message message, String fullHeader) {
        authPolicy.setAuthorization((String) message.get("TOKEN_EDENOR"));
        if (this.getClientAccessToken().getTokenKey() == null) {
            return this.refreshAccessToken(authPolicy) ? this.createAuthorizationHeader() : null;
        } else if (fullHeader == null) {
            if (this.refreshEarly) {
                this.refreshAccessTokenIfExpired(authPolicy);
            }
            return this.createAuthorizationHeader();
        } else {
            return this.refreshAccessToken(authPolicy) ? this.createAuthorizationHeader() : null;
        }
    }

    public void setAccessTokenServiceClient(WebClient accessTokenServiceClient) {
        this.accessTokenServiceClient = accessTokenServiceClient;
    }

    private boolean refreshAccessToken(AuthorizationPolicy authPolicy) {
        ClientAccessToken at = this.getClientAccessToken();
        accessTokenServiceClient.replaceHeader("Authorization",authPolicy.getAuthorization());
        Response response = accessTokenServiceClient.get();

        Map map;
        try {
            map = (new OAuthJSONProvider()).readJSONResponse((InputStream)response.getEntity());
        } catch (IOException ioException) {
            throw new ResponseProcessingException(response, ioException);
        }
        if (200 == response.getStatus()) {
            ClientAccessToken token = OAuthClientUtils.fromMapToClientToken(map, "bearer");
            this.setClientAccessToken(token);
            return true;
        } else if (response.getStatus() >= 400 && map.containsKey("estado") && ((String) map.get("estado")).equals("KO")) {
            OAuthError error = new OAuthError((String)map.get("codigoError"), (String)map.get("descripcionError"));
            error.setErrorUri((String)map.get("error_uri"));
            throw new OAuthServiceException(error);
        } else {
            throw new OAuthServiceException("server_error");
        }


    }
    private void refreshAccessTokenIfExpired(AuthorizationPolicy authPolicy) {
        ClientAccessToken at = this.getClientAccessToken();
        if (isExpired(at.getIssuedAt(), at.getExpiresIn())) {
            this.refreshAccessToken(authPolicy);
        }

    }

    public void setRefreshEarly(boolean refreshEarly) {
        this.refreshEarly = refreshEarly;
    }

    private  boolean isExpired(Long issuedAt, Long lifetime) {
        return lifetime == null || lifetime < -1L || lifetime > 0L && issuedAt + lifetime * 1000L < System.currentTimeMillis() ;
    }

}
