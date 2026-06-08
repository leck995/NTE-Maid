package cn.tealc.taygedo;

import cn.tealc.taygedo.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

import static cn.tealc.taygedo.TaygedoConstants.*;

/**
 * 塔吉多（Taygedo）API客户端
 *
 * <h3>认证架构</h3>
 * 采用两级认证体系：
 * <ol>
 *   <li><b>老虎用户中心（Laohu）</b>：通过短信验证码或密码登录获取老虎token</li>
 *   <li><b>塔吉多用户中心</b>：用老虎token换取塔吉多的accessToken/refreshToken</li>
 * </ol>
 *
 * <h3>登录流程</h3>
 * <pre>
 *   sendCaptcha → checkCaptcha → loginWithCaptcha → userCenterLogin
 *   或
 *   loginWithPassword → userCenterLogin
 * </pre>
 *
 * <h3>签到流程</h3>
 * <pre>
 *   getGameRoles（获取绑定角色） → appSignin（APP签到）
 *   → 对每个角色：gameSignin → getSigninState → getSigninRewards
 * </pre>
 *
 * <h3>请求类型</h3>
 * 支持三种请求构建方式：
 * <ul>
 *   <li><b>Laohu请求</b>：发往老虎用户中心，使用MD5签名</li>
 *   <li><b>Native请求</b>：模拟iOS原生App，使用DS签名头</li>
 *   <li><b>H5请求</b>：模拟内嵌WebView，带Origin/Referer头</li>
 * </ul>
 */
