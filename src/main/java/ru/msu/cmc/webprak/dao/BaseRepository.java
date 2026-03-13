package ru.msu.cmc.webprak.dao;

import java.util.Collection;

public interface BaseRepository<T, ID> {
    T getById(ID id);
    Collection<T> getAll();
    void save(T entity);
    void saveCollection(Collection<T> entities);
    void delete(T entity);
    void update(T entity);
}
