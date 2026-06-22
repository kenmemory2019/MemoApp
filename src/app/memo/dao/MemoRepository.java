package app.memo.dao;

import java.util.List;

import app.memo.entity.MemoEntity;

public interface MemoRepository {
    List<MemoEntity> findAll();

    MemoEntity findById(int id);

    void insert(MemoEntity entity);

    int update(MemoEntity entity);

    int deleteById(int id);
}