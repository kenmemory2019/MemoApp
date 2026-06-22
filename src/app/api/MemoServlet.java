package app.api;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import app.memo.MemoService;
import app.memo.dto.MemoRequest;
import app.memo.dto.MemoResponse;
import app.util.JsonUtil;

@WebServlet("/memos/*")
public class MemoServlet extends HttpServlet {
    private MemoService memoService;

    @Override
    public void init() throws ServletException {
        Object value = getServletContext().getAttribute("memoService");

        if (!(value instanceof MemoService)) {
            throw new ServletException("MemoService is not initialized.");
        }

        this.memoService = (MemoService) value;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();

        if (pathInfo == null || "/".equals(pathInfo)) {
            List<MemoResponse> memos = memoService.findAll();
            sendJson(response, 200, JsonUtil.toJson(memos));
            return;
        }

        Integer id = getIdFromPathInfo(pathInfo);

        if (id == null) {
            sendJson(response, 404, "{\"error\":\"not found\"}");
            return;
        }

        MemoResponse memo = memoService.findById(id);

        if (memo == null) {
            sendJson(response, 404, "{\"error\":\"memo not found\"}");
            return;
        }

        sendJson(response, 200, JsonUtil.toJson(memo));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String body = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        MemoRequest memoRequest = JsonUtil.toMemoRequest(body);

        MemoResponse created = memoService.addMemo(memoRequest);
        sendJson(response, 201, JsonUtil.toJson(created));
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Integer id = getIdFromPathInfo(request.getPathInfo());

        if (id == null) {
            sendJson(response, 404, "{\"error\":\"not found\"}");
            return;
        }

        String body = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        MemoRequest memoRequest = JsonUtil.toMemoRequest(body);

        boolean updated = memoService.updateMemo(id, memoRequest);

        if (!updated) {
            sendJson(response, 404, "{\"error\":\"memo not found\"}");
            return;
        }

        MemoResponse memo = memoService.findById(id);
        sendJson(response, 200, JsonUtil.toJson(memo));
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Integer id = getIdFromPathInfo(request.getPathInfo());

        if (id == null) {
            sendJson(response, 404, "{\"error\":\"not found\"}");
            return;
        }

        boolean deleted = memoService.deleteMemo(id);

        if (!deleted) {
            sendJson(response, 404, "{\"error\":\"memo not found\"}");
            return;
        }

        sendJson(response, 200, "{\"message\":\"deleted\"}");
    }

    private Integer getIdFromPathInfo(String pathInfo) {
        if (pathInfo == null || pathInfo.length() <= 1) {
            return null;
        }

        String idText = pathInfo.substring(1);

        try {
            return Integer.valueOf(idText);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void sendJson(HttpServletResponse response, int statusCode, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);

        response.setStatus(statusCode);
        response.setContentType("application/json; charset=UTF-8");
        response.setContentLength(bytes.length);

        response.getOutputStream().write(bytes);
    }
}