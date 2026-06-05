package app.memo.dto;

public class MemoResponse {
    private final int id;
    private final String content;

    public MemoResponse(int id, String content) {
        this.id = id;
        this.content = content;
    }

    public int getId() {
        return id;
    }

    public String getContent() {
        return content;
    }
}