public class TaygedoApi {
    private static final Logger log = LoggerFactory.getLogger(TaygedoApi.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final HttpClient httpClient;

    /** 是否输出详细日志（请求体和响应体），默认关闭 */
    private volatile boolean verbose = true;

    public TaygedoApi() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public TaygedoApi(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /** 设置是否输出请求体和响应体的详细日志 */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public boolean isVerbose() {
        return verbose;
    }

    // ==================== 登录流程 ====================

    /**
     * 发送短信验证码
     *
     * @param phone    手机号
     * @param deviceId 设备唯一标识
     * @throws TaygedoException 发送失败时抛出
     */
    public void sendCaptcha(String phone, String deviceId) {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("deviceType", "LGE-AN10");
        body.put("type", "16");
        body.put("deviceId", deviceId);
        body.put("deviceName", "LGE-AN10");
        body.put("versionCode", "1");
        body.put("t", String.valueOf(System.currentTimeMillis() / 1000));
        body.put("areaCodeId", "1");
        body.put("appId", "10550");
        body.put("deviceSys", "12");
        body.put("cellphone", phone);
        body.put("deviceModel", "LGE-AN10");
        body.put("sdkVersion", "4.129.0");
        body.put("bid", "com.pwrd.htassistant");
        body.put("channelId", "1");

        String encodedBody = signedLaohuBody(body);
        HttpRequest request = buildLaohuRequestWithBody("/m/newApi/sendPhoneCaptchaWithOutLogin", encodedBody);
        executeLaohuAndParse(request, "sendCaptcha", encodedBody);
    }

    /**
     * 校验短信验证码（可选的独立校验步骤）
     *
     * @param phone    手机号
     * @param captcha  用户输入的验证码
     * @param deviceId 设备唯一标识
     * @throws TaygedoException 校验失败时抛出
     */
    public void checkCaptcha(String phone, String captcha, String deviceId) {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("deviceType", "LGE-AN10");
        body.put("deviceId", deviceId);
        body.put("deviceName", "LGE-AN10");
        body.put("t", String.valueOf(System.currentTimeMillis() / 1000));
        body.put("areaCodeId", "1");
        body.put("appId", "10550");
        body.put("deviceSys", "12");
        body.put("cellphone", phone);
        body.put("captcha", captcha);
        body.put("deviceModel", "LGE-AN10");
        body.put("sdkVersion", "4.129.0");
        body.put("bid", "com.pwrd.htassistant");
        body.put("channelId", "1");

        String encodedBody = signedLaohuBody(body);
        HttpRequest request = buildLaohuRequestWithBody("/m/newApi/checkPhoneCaptchaWithOutLogin", encodedBody);
        executeLaohuAndParse(request, "checkCaptcha", encodedBody);
    }

    /**
     * 短信验证码登录
     * 手机号和验证码会经过AES-128-ECB加密后传输
     *
     * @param phone    手机号
     * @param captcha  短信验证码
     * @param deviceId 设备唯一标识
     * @return 老虎平台登录结果（token + userId）
     * @throws TaygedoException 登录失败时抛出
     */
    public LoginResult loginWithCaptcha(String phone, String captcha, String deviceId) {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("deviceType", "LGE-AN10");
        body.put("idfa", "");
        body.put("sign", "");
        body.put("adm", "");
        body.put("type", "16");
        body.put("deviceId", deviceId);
        body.put("version", "1");
        body.put("deviceName", "LGE-AN10");
        body.put("mac", "");
        body.put("t", String.valueOf(System.currentTimeMillis()));
        body.put("areaCodeId", "1");
        body.put("captcha", aesBase64Encode(captcha));
        body.put("appId", "10550");
        body.put("deviceSys", "12");
        body.put("cellphone", aesBase64Encode(phone));
        body.put("deviceModel", "LGE-AN10");
        body.put("sdkVersion", "4.129.0");
        body.put("bid", "com.pwrd.htassistant");
        body.put("channelId", "1");

        String encodedBody = signedLaohuBody(body);
        HttpRequest request = buildLaohuRequestWithBody("/openApi/sms/new/login", encodedBody);
        JsonNode data = executeLaohuAndParse(request, "loginWithCaptcha", encodedBody);
        return extractLaohuLoginResult(data, "loginWithCaptcha");
    }

    /**
     * 密码登录（跳过验证码流程）
     * 手机号和密码会经过AES-128-ECB加密后传输
     *
     * @param phone    手机号
     * @param password 密码
     * @param deviceId 设备唯一标识
     * @return 老虎平台登录结果（token + userId）
     * @throws TaygedoException 登录失败时抛出
     */
    public LoginResult loginWithPassword(String phone, String password, String deviceId) {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("deviceType", "LGE-AN10");
        body.put("idfa", "");
        body.put("sign", "");
        body.put("adm", "");
        body.put("deviceId", deviceId);
        body.put("version", "1");
        body.put("deviceName", "LGE-AN10");
        body.put("mac", "");
        body.put("t", String.valueOf(System.currentTimeMillis()));
        body.put("appId", "10550");
        body.put("deviceSys", "12");
        body.put("username", aesBase64Encode(phone));
        body.put("password", aesBase64Encode(password));
        body.put("deviceModel", "LGE-AN10");
        body.put("sdkVersion", "4.129.0");
        body.put("bid", "com.pwrd.htassistant");
        body.put("channelId", "1");

        String encodedBody = signedLaohuBody(body);
        HttpRequest request = buildLaohuRequestWithBody("/openApi/secureLogin", encodedBody);
        JsonNode data = executeLaohuAndParse(request, "loginWithPassword", encodedBody);
        return extractLaohuLoginResult(data, "loginWithPassword");
    }

    /**
     * 塔吉多用户中心登录
     * 用老虎token换取塔吉多的访问令牌和刷新令牌，这是完成登录的最后一步
     *
     * @param token    老虎平台token
     * @param userId   老虎平台userId
     * @param deviceId 设备唯一标识
     * @return 塔吉多登录结果（accessToken + refreshToken + uid）
     * @throws TaygedoException 登录失败时抛出
     */
    public UserCenterLoginResult userCenterLogin(String token, String userId, String deviceId) {
        String body = formEncode(Map.of("token", token, "userIdentity", userId, "appId", "10551"));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TAYGEDO_BASE_URL + "/usercenter/api/login"))
                .header("platform", "android")
                .header("deviceid", deviceId)
                .header("authorization", "")
                .header("appversion", "1.1.0")
                .header("uid", "10000000")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("User-Agent", "okhttp/4.12.0")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        JsonNode root = executeAndParse(request, "userCenterLogin", body);
        JsonNode data = root.get("data");
        if (data == null || !data.has("accessToken") || !data.has("refreshToken") || !data.has("uid")) {
            throw new TaygedoException("userCenterLogin 返回数据缺少必要字段");
        }
        try {
            return MAPPER.treeToValue(data, UserCenterLoginResult.class);
        } catch (JsonProcessingException e) {
            throw new TaygedoException("userCenterLogin 解析响应失败: " + e.getMessage(), e);
        }
    }

