package com.structured.concurrency;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.structured.concurrency.LongRunningTask.TaskResponse;

/**
 * LongRunningTask
 */
public class LongRunningTask implements Callable<TaskResponse>{
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

    record TaskResponse(String name, String response, long timeTaken){

    }
    @Override
    public TaskResponse call() throws Exception {
        long start = System.currentTimeMillis();
        print("Started");
        Thread.sleep(Duration.ofSeconds(this.time));
        if(fail){
            print("Failed");
            throw new RuntimeException(name + " : Failed");
        }
        print("Completed");
        long end = System.currentTimeMillis();
        return new TaskResponse(name, this.output, end-start);
    }


    private void print(String msg){
        System.out.printf("> %s : %s\n", name, msg);
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("> Main: Started");
        LongRunningTask task = new LongRunningTask("LongTask1", 10, "json-response1", true);
        try(var service = Executors.newFixedThreadPool(2)){
            Future<TaskResponse> taskFuture = service.submit(task);
            Thread.sleep(Duration.ofSeconds(5));
            taskFuture.cancel(true);
        }
        System.out.println("> Main: Completed");
    }

}
