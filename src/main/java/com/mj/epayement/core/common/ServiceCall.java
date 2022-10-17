package com.mj.epayement.core.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 * Call service
 *
 * @author Marwen JABEUR
 */
@Service
public class ServiceCall {

    @Autowired
    private RestTemplate restTemplate;

    /**
     * Call service
     *
     * @param url                          service url
     * @param map                          @LinkedMultiValueMap<String, String>
     *                                     Data to send
     * @param httpMethod                   http method used (GET/POST/PUT/DELETE)
     * @return String
     */
    public String callService(String url, LinkedMultiValueMap<String, String> map, HttpMethod httpMethod) {
        var entity = initHttpEntity(map);
        //send request
        switch (httpMethod) {
            case GET:
                break;
            case POST:
                return restTemplate.postForObject(url, entity, String.class);
            default:
                return null;
        }
        return null;
    }

    /**
     * init Http Entity
     *
     * @param map @{@link LinkedMultiValueMap}
     * @return @{@link HttpEntity}
     */
    private HttpEntity<MultiValueMap<String, String>> initHttpEntity(LinkedMultiValueMap<String, String> map) {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf(MediaType.MULTIPART_FORM_DATA_VALUE));
        return new HttpEntity<>(map, headers);
    }
}
