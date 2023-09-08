package ar.com.edenor.ocp.security;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.cxf.interceptor.security.AuthenticationException;
import org.springframework.beans.factory.annotation.Value;

import javax.net.ssl.*;
import java.io.*;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Map;

@Slf4j
public class KeycloakClient {

	private URL infoUrl;
	private RSAPublicKey rsaPublicKey;

	@Value("${edenor.keycloak.realm}")
	private String keycloackRealm;

	@Value("${edenor.keycloak.auth-server-url}")
	private String keycloackAuthServerUrl;

	@Value("${edenor.keycloak.connect-timeout}")
	private int keycloackConnectTimeout;

	@Value("${edenor.keycloak.roles}")
	private String keycloackRoles;


	@Value("${edenor.keycloak.enabled}")
	private boolean securityEnabled;

	private static final int ERROR_CODE_400 = 400;
	private static final int ERROR_CODE_500 = 500;
	
	public void init() throws Exception {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }
        };
 
        // Install the all-trusting trust manager
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
 
        // Create all-trusting host name verifier
        HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
 
        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

        String sInfoUrl = keycloackAuthServerUrl+"/realms/"+keycloackRealm;

        log.info("## Absolut Keycloack Auth Server Info Url: " + sInfoUrl);

        infoUrl = new URL(sInfoUrl);

        // rsaPublicKey = getRSAPublicKey();

	}
	

	public Map getToken(String token)throws JsonParseException, JsonMappingException, IOException {

		byte [] barr = Base64.getDecoder().decode(token.split("\\.")[1]);
		ObjectMapper mapper = new ObjectMapper();
		String json = new String(barr);
		Map<Object, Object> map = mapper.readValue(json, Map.class);
		return map;
	}

	private RSAPublicKey getRSAPublicKey() throws Exception {
		HttpURLConnection conn = (HttpURLConnection) infoUrl.openConnection();
		conn.setConnectTimeout(keycloackConnectTimeout);
		conn.setRequestMethod("GET");
		conn.setDoOutput(true);
		String response = "";
		ObjectMapper mapper = new ObjectMapper();
		int statusCode = conn.getResponseCode();
		if (statusCode >= 200 && statusCode < 400) {
			response = extract(conn.getInputStream());
			Map<Object, Object> map = mapper.readValue(response, Map.class);
			return buildRSAPublicKey((String)map.get("public_key"));
		}else {
			response = extract(conn.getErrorStream());
			if (statusCode >= ERROR_CODE_400 && statusCode < ERROR_CODE_500) {
				throw new AuthenticationException("Authentication failed (details can be found in server log)");
			}else if (statusCode >= ERROR_CODE_500) {
				throw new ConnectException("Error en la llamada a RH-SSO: " + response);
			}
			throw new AuthenticationException("Authentication failed (details can be found in server log)");
		}
	}

	private String extract(InputStream is) throws Exception {
		Reader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
		StringBuilder data = new StringBuilder();
		for (int c; (c = in.read()) >= 0;) {
			data.append((char) c);
		}
		return data.toString();
	}
	
	public void validateRoles(String token)throws JsonParseException, JsonMappingException, IOException {
		Map<Object, Object> map = getToken(token);
		String roles = map.get("realm_access").toString();

		if (roles != null && !roles.contains(keycloackRoles))
		{
			throw new AuthenticationException("Authentication failed (details can be found in server log)");
		}
	}

	private RSAPublicKey buildRSAPublicKey(String publicKey) throws InvalidKeySpecException , NoSuchAlgorithmException {
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKey));
		RSAPublicKey pubKey = (RSAPublicKey) keyFactory.generatePublic(keySpecX509);
		return pubKey;
	}

	public RSAPublicKey getRsaPublicKey() {
		return rsaPublicKey;
	}

	public boolean isSecurityEnabled() {
		return securityEnabled;
	}

	public void setSecurityEnabled(boolean securityEnabled) {
		this.securityEnabled = securityEnabled;
	}
}