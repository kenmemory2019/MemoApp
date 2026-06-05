package app.memo.entity;

public class MemoEntity {
    private final int id;
    private final String content;

    public MemoEntity(int id, String content) {
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
