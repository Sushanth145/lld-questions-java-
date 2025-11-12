package questions;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// --------------------- MODELS ---------------------

interface Voteable {
    void upvote();
    int getVotes();
}

abstract class Post implements Voteable {
    protected String content;
    protected int votes = 0;
    protected User author;
    protected Date createdAt;

    public Post(String content, User author) {
        this.content = content;
        this.author = author;
        this.createdAt = new Date();
    }

    public void upvote() {
        votes++;
        author.notifyVote(this); // Observer trigger
    }

    public int getVotes() {
        return votes;
    }

    public String getContent() {
        return content;
    }

    public User getAuthor() {
        return author;
    }
}

class Question extends Post {
    private List<Answer> answers = new ArrayList<>();
    private List<Comment> comments = new ArrayList<>();
    private List<String> tags = new ArrayList<>();

    public Question(String content, User author, List<String> tags) {
        super(content, author);
        this.tags = tags;
    }

    public void addAnswer(Answer ans) {
        answers.add(ans);
    }

    public void addComment(Comment c) {
        comments.add(c);
    }

    public List<Answer> getAnswers() {
        return answers;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public List<String> getTags() {
        return tags;
    }
}

class Answer extends Post {
    private List<Comment> comments = new ArrayList<>();

    public Answer(String content, User author) {
        super(content, author);
    }

    public void addComment(Comment c) {
        comments.add(c);
    }

    public List<Comment> getComments() {
        return comments;
    }
}

class Comment extends Post {
    public Comment(String content, User author) {
        super(content, author);
    }
}

// --------------------- USER (Observer) ---------------------

class User {
    private String name;
    private String email;
    private int reputation = 0;

    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public void notifyVote(Post p) {
        // Observer pattern – called when one of user's posts gets a vote
        this.reputation += 10;
    }

    public String getName() {
        return name;
    }

    public int getReputation() {
        return reputation;
    }
}

// --------------------- FACTORY PATTERN ---------------------

class PostFactory {
    public static Question createQuestion(String content, User user, List<String> tags) {
        return new Question(content, user, tags);
    }

    public static Answer createAnswer(String content, User user) {
        return new Answer(content, user);
    }

    public static Comment createComment(String content, User user) {
        return new Comment(content, user);
    }
}

// --------------------- REPOSITORY PATTERN ---------------------

class QuestionRepository {
    private Map<String, List<Question>> questionsByTag = new ConcurrentHashMap<>();

    public synchronized void addQuestion(Question q) {
        for (String tag : q.getTags()) {
            questionsByTag.putIfAbsent(tag, new ArrayList<>());
            questionsByTag.get(tag).add(q);
        }
    }

    public List<Question> getQuestionsByTag(String tag) {
        return questionsByTag.getOrDefault(tag, Collections.emptyList());
    }

    public List<Question> getAllQuestions() {
        List<Question> all = new ArrayList<>();
        for (List<Question> qs : questionsByTag.values()) {
            all.addAll(qs);
        }
        return all;
    }
}

// --------------------- STRATEGY PATTERN FOR SEARCH ---------------------

interface SearchStrategy {
    List<Question> search(String query, QuestionRepository repo);
}

class TagSearchStrategy implements SearchStrategy {
    public List<Question> search(String tag, QuestionRepository repo) {
        return repo.getQuestionsByTag(tag);
    }
}

class KeywordSearchStrategy implements SearchStrategy {
    public List<Question> search(String keyword, QuestionRepository repo) {
        List<Question> result = new ArrayList<>();
        for (Question q : repo.getAllQuestions()) {
            if (q.getContent().toLowerCase().contains(keyword.toLowerCase())) {
                result.add(q);
            }
        }
        return result;
    }
}

class UserSearchStrategy implements SearchStrategy {
    public List<Question> search(String username, QuestionRepository repo) {
        List<Question> result = new ArrayList<>();
        for (Question q : repo.getAllQuestions()) {
            if (q.getAuthor().getName().equalsIgnoreCase(username)) {
                result.add(q);
            }
        }
        return result;
    }
}

// --------------------- SINGLETON PLATFORM ---------------------

class StackOverflowPlatform {
    private static StackOverflowPlatform instance;
    private QuestionRepository questionRepo = new QuestionRepository();

    private StackOverflowPlatform() {}

    public static synchronized StackOverflowPlatform getInstance() {
        if (instance == null) {
            instance = new StackOverflowPlatform();
        }
        return instance;
    }

    public void postQuestion(String content, User user, List<String> tags) {
        Question q = PostFactory.createQuestion(content, user, tags);
        questionRepo.addQuestion(q);
    }

    public List<Question> searchQuestions(SearchStrategy strategy, String query) {
        return strategy.search(query, questionRepo);
    }

    public QuestionRepository getRepository() {
        return questionRepo;
    }
}

// --------------------- TEST / DEMO ---------------------

public class StackOverflowPlatformDemo {
    public static void main(String[] args) {
        StackOverflowPlatform platform = StackOverflowPlatform.getInstance();

        User alice = new User("Alice", "alice@mail.com");
        User bob = new User("Bob", "bob@mail.com");

        // Post questions
        platform.postQuestion("How to learn design patterns?", alice, Arrays.asList("java", "design-patterns"));
        platform.postQuestion("What is the Observer pattern?", bob, Arrays.asList("java", "oop"));

        // Search by tag
        SearchStrategy tagSearch = new TagSearchStrategy();
        List<Question> javaQs = platform.searchQuestions(tagSearch, "java");
        for (Question q : javaQs) {
            System.out.println("Found: " + q.getContent());
        }

        // Upvote and check reputation
        Question q = javaQs.get(0);
        q.upvote();
        System.out.println(alice.getName() + " reputation: " + alice.getReputation());
    }
}
