package fpt.org.inblue.service;

import fpt.org.inblue.enums.PythonService;
import org.springframework.http.HttpMethod;

public interface PythonApiClient {
    <T> T callApi( PythonService targetService, String endpoint, HttpMethod method, Object requestBody, Class<T> responseType );
}
