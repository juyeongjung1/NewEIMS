package jp.co.trainocate.book.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jp.co.trainocate.book.entity.Book;
import jp.co.trainocate.book.form.BookForm;
import jp.co.trainocate.book.service.BookService;
import jp.co.trainocate.book.service.GenreService;
import lombok.RequiredArgsConstructor;

/**
 * 【課題2.2, 2.3】【課題3.4 〜 3.7】【課題4.1】
 * 書籍管理の各種画面遷移とCRUD操作、および入力チェックを担当するController。
 */
@Controller
@RequestMapping("/book")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;
    private final GenreService genreService;

    @GetMapping("/index")
    public String bookIndex() {
        return "book_index";
    }

    @GetMapping("/list")
    public String bookList(Model model) {
        List<Book> bookList = bookService.findAllBooks();
        model.addAttribute("books", bookList); 
        return "book_list";
    }

    @GetMapping("/search/title")
    public String searchByTitle(String keyword, Model model) {
        List<Book> bookList = bookService.findBooksByTitle(keyword);
        model.addAttribute("books", bookList);
        model.addAttribute("searchType", "title");
        model.addAttribute("keyword", keyword);
        return "book_search_result";
    }

    @GetMapping("/search/price")
    public String searchByPrice(Integer minPrice, Integer maxPrice, Model model) {
        List<Book> bookList = bookService.findBooksByPrice(minPrice, maxPrice);
        model.addAttribute("books", bookList);
        model.addAttribute("searchType", "price");
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        return "book_search_result";
    }

    /**
     * 【課題6.3追加】ジャンル名と上限価格による複合検索
     */
    @GetMapping("/search/complex")
    public String searchByComplex(String genreName, Integer maxPrice, Model model) {
        List<Book> bookList = bookService.findBooksByComplexCondition(genreName, maxPrice);
        model.addAttribute("books", bookList);
        model.addAttribute("searchType", "complex");
        model.addAttribute("genreName", genreName);
        model.addAttribute("maxPrice", maxPrice);
        return "book_search_result";
    }

    @GetMapping("/detail/{id}")
    public String bookDetail(@PathVariable Integer id, Model model) {
        Book book = bookService.findBookById(id);
        model.addAttribute("book", book);
        return "book_detail";
    }

    @GetMapping("/form")
    public String bookForm(Model model) {
        /*
        // ==============================================================
        // 【課題2.3の解答】第2章, 3章では単純に画面を返すだけでした
        // ==============================================================
        // return "book_form";
        */

        // ==============================================================
        model.addAttribute("bookForm", new BookForm());

        // 【課題6.2追加】ジャンル一覧を取得してModelに渡す
        model.addAttribute("genres", genreService.findAllGenres());

        return "book_form";
    }

    /**
     * 【課題4.1】登録時の入力チェック（Validation）
     * @Valid を付けることで入力チェックが実行され、結果が BindingResult に格納されます。
     */
    @PostMapping("/register")
    public String register(@Valid BookForm bookForm, BindingResult result, Model model) {
        /*
        // ==============================================================
        // 【課題3.6の解答】第3章までは入力チェックなく、そのまま保存していました
        // ==============================================================
        // Book book = bookService.saveBook(bookForm);
        // model.addAttribute("book", book);
        // return "book_confirm";
        */

        // ==============================================================
        // 【課題4.1の解答】BindingResult によるエラー判定
        // ==============================================================
        if (result.hasErrors()) {
            // 【課題6.2追加】エラー再表示時もジャンル一覧が必要
            model.addAttribute("genres", genreService.findAllGenres());
            return "book_form";
        }

        // エラーがない場合のみ保存処理を実行
        Book book = bookService.saveBook(bookForm);
        model.addAttribute("book", book);
        return "book_confirm";
    }

    @GetMapping("/update/{id}")
    public String updateForm(@PathVariable Integer id, Model model) {
        Book book = bookService.findBookById(id);
        
        /*
        // ==============================================================
        // 【課題3.7の解答】第3章では Book をそのまま Model に渡していました
        // ==============================================================
        // model.addAttribute("book", book);
        */

        // ==============================================================
        // 【課題4.1の解答】入力チェックエラーによる再表示を考慮し、
        // 画面の th:object と整合性を合わせるため BookForm に詰め替えて渡します
        // ==============================================================
        BookForm bookForm = new BookForm();
        bookForm.setId(book.getId());
        bookForm.setTitle(book.getTitle());
        bookForm.setAuthor(book.getAuthor());
        bookForm.setPrice(book.getPrice());
        bookForm.setGenreId(book.getGenreId());
        
        model.addAttribute("bookForm", bookForm);

        // 【課題6.2/6.4追加】ジャンル一覧を取得してModelに渡す
        model.addAttribute("genres", genreService.findAllGenres());

        return "book_update";
    }

    @PostMapping("/update/{id}")
    public String update(@PathVariable Integer id, @Valid BookForm bookForm, BindingResult result, Model model) {
        /*
        // ==============================================================
        // 【課題3.7の解答】第3章まではそのまま保存していました
        // ==============================================================
        // bookForm.setId(id);
        // Book book = bookService.saveBook(bookForm);
        // model.addAttribute("book", book);
        // return "book_confirm"; 
        */

        // ==============================================================
        // 【課題4.1の解答】更新時の入力チェック処理
        // ==============================================================
        if (result.hasErrors()) {
            // PathVariableのIDをFormにセットし直しておく（URLパラメーターとの整合用）
            bookForm.setId(id);
            // 【課題6.2/6.4追加】エラー再表示時もジャンル一覧が必要
            model.addAttribute("genres", genreService.findAllGenres());
            return "book_update";
        }

        bookForm.setId(id);
        Book book = bookService.saveBook(bookForm);
        model.addAttribute("book", book);
        return "book_confirm"; 
    }

    @RequestMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        bookService.deleteBook(id);
        return "redirect:/book/list";
    }
}