    /**
     * 刷新访问令牌
     * 使用refreshToken获取新的accessToken。如果refreshToken已失效（HTTP 402），
     * 会抛出包含"REFRESH_REJECTED_402"消息的异常，调用方应引导用户重新登录
     *
     * @param refreshToken 当前的刷新令牌
     * @param deviceId     设备唯一标识
     * @return 新的令牌对（accessToken + refreshToken）
     * @throws TaygedoException 刷新失败时抛出，HTTP 402表示令牌已失效需要重新登录
     */
    public RefreshTokenResult refreshToken(String refreshToken, String deviceId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TAYGEDO_BASE_URL + "/usercenter/api/refreshToken"))
                .header("authorization", refreshToken)
                .header("deviceid", deviceId)
                .header("appversion", "1.1.0")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("User-Agent", "okhttp/4.12.0")
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        logRequest(request, null, "refreshToken");

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new TaygedoException("refreshToken 请求失败: " + e.getMessage(), e);
        }

        logResponse(response, "refreshToken");

        if (response.statusCode() == 402) {
            throw new TaygedoException("REFRESH_REJECTED_402: refreshToken 已失效，请重新登录");
        }

        JsonNode root = parseJsonBody(response, "refreshToken");
        checkApiCode(root, response, "refreshToken");
        JsonNode data = root.get("data");
        if (data == null || !data.has("accessToken") || !data.has("refreshToken")) {
            throw new TaygedoException("refreshToken 返回数据缺少必要字段");
        }
        try {
            return MAPPER.treeToValue(data, RefreshTokenResult.class);
        } catch (JsonProcessingException e) {
            throw new TaygedoException("refreshToken 解析响应失败: " + e.getMessage(), e);
        }
    }

    // ==================== 游戏角色 ====================

    /**
     * 获取游戏角色列表
     * 查询用户在指定游戏中的所有绑定角色，已自动过滤roleId为null的无效条目
     *
     * @param accessToken 塔吉多访问令牌
     * @param uid         用户ID
     * @param deviceId    设备唯一标识
     * @param gameId      游戏ID（如 1256, 1257, 1289）
     * @return 角色列表
     * @throws TaygedoException 请求失败时抛出
     */
    public GameRolesResult getGameRoles(String accessToken, String uid, String deviceId, String gameId) {
        HttpRequest request = buildNativeGet("/usercenter/api/v2/getGameRoles",
                Map.of("gameId", gameId), accessToken, uid, deviceId);
        JsonNode root = executeAndParse(request, "getGameRoles", null);
        JsonNode data = root.get("data");
        if (data == null) {
            throw new TaygedoException("getGameRoles 返回数据为空");
        }
        try {
            GameRolesResult result = MAPPER.treeToValue(data, GameRolesResult.class);
            if (result.getRoles() != null) {
                result.setRoles(result.getRoles().stream()
                        .filter(r -> r.getRoleId() != null)
                        .collect(Collectors.toList()));
            }
            return result;
        } catch (JsonProcessingException e) {
            throw new TaygedoException("getGameRoles 解析响应失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取绑定的主角色
     *
     * @param accessToken 塔吉多访问令牌
     * @param uid         用户ID
     * @param gameId      游戏ID
     * @return 绑定角色信息
     * @throws TaygedoException 请求失败时抛出
     */
    public BindRoleInfo getBindRole(String accessToken, String uid, String gameId) {
        String url = buildUrl("/apihub/api/getGameBindRole", Map.of("uid", uid, "gameId", gameId));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", accessToken)
                .GET()
                .build();

        JsonNode root = executeAndParse(request, "getBindRole", null);
        JsonNode dataNode = root.get("data");
        if (dataNode == null) {
            throw new TaygedoException("getBindRole 返回数据为空");
        }
        try {
            return MAPPER.treeToValue(dataNode, BindRoleInfo.class);
        } catch (JsonProcessingException e) {
            throw new TaygedoException("getBindRole 解析响应失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取游戏记录卡（角色发现的备用数据源）
     * 当getGameRoles返回空时，可尝试此接口获取角色信息
     *
     * @param accessToken 塔吉多访问令牌
     * @param uid         用户ID
     * @param deviceId    设备唯一标识
     * @return 游戏角色卡列表
     * @throws TaygedoException 请求失败时抛出
     */
    public GameRecordCardResult getGameRecordCards(String accessToken, String uid, String deviceId) {
        HttpRequest request = buildNativeGet("/apihub/api/getGameRecordCard",
                Map.of("uid", uid), accessToken, uid, deviceId);
        JsonNode root = executeAndParse(request, "getGameRecordCards", null);
        JsonNode dataNode = root.get("data");
        if (dataNode == null || !dataNode.isArray()) {
            throw new TaygedoException("getGameRecordCards 返回数据为空");
        }
        try {
            List<GameRecordCard> cards = new ArrayList<>();
            for (JsonNode node : dataNode) {
                GameRecordCard card = MAPPER.treeToValue(node, GameRecordCard.class);
                if (card.getGameId() != null) {
                    cards.add(card);
                }
            }
            GameRecordCardResult result = new GameRecordCardResult();
            result.setCards(cards);
            return result;
        } catch (JsonProcessingException e) {
            throw new TaygedoException("getGameRecordCards 解析响应失败: " + e.getMessage(), e);
        }
    }

    // ==================== 签到 ====================

    /**
     * APP签到
     * 塔吉多App每日签到，获得经验值和金币
     *
     * @param accessToken 塔吉多访问令牌
     * @param uid         用户ID
     * @param deviceId    设备唯一标识
     * @return 签到结果（经验值 + 金币数）
     * @throws TaygedoException 签到失败时抛出（注意：重复签到也会抛出异常，调用方需自行处理幂等）
     */
    public AppSigninResult appSignin(String accessToken, String uid, String deviceId) {
        String body = formEncode(Map.of("communityId", "1"));
        HttpRequest request = buildNativePostWithBody("/apihub/api/signin",
                null, body, accessToken, uid, deviceId);
        JsonNode root = executeAndParse(request, "appSignin", body);
        JsonNode data = root.get("data");
        if (data == null) {
            throw new TaygedoException("appSignin 返回数据为空");
        }
        try {
            return MAPPER.treeToValue(data, AppSigninResult.class);
        } catch (JsonProcessingException e) {
            throw new TaygedoException("appSignin 解析响应失败: " + e.getMessage(), e);
        }
    }

    /**
     * 游戏签到
     * 对指定游戏的指定角色执行每日签到
     *
     * @param accessToken 塔吉多访问令牌
     * @param roleId      角色ID
     * @param gameId      游戏ID
     * @throws TaygedoException 签到失败时抛出（重复签到也会抛出异常）
     */
    public void gameSignin(String accessToken, String roleId, String gameId) {
        String body = formEncode(Map.of("roleId", roleId, "gameId", gameId));
        HttpRequest request = buildH5PostWithBody("/apihub/awapi/sign", null, body, accessToken);
        executeAndParse(request, "gameSignin", body);
    }

    /**
     * 获取签到状态
     * 查询当前月份已签到的累计天数
     *
     * @param accessToken 塔吉多访问令牌
     * @param gameId      游戏ID
     * @return 签到状态（本月已签到天数）
     * @throws TaygedoException 请求失败时抛出
     */
    public SigninState getSigninState(String accessToken, String gameId) {
        HttpRequest request = buildH5Get("/apihub/awapi/signin/state",
                Map.of("gameId", gameId), accessToken);
        JsonNode root = executeAndParse(request, "getSigninState", null);
        JsonNode data = root.get("data");
        if (data == null) {
            throw new TaygedoException("getSigninState 返回数据为空");
        }
        try {
            return MAPPER.treeToValue(data, SigninState.class);
        } catch (JsonProcessingException e) {
            throw new TaygedoException("getSigninState 解析响应失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取签到奖励列表
     * 返回当月每天的签到奖励配置，通过签到天数-1作为索引获取当天奖励
     *
     * @param accessToken 塔吉多访问令牌
     * @param gameId      游戏ID
     * @return 签到奖励列表（索引0为第1天奖励）
     * @throws TaygedoException 请求失败时抛出
     */
    public List<SigninReward> getSigninRewards(String accessToken, String gameId) {
        HttpRequest request = buildH5Get("/apihub/awapi/sign/rewards",
                Map.of("gameId", gameId), accessToken);
        JsonNode root = executeAndParse(request, "getSigninRewards", null);
        JsonNode data = root.get("data");
        if (data == null || !data.isArray()) {
            throw new TaygedoException("getSigninRewards 返回数据为空");
        }
        try {
            List<SigninReward> rewards = new ArrayList<>();
            for (JsonNode node : data) {
                rewards.add(MAPPER.treeToValue(node, SigninReward.class));
            }
            return rewards;
        } catch (JsonProcessingException e) {
            throw new TaygedoException("getSigninRewards 解析响应失败: " + e.getMessage(), e);
        }
    }

    // ==================== 金币任务 ====================

    /**
     * 获取用户任务状态
     * 查询每日金币任务的完成进度（浏览、点赞、分享等）
     *
     * @param accessToken 塔吉多访问令牌
     * @param uid         用户ID
     * @param deviceId    设备唯一标识
     * @return 金币任务列表，每项包含任务编码、已完成次数、上限次数
     * @throws TaygedoException 请求失败时抛出
     */
    public List<CoinTask> getUserTasks(String accessToken, String uid, String deviceId) {
        HttpRequest request = buildNativeGet("/apihub/api/getUserTasks",
                Map.of("gid", "1"), accessToken, uid, deviceId);
        JsonNode root = executeAndParse(request, "getUserTasks", null);
        JsonNode data = root.get("data");
        if (data == null) {
            throw new TaygedoException("getUserTasks 返回数据为空");
        }
        JsonNode taskList = data.get("task_list1");
        if (taskList == null || !taskList.isArray()) {
            throw new TaygedoException("getUserTasks task_list1 为空");
        }
        try {
            List<CoinTask> tasks = new ArrayList<>();
            for (JsonNode node : taskList) {
                CoinTask task = new CoinTask();
                String code = node.has("code") ? node.get("code").asText() :
                        node.has("taskKey") ? node.get("taskKey").asText() : "";
                if (code.isEmpty()) continue;
                task.setCode(code);
                task.setCompleteTimes(node.has("completeTimes") ? node.get("completeTimes").asInt() : 0);
                task.setLimitTimes(node.has("limitTimes") ? node.get("limitTimes").asInt() : 0);
                tasks.add(task);
            }
            return tasks;
        } catch (Exception e) {
            throw new TaygedoException("getUserTasks 解析响应失败: " + e.getMessage(), e);
        }
    }

    /**
     * BBS签到
     * 社区金币签到（communityId=2），区别于APP签到（communityId=1）
     *
     * @param accessToken 塔吉多访问令牌
     * @param uid         用户ID
     * @param deviceId    设备唯一标识
     * @throws TaygedoException 签到失败时抛出
     */
    public void bbsSignin(String accessToken, String uid, String deviceId) {
        String body = formEncode(Map.of("communityId", "2"));
        HttpRequest request = buildNativePostWithBody("/apihub/api/signin",
                null, body, accessToken, uid, deviceId);
        executeAndParse(request, "bbsSignin", body);
    }

    /**
     * 获取推荐帖子列表
     * 用于完成浏览、点赞、分享等金币任务
     *
     * @param accessToken 塔吉多访问令牌
     * @param uid         用户ID
     * @param deviceId    设备唯一标识
     * @param count       每页数量
     * @param page        页码
     * @return 推荐帖子列表
     * @throws TaygedoException 请求失败时抛出
     */
    public List<RecommendPost> getRecommendPostList(String accessToken, String uid, String deviceId,
                                                     int count, int page) {
        HttpRequest request = buildNativeGet("/bbs/api/getRecommendPostList",
                Map.of("communityId", "2", "count", String.valueOf(count), "page", String.valueOf(page)),
                accessToken, uid, deviceId);
        JsonNode root = executeAndParse(request, "getRecommendPostList", null);
        JsonNode dataNode = root.get("data");
        if (dataNode == null) {
            throw new TaygedoException("getRecommendPostList 返回数据为空");
        }
        // 兼容多种响应格式：直接数组 / {list: [...]} / {posts: [...]}
        JsonNode list;
        if (dataNode.isArray()) {
            list = dataNode;
        } else if (dataNode.has("list") && dataNode.get("list").isArray()) {
            list = dataNode.get("list");
        } else if (dataNode.has("posts") && dataNode.get("posts").isArray()) {
            list = dataNode.get("posts");
        } else {
            throw new TaygedoException("getRecommendPostList 响应数据格式异常");
        }
        try {
            List<RecommendPost> posts = new ArrayList<>();
            for (JsonNode node : list) {
                if (node.has("postId") || node.has("id")) {
                    posts.add(MAPPER.treeToValue(node, RecommendPost.class));
                }
            }
            return posts;
        } catch (JsonProcessingException e) {
            throw new TaygedoException("getRecommendPostList 解析响应失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取帖子详情（同时标记为"已浏览"，用于完成浏览金币任务）
     *
     * @param accessToken 塔吉多访问令牌
     * @param uid         用户ID
     * @param deviceId    设备唯一标识
     * @param postId      帖子ID
     * @return 帖子详情
     * @throws TaygedoException 请求失败时抛出
     */
    public RecommendPost getPostFull(String accessToken, String uid, String deviceId, String postId) {
        HttpRequest request = buildNativeGet("/bbs/api/getPostFull",
                Map.of("postId", postId), accessToken, uid, deviceId);
        JsonNode root = executeAndParse(request, "getPostFull", null);
        JsonNode data = root.get("data");
        if (data == null || !data.isObject()) {
            throw new TaygedoException("getPostFull 返回数据为空");
        }
        try {
            return MAPPER.treeToValue(data, RecommendPost.class);
        } catch (JsonProcessingException e) {
            throw new TaygedoException("getPostFull 解析响应失败: " + e.getMessage(), e);
        }
    }

    /**
     * 点赞帖子（用于完成点赞金币任务）
     *
     * @param accessToken 塔吉多访问令牌
     * @param uid         用户ID
     * @param deviceId    设备唯一标识
     * @param postId      帖子ID
     * @throws TaygedoException 点赞失败时抛出
     */
    public void likePost(String accessToken, String uid, String deviceId, String postId) {
        String body = formEncode(Map.of("postId", postId));
        HttpRequest request = buildNativePostWithBody("/bbs/api/post/like",
                null, body, accessToken, uid, deviceId);
        executeAndParse(request, "likePost", body);
    }

    /**
     * 分享帖子（用于完成分享金币任务）
     *
     * @param accessToken 塔吉多访问令牌
     * @param uid         用户ID
     * @param deviceId    设备唯一标识
     * @param postId      帖子ID
     * @param platform    分享平台（如 qq, wechat）
     * @throws TaygedoException 分享失败时抛出
     */
    public void sharePost(String accessToken, String uid, String deviceId, String postId, String platform) {
        String body = formEncode(Map.of("platform", platform, "postId", postId));
        HttpRequest request = buildNativePostWithBody("/bbs/api/post/share",
                null, body, accessToken, uid, deviceId);
        executeAndParse(request, "sharePost", body);
    }

    /**
     * 获取金币任务状态
     * 查询当日金币获取汇总（今日已获得/每日上限）
     *
     * @param accessToken 塔吉多访问令牌
     * @return 金币状态
     * @throws TaygedoException 请求失败时抛出
     */
    public CoinState getUserCoinTaskState(String accessToken) {
        HttpRequest request = buildH5Get("/apihub/api/getUserCoinTaskState", null, accessToken);
        JsonNode root = executeAndParse(request, "getUserCoinTaskState", null);
        JsonNode data = root.get("data");
        if (data == null || !data.isObject()) {
            throw new TaygedoException("getUserCoinTaskState 返回数据为空");
        }
        try {
            return MAPPER.treeToValue(data, CoinState.class);
        } catch (JsonProcessingException e) {
            throw new TaygedoException("getUserCoinTaskState 解析响应失败: " + e.getMessage(), e);
        }
    }

    // ==================== 加密工具方法 ====================

    /**
     * 生成DS签名
     * 格式：{timestamp},{nonce},{MD5(timestamp + nonce + appVer + dsSecret)}
     * 用于Native请求的ds请求头
     */
    private static String makeDs() {
        long timestamp = System.currentTimeMillis() / 1000;
        String nonce = makeNonce();
        String raw = timestamp + nonce + TAYGEDO_APP_VER + TAYGEDO_DS_SECRET;
        String sign = org.apache.commons.codec.digest.DigestUtils.md5Hex(raw);
        return timestamp + "," + nonce + "," + sign;
    }

    /**
     * 生成8位随机nonce
     * 使用拒绝采样避免模偏差，字符集为大小写字母+数字（62个字符）
     */
    private static String makeNonce() {
        int alphabetLen = NONCE_ALPHABET.length();
        int fairRange = (256 / alphabetLen) * alphabetLen;
        StringBuilder nonce = new StringBuilder(8);
        byte[] bytes = new byte[8];
        while (nonce.length() < 8) {
            SECURE_RANDOM.nextBytes(bytes);
            for (byte b : bytes) {
                int unsigned = b & 0xFF;
                if (unsigned < fairRange) {
                    nonce.append(NONCE_ALPHABET.charAt(unsigned % alphabetLen));
                    if (nonce.length() == 8) break;
                }
            }
        }
        return nonce.toString();
    }

    /**
     * 老虎平台请求签名
     * 将参数按key排序后拼接所有value，再追加LAOHU_SECRET，计算MD5
     */
    private static String laohuSign(Map<String, String> data) {
        String values = data.keySet().stream()
                .sorted()
                .map(data::get)
                .collect(Collectors.joining());
        return org.apache.commons.codec.digest.DigestUtils.md5Hex(values + LAOHU_SECRET);
    }

    /**
     * 构建带签名的老虎请求体
     * 先对原始数据计算签名，将sign字段加入参数，再form编码
     */
    private static String signedLaohuBody(Map<String, String> data) {
        Map<String, String> withSign = new LinkedHashMap<>(data);
        withSign.put("sign", laohuSign(data));
        return formEncode(withSign);
    }

    /**
     * AES-128-ECB加密并Base64编码
     * 密钥为LAOHU_SECRET的后16个字符，用于加密手机号和验证码
     */
    private static String aesBase64Encode(String value) {
        try {
            String keyStr = LAOHU_SECRET.substring(LAOHU_SECRET.length() - AES_KEY_LENGTH);
            SecretKeySpec key = new SecretKeySpec(keyStr.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encrypted = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new TaygedoException("AES 加密失败: " + e.getMessage(), e);
        }
    }

    // ==================== HTTP请求构建 ====================

    /** 将Map编码为application/x-www-form-urlencoded格式 */
    private static String formEncode(Map<String, String> data) {
        return data.entrySet().stream()
                .map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8) + "="
                        + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
    }

    /** 构建塔吉多API完整URL（基础地址 + 路径 + 查询参数） */
    private static String buildUrl(String path, Map<String, String> query) {
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

    /** 构建老虎用户中心POST请求（使用预编码的body） */
    private HttpRequest buildLaohuRequestWithBody(String path, String encodedBody) {
        return HttpRequest.newBuilder()
                .uri(URI.create(LAOHU_BASE_URL + path))
                .header("platform", "android")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(encodedBody))
                .build();
    }

    private HttpRequest buildNativeGet(String path, Map<String, String> query,
                                        String accessToken, String uid, String deviceId) {
        return buildNativeRequest("GET", path, query, null, accessToken, uid, deviceId);
    }

    private HttpRequest buildNativePostWithBody(String path, Map<String, String> query,
                                                 String encodedBody,
                                                 String accessToken, String uid, String deviceId) {
        return buildNativeRequest("POST", path, query, encodedBody, accessToken, uid, deviceId);
    }

    /**
     * 构建Native请求（模拟iOS原生App）
     * 设置Android/iOS混合的请求头：platform=ios, User-Agent=iOS，同时模拟App版本和设备ID
     */
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
                .header("ds", makeDs())
                .header("User-Agent", NATIVE_USER_AGENT);

        if (body != null && !body.isEmpty()) {
            builder.header("Content-Type", "application/x-www-form-urlencoded");
            builder.method(method, HttpRequest.BodyPublishers.ofString(body));
        } else {
            builder.method(method, HttpRequest.BodyPublishers.noBody());
        }
        return builder.build();
    }

    private HttpRequest buildH5Get(String path, Map<String, String> query, String accessToken) {
        return buildH5Request("GET", path, query, null, accessToken);
    }

    private HttpRequest buildH5PostWithBody(String path, Map<String, String> query,
                                             String body, String accessToken) {
        return buildH5Request("POST", path, query, body, accessToken);
    }

    /**
     * 构建H5请求（模拟App内嵌WebView）
     * 设置浏览器风格的User-Agent，添加Origin和Referer头
     */
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

    // ==================== 请求/响应日志 ====================

    /** 打印请求信息：方法、URL、请求体 */
    private void logRequest(HttpRequest request, String requestBody, String endpointName) {
        String method = request.method();
        String uri = request.uri().toString();
        if (requestBody != null && !requestBody.isEmpty()) {
            log.info("[{}] {} {} | body: {}", endpointName, method, uri, requestBody);
        } else {
            log.info("[{}] {} {}", endpointName, method, uri);
        }
    }

    /** 打印响应信息：状态码、响应体（仅verbose=true时打印完整响应体） */
    private void logResponse(HttpResponse<String> response, String endpointName) {
        if (verbose) {
            String body = response.body();
            log.info("[{}] HTTP {} | body: {}", endpointName, response.statusCode(),
                    body != null ? body : "");
        }
    }

    // ==================== 响应解析 ====================

    /** 发送塔吉多API请求并解析响应，自动检查code==0 */
    private JsonNode executeAndParse(HttpRequest request, String endpointName, String requestBody) {
        logRequest(request, requestBody, endpointName);
        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new TaygedoException(endpointName + " 请求发送失败: " + e.getMessage(), e);
        }
        logResponse(response, endpointName);
        JsonNode root = parseJsonBody(response, endpointName);
        checkApiCode(root, response, endpointName);
        return root;
    }

    /** 发送老虎平台请求并解析响应，自动检查code==0 */
    private JsonNode executeLaohuAndParse(HttpRequest request, String endpointName, String requestBody) {
        logRequest(request, requestBody, endpointName);
        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new TaygedoException(endpointName + " 请求发送失败: " + e.getMessage(), e);
        }
        logResponse(response, endpointName);
        JsonNode root = parseJsonBody(response, endpointName);
        checkLaohuCode(root, response, endpointName);
        return root;
    }

    /** 解析HTTP响应体为JSON，失败时给出详细错误信息 */
    private JsonNode parseJsonBody(HttpResponse<String> response, String endpointName) {
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

    /** 检查塔吉多API响应code（使用msg字段） */
    private void checkApiCode(JsonNode root, HttpResponse<String> response, String endpointName) {
        int code = root.has("code") ? root.get("code").asInt() : -1;
        if (response.statusCode() >= 200 && response.statusCode() < 300 && code == 0) {
            return;
        }
        String msg = root.has("msg") ? root.get("msg").asText() : "";
        throw buildError(endpointName, response, code, msg);
    }

    /** 检查老虎平台响应code（使用message字段，兼容msg） */
    private void checkLaohuCode(JsonNode root, HttpResponse<String> response, String endpointName) {
        int code = root.has("code") ? root.get("code").asInt() : -1;
        if (response.statusCode() >= 200 && response.statusCode() < 300 && code == 0) {
            return;
        }
        String msg = root.has("message") ? root.get("message").asText() :
                root.has("msg") ? root.get("msg").asText() : "";
        throw buildError(endpointName, response, code, msg);
    }

    /** 从老虎登录响应中提取token、userId、nickname等信息 */
    private LoginResult extractLaohuLoginResult(JsonNode root, String endpointName) {
        JsonNode result = root.get("result");
        if (result == null || !result.has("token") || result.get("userId") == null) {
            throw new TaygedoException(endpointName + " 返回数据缺少 token 或 userId");
        }
        LoginResult loginResult = new LoginResult();
        loginResult.setToken(result.get("token").asText());
        loginResult.setUserId(String.valueOf(result.get("userId").asText()));
        if (result.has("nickname")) {
            loginResult.setNickname(result.get("nickname").asText());
        }
        if (result.has("cellphone")) {
            loginResult.setCellphone(result.get("cellphone").asText());
        }
        if (result.has("headImg")) {
            loginResult.setHeadImg(result.get("headImg").asText());
        }
        return loginResult;
    }

    /** 构建详细的API错误异常 */
    private TaygedoException buildError(String endpointName, HttpResponse<String> response, int code, String msg) {
        if (msg != null && !msg.isBlank() && !"ok".equalsIgnoreCase(msg)) {
            return new TaygedoException(endpointName + ": " + msg);
        }
        return new TaygedoException(endpointName + " 请求失败（HTTP " + response.statusCode()
                + "，code=" + code + (msg.isEmpty() ? "" : "，msg=" + msg)
                + "，响应：" + summarize(response.body()) + "）");
    }

    /** 截断长响应文本，用于错误消息和日志中展示 */
    private static String summarize(String text) {
        if (text == null) return "";
        String normalized = text.replaceAll("\\s+", " ").trim();
        return normalized.length() > 160 ? normalized.substring(0, 157) + "..." : normalized;
    }
}
