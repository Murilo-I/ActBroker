package com.actbroker.ActBroker.service;

import com.actbroker.ActBroker.dto.BrokerDto;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;

public class BrokerService {

    private final static Logger log = LoggerFactory.getLogger(BrokerService.class);
    private final static String BROKER_API = "https://607732991ed0ae0017d6a9b0.mockapi.io/insurance/v1/broker/";
    private final static String BROKER_DATA_API = "https://607732991ed0ae0017d6a9b0.mockapi.io/insurance/v1/brokerData/";

    public JSONObject getBroker(String document, boolean isInternalCall) {
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response;
        HttpRequest request;

        BrokerDto dto = new BrokerDto();

        try {
            request = HttpRequest.newBuilder(new URI(BROKER_API+document)).GET().build();
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject personalInfo = new JSONObject(response.body());

            log.info("GET Broker - Status Code -> " + response.statusCode());
            log.info("ResponseBody -> " + response.body());

            dto.setName(personalInfo.getString("name"));
            dto.setDocument(personalInfo.getString("document"));
            dto.setCode(personalInfo.getString("code"));
            dto.setCreateDate(LocalDate.parse(personalInfo.getString("createDate")));

            request = HttpRequest.newBuilder(new URI(BROKER_DATA_API+dto.getCode())).GET().build();
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject brokerSituation = new JSONObject(response.body());

            log.info("GET BrokerData - Status Code -> " + response.statusCode());
            log.info("ResponseBody -> " + response.body());

            dto.setActive(brokerSituation.getBoolean("active"));
            dto.setCommissionRate(brokerSituation.getFloat("commissionRate"));

        } catch (URISyntaxException | IOException | InterruptedException e) {
            log.error("Error -> " + e.getMessage(), e);
            return new JSONObject().put("errorMessage", "Something went wrong, wait while we fix it!");
        }

        return isValid(dto, isInternalCall);
    }

    private JSONObject isValid(BrokerDto dto, boolean isInternalCall) {
        JSONObject json = new JSONObject();

        if(dto.getActive() || isInternalCall) {
            json.put("name", dto.getName());
            json.put("document", dto.getDocument());
            json.put("code", dto.getCode());
            json.put("createDate", dto.getCreateDate());
            json.put("commissionRate", dto.getCommissionRate());
            json.put("active", dto.getActive());
        }
        else json.put("errorMessage", "Broker not active!");

        return json;
    }

    public JSONObject activateBroker(String document) {
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response;
        HttpRequest request;

        JSONObject broker = getBroker(document, true);

        try {
            if(activationRule()) {
                request = HttpRequest.newBuilder(new URI(BROKER_DATA_API + broker.getString("code")))
                        .header("Content-Type", "application/json")
                        .PUT(HttpRequest.BodyPublishers.ofString("{\"active\":true}")).build();

                response = client.send(request, HttpResponse.BodyHandlers.ofString());
                broker.put("active", true);
                log.info("PUT BrokerData - Status Code -> " + response.statusCode());
                log.info("ResponseBody -> " + response.body());
            }
            else broker.put("infoMessage", "This Broker can't be activate for some reason!");
        } catch (URISyntaxException | IOException | InterruptedException e) {
            log.error("Error -> " + e.getMessage(), e);
            return new JSONObject().put("errorMessage", "Error trying to activate this Broker!");
        }

        return broker;
    }

    private boolean activationRule() {
//      TODO -> Implement Activation Rule.
        return true;
    }
}
