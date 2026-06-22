package app.memo.dao;

import java.util.ArrayList;
import java.util.List;

import app.memo.entity.MemoEntity;

public class InMemoryMemoRepository implements MemoRepository {

	private final List<MemoEntity> memos = new ArrayList<>();
	private int sequence = 1;

	@Override
	public synchronized List<MemoEntity> findAll() {
		return new ArrayList<>(memos);
	}

	@Override
	public synchronized MemoEntity findById(int id) {
		for (int i = 0; i < memos.size(); i++) {
			MemoEntity memo = memos.get(i);

			if (memo.getId() == id) {
				return memo;
			}
		}

		return null;
	}

	@Override
	public synchronized void insert(MemoEntity entity) {
		int id = sequence;
		sequence++;

		MemoEntity newMemo = new MemoEntity(id, entity.getContent());
		memos.add(0, newMemo);
	}

	@Override
	public synchronized int update(MemoEntity entity) {
		for (int i = 0; i < memos.size(); i++) {
			MemoEntity memo = memos.get(i);

			if (memo.getId() == entity.getId()) {
				memos.set(i, entity);
				return 1;
			}
		}

		return 0;
	}

	@Override
	public synchronized int deleteById(int id) {
		for (int i = 0; i < memos.size(); i++) {
			MemoEntity memo = memos.get(i);

			if (memo.getId() == id) {
				memos.remove(i);
				return 1;
			}
		}

		return 0;
	}
}
