package app.api;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import app.memo.MemoService;
import app.memo.dao.InMemoryMemoRepository;
import app.memo.dao.MyBatisMemoRepository;
import app.memo.dao.MemoRepository;

@WebListener
public class AppContextListener implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent event) {
		try {
			System.out.println("=== AppContextListener start ===");

			MemoService memoService = createMemoService();

			System.out.println("=== MemoService created ===");

			event.getServletContext().setAttribute("memoService", memoService);

			System.out.println("=== MemoService set to ServletContext ===");

		} catch (Throwable e) {
			System.out.println("=== AppContextListener failed ===");
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private MemoService createMemoService() {
		MemoRepository memoRepository;

		try {
			System.out.println("=== create DB repository ===");

			memoRepository = new MyBatisMemoRepository();

			// MyBatisの初期化だけだとDB接続まで確認できない場合があるので、
			// ここで一度DBアクセスして確認する。
			memoRepository.findAll();

			System.out.println("=== DB repository ready ===");

		} catch (Exception e) {
			System.out.println("=== DB connection failed. Use in-memory repository. ===");
			e.printStackTrace();

			memoRepository = new InMemoryMemoRepository();
		}

		return new MemoService(memoRepository);
	}
}