package org.superbiz.moviefun;

import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.superbiz.moviefun.albums.Album;
import org.superbiz.moviefun.albums.AlbumFixtures;
import org.superbiz.moviefun.albums.AlbumsBean;
import org.superbiz.moviefun.movies.Movie;
import org.superbiz.moviefun.movies.MovieFixtures;
import org.superbiz.moviefun.movies.MoviesBean;

import java.util.Map;

@Controller
public class HomeController {

    private final MoviesBean moviesBean;
    private final AlbumsBean albumsBean;
    private final MovieFixtures movieFixtures;
    private final AlbumFixtures albumFixtures;
    private final TransactionTemplate moviesTxTemplate;
    private final TransactionTemplate albumsTxTemplate;
    private final JpaTransactionManager albumsTm;
    private final JpaTransactionManager moviesTm;

    public HomeController(MoviesBean moviesBean, AlbumsBean albumsBean, MovieFixtures movieFixtures,
                          AlbumFixtures albumFixtures, JpaTransactionManager albumsTm, JpaTransactionManager moviesTm) {
        this.moviesBean = moviesBean;
        this.albumsBean = albumsBean;
        this.movieFixtures = movieFixtures;
        this.albumFixtures = albumFixtures;
        this.albumsTm = albumsTm;
        this.moviesTm = moviesTm;
        this.albumsTxTemplate = new TransactionTemplate(albumsTm);
        this.moviesTxTemplate = new TransactionTemplate(moviesTm);
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/setup")
    public String setup(Map<String, Object> model) {

        moviesTxTemplate.execute((TransactionCallback<Void>) transactionStatus -> {
            for (Movie movie : movieFixtures.load()) {
                moviesBean.addMovie(movie);
            }
            return null;
        });

        albumsTxTemplate.execute((TransactionCallback<Void>) transactionStatus -> {
            for (Album album : albumFixtures.load()) {
                albumsBean.addAlbum(album);
            }
            return null;
        });

/*
        runWithinTx(moviesTm, "add-movies", () -> {
            for (Movie movie : movieFixtures.load()) {
                moviesBean.addMovie(movie);
            }
        });

        runWithinTx(albumsTm, "add-albums", () -> {
            for (Album album : albumFixtures.load()) {
                albumsBean.addAlbum(album);
            }
        });
*/
        model.put("movies", moviesBean.getMovies());
        model.put("albums", albumsBean.getAlbums());

        return "setup";
    }

    private void runWithinTx(JpaTransactionManager txManager, String txName, Runnable operation) {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName(txName);
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txManager.getTransaction(def);
        try {
            operation.run();
        } catch (Exception ex) {
            txManager.rollback(status);
            throw ex;
        } finally {
            try {
                txManager.commit(status);
            } catch (Exception e) {
                e.printStackTrace();
                txManager.rollback(status);
            }
        }
    }
}
