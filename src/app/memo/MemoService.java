package app.memo;

import java.util.ArrayList;
import java.util.List;

import app.memo.dao.MemoRepository;
import app.memo.dto.MemoRequest;
import app.memo.dto.MemoResponse;
import app.memo.entity.MemoEntity;

public class MemoService {

	private final MemoRepository memoRepository;

	public MemoService(MemoRepository memoRepository) {
		this.memoRepository = memoRepository;
	}

	public List<MemoResponse> findAll() {
		List<MemoEntity> entities = memoRepository.findAll();
		List<MemoResponse> responses = new ArrayList<>();

		for (MemoEntity entity : entities) {
			responses.add(toResponse(entity));
		}

		return responses;
	}

	public MemoResponse findById(int id) {
		MemoEntity entity = memoRepository.findById(id);

		if (entity == null) {
			return null;
		}

		return toResponse(entity);
	}

	public MemoResponse addMemo(MemoRequest request) {
		String content = getContent(request);

		MemoEntity entity = new MemoEntity(0, content);
		memoRepository.insert(entity);

		List<MemoEntity> entities = memoRepository.findAll();

		if (entities.isEmpty()) {
			return null;
		}

		return toResponse(entities.get(0));
	}

	public boolean updateMemo(int id, MemoRequest request) {
		MemoEntity existing = memoRepository.findById(id);

		if (existing == null) {
			return false;
		}

		String content = getContent(request);

		MemoEntity entity = new MemoEntity(id, content);
		int count = memoRepository.update(entity);

		return count > 0;
	}

	public boolean deleteMemo(int id) {
		int count = memoRepository.deleteById(id);

		return count > 0;
	}

	private MemoResponse toResponse(MemoEntity entity) {
		return new MemoResponse(
				entity.getId(),
				entity.getContent());
	}

	private String getContent(MemoRequest request) {
		if (request == null || request.getContent() == null) {
			throw new IllegalArgumentException("content is required");
		}

		String content = request.getContent().trim();

		if (content.length() == 0) {
			throw new IllegalArgumentException("content is required");
		}

		return content;
	}
}
