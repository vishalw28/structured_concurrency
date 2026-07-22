package com.structured.concurrency;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.structured.concurrency.LongRunningTask.TaskResponse;

/**
 * LongRunningTask
 */
public class LongRunningTask implements Callable<TaskResponse> {
    private final String name;
    private final int time;
    private final String output;
    private final boolean fail;

    public LongRunningTask(String name, int time, String output, boolean fail) {
        this.name = name;
        this.time = time;
        this.output = output;
        this.fail = fail;
    }

    public String getName(){return name;}
    record TaskResponse(String name, String response, long timeTaken) {

    }

    @Override
    public TaskResponse call() throws Exception {
        long start = System.currentTimeMillis();
        print("Started");
        Thread.sleep(Duration.ofSeconds(this.time));
        if (fail) {
            print("Failed");
            throw new RuntimeException(name + " : Failed");
        }
        print("Completed");
        long end = System.currentTimeMillis();
        return new TaskResponse(name, this.output, end - start);
    }

    private void print(String msg) {
        System.out.printf("> %s : %s\n", name, msg);
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("> Main: Started");
        LongRunningTask task = new LongRunningTask("LongTask1", 10, "json-response1", true);
        var dbTask = new LongRunningTask("dataTask", 3, "row1", false);
        var restTask = new LongRunningTask("restTask", 10, "json2", false);
        var extTask = new LongRunningTask("extTask", 5, "json2", false);

        // execute the sub tasks in parallel.
        // Throw exception if interrupted or any task fails
        Map<String, TaskResponse> result = STaskScopeOnFailureExecutor.execute(List.of(dbTask, extTask, restTask));

        // print results of all tasks
        result.forEach((k, v) -> {
            System.out.printf("%s : %s\n", k, v);
        });

        // extract output for an individual task
        TaskResponse extResponse = result.get("extTask");

        System.out.println("> Main: Completed");
    }

}
