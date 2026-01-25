package com.mopl.domain.support.transaction;

public interface AfterCommitExecutor {

    void execute(Runnable action);
}
