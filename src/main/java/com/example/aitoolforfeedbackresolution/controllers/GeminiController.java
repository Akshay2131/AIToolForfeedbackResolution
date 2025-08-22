package com.example.aitoolforfeedbackresolution.controllers;

import com.example.aitoolforfeedbackresolution.services.GeminiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class GeminiController{

    // Inject the API key from environment variables or application.properties
    @Value("${GOOGLE_API_KEY}")
    private String geminiApiKey;

    @Autowired
    private GeminiService geminiService;

    @Autowired
    private ObjectMapper objectMapper;

    String prompt = "You are an expert SQL generator. You will be given a SQL table schema, some column significance, and a user query in natural language. Your task is to generate the best possible SQL query that retrieves the requested data.   Table Name:   LOG_ERROR   Table Schema:   SNO (BIGINT) → Auto increment primary key   XML_GUID (VARCHAR) → Unique identifier for the XML   IN_OUT (VARCHAR) → Direction of message (IN/OUT)   DATE_TIME (DATETIME) → Timestamp of the record   XML (TEXT) → Request XML sent for syncing   RESPONSE_XML (TEXT) → Response XML from ERP   ERROR_MESSAGE (TEXT) → Error message if any issue occurs   FAIL_REASN (TEXT) → If failed, contains failure reason; if success → \"success-\"   MAIN_ID_XML (VARCHAR) → Contains profileId or companyId if available   RESPONSE_CODE (INT) → 200 = success, 400/500 = error   Column Significance:   XML → Contains profileId (SumsUserID) inside XML   RESPONSE_XML → Contains ERP response (including success/failure)   ERROR_MESSAGE → Error details   FAIL_REASN → Additional reason for failure or \"success-\" if successful   RESPONSE_CODE → 200 = success, otherwise failure eg.     SNO: 128217452      XML_GUID: f4f00e27-d687-4bd2-9fd0-4a60b8c720b3        IN_OUT: OUT     DATE_TIME: 2025-08-22 00:00:23           XML: <?xml version=\"1.0\" encoding=\"UTF-8\"?> <WSRequest TransactionID=\"{f4f00e27-d687-4bd2-9fd0-4a60b8c720b3}\" UserID=\"nav\" RequestDate=\"2025-08-21T00:55:35\"><Object Type=\"Codeunit\" ID=\"50057\"><Codeunit><Trigger Name=\"SyncNAVCustomerModify\"><Parameter Name=\"SyncNAVCustomerXML\" Type=\"XML\"><SyncNAV ResponsibilityCenter=\"99 acres\"><Customer ActionType=\"MODIFY\"><SumsUserID>47719642</SumsUserID><Name>Deep Sagar</Name><ContactPerson>Deep Sagar</ContactPerson><Email>deepsagarsahoo04@gmail.com</Email><Address>BHUBANESWAR</Address><Class>BROKER</Class><City>Bhubaneswar</City><Mobile>9090013665</Mobile><Activated>Y</Activated><UserName>DEEPSAGARSAHOO04@GMAIL.COM</UserName><STD>674</STD><ISD>91</ISD><HomePage/><GST_REGISTERED>NO</GST_REGISTERED><No>CUST/5538539</No><International>FALSE</International><CountryCode>66</CountryCode></Customer></SyncNAV></Parameter></Trigger></Codeunit></Object></WSRequest>  RESPONSE_XML: <WSResponse Status=\"0\" TransactionID=\"{f4f00e27-d687-4bd2-9fd0-4a60b8c720b3}\"><ErrorDetail><Code>ERROR</Code><Message>The field Company Classification of table Contact contains a value (BROKER) that cannot be found in the related table (Contact Information Setup).</Message></ErrorDetail></WSResponse> ERROR_MESSAGE: ERR_INCORRECT_RESPONSE_RCVD    FAIL_REASN: The field Company Classification of table Contact contains a value (BROKER) that cannot be found in the related table (Contact Information Setup).-   MAIN_ID_XML: NULL RESPONSE_CODE: 400 1 row in set (0.00 sec)   Instructions for Query Generation (): 1. Choose between the defined use cases Case 1: input contains keywords like profile,navId,sumsId,CON/,CUST/,SumsUserID syncing issues Case 2: input contains keywords liek transaction,SO,99A/ syncing issues Based on query choose a use case , and bases on use case select the variable value from the query input i.e if profile 1234234 get make a query based on below rules for each use cases;   For All Use cases select try to filter by input if provided. Query LOG_ERROR table  with like statement for '%variablevalue%' on XML columnfrom the query input. if DATE_TIME is not specified the take it as current date. all columns should be selected ignore MAIN_ID_XML   User query: \uD83D\uDC49 Hi rohan,ashrah not 47719642 is not getting modified  ";

    String promptTwoPartOne = "Given the provided data you have to do analysis-->>\n"+
            "this data represents all the data /logsfor a particular profile/SumsUserID/navId/CustId or transaction/SO/NAV_TXN_ID -->>";

    String promptTwoPartTwo = "Please provide a one- or two-line summary of any errors found in the analysis data for a specific profile (SumsUserID, navId, CustId) or transaction (SO, NAV_TXN_ID), ordered from most recent to least recent. If no errors are found, please provide a one- or two-line summary of the successful analysis. If the error if because of `Company Classification` value \"BROKER\" in the Contact table is not found in the related`Contact Information Setup` table, resulting in a 400 error.\n"+
            "It means the User Class is Differrent in both systems ie. ERP and PMS";

    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/ask-gemini-flash")
    public ResponseEntity<String> askGeminiFlash(@RequestBody String userPrompt) {
        // Basic validation for API key
        userPrompt = prompt;
        ResponseEntity<String> result = geminiApiCall(userPrompt,true);
        if(result.getStatusCode().is2xxSuccessful()){
            String getData =result.getBody();
            String secondprompt = promptTwoPartOne + "\n" + getData + promptTwoPartTwo;
            return geminiApiCall(secondprompt,false);
        }

        return new ResponseEntity<>("No valid content found in Gemini response.",HttpStatus.NO_CONTENT);
    }

    private ResponseEntity<String> geminiApiCall(String userPrompt,Boolean flowRun){
        if (geminiApiKey == null || geminiApiKey.isEmpty()) {
            return new ResponseEntity<>("Gemini API key is not configured.",HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // Define the specific Gemini 2.0 Flash model endpoint
        // The API key is added as a query parameter in the URL, which is common for some Google APIs.
        // However, the curl command specified X-goog-api-key header. Let's use that for consistency.
        String geminiApiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";

        // Prepare request headers as specified in the curl command
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // Add the API key to the 'X-goog-api-key' header
        headers.set("X-goog-api-key", geminiApiKey);

        // Prepare the request body (JSON payload for Gemini API)
        // This structure matches the 'contents' array with 'parts' and 'text'
        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text",userPrompt);

        Map<String, Object> content = new HashMap<>();
        content.put("parts", Arrays.asList(textPart)); // List containing one text part

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", Arrays.asList(content)); // List containing one content object

        // Create the HttpEntity which combines headers and body for the request
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            // Make the POST request to the Gemini API
            ResponseEntity<Map> geminiResponse = restTemplate.exchange(
                    geminiApiUrl,
                    HttpMethod.POST,
                    entity,
                    Map.class // Expecting a Map to represent the raw JSON response
            );

            // Process Gemini's response to extract the generated text
            if (geminiResponse.getStatusCode() == HttpStatus.OK && geminiResponse.getBody() != null) {
                Map<String, Object> responseBody = geminiResponse.getBody();
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");

                if (candidates != null && !candidates.isEmpty()) {
                    Map<String, Object> firstCandidate = candidates.get(0);
                    Map<String, Object> contentMap = (Map<String, Object>) firstCandidate.get("content");
                    List<Map<String, Object>> parts = (List<Map<String, Object>>) contentMap.get("parts");

                    if (parts != null && !parts.isEmpty()) {
                        String generatedText = (String) parts.get(0).get("text");
                        if(flowRun){
                            String result = geminiService.getData(generatedText);
                            String jsonText = objectMapper.writeValueAsString(result);
                            return new ResponseEntity<>(jsonText,HttpStatus.OK);
                        }else{
                            return new ResponseEntity<>(generatedText,HttpStatus.OK);
                        }
                    }
                }
                // If no content is found or structure is unexpected
                return new ResponseEntity<>("No valid content found in Gemini response.",HttpStatus.NO_CONTENT);
            } else {
                // Handle non-200 HTTP responses from Gemini API
                return new ResponseEntity<>("Error calling Gemini API: "+geminiResponse.getStatusCode()+" - "+geminiResponse.getBody(),geminiResponse.getStatusCode());
            }

        } catch (Exception e) {
            // Catch any exceptions during the API call (e.g., network issues, malformed URL)
            System.err.println("Exception calling Gemini API: " + e.getMessage());
            e.printStackTrace(); // Print stack trace for debugging
            return new ResponseEntity<>("An internal error occurred while communicating with Gemini API: "+e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/test")
    public ResponseEntity<String> testSql() {
        String query = "SELECT ERROR_MESSAGE, FAIL_REASN, RESPONSE_CODE FROM LOG_ERROR WHERE XML LIKE \"%47719642%\"";
        String data = geminiService.getData(query);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }
}
