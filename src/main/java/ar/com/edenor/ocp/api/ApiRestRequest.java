package ar.com.edenor.ocp.api;

import ar.com.edenor.ocp.util.StringDeserializer;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import org.joda.time.DateTime;

import java.util.Date;

@Data
public class ApiRestRequest {
    @JsonProperty("requestID")
    private String  requestId;
    private String  sourceSystem;
    private String  externalCode;
    private String  foreman;
    private String  code;
    private String  description;
    private String  outcomeCause;
    private String  outcomeNotes;
    @JsonFormat
            (shape = JsonFormat.Shape.STRING, pattern  = "yyyy-MM-dd'T'HH:mm:ss")
    private Date  outcomeStartDate;
    @JsonFormat
            (shape = JsonFormat.Shape.STRING, pattern  = "yyyy-MM-dd'T'HH:mm:ss")
    private Date outcomeEndDate;
    private Boolean anomaly;
    private String  comments;
    private Boolean seal;
    private String  installation;
    private Long  installationId;
    private String  cause;
    private Long  causeId;
    private String  activities;
    private Long  activitiesId;

}
