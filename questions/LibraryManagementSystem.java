package questions;
import java.util.*;

// ====================== MODEL CLASSES ===========================

class Book {
    String title;
    String author;
    String isbn;
    String category;

    int totalCopies = 0;
    int availableCopies = 0;

    Book(String title, String author, String isbn, String category) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.category = category;
    }
}

class User {
    String name;
    int currentBorrowCount = 0;
    int maxLimit = 3; // default

    User(String name) {
        this.name = name;
    }
}

// ====================== INTERFACES ===============================

// Interface Segregation + Dependency Inversion
interface IBookRepository {
    void addBook(Book b);
    Book getBook(String isbn);
    boolean exists(String isbn);
}

// Open-Close for changing borrowing rules
interface BorrowPolicy {
    boolean canBorrow(User user, Book book);
}

// ====================== REPOSITORY IMPLEMENTATION ===============

class InMemoryBookRepository implements IBookRepository {

    private Map<String, Book> books = new HashMap<>();

    @Override
    public void addBook(Book b) {
        books.put(b.isbn, b);
    }

    @Override
    public Book getBook(String isbn) {
        return books.get(isbn);
    }

    @Override
    public boolean exists(String isbn) {
        return books.containsKey(isbn);
    }
}

// ====================== POLICY IMPLEMENTATION ====================

class DefaultBorrowPolicy implements BorrowPolicy {

    @Override
    public boolean canBorrow(User user, Book book) {
        return user.currentBorrowCount < user.maxLimit && book.availableCopies > 0;
    }
}

// ====================== SERVICE ================================

class LibraryService {

    private final IBookRepository repo;       // DIP: depends on abstraction
    private final BorrowPolicy borrowPolicy;  // OCP: pluggable borrow rule

    LibraryService(IBookRepository repo, BorrowPolicy policy) {
        this.repo = repo;
        this.borrowPolicy = policy;
    }

    public void addBookStock(Book b, int count) {
        if (!repo.exists(b.isbn)) {
            repo.addBook(b);
        }
        b.totalCopies += count;
        b.availableCopies += count;
    }

    public void checkAvailability(String isbn) {
        Book b = repo.getBook(isbn);

        if (b == null) {
            System.out.println("Book not found.");
        } else {
            System.out.println("Available copies: " + b.availableCopies);
        }
    }

    public void borrowBook(String isbn, User user) {
        Book b = repo.getBook(isbn);

        if (b == null) {
            System.out.println("Book not found in library.");
            return;
        }

        if (!borrowPolicy.canBorrow(user, b)) {
            System.out.println("Borrow not allowed based on policy.");
            return;
        }

        b.availableCopies--;
        user.currentBorrowCount++;

        System.out.println("Borrowed successfully!");
    }

    public void returnBook(String isbn, User user) {
        Book b = repo.getBook(isbn);

        if (b == null) {
            System.out.println("Unknown book! Adding to library...");
            b = new Book("Unknown", "Unknown", isbn, "Misc");
            repo.addBook(b);
        }

        b.availableCopies++;
        b.totalCopies++;
        user.currentBorrowCount--;

        System.out.println("Returned successfully!");
    }
}

// ====================== MAIN ====================================

public class LibraryManagementSystem {
    public static void main(String[] args) {

        IBookRepository repo = new InMemoryBookRepository();
        BorrowPolicy policy = new DefaultBorrowPolicy();
        LibraryService service = new LibraryService(repo, policy);

        Book b1 = new Book("Atomic Habits", "James Clear", "ISBN123", "Self Help");
        User u1 = new User("Sushanth");

        service.addBookStock(b1, 3);
        service.checkAvailability("ISBN123");

        service.borrowBook("ISBN123", u1);
        service.checkAvailability("ISBN123");

        service.returnBook("ISBN123", u1);
        service.checkAvailability("ISBN123");
    }
}
