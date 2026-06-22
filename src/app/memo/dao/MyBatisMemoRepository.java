package app.memo.dao;

import java.io.Reader;
import java.util.List;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import app.memo.entity.MemoEntity;
import app.memo.mapper.MemoMapper;

public class MyBatisMemoRepository implements MemoRepository{
    private final SqlSessionFactory sqlSessionFactory;

    public MyBatisMemoRepository() {
        try {
            Reader reader = Resources.getResourceAsReader("mybatis-config.xml");
            this.sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<MemoEntity> findAll() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            MemoMapper mapper = session.getMapper(MemoMapper.class);
            return mapper.findAll();
        }
    }

    public MemoEntity findById(int id) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            MemoMapper mapper = session.getMapper(MemoMapper.class);
            return mapper.findById(id);
        }
    }

    public void insert(MemoEntity memo) {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            MemoMapper mapper = session.getMapper(MemoMapper.class);
            mapper.insert(memo);
        }
    }

    public int update(MemoEntity memo) {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            MemoMapper mapper = session.getMapper(MemoMapper.class);
            return mapper.update(memo);
        }
    }

    public int deleteById(int id) {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            MemoMapper mapper = session.getMapper(MemoMapper.class);
            return mapper.deleteById(id);
        }
    }
}
