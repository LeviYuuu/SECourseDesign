package com.se.backend.service.impl;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.se.backend.config.AiConfig;
import com.se.backend.entity.AudioObject;
import com.se.backend.mapper.AudioObjectMapper;
import com.se.backend.service.SpeechService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okio.ByteString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
public class SpeechServiceImpl implements SpeechService {

    @Autowired
    private AudioObjectMapper audioObjectMapper;

    @Autowired
    private AiConfig aiConfig;

    private static final String UPLOAD_DIR = System.getProperty("user.dir") + File.separator + "uploads" + File.separator;
    private static final String SUCCESS_CODE = "20000000";

    // ================= ASR 部分 (保持不变) =================
    @Override
    public Map<String, Object> uploadAudio(MultipartFile audioFile, String sessionId, Integer userId) {
        if (audioFile == null || audioFile.isEmpty()) throw new RuntimeException("文件为空");
        try {
            if (!FileUtil.exist(UPLOAD_DIR)) FileUtil.mkdir(UPLOAD_DIR);
            String originalFilename = audioFile.getOriginalFilename();
            String suffix = FileUtil.getSuffix(originalFilename);
            if (suffix == null || suffix.isEmpty()) suffix = "wav";

            String fileId = IdUtil.simpleUUID();
            String newFileName = fileId + "." + suffix;
            File dest = new File(UPLOAD_DIR + newFileName);
            audioFile.transferTo(dest);

            String audioUrl = "/static/audio/" + newFileName;
            AudioObject audio = new AudioObject();
            audio.setSessionId(sessionId);
            audio.setUserId(userId);
            audio.setAudioUrl(audioUrl);
            audio.setSize(audioFile.getSize());
            audio.setCreatedAt(LocalDateTime.now());
            audioObjectMapper.insert(audio);

            Map<String, Object> data = new HashMap<>();
            data.put("audioId", audio.getAudioId());
            data.put("audioUrl", audioUrl);
            return data;
        } catch (Exception e) {
            log.error("Upload failed", e);
            throw new RuntimeException("上传失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> transcribeAudio(Integer audioId) {
        AudioObject audio = audioObjectMapper.selectById(audioId);
        if (audio == null) throw new RuntimeException("未找到音频记录");
        String fileName = audio.getAudioUrl().replace("/static/audio/", "");
        File file = new File(UPLOAD_DIR + fileName);
        if (!file.exists()) throw new RuntimeException("音频文件丢失");

        String reqId = submitAsrTask(file);
        String text = pollAsrResult(reqId);

        if (StrUtil.isBlank(text)) {
            throw new RuntimeException("未检测到有效语音");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("transcript", text);
        result.put("status", "processed");
        return result;
    }

    // ================= TTS 核心修复部分 (状态机实现) =================

    @Override
    public String generateTTS(String text, String voice, Float speed) {
        if (StrUtil.isBlank(text)) return "";

        AiConfig.VolcengineConfig volcConfig = getVolcConfig();
        AiConfig.TtsConfig ttsConfig = volcConfig.getTts();

        String sessionId = UUID.randomUUID().toString();
        String fileName = IdUtil.simpleUUID() + "." + ttsConfig.getFormat();
        File destFile = new File(UPLOAD_DIR + fileName);

        if (!FileUtil.exist(UPLOAD_DIR)) FileUtil.mkdir(UPLOAD_DIR);

        CountDownLatch latch = new CountDownLatch(1);
        ByteArrayOutputStream audioBuffer = new ByteArrayOutputStream();
        AtomicBoolean hasError = new AtomicBoolean(false);

        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(20, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(ttsConfig.getWsUrl())
                .addHeader("X-Api-App-Key", volcConfig.getAppId())
                .addHeader("X-Api-Access-Key", volcConfig.getAccessToken())
                .addHeader("X-Api-Resource-Id", ttsConfig.getResourceId())
                .addHeader("X-Api-Connect-Id", sessionId)
                .build();

        WebSocketListener listener = new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                log.info("TTS WS Connected. Step 1: Sending StartConnection...");
                // 1. 发送 StartConnection (Event 1, 无 SessionID)
                byte[] msg = pack(0x01, 0x04, 0x10, 1, sessionId, "{}".getBytes(StandardCharsets.UTF_8), false);
                webSocket.send(ByteString.of(msg));
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                try {
                    byte[] data = bytes.toByteArray();
                    if (data.length < 4) return;

                    byte msgType = (byte) ((data[1] & 0xF0) >> 4);
                    byte flags = (byte) (data[1] & 0x0F);
                    int offset = 4;

                    // A. 错误帧处理 (0xF)
                    if (msgType == 0x0F) {
                        int errorCode = ByteBuffer.wrap(data, offset, 4).getInt();
                        offset += 4;
                        String errorMsg = new String(data, offset, data.length - offset, StandardCharsets.UTF_8);
                        log.error("TTS Server Error Frame: Code={}, Msg={}", errorCode, errorMsg);
                        hasError.set(true);
                        latch.countDown();
                        return;
                    }

                    // B. 解析 Event Code
                    int eventCode = 0;
                    if ((flags & 0x04) == 0x04) {
                        eventCode = ByteBuffer.wrap(data, offset, 4).getInt();
                        offset += 4;
                    }

                    // C. 跳过 ID
                    if (offset + 4 <= data.length) {
                        int idLen = ByteBuffer.wrap(data, offset, 4).getInt();
                        offset += 4;
                        offset += idLen;
                    }

                    // D. 读取 Payload
                    byte[] payload = new byte[0];
                    if (offset + 4 <= data.length) {
                        int payloadLen = ByteBuffer.wrap(data, offset, 4).getInt();
                        offset += 4;
                        if (payloadLen > 0 && offset + payloadLen <= data.length) {
                            payload = Arrays.copyOfRange(data, offset, offset + payloadLen);
                        }
                    }

                    // --- 状态机 ---
                    
                    if (eventCode == 50) { // ConnectionStarted
                        log.info("Step 2: ConnectionStarted. Sending StartSession...");
                        
                        // 构造 StartSession Payload
                        JSONObject reqJson = new JSONObject();
                        
                        // 补全 app 信息，虽然 Header 有，但 Payload 中加上更保险
                        JSONObject app = new JSONObject();
                        app.put("appid", volcConfig.getAppId());
                        app.put("token", volcConfig.getAccessToken()); 
                        app.put("cluster", "volc_auth_tag");
                        reqJson.put("app", app);

                        JSONObject user = new JSONObject();
                        user.put("uid", "backend_user");
                        reqJson.put("user", user);
                        
                        // 2.0 模型必须参数
                        reqJson.put("namespace", "BidirectionalTTS"); 

                        JSONObject reqParams = new JSONObject();
                        reqParams.put("speaker", StrUtil.isNotBlank(voice) ? voice : ttsConfig.getVoiceType());
                        
                        JSONObject audioParams = new JSONObject();
                        audioParams.put("format", ttsConfig.getFormat());
                        audioParams.put("sample_rate", 24000);
                        if (speed != null) {
                            int speedVal = (int) ((speed - 1.0) * 100);
                            audioParams.put("speech_rate", speedVal);
                        }
                        reqParams.put("audio_params", audioParams);
                        reqJson.put("req_params", reqParams);

                        // 2. 发送 StartSession (Event 100, 带 SessionID)
                        byte[] msg = pack(0x01, 0x04, 0x10, 100, sessionId, reqJson.toJSONString().getBytes(StandardCharsets.UTF_8), true);
                        webSocket.send(ByteString.of(msg));

                    } else if (eventCode == 150) { // SessionStarted
                        log.info("Step 3: SessionStarted. Sending Text & FinishSignal...");
                        
                        // 3.1 发送 TaskRequest (Event 200) - 发送文本
                        JSONObject taskReq = new JSONObject();
                        JSONObject reqParams = new JSONObject();
                        reqParams.put("text", text);
                        // 只发送最核心的 req_params，避免嵌套层级错误
                        taskReq.put("req_params", reqParams);
                        
                        byte[] textMsg = pack(0x01, 0x04, 0x10, 200, sessionId, taskReq.toJSONString().getBytes(StandardCharsets.UTF_8), true);
                        webSocket.send(ByteString.of(textMsg));

                        // 3.2 【关键修复】立即发送 FinishSession (Event 102)
                        // 告诉服务端：我没有更多文本了，请结算音频
                        log.info("Sending FinishSession signal...");
                        byte[] finishMsg = pack(0x01, 0x04, 0x10, 102, sessionId, "{}".getBytes(StandardCharsets.UTF_8), true);
                        webSocket.send(ByteString.of(finishMsg));

                    } else if (eventCode == 352 || msgType == 0xB) { // Audio Data
                        // log.debug("Received Audio Chunk: {} bytes", payload.length);
                        audioBuffer.write(payload);

                    } else if (eventCode == 152) { // SessionFinished
                        log.info("Step 4: TTS Finished. Audio bytes: {}", audioBuffer.size());
                        webSocket.close(1000, "Done");
                        latch.countDown();

                    } else if (eventCode == 153 || eventCode == 51) { // Failed
                        log.error("TTS Protocol Failed Event: {}", eventCode);
                        // 尝试解析 payload 里的错误信息
                        try {
                            log.error("Error Detail: {}", new String(payload, StandardCharsets.UTF_8));
                        } catch(Exception e){}
                        
                        hasError.set(true);
                        latch.countDown();
                    }

                } catch (Exception e) {
                    log.error("TTS Msg Parse Error", e);
                    latch.countDown();
                }
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                log.error("TTS WebSocket Failure: {}", t.getMessage());
                if (response != null) {
                    try {
                        log.error("Response: code={}, body={}", response.code(), response.body() != null ? response.body().string() : "");
                    } catch (Exception e) {}
                }
                hasError.set(true);
                latch.countDown();
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                latch.countDown();
            }
        };

        client.newWebSocket(request, listener);

        try {
            boolean finished = latch.await(20, TimeUnit.SECONDS);
            if (!finished) {
                log.warn("TTS Timeout - Protocol Hang");
                return "";
            }
            if (hasError.get() || audioBuffer.size() == 0) {
                log.warn("TTS Failed/Empty. Err={}", hasError.get());
                return "";
            }
            FileUtil.writeBytes(audioBuffer.toByteArray(), destFile);
            return "/static/audio/" + fileName;

        } catch (InterruptedException e) {
            log.error("TTS Interrupted", e);
            return "";
        }
    }

    private byte[] pack(int msgType, int flags, int serial, int event, String sessionId, byte[] payload, boolean includeSessionId) {
        byte header = (byte) 0x11; 
        byte typeFlags = (byte) ((msgType << 4) | (flags & 0x0F));
        byte serialComp = (byte) (serial & 0xF0); 
        byte reserved = 0x00;

        byte[] sessionBytes = sessionId.getBytes(StandardCharsets.UTF_8);
        int totalLen = 4 + 4 + (includeSessionId ? (4 + sessionBytes.length) : 0) + 4 + payload.length;

        ByteBuffer buffer = ByteBuffer.allocate(totalLen);
        buffer.put(header);
        buffer.put(typeFlags);
        buffer.put(serialComp);
        buffer.put(reserved);
        
        buffer.putInt(event);
        if (includeSessionId) {
            buffer.putInt(sessionBytes.length);
            buffer.put(sessionBytes);
        }
        buffer.putInt(payload.length);
        buffer.put(payload);
        
        return buffer.array();
    }

    // ... 私有辅助方法 (ASR用) ...
    private AiConfig.VolcengineConfig getVolcConfig() {
        if (aiConfig.getSpeech() == null || aiConfig.getSpeech().getVolcengine() == null) {
            throw new RuntimeException("火山引擎配置缺失");
        }
        return aiConfig.getSpeech().getVolcengine();
    }

    private String submitAsrTask(File file) {
        AiConfig.VolcengineConfig volcConfig = getVolcConfig();
        String url = volcConfig.getAsr().getSubmitUrl();
        String reqId = IdUtil.fastUUID();

        Map<String, String> headers = new HashMap<>();
        headers.put("X-Api-App-Key", volcConfig.getAppId());
        headers.put("X-Api-Access-Key", volcConfig.getAccessToken());
        headers.put("X-Api-Resource-Id", volcConfig.getAsr().getResourceId());
        headers.put("X-Api-Request-Id", reqId);
        headers.put("X-Api-Sequence", "-1");

        JSONObject userJson = new JSONObject();
        userJson.put("uid", "backend_user");
        JSONObject audioJson = new JSONObject();
        String suffix = FileUtil.getSuffix(file);
        audioJson.put("format", suffix != null ? suffix : "wav");
        audioJson.put("source", "base64");
        audioJson.put("data", Base64.encode(file));
        JSONObject requestInner = new JSONObject();
        requestInner.put("model_name", "bigmodel");
        requestInner.put("enable_itn", true);
        requestInner.put("enable_punc", true);

        JSONObject body = new JSONObject();
        body.put("user", userJson);
        body.put("audio", audioJson);
        body.put("request", requestInner);

        try {
            HttpResponse response = HttpRequest.post(url).addHeaders(headers).body(body.toJSONString()).timeout(30000).execute();
            String code = response.header("X-Api-Status-Code");
            if (code == null) {
                 JSONObject json = JSON.parseObject(response.body());
                 if (json != null && json.containsKey("code")) code = json.getString("code");
            }
            if (SUCCESS_CODE.equals(code) || "1000".equals(code)) return reqId;
            throw new RuntimeException("提交失败: " + response.body());
        } catch (Exception e) {
            log.error("ASR Submit", e);
            throw new RuntimeException("ASR异常: " + e.getMessage());
        }
    }

    private String pollAsrResult(String reqId) {
        AiConfig.VolcengineConfig volcConfig = getVolcConfig();
        String url = volcConfig.getAsr().getQueryUrl();
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Api-App-Key", volcConfig.getAppId());
        headers.put("X-Api-Access-Key", volcConfig.getAccessToken());
        headers.put("X-Api-Resource-Id", volcConfig.getAsr().getResourceId());
        headers.put("X-Api-Request-Id", reqId);

        for (int i = 0; i < 60; i++) {
            try {
                HttpResponse response = HttpRequest.post(url).addHeaders(headers).body("{}").timeout(5000).execute();
                JSONObject resJson = JSON.parseObject(response.body());
                String code = response.header("X-Api-Status-Code");
                 if (code == null && resJson.containsKey("code")) code = resJson.getString("code");
                
                if (SUCCESS_CODE.equals(code)) {
                     if (resJson.containsKey("result")) {
                         return resJson.getJSONObject("result").getString("text");
                     }
                }
            } catch (Exception e) {}
            ThreadUtil.sleep(500);
        }
        return "";
    }
}
