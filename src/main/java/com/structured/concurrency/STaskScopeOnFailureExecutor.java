package com.structured.concurrency;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.StructuredTaskScope.Joiner;
import java.util.concurrent.StructuredTaskScope.Subtask;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.structured.concurrency.LongRunningTask.TaskResponse;

public class STaskScopeOnFailureExecutor {
    public static Map<String, TaskResponse> execute(List<LongRunningTask> tasks) throws InterruptedException{
        Map<String, TaskResponse> map = null;
        try(var scope = StructuredTaskScope.open(Joiner.awaitAllSuccessfulOrThrow())) {
            var subTasks = tasks.stream()
                .map(scope::fork)
                .toList();
            scope.join();
            map = subTasks.stream()
                .map(t -> t.get())
                .collect(Collectors.toMap(TaskResponse::name, Function.identity()));
        }
        return map;
    }    
}
