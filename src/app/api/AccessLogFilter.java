package app.api;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;

@WebFilter("/memos/*")
public class AccessLogFilter implements Filter {

    private Path logPath;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String catalinaBase = System.getProperty("catalina.base");

        if (catalinaBase != null) {
            logPath = Paths.get(catalinaBase, "logs", "memoapp-access.log");
        } else {
            logPath = Paths.get("memoapp-access.log");
        }

        try {
            if (logPath.getParent() != null) {
                Files.createDirectories(logPath.getParent());
            }
        } catch (IOException e) {
            throw new ServletException(e);
        }
    }

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        long start = System.currentTimeMillis();

        try {
            chain.doFilter(request, response);
        } finally {
            long end = System.currentTimeMillis();
            long duration = end - start;

            String time = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            String log = time
                    + " method=" + httpRequest.getMethod()
                    + " uri=" + httpRequest.getRequestURI()
                    + " remote=" + httpRequest.getRemoteAddr()
                    + " durationMs=" + duration;

            writeLog(log);
            System.out.println("[ACCESS] " + log);
        }
    }

    private synchronized void writeLog(String log) throws IOException {
        Files.writeString(
                logPath,
                log + System.lineSeparator(),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
        );
    }
}