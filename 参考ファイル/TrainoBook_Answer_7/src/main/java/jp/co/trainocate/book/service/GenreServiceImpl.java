package jp.co.trainocate.book.service;

import java.util.List;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import jp.co.trainocate.book.entity.Genre;
import jp.co.trainocate.book.repository.GenreRepository;

/**
 * 【課題6.2追加】GenreServiceの具体的な実装クラス。
 * 
 * GenreRepositoryを介してデータベースとのやり取りを行います。
 */
@Service
@RequiredArgsConstructor
public class GenreServiceImpl implements GenreService {

    private final GenreRepository genreRepository;

    @Override
    public List<Genre> findAllGenres() {
        return genreRepository.findAll();
    }
}
