package goormton.univ.Repo.Exception;

public class PrivateRepositoryAccessException extends RuntimeException {
    public PrivateRepositoryAccessException() {
        super("This repository may be private or inaccessible.");
    }
}