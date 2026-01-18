package fpt.org.inblue.service;

import org.springframework.http.HttpMethod;

public interface PythonApiClient {
    <T> T callApi(String endpoint, HttpMethod method, Object requestBody, Class<T> responseType);
}
