package app;

import app.api.ApiServer;
import app.memo.MemoService;
import app.memo.dao.MemoDao;

public class Main {
    public static void main(String[] args) throws Exception {
        MemoDao memoDao = new MemoDao();
        MemoService memoService = new MemoService(memoDao);

        ApiServer apiServer = new ApiServer(memoService);
        apiServer.start();
    }
}
