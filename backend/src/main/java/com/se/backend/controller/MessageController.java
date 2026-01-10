package com.se.backend.controller;

import com.se.backend.common.Result;
import com.se.backend.dto.MessageRequest;
import com.se.backend.dto.MessageResponse;
import com.se.backend.service.ChatService;
import com.se.backend.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat")
public class MessageController {

    @Autowired
    private ChatService chatService;

    /**
     * 发送消息接口
     * 目前是同步返回完整回复（MVP模式）。
     * 未来如果接入真实 LLM 流式输出，此接口可改为异步，只返回 "处理中" 状态，
     * 而内容通过 WebSocket 推送。
     */
    @PostMapping("/send")
    public Result<MessageResponse> sendMessage(@RequestBody MessageRequest req) {
        Long userId = UserContext.getUserId();
        // 调用核心业务逻辑
        MessageResponse response = chatService.processMessage(userId, req);
        return Result.success(response);
    }
}
