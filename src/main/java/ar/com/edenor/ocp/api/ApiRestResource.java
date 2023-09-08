package ar.com.edenor.ocp.api;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/cierreTdc")
public interface ApiRestResource {
    @POST
    @Path("/")
    @Produces({"application/json"})
    ApiRestResponse doAction(ApiRestRequest request);

}
