package info.patrickmaciel;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

public class Main implements RequestHandler<Map<String, Object>, Map<String, String>> {
  private final ObjectMapper objectMapper = new ObjectMapper();
  private final S3Client s3Client = S3Client.builder().build();

  @Override
  public Map<String, String> handleRequest(Map<String, Object> input, Context context) {
    // parameter input was stringObjectMap (originally)
    Map<String, String> bodyMap;

    try {
      String body = input.get("body").toString();

      bodyMap = objectMapper.readValue(body, Map.class);
    } catch (Exception e) {
      throw new RuntimeException("Error parsing body", e);
    }

    String originalUrl = bodyMap.get("originalUrl");
    String expirationTime = bodyMap.get("expirationTime");
    Long expirationTimeInSeconds = Long.parseLong(expirationTime);

    String shortUrlCode = UUID.randomUUID().toString().substring(0, 8);

    UrlData urlData = new UrlData(originalUrl, expirationTimeInSeconds);

    try {
      String urlDataJson = objectMapper.writeValueAsString(urlData);

      PutObjectRequest putObjectRequest = PutObjectRequest.builder()
          .bucket("url-shortener-storage-patrickmaciel")
          .key(shortUrlCode + ".json")
          .build();

      s3Client.putObject(putObjectRequest, RequestBody.fromString(urlDataJson));
    } catch (Exception e) {
      throw new RuntimeException("Error saving data", e);
    }


    Map<String, String> response = new HashMap<>();
    response.put("code", shortUrlCode);
    return response;
  }
}