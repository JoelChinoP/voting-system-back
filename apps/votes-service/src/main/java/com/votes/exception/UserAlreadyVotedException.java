package com.votes.exception;

public class UserAlreadyVotedException extends VotingException {
    
    public UserAlreadyVotedException(String message) {
        super(message);
    }
}
