package cn.tealc.taygedo;

import cn.tealc.taygedo.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final TaygedoHttpClient http;

    public TaygedoApi() {
        this.http = new TaygedoHttpClient();
    }

    public TaygedoApi(HttpClient httpClient) {
        this.http = new TaygedoHttpClient(httpClient);
    }

    public void setVerbose(boolean verbose) {
        http.setVerbose(verbose);
    }

    public boolean isVerbose() {
        return http.isVerbose();
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

        String encodedBody = TaygedoCrypto.signedLaohuBody(body);
        HttpRequest request = http.buildLaohuRequestWithBody("/m/newApi/sendPhoneCaptchaWithOutLogin", encodedBody);
        http.executeLaohuAndParse(request, "sendCaptcha", encodedBody);
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

        String encodedBody = TaygedoCrypto.signedLaohuBody(body);
        HttpRequest request = http.buildLaohuRequestWithBody("/m/newApi/checkPhoneCaptchaWithOutLogin", encodedBody);
        http.executeLaohuAndParse(request, "checkCaptcha", encodedBody);
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
        body.put("captcha", TaygedoCrypto.aesBase64Encode(captcha));
        body.put("appId", "10550");
        body.put("deviceSys", "12");
        body.put("cellphone", TaygedoCrypto.aesBase64Encode(phone));
        body.put("deviceModel", "LGE-AN10");
        body.put("sdkVersion", "4.129.0");
        body.put("bid", "com.pwrd.htassistant");
        body.put("channelId", "1");

        String encodedBody = TaygedoCrypto.signedLaohuBody(body);
        HttpRequest request = http.buildLaohuRequestWithBody("/openApi/sms/new/login", encodedBody);
        JsonNode data = http.executeLaohuAndParse(request, "loginWithCaptcha", encodedBody);
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
        body.put("username", TaygedoCrypto.aesBase64Encode(phone));
        body.put("password", TaygedoCrypto.aesBase64Encode(password));
        body.put("deviceModel", "LGE-AN10");
        body.put("sdkVersion", "4.129.0");
        body.put("bid", "com.pwrd.htassistant");
        body.put("channelId", "1");

        String encodedBody = TaygedoCrypto.signedLaohuBody(body);
        HttpRequest request = http.buildLaohuRequestWithBody("/openApi/secureLogin", encodedBody);
        JsonNode data = http.executeLaohuAndParse(request, "loginWithPassword", encodedBody);
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
        // 请求头对齐官方 1.2.x 客户端（参照 taygedo Rust official profile），
        // 旧的 1.1.0 兼容头会被服务端拒绝并返回 code:22 invalid request
        String body = TaygedoHttpClient.formEncode(Map.of("token", token, "userIdentity", userId, "appId", "10551"));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TAYGEDO_BASE_URL + "/usercenter/api/login"))
                .header("Accept", "application/json, text/plain, */*")
                .header("authorization", "")
                .header("appversion", TAYGEDO_APP_VER)
                .header("platform", "android")
                .header("uid", "0")
                .header("debug-uid", "3")
                .header("deviceId", deviceId)
                .header("ds", TaygedoCrypto.makeDs())
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("User-Agent", NATIVE_USER_AGENT)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        JsonNode root = http.executeAndParse(request, "userCenterLogin", body);
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
        // 服务端已收紧策略，refreshToken 接口同样要求 1.2.x 版本 + ds 签名，
        // 旧的 1.1.0 头现在也会被拒绝并返回 code:22 invalid request。
        // 请求头对齐 userCenterLogin 的 official 风格。
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TAYGEDO_BASE_URL + "/usercenter/api/refreshToken"))
                .header("Accept", "application/json, text/plain, */*")
                .header("authorization", refreshToken)
                .header("appversion", TAYGEDO_APP_VER)
                .header("platform", "android")
                .header("uid", "0")
                .header("debug-uid", "3")
                .header("deviceId", deviceId)
                .header("ds", TaygedoCrypto.makeDs())
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("User-Agent", NATIVE_USER_AGENT)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        http.logRequest(request, null, "refreshToken");
        HttpResponse<String> response = http.send(request, "refreshToken");
        http.logResponse(response, "refreshToken");

        if (response.statusCode() == 402) {
            throw new TaygedoException("REFRESH_REJECTED_402: refreshToken 已失效，请重新登录");
        }

        JsonNode root = http.parseJsonBody(response, "refreshToken");
        http.checkApiCode(root, response, "refreshToken");
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
        HttpRequest request = http.buildNativeGet("/usercenter/api/v2/getGameRoles",
                Map.of("gameId", gameId), accessToken, uid, deviceId);
        JsonNode root = http.executeAndParse(request, "getGameRoles", null);
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
        String url = http.buildUrl("/apihub/api/getGameBindRole", Map.of("uid", uid, "gameId", gameId));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", accessToken)
                .GET()
                .build();

        JsonNode root = http.executeAndParse(request, "getBindRole", null);
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
     *
     * @param accessToken 塔吉多访问令牌
     * @param uid         用户ID
     * @param deviceId    设备唯一标识
     * @return 游戏角色卡列表
     * @throws TaygedoException 请求失败时抛出
     */
    public GameRecordCardResult getGameRecordCards(String accessToken, String uid, String deviceId) {
        HttpRequest request = http.buildNativeGet("/apihub/api/getGameRecordCard",
                Map.of("uid", uid), accessToken, uid, deviceId);
        JsonNode root = http.executeAndParse(request, "getGameRecordCards", null);
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
    // ==================== 抽卡数据 ====================

    /**
     * 获取异环抽卡数据
     *
     * @param accessToken 塔吉多访问令牌
     * @return 抽卡数据（玩家信息 + 各卡池抽卡记录）
     * @throws TaygedoException 请求失败时抛出
     */
    public GameGachaResult getGameGacha(String accessToken) {
        String url = http.buildUrl("/apihub/awapi/yh/gacha", Map.of());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", accessToken)
                .GET()
                .build();

        JsonNode root = http.executeAndParse(request, "getGameGacha", null);
        JsonNode data = root.get("data");
        if (data == null || !data.isObject()) {
            throw new TaygedoException("getGameGacha 返回数据为空");
        }
        try {
            return MAPPER.treeToValue(data, GameGachaResult.class);
        } catch (JsonProcessingException e) {
            throw new TaygedoException("getGameGacha 解析响应失败: " + e.getMessage(), e);
        }
    }


    // ==================== 签到 ====================

    /**
     * APP签到
     *
     * @param accessToken 塔吉多访问令牌
     * @param uid         用户ID
     * @param deviceId    设备唯一标识
     * @return 签到结果（经验值 + 金币数）
     * @throws TaygedoException 签到失败时抛出（注意：重复签到也会抛出异常）
     */
    public AppSigninResult appSignin(String accessToken, String uid, String deviceId) {
        String body = TaygedoHttpClient.formEncode(Map.of("communityId", "1"));
        HttpRequest request = http.buildNativePostWithBody("/apihub/api/signin",
                null, body, accessToken, uid, deviceId);
        JsonNode root = http.executeAndParse(request, "appSignin", body);
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
     * 异环游戏签到
     *
     * @param accessToken 塔吉多访问令牌
     * @param roleId      角色ID
     * @param gameId      游戏ID
     * @throws TaygedoException 签到失败时抛出（重复签到也会抛出异常）
     */
    public void gameSignin(String accessToken, String roleId, String gameId) {
        String body = TaygedoHttpClient.formEncode(Map.of("roleId", roleId, "gameId", gameId));
        HttpRequest request = http.buildH5PostWithBody("/apihub/awapi/sign", null, body, accessToken);
        http.executeAndParse(request, "gameSignin", body);
    }

    /**
     * 获取异环签到状态
     *
     * @param accessToken 塔吉多访问令牌
     * @param gameId      游戏ID
     * @return 签到状态（本月已签到天数）
     * @throws TaygedoException 请求失败时抛出
     */
    public SigninState getSigninState(String accessToken, String gameId) {
        HttpRequest request = http.buildH5Get("/apihub/awapi/signin/state",
                Map.of("gameId", gameId), accessToken);
        JsonNode root = http.executeAndParse(request, "getSigninState", null);
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
     * 获取异环签到奖励列表
     *
     * @param accessToken 塔吉多访问令牌
     * @param gameId      游戏ID
     * @return 签到奖励列表（索引0为第1天奖励）
     * @throws TaygedoException 请求失败时抛出
     */
    public List<SigninReward> getSigninRewards(String accessToken, String gameId) {
        HttpRequest request = http.buildH5Get("/apihub/awapi/sign/rewards",
                Map.of("gameId", gameId), accessToken);
        JsonNode root = http.executeAndParse(request, "getSigninRewards", null);
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
     *
     * @param accessToken 塔吉多访问令牌
     * @param uid         用户ID
     * @param deviceId    设备唯一标识
     * @return 金币任务列表
     * @throws TaygedoException 请求失败时抛出
     */
    public List<CoinTask> getUserTasks(String accessToken, String uid, String deviceId) {
        HttpRequest request = http.buildNativeGet("/apihub/api/getUserTasks",
                Map.of("gid", "1"), accessToken, uid, deviceId);
        JsonNode root = http.executeAndParse(request, "getUserTasks", null);
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
     *
     * @param accessToken 塔吉多访问令牌
     * @param uid         用户ID
     * @param deviceId    设备唯一标识
     * @throws TaygedoException 签到失败时抛出
     */
    public void bbsSignin(String accessToken, String uid, String deviceId) {
        String body = TaygedoHttpClient.formEncode(Map.of("communityId", "2"));
        HttpRequest request = http.buildNativePostWithBody("/apihub/api/signin",
                null, body, accessToken, uid, deviceId);
        http.executeAndParse(request, "bbsSignin", body);
    }

    /**
     * 获取推荐帖子列表
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
        HttpRequest request = http.buildNativeGet("/bbs/api/getRecommendPostList",
                Map.of("communityId", "2", "count", String.valueOf(count), "page", String.valueOf(page)),
                accessToken, uid, deviceId);
        JsonNode root = http.executeAndParse(request, "getRecommendPostList", null);
        JsonNode dataNode = root.get("data");
        if (dataNode == null) {
            throw new TaygedoException("getRecommendPostList 返回数据为空");
        }
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
        HttpRequest request = http.buildNativeGet("/bbs/api/getPostFull",
                Map.of("postId", postId), accessToken, uid, deviceId);
        JsonNode root = http.executeAndParse(request, "getPostFull", null);
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
        String body = TaygedoHttpClient.formEncode(Map.of("postId", postId));
        HttpRequest request = http.buildNativePostWithBody("/bbs/api/post/like",
                null, body, accessToken, uid, deviceId);
        http.executeAndParse(request, "likePost", body);
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
        String body = TaygedoHttpClient.formEncode(Map.of("platform", platform, "postId", postId));
        HttpRequest request = http.buildNativePostWithBody("/bbs/api/post/share",
                null, body, accessToken, uid, deviceId);
        http.executeAndParse(request, "sharePost", body);
    }

    /**
     * 获取金币任务状态
     *
     * @param accessToken 塔吉多访问令牌
     * @return 金币状态
     * @throws TaygedoException 请求失败时抛出
     */
    public CoinState getUserCoinTaskState(String accessToken) {
        HttpRequest request = http.buildH5Get("/apihub/api/getUserCoinTaskState", null, accessToken);
        JsonNode root = http.executeAndParse(request, "getUserCoinTaskState", null);
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

    // ==================== 角色详情面板 ====================

    /**
     * 获取异环角色综合面板（头像/等级/成就总览/区域总览/房产/载具/角色简版）
     *
     * @param accessToken 塔吉多访问令牌
     * @param roleId      角色 ID
     * @return 角色综合面板
     * @throws TaygedoException 请求失败时抛出
     */
    public RoleHome getRoleHome(String accessToken, String roleId) {
        String url = http.buildUrl("/apihub/awapi/yh/roleHome", Map.of("roleId", roleId));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", accessToken)
                .GET()
                .build();

        JsonNode root = http.executeAndParse(request, "getRoleHome", null);
        JsonNode data = root.get("data");
        if (data == null || !data.isObject()) {
            throw new TaygedoException("getRoleHome 返回数据为空");
        }
        try {
            return MAPPER.treeToValue(data, RoleHome.class);
        } catch (JsonProcessingException e) {
            throw new TaygedoException("getRoleHome 解析响应失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取角色详细列表（每个角色 15+ 属性 + 城市技能 + 副手弧盘/驱动盘套装）
     *
     * @param accessToken 塔吉多访问令牌
     * @param roleId      角色 ID
     * @return 角色详细列表
     * @throws TaygedoException 请求失败时抛出
     */
    public List<CharacterDetail> getRoleCharacters(String accessToken, String roleId) {
        String url = http.buildUrl("/apihub/awapi/yh/characters", Map.of("roleId", roleId));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", accessToken)
                .GET()
                .build();

        JsonNode root = http.executeAndParse(request, "getRoleCharacters", null);
        JsonNode data = root.get("data");
        if (data == null || !data.isArray()) {
            throw new TaygedoException("getRoleCharacters 返回数据为空");
        }
        try {
            List<CharacterDetail> characters = new ArrayList<>();
            for (JsonNode node : data) {
                characters.add(MAPPER.treeToValue(node, CharacterDetail.class));
            }
            return characters;
        } catch (JsonProcessingException e) {
            throw new TaygedoException("getRoleCharacters 解析响应失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取角色成就进度（已达成数/总数 + 金银铜牌数 + 分类明细）
     *
     * @param accessToken 塔吉多访问令牌
     * @param roleId      角色 ID
     * @return 成就进度
     * @throws TaygedoException 请求失败时抛出
     */
    public AchievementProgress getAchievementProgress(String accessToken, String roleId) {
        String url = http.buildUrl("/apihub/awapi/yh/achieveProgress", Map.of("roleId", roleId));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", accessToken)
                .GET()
                .build();

        JsonNode root = http.executeAndParse(request, "getAchievementProgress", null);
        JsonNode data = root.get("data");
        if (data == null || !data.isObject()) {
            throw new TaygedoException("getAchievementProgress 返回数据为空");
        }
        try {
            return MAPPER.treeToValue(data, AchievementProgress.class);
        } catch (JsonProcessingException e) {
            throw new TaygedoException("getAchievementProgress 解析响应失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取各区域探索进度（含子项明细）
     *
     * @param accessToken 塔吉多访问令牌
     * @param roleId      角色 ID
     * @return 区域进度列表
     * @throws TaygedoException 请求失败时抛出
     */
    public List<AreaProgress> getAreaProgress(String accessToken, String roleId) {
        String url = http.buildUrl("/apihub/awapi/yh/areaProgress", Map.of("roleId", roleId));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", accessToken)
                .GET()
                .build();

        JsonNode root = http.executeAndParse(request, "getAreaProgress", null);
        JsonNode data = root.get("data");
        if (data == null || !data.isArray()) {
            throw new TaygedoException("getAreaProgress 返回数据为空");
        }
        try {
            List<AreaProgress> areas = new ArrayList<>();
            for (JsonNode node : data) {
                areas.add(MAPPER.treeToValue(node, AreaProgress.class));
            }
            return areas;
        } catch (JsonProcessingException e) {
            throw new TaygedoException("getAreaProgress 解析响应失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取角色房产列表（含家具明细）
     *
     * @param accessToken 塔吉多访问令牌
     * @param roleId      角色 ID
     * @return 房产数据（含列表与汇总信息）
     * @throws TaygedoException 请求失败时抛出
     */
    public RealEstateResult getRealestate(String accessToken, String roleId) {
        String url = http.buildUrl("/apihub/awapi/yh/realestate", Map.of("roleId", roleId));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", accessToken)
                .GET()
                .build();

        JsonNode root = http.executeAndParse(request, "getRealestate", null);
        JsonNode data = root.get("data");
        if (data == null || !data.isObject()) {
            throw new TaygedoException("getRealestate 返回数据为空");
        }
        try {
            return MAPPER.treeToValue(data, RealEstateResult.class);
        } catch (JsonProcessingException e) {
            throw new TaygedoException("getRealestate 解析响应失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取角色载具列表（含基础/进阶属性、涂装模型）
     *
     * @param accessToken 塔吉多访问令牌
     * @param roleId      角色 ID
     * @return 载具数据（含列表与汇总信息）
     * @throws TaygedoException 请求失败时抛出
     */
    public VehicleList getVehicles(String accessToken, String roleId) {
        String url = http.buildUrl("/apihub/awapi/yh/vehicles", Map.of("roleId", roleId));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", accessToken)
                .GET()
                .build();

        JsonNode root = http.executeAndParse(request, "getVehicles", null);
        JsonNode data = root.get("data");
        if (data == null || !data.isObject()) {
            throw new TaygedoException("getVehicles 返回数据为空");
        }
        try {
            return MAPPER.treeToValue(data, VehicleList.class);
        } catch (JsonProcessingException e) {
            throw new TaygedoException("getVehicles 解析响应失败: " + e.getMessage(), e);
        }
    }

    // ==================== 私有工具方法 ====================

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
}
