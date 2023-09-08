package ar.com.edenor.ocp.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.security.AccessDeniedException;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

import java.util.*;

@Slf4j
public class RestSecurityInterceptor extends AbstractPhaseInterceptor<Message> {

	public static final String TOKEN_PREFIX = "Bearer ";
	public static final String HEADER_AUTHORIZATION = "Authorization";

	private KeycloakClient kClient;
    private LinkedHashMap<String,Date> map = new LinkedHashMap<String,Date>();
	
    public RestSecurityInterceptor() {
        super(Phase.POST_LOGICAL);
    }

	@Override
	public void handleMessage(Message message) throws Fault {
    	if (kClient.isSecurityEnabled()) {
			List values = (List)((Map)message.get(Message.PROTOCOL_HEADERS)).get(HEADER_AUTHORIZATION);
			if (Objects.nonNull(values)) {
				String token = (String) values.get(0);

				try {
					DecodedJWT jwt = JWT.require(Algorithm.RSA256(kClient.getRsaPublicKey(),null))
							.build()
							.verify(token.replace(TOKEN_PREFIX, ""));
					kClient.validateRoles(token);

				} catch (JWTVerificationException ex) {
					throw new AccessDeniedException(ex.getMessage());
				} catch (Exception ex) {
					throw new AccessDeniedException(ex.getMessage());
				}

			} else {
				throw new AccessDeniedException("The Token's Signature resulted invalid");
			}
		}

	}

	public void setkClient(KeycloakClient kClient) {
		this.kClient = kClient;
	}
	
}
