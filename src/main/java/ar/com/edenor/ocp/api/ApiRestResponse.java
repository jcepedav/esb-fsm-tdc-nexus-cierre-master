package ar.com.edenor.ocp.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiRestResponse {
	private String status;
	private String errorCode;
	private String errorDescription;
}
