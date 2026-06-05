package app.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import app.memo.MemoService;
import app.memo.dto.MemoRequest;
import app.memo.dto.MemoResponse;
import app.util.JsonUtil;

public class ApiServer {
    private final MemoService memoService;
    private HttpServer server;

    public ApiServer(MemoService memoService) {
        this.memoService = memoService;
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/memos", this::handle);
        server.start();

        System.out.println("API server started: http://localhost:8080/memos");
        System.out.println("GET    /memos");
        System.out.println("GET    /memos/{id}");
        System.out.println("POST   /memos");
        System.out.println("PUT    /memos/{id}");
        System.out.println("DELETE /memos/{id}");
    }

    private void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            if ("GET".equals(method) && "/memos".equals(path)) {
                handleGetAll(exchange);
                return;
            }

            if ("POST".equals(method) && "/memos".equals(path)) {
                handlePost(exchange);
                return;
            }

            Integer id = getIdFromPath(path);

            if (id == null) {
                sendJson(exchange, 404, "{\"error\":\"not found\"}");
                return;
            }

            if ("GET".equals(method)) {
                handleGetOne(exchange, id);
                return;
            }

            if ("PUT".equals(method)) {
                handlePut(exchange, id);
                return;
            }

            if ("DELETE".equals(method)) {
                handleDelete(exchange, id);
                return;
            }

            sendJson(exchange, 405, "{\"error\":\"method not allowed\"}");
        } catch (Exception e) {
            e.printStackTrace();
            sendJson(exchange, 500, "{\"error\":\"internal server error\"}");
        }
    }

    private void handleGetAll(HttpExchange exchange) throws IOException {
        List<MemoResponse> memos = memoService.findAll();
        sendJson(exchange, 200, JsonUtil.toJson(memos));
    }

    private void handleGetOne(HttpExchange exchange, int id) throws IOException {
        MemoResponse memo = memoService.findById(id);

        if (memo == null) {
            sendJson(exchange, 404, "{\"error\":\"memo not found\"}");
            return;
        }

        sendJson(exchange, 200, JsonUtil.toJson(memo));
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        MemoRequest request = JsonUtil.toMemoRequest(body);

        MemoResponse created = memoService.addMemo(request);
        sendJson(exchange, 201, JsonUtil.toJson(created));
    }

    private void handlePut(HttpExchange exchange, int id) throws IOException {
        String body = readBody(exchange);
        MemoRequest request = JsonUtil.toMemoRequest(body);

        boolean updated = memoService.updateMemo(id, request);

        if (!updated) {
            sendJson(exchange, 404, "{\"error\":\"memo not found\"}");
            return;
        }

        MemoResponse memo = memoService.findById(id);
        sendJson(exchange, 200, JsonUtil.toJson(memo));
    }

    private void handleDelete(HttpExchange exchange, int id) throws IOException {
        boolean deleted = memoService.deleteMemo(id);

        if (!deleted) {
            sendJson(exchange, 404, "{\"error\":\"memo not found\"}");
            return;
        }

        sendJson(exchange, 200, "{\"message\":\"deleted\"}");
    }

    private Integer getIdFromPath(String path) {
        String prefix = "/memos/";

        if (!path.startsWith(prefix)) {
            return null;
        }

        String idText = path.substring(prefix.length());

        if (idText.length() == 0) {
            return null;
        }

        try {
            return Integer.valueOf(idText);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String readBody(HttpExchange exchange) throws IOException {
        try (InputStream input = exchange.getRequestBody()) {
            byte[] bytes = input.readAllBytes();
            return new String(bytes, StandardCharsets.UTF_8);
        }
    }

    private void sendJson(HttpExchange exchange, int statusCode, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);

        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }
}
