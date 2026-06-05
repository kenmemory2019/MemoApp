package app.memo.dto;

public class MemoRequest {
    private String content;

    public MemoRequest() {
    }

    public MemoRequest(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }
}
