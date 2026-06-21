package cn.tealc.taygedo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.tealc.taygedo.TaygedoConstants.*;

/**
 * HTTP请求构建与响应解析（package-private，仅供TaygedoApi使用）
 *
 * <h3>支持的请求类型</h3>
 * <ul>
 *   <li><b>Laohu请求</b>：发往老虎用户中心，使用MD5签名</li>
 *   <li><b>Native请求</b>：模拟iOS原生App，使用DS签名头</li>
 *   <li><b>H5请求</b>：模拟内嵌WebView，带Origin/Referer头</li>
 * </ul>
 */
class TaygedoHttpClient {
    private static final Logger log = LoggerFactory.getLogger(TaygedoHttpClient.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final HttpClient httpClient;
    private volatile boolean verbose = true;

    TaygedoHttpClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    TaygedoHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    void setVerbose(boolean verbose) { this.verbose = verbose; }
    boolean isVerbose() { return verbose; }

    // ==================== URL / Form 编码 ====================

    /** 将Map编码为application/x-www-form-urlencoded格式 */
    static String formEncode(Map<String, String> data) {
        return data.entrySet().stream()
                .map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8) + "="
                        + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
    }

    /** 构建塔吉多API完整URL（基础地址 + 路径 + 查询参数） */
    String buildUrl(String path, Map<String, String> query) {
        StringBuilder sb = new StringBuilder(TAYGEDO_BASE_URL).append(path);
        if (query != null && !query.isEmpty()) {
            sb.append('?');
            sb.append(query.entrySet().stream()
                    .map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8) + "="
                            + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                    .collect(Collectors.joining("&")));
        }
        return sb.toString();
    }

    // ==================== 请求构建 ====================

    /** 构建老虎用户中心POST请求（使用预编码的body） */
    HttpRequest buildLaohuRequestWithBody(String path, String encodedBody) {
        return HttpRequest.newBuilder()
                .uri(URI.create(LAOHU_BASE_URL + path))
                .header("platform", "android")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(encodedBody))
                .build();
    }

    HttpRequest buildNativeGet(String path, Map<String, String> query,
                               String accessToken, String uid, String deviceId) {
        return buildNativeRequest("GET", path, query, null, accessToken, uid, deviceId);
    }

    HttpRequest buildNativePostWithBody(String path, Map<String, String> query,
                                        String encodedBody,
                                        String accessToken, String uid, String deviceId) {
        return buildNativeRequest("POST", path, query, encodedBody, accessToken, uid, deviceId);
    }

    HttpRequest buildH5Get(String path, Map<String, String> query, String accessToken) {
        return buildH5Request("GET", path, query, null, accessToken);
    }

    HttpRequest buildH5PostWithBody(String path, Map<String, String> query,
                                    String body, String accessToken) {
        return buildH5Request("POST", path, query, body, accessToken);
    }

    private HttpRequest buildNativeRequest(String method, String path, Map<String, String> query,
                                           String body,
                                           String accessToken, String uid, String deviceId) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(buildUrl(path, query)))
                .header("Accept", "application/json")
                .header("Authorization", accessToken)
                .header("appversion", TAYGEDO_APP_VER)
                .header("platform", "ios")
                .header("uid", uid)
                .header("deviceid", deviceId)
                .header("ds", TaygedoCrypto.makeDs())
                .header("User-Agent", NATIVE_USER_AGENT);

        if (body != null && !body.isEmpty()) {
            builder.header("Content-Type", "application/x-www-form-urlencoded");
            builder.method(method, HttpRequest.BodyPublishers.ofString(body));
        } else {
            builder.method(method, HttpRequest.BodyPublishers.noBody());
        }
        return builder.build();
    }

    private HttpRequest buildH5Request(String method, String path, Map<String, String> query,
                                       String body, String accessToken) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(buildUrl(path, query)))
                .header("Accept", "application/json")
                .header("Authorization", accessToken)
                .header("Origin", H5_ORIGIN)
                .header("Referer", H5_ORIGIN + "/")
                .header("User-Agent", H5_USER_AGENT);

        if (body != null && !body.isEmpty()) {
            builder.header("Content-Type", "application/x-www-form-urlencoded");
            builder.method(method, HttpRequest.BodyPublishers.ofString(body));
        } else {
            builder.method(method, HttpRequest.BodyPublishers.noBody());
        }
        return builder.build();
    }

    // ==================== 请求执行与响应解析 ====================

    /** 发送塔吉多API请求并解析响应，自动检查code==0 */
    JsonNode executeAndParse(HttpRequest request, String endpointName, String requestBody) {
        logRequest(request, requestBody, endpointName);
        HttpResponse<String> response = send(request, endpointName);
        logResponse(response, endpointName);
        JsonNode root = parseJsonBody(response, endpointName);
        checkApiCode(root, response, endpointName);
        return root;
    }

    /** 发送老虎平台请求并解析响应，自动检查code==0 */
    JsonNode executeLaohuAndParse(HttpRequest request, String endpointName, String requestBody) {
        logRequest(request, requestBody, endpointName);
        HttpResponse<String> response = send(request, endpointName);
        logResponse(response, endpointName);
        JsonNode root = parseJsonBody(response, endpointName);
        checkLaohuCode(root, response, endpointName);
        return root;
    }

    HttpResponse<String> send(HttpRequest request, String endpointName) {
        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new TaygedoException(endpointName + " 请求发送失败: " + e.getMessage(), e);
        }
    }

    // ==================== 日志 ====================

    void logRequest(HttpRequest request, String requestBody, String endpointName) {
        String method = request.method();
        String uri = request.uri().toString();
        if (requestBody != null && !requestBody.isEmpty()) {
            log.info("[{}] {} {} | body: {}", endpointName, method, uri, requestBody);
        } else {
            log.info("[{}] {} {}", endpointName, method, uri);
        }
    }

    void logResponse(HttpResponse<String> response, String endpointName) {
        if (verbose) {
            String body = response.body();
            log.info("[{}] HTTP {} | body: {}", endpointName, response.statusCode(),
                    body != null ? body : "");
        }
    }

    // ==================== JSON & 错误检查 ====================

    JsonNode parseJsonBody(HttpResponse<String> response, String endpointName) {
        if (response.statusCode() == 401)
            throw new TaygedoException("账号已过期，请重新登录");

        String body = response.body();
        if (body == null || body.isBlank()) {
            throw new TaygedoException(endpointName + " 返回了无效 JSON（HTTP " + response.statusCode() + "，响应为空）");
        }
        try {
            return MAPPER.readTree(body);
        } catch (JsonProcessingException e) {
            throw new TaygedoException(endpointName + " 返回了无效 JSON（HTTP " + response.statusCode()
                    + "，响应：" + summarize(body) + "）", e);
        }
    }

    void checkApiCode(JsonNode root, HttpResponse<String> response, String endpointName) {
        int code = root.has("code") ? root.get("code").asInt() : -1;
        if (response.statusCode() >= 200 && response.statusCode() < 300 && code == 0) {
            return;
        }
        String msg = root.has("msg") ? root.get("msg").asText() : "";
        throw buildError(endpointName, response, code, msg);
    }

    private void checkLaohuCode(JsonNode root, HttpResponse<String> response, String endpointName) {
        int code = root.has("code") ? root.get("code").asInt() : -1;
        if (response.statusCode() >= 200 && response.statusCode() < 300 && code == 0) {
            return;
        }
        String msg = root.has("message") ? root.get("message").asText() :
                root.has("msg") ? root.get("msg").asText() : "";
        throw buildError(endpointName, response, code, msg);
    }

    private TaygedoException buildError(String endpointName, HttpResponse<String> response, int code, String msg) {
        TaygedoException ex = new TaygedoException(msg == null || msg.isEmpty() ? "" : msg);
        ex.setCode(code);
        ex.setBody(response.body());
        return ex;
    }

    static String summarize(String text) {
        if (text == null) return "";
        String normalized = text.replaceAll("\\s+", " ").trim();
        return normalized.length() > 160 ? normalized.substring(0, 157) + "..." : normalized;
    }
}
