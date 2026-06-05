package app.memo.mapper;

import java.util.List;

import app.memo.entity.MemoEntity;

public interface MemoMapper {
    List<MemoEntity> findAll();

    MemoEntity findById(int id);

    void insert(MemoEntity memo);

    int update(MemoEntity memo);

    int deleteById(int id);
}
