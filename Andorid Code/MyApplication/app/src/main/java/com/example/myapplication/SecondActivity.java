package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class Block{
    public View self;
    public View NBorder;
    public View SBorder;
    public View EBorder;
    public View WBorder;
    public View getEdge(String direction){
        switch (direction) {
            case "N":
                return NBorder;
            case "S":
                return SBorder;
            case "E":
                return EBorder;
            default:
                return WBorder;
        }
    }
}

public class SecondActivity extends AppCompatActivity {
    MessageSender messageSender = MessageSender.getinstance();
    private final ScheduledExecutorService[] scheduledExecutorService = new ScheduledExecutorService[4];
    ScheduledExecutorService scheduledExecutorService1;
    private Handler handler = new Handler();

    TextView distanceFront, distanceLeft, distanceRight;
    int blockSize = 25;


    HashMap<Integer, Block> grid = new HashMap<>();

    float maxDistance = 0;
    String curDirection = "N";
    int x = 4, y = 4, newx = 4, newy = 4;
    //int[] prev = new int[]{4, 4};
    int flag = 0;
    int first = 1;
    int wallx, wally;
    View e1;
    boolean isTurning = false;

    float front, left, right;
    HashMap<String, Integer> sign = new HashMap<String, Integer>(){{
        put("N", 1);
        put("S", -1);
        put("E", -1);
        put("W", 1);
    }};
    
    HashMap<String, String> newDirection = new HashMap<String, String>(){{
        put("Nr", "E");
        put("Nl", "W");
        put("Sr", "W");
        put("Sl", "E");
        put("Er", "S");
        put("El", "N");
        put("Wr", "N");
        put("Wl", "S");
    }};


    HashMap<Integer, String> colors = new HashMap<Integer, String>(){{
        put(1, "#4287f5");
        put(0, "#f25c33");
    }};
    HashMap<Integer, Drawable> drawables = new HashMap<Integer, Drawable>();
    HashSet<View> boundary = new HashSet<>();


    String robotImg = "@drawable/robot_n";
    String blankImg = "@drawable/blank";
    String greyImg = "@drawable/drawable_grey";
    Drawable drawable, drawableBlank, drawablegrey;
    View block, edge1, edge2, edgeCancell;

    int i = 0, j = 0;
    Block b1 = grid.get(i++), b2;

    String turn = "N";
    float newDistance;
    int maxBlockNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        if (getIntent().hasExtra("com.example.myapplication")){
            TextView tv1 = (TextView) findViewById(R.id.secondTv1);
            String ipAdress = getIntent().getExtras().getString("com.example.myapplication");
            tv1.setText(ipAdress);
        }

        scheduledExecutorService1 = Executors.newScheduledThreadPool(1);
        DistanceUpdaterTask distanceUpdaterTask = new DistanceUpdaterTask();
        scheduledExecutorService1.scheduleWithFixedDelay(distanceUpdaterTask, 0, 1, TimeUnit.SECONDS);

        distanceFront = (TextView) findViewById(R.id.distanceTv1);
        distanceLeft = (TextView) findViewById(R.id.distanceTv2);
        distanceRight = (TextView) findViewById(R.id.distanceTv3);


        Button forwardBtn = (Button)findViewById(R.id.secondBtn1);
        Button leftBtn = (Button)findViewById(R.id.secondBtn2);
        Button rightBtn = (Button)findViewById(R.id.secondBtn3);
        Button backwardBtn = (Button)findViewById(R.id.secondBtn4);

        Context context = getApplicationContext();
        int id = getResources().getIdentifier(robotImg, "drawable", context.getPackageName());
        drawable = getResources().getDrawable(id);
        id = getResources().getIdentifier(blankImg, "drawable", context.getPackageName());
        drawableBlank = getResources().getDrawable(id);
        id = getResources().getIdentifier(greyImg, "drawable", context.getPackageName());
        drawablegrey = getResources().getDrawable(id);

        drawables.put(0, drawable);
        drawables.put(1, drawableBlank);
        drawables.put(2, drawablegrey);

        id = getResources().getIdentifier("block" + Integer.toString(0) + Integer.toString(0), "id", context.getPackageName());
        View test = findViewById(id);
        Block b;

        for (int i = 0;i < 5;i++) {
            for (int j = 0; j < 5; j++) {
                b = new Block();
                id = getResources().getIdentifier("block" + Integer.toString(i) + Integer.toString(j), "id", context.getPackageName());
                b.self = findViewById(id);
                id = getResources().getIdentifier("h" + Integer.toString(i) + Integer.toString(j), "id", context.getPackageName());
                b.NBorder = findViewById(id);
                id = getResources().getIdentifier("h" + Integer.toString(i + 1) + Integer.toString(j), "id", context.getPackageName());
                b.SBorder = findViewById(id);
                id = getResources().getIdentifier("v" + Integer.toString(i) + Integer.toString(j), "id", context.getPackageName());
                b.WBorder = findViewById(id);
                id = getResources().getIdentifier("v" + Integer.toString(i) + Integer.toString(j + 1), "id", context.getPackageName());
                b.EBorder = findViewById(id);
                grid.put(i * 5 + j, b);
            }
        }
        //Initialize the starting position
        grid.get(24).self.setBackground(drawable);
        if (curDirection == "N")
            grid.get(24).WBorder.setBackgroundColor(Color.parseColor("#4287f5"));
        else
            grid.get(24).NBorder.setBackgroundColor(Color.parseColor("#4287f5"));

        int[] indexToAdd = new int[]{0, 5};
        int curRowOrCol;
        //add horizontal boundary
        for(i = 0;i < 2;i++){
            curRowOrCol = indexToAdd[i];
            for (j = 0;j < 5;j++){
                if (curRowOrCol == 5 && j == 4) continue;
                boundary.add(findViewById(getResources().getIdentifier("h" + String.valueOf(curRowOrCol) + String.valueOf(j), "id", context.getPackageName())));
                Log.i("Second Activity", "h" + String.valueOf(i) + String.valueOf(curRowOrCol));
            }
            for (j = 5;j < 11;j++)
                boundary.add(findViewById(getResources().getIdentifier("h" + String.valueOf(curRowOrCol) + String.valueOf(j), "id", context.getPackageName())));
        }
        //add vertical boundary
        for(i = 0;i < 2;i++){
            curRowOrCol = indexToAdd[i];
            for (j = 0;j < 5;j++){
                Log.i("Second Activity", "v" + String.valueOf(j) + String.valueOf(curRowOrCol));
                boundary.add(findViewById(getResources().getIdentifier("v" + String.valueOf(j) + String.valueOf(curRowOrCol), "id", context.getPackageName())));
            }
            for (j = 0;j < 4;j++)
                boundary.add(findViewById(getResources().getIdentifier("vs" + String.valueOf(j) + String.valueOf(curRowOrCol), "id", context.getPackageName())));
        }

        Iterator<View> it = boundary.iterator();
        while (it.hasNext()){
            it.next().setBackgroundColor(Color.parseColor("#4287f5"));
        }



        //button being pressed for a long time?
        /*
        forwardBtn.setOnClickListener(this);
        leftBtn.setOnClickListener(this);
        rightBtn.setOnClickListener(this);
        backwardBtn.setOnClickListener(this);
         */

        forwardBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
                    messageSender.sendWithNoReply(1);
                else if (motionEvent.getAction() == MotionEvent.ACTION_UP)
                    messageSender.sendWithNoReply(0);
                return false;
            }
        });

        leftBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
                    messageSender.sendWithNoReply(2);
                else if (motionEvent.getAction() == MotionEvent.ACTION_UP)
                    messageSender.sendWithNoReply(0);
                return false;
            }
        });
        rightBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
                    messageSender.sendWithNoReply(3);
                else if (motionEvent.getAction() == MotionEvent.ACTION_UP)
                    messageSender.sendWithNoReply(0);
                return false;
            }
        });
        backwardBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
                    messageSender.sendWithNoReply(4);
                else if (motionEvent.getAction() == MotionEvent.ACTION_UP)
                    messageSender.sendWithNoReply(0);
                return false;
            }
        });

        Button testBtn = (Button) findViewById(R.id.testBtn);
        Button resetBtn = (Button) findViewById(R.id.resetBtn);
        View block00 = findViewById(R.id.block00);


        b1 = grid.get(0);
        b1.NBorder.setBackgroundColor(Color.parseColor("#4287f5"));
        testBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                b1 = grid.get(i++);
                b1.self.setBackground(drawable);
                b1.NBorder.setBackgroundColor(Color.parseColor("#4287f5"));
                b1.SBorder.setBackgroundColor(Color.parseColor("#f25c33"));
                b1.WBorder.setBackgroundColor(Color.parseColor("#d533f2"));
                b1.EBorder.setBackgroundColor(Color.parseColor("#33f26f"));
                Log.i("secondActivity", "success");
            }
        });

        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (int i = 0;i < 25;i++){
                    b1 = grid.get(i);
                    b1.self.setBackground(drawablegrey);
                    if (!boundary.contains(b1.NBorder))
                        b1.NBorder.setBackgroundColor(Color.parseColor("#e0e0e0"));
                    if (!boundary.contains(b1.SBorder))
                        b1.SBorder.setBackgroundColor(Color.parseColor("#e0e0e0"));
                    if (!boundary.contains(b1.EBorder))
                        b1.EBorder.setBackgroundColor(Color.parseColor("#e0e0e0"));
                    if (!boundary.contains(b1.WBorder))
                        b1.WBorder.setBackgroundColor(Color.parseColor("#e0e0e0"));
                }
                grid.get(24).self.setBackground(drawable);
                grid.get(24).WBorder.setBackgroundColor(Color.parseColor("#4287f5"));
                first = 1;
                x = 4;
                y = 4;
                curDirection = "N";
                Log.i("secondActivity", "Map reset success!");
            }
        });
        newRunnable.run();

    }
    int idx = 0;
    String color;
    private Runnable newRunnable = new Runnable() {
        @Override
        public void run() {
            /*
            b1 = grid.get(0);
            b2 = grid.get(1);
            //color = colors.get(idx);
            b1.self.setBackground(drawables.get(idx));
            b2.self.setBackground(drawables.get(3 - idx));
            idx = (idx + 1)%3;
             */

            // At the starting point
            if (first == 1 && front != 0) {
                if (curDirection == "N" || curDirection == "S") {
                    wallx = 4 - sign.get(curDirection) * (int) (front / blockSize);
                    wally = y;
                }
                else{
                    wally = 4 - sign.get(curDirection) * (int) (front / blockSize);
                    wallx = x;
                }
                System.out.println("The position of the first wall, wallx: " + String.valueOf(wallx) + ", wally: " + String.valueOf(wally));
                if (grid.containsKey(wallx*5 + wally)) {
                    if (curDirection == "N")
                        edge1 = grid.get(wallx * 5 + wally).NBorder;
                    else
                        edge1 = grid.get(wallx * 5 + wally).WBorder;
                    Log.i("secondActivity", "Wall added:" + String.valueOf(wallx * 5 + y));
                    edge1.setBackgroundColor(Color.parseColor("#4287f5"));
                    maxDistance = front;
                    first = 0;
                }
                else
                    Log.i("secondActivity", "================= key error111! wallx:" + String.valueOf(wallx) + ", wally:" + String.valueOf(wally));
            }
            //Check if turn is finished
            if (flag == 1) {
                //if the robot is still turning around then front may be the distance betweeen the side wall and the robot, which is smaller than maxDistance
                System.out.println("maxDistance: " + String.valueOf(maxDistance));
                if (front > maxDistance - 15 && front > 15){
                    Log.i("secondActivity", "Turning completed!");
                    turn = "N";
                    flag = 0;
                }else {
                    Log.i("secondActivity", "Turning in progress!");

                    if (front < 8){
                        if (turn == "l"){
                            newDistance = left;
                        }
                        else if (turn == "r"){
                            newDistance = right;
                        }
                        else{
                            newDistance = Math.max(left, right);
                        }

                        maxBlockNum = (int) (newDistance / blockSize);
                        if (newDistance < 100 && newDistance != maxDistance){
                            if (turn == "N") {
                                if (newDistance == left) turn = "l";
                                else turn = "r";
                                curDirection = newDirection.get(curDirection + turn);
                                System.out.println("44444444444444444444 New direction " + curDirection);
                                if (curDirection == "E" || curDirection == "N") edgeCancell = edge1;
                                else edgeCancell = edge2;
                            }
                            else{
                                edgeCancell = edge1;
                            }

                            maxDistance = newDistance;

                            if (!boundary.contains(edgeCancell)) {
                                edgeCancell.setBackgroundColor(Color.parseColor("#e0e0e0"));
                                Log.i("secondActivity", "Wall cancelled:" + ", wally:" + String.valueOf(wally) + ", wallx:" + String.valueOf(wallx));
                            }
                            if (curDirection == "N" || curDirection == "S") {
                                wallx = x - sign.get(curDirection) * (int) (newDistance / blockSize);
                                wally = y;
                            }else{
                                wallx = x;
                                wally = y - sign.get(curDirection) * (int) (newDistance / blockSize);
                            }
                            edge1 = grid.get(wallx * 5 + wally).getEdge(curDirection);
                            edge1.setBackgroundColor(Color.parseColor("#4287f5"));
                            Log.i("secondActivity", "Wall added:" + ", wally:" + String.valueOf(wally) + ", wallx:" + String.valueOf(wallx));
                        }
                    }
                }

            }
            //Check  front to make sure that the data is correct
            if (front <= maxDistance && front > blockSize && first == 0) {
                if (flag == 0) {
                    if (curDirection.equals("N") || curDirection.equals("S")) {
                        System.out.println("111111111111111");
                        newx = wallx + sign.get(curDirection) * (int) (front / blockSize);
                        Log.i("secondActivity", "newx:" + String.valueOf(newx) + ", y:" + String.valueOf(y) + ", x:" + String.valueOf(x));
                        if (x != newx) {
                            //put robot to the new position
                            if (grid.containsKey(newx * 5 + y)) {
                                //Change the previous spot to blank
                                if (grid.containsKey(x * 5 + y)) {
                                    block = grid.get(x * 5 + y).self;
                                    System.out.println("Changing drawable to blank" + ", y:" + String.valueOf(y) + ", x:" + String.valueOf(x));
                                    block.setBackground(drawableBlank);
                                    x = newx;
                                } else
                                    Log.i("secondActivity", "================= key error 222! x:" + String.valueOf(x) + ", y:" + String.valueOf(y));

                                block = grid.get(newx * 5 + y).self;
                                block.setBackground(drawable);

                                edge1 = grid.get(newx * 5 + y).EBorder;
                                edge1.setBackgroundColor(Color.parseColor("#4287f5"));

                                edge2 = grid.get(newx * 5 + y).WBorder;
                                edge2.setBackgroundColor(Color.parseColor("#4287f5"));

                            } else
                                Log.i("secondActivity", "================= key error! 333 newx:" + String.valueOf(newx) + ", y:" + String.valueOf(y));
                        }
                    }
                    else{
                        System.out.println("22222222222222222222");
                        newy = wally + sign.get(curDirection)*(int)(front/blockSize);
                        if (y != newy){
                            if (grid.containsKey(x * 5 + newy)) {
                                if (grid.containsKey(x * 5 + y)) {
                                    block = grid.get(x * 5 + y).self;
                                    System.out.println("Changing drawable to blank" + ", y:" + String.valueOf(y) + ", x:" + String.valueOf(x));
                                    block.setBackground(drawableBlank);
                                    y = newy;
                                }else
                                    Log.i("secondActivity", "================= key error! 444 x:" + String.valueOf(x) + ", y:" + String.valueOf(y));
                                block = grid.get(x * 5 + newy).self;
                                block.setBackground(drawable);

                                //we dont set wall at the entry
                                edge1 = grid.get(x * 5 + newy).NBorder;
                                if (x * 5 + newy != 24)
                                    edge1.setBackgroundColor(Color.parseColor("#4287f5"));

                                edge2 = grid.get(x * 5 + newy).SBorder;
                                if (x * 5 + newy != 24)
                                    edge2.setBackgroundColor(Color.parseColor("#4287f5"));
                            }else{
                                Log.i("secondActivity", "================= key error! 777 x:" + String.valueOf(x) + ", newy:" + String.valueOf(newy));
                            }
                        }
                    }

                }
            }
            //make a turn


            if (front < blockSize && flag == 0 && first == 0){
                System.out.println("3333333333333333333333");
                flag = 1;
                if (grid.containsKey(x * 5 + y)) {
                    block = grid.get(x * 5 + y).self;
                    System.out.println("Changing drawable to blank" + ", y:" + String.valueOf(y) + ", x:" + String.valueOf(x));
                    block.setBackground(drawableBlank);
                } else
                    Log.i("secondActivity", "================= key error 888! x:" + String.valueOf(x) + ", y:" + String.valueOf(y));

                if (curDirection == "N" || curDirection == "S"){
                    newx = wallx + sign.get(curDirection) * (int) (front / blockSize);
                    if (x != newx)
                        x -= sign.get(curDirection);
                }
                else {
                    newy = wally + sign.get(curDirection)*(int)(front / blockSize);
                    if (y != newy)
                        y -= sign.get(curDirection);
                }

                if (grid.containsKey(x * 5 + y)) {
                    block = grid.get(x * 5 + y).self;
                    System.out.println("Changing position of robot" + ", y:" + String.valueOf(y) + ", x:" + String.valueOf(x));
                    block.setBackground(drawable);
                }else
                    Log.i("secondActivity", "================= key error 999! x:" + String.valueOf(x) + ", y:" + String.valueOf(y));

                Log.i("secondActivity", "left:" + String.valueOf(left) + ", right: " + String.valueOf(right));
                if  (left > blockSize || right > blockSize) {
                    Log.i("secondActivity", "Turning start!");
                    maxDistance = Math.max(left, right);
                    maxBlockNum = (int) (maxDistance / blockSize);
                    if (maxDistance == left)
                        turn = "l";
                    else
                        turn = "r";

                    //new direction after the turn
                    curDirection = newDirection.get(curDirection + turn);
                    System.out.println("New direction: " + curDirection);
                    //New wall after the turn
                    if (curDirection == "N" || curDirection == "S") {
                        wallx = x - sign.get(curDirection) * (int) (maxDistance / blockSize);
                        wally = y;
                        if (grid.containsKey(wallx * 5 + wally)) {
                            if (curDirection == "N") {
                                edge1 = grid.get(wallx * 5 + wally).NBorder;
                                edge2 = grid.get(x * 5 + y).SBorder;
                            }
                            else {
                                edge1 = grid.get(wallx * 5 + wally).SBorder;
                                edge2 = grid.get(x * 5 + y).NBorder;
                            }

                        } else {
                            Log.i("secondActivity", "================= key error! 555 wallx:" + String.valueOf(wallx) + ", wally:" + String.valueOf(wally));
                        }
                        Log.i("secondActivity", "Wall added when the robot is at the corner, wallx: " + String.valueOf(wallx) + ", wally: " + String.valueOf(wally));
                        Log.i("secondActivity", "Wall added when the robot is at the corner, wallx: " + String.valueOf(x) + ", wally: " + String.valueOf(y));
                        edge1.setBackgroundColor(Color.parseColor("#4287f5"));
                        edge2.setBackgroundColor(Color.parseColor("#4287f5"));

                    } else {
                        wallx = x;
                        wally = y - sign.get(curDirection) * (int) (maxDistance / blockSize);
                        if (grid.containsKey(wallx * 5 + wally)) {
                            if (curDirection == "E") {
                                edge1 = grid.get(wallx * 5 + wally).EBorder;
                                edge2 = grid.get(x * 5 + y).WBorder;
                            }

                            else {
                                edge1 = grid.get(wallx * 5 + wally).WBorder;
                                edge2 = grid.get(x * 5 + y).EBorder;
                            }

                        } else {
                            Log.i("secondActivity", "================= key error! 666 x:" + String.valueOf(x) + ", wally:" + String.valueOf(wally));
                        }
                        Log.i("secondActivity", "Wall added when the robot is at the cornor, wallx: " + String.valueOf(wallx) + ", wally: " + String.valueOf(wally));
                        Log.i("secondActivity", "Wall added when the robot is at the corner, wallx: " + String.valueOf(x) + ", wally: " + String.valueOf(y));
                        edge1.setBackgroundColor(Color.parseColor("#4287f5"));
                        edge2.setBackgroundColor(Color.parseColor("#4287f5"));
                    }

                }else{
                    if (curDirection == "N" || curDirection == "S"){
                        edge1 = grid.get(x * 5 + y).EBorder;
                        edge2 = grid.get(x * 5 + y).WBorder;
                    }
                    else{
                        edge1 = grid.get(x * 5 + y).NBorder;
                        edge2 = grid.get(x * 5 + y).SBorder;
                    }
                    edge1.setBackgroundColor(Color.parseColor("#4287f5"));
                    edge2.setBackgroundColor(Color.parseColor("#4287f5"));
                }
            }



            handler.postDelayed(this, 1000);
        }
    };




    private void moveContinuous(int viewId, int direction) {
        int vid = viewId;
        scheduledExecutorService[direction] = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService[direction].scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                messageSender.sendWithNoReply(direction);
            }
        }, 0, 600, TimeUnit.MILLISECONDS);
    }

    private void stopMoveContinuous(String taskid, int direction) {
        System.out.println("Long press " + taskid + " stopped!");
        if (scheduledExecutorService[direction] != null){
            scheduledExecutorService[direction].shutdownNow();
        }

    }

    class DistanceUpdaterTask implements Runnable{
        public void DistanceUpdaterTask(){}
        String[] distance = new String[]{"NA", "NA", "NA"};
        float[] distanceVal = new float[]{0, 0, 0};


        @Override
        public void run() {
            System.out.println("Query distance...");
            String messageBack = MessageSender.send(5);
            System.out.println(messageBack);

            distance = messageBack.split(";", 3);
            for (int i = 0;i < 3;i++){
                String dStr = distance[i];
                int idx = dStr.indexOf(".");
                distanceVal[i] = Float.parseFloat(dStr.substring(0, idx + 2));
            }
            distanceFront.setText(String.valueOf(distanceVal[1]));
            distanceLeft.setText(String.valueOf(distanceVal[0]));
            distanceRight.setText(String.valueOf(distanceVal[2]));
            if (distanceVal[1] < 1000)
                front = distanceVal[1];
            else
                Log.i("SecondActivity", "Error value from ultra sonic sensor: " + distanceFront);
            if (distanceVal[0] < 1000)
                left = distanceVal[0];
            else
                Log.i("SecondActivity", "Error value from ultra sonic sensor: " + distanceLeft);
            if (distanceVal[2] < 1000)
                right = distanceVal[2];
            else
                Log.i("SecondActivity", "Error value from ultra sonic sensor: " + distanceRight);

            if (first == 1) maxDistance = front;
            /*
            distanceFront.post(new Runnable() {
                @Override
                public void run() {
                    distanceFront.setText(String.valueOf(distanceVal[1]));
                }
            });
            distanceLeft.post(new Runnable() {
                @Override
                public void run() {
                    distanceLeft.setText(String.valueOf(distanceVal[0]));
                }
            });
            distanceRight.post(new Runnable() {
                @Override
                public void run() {
                    distanceRight.setText(String.valueOf(distanceVal[2]));
                }
            });

             */
            /*
            e1 = grid.get(1).NBorder;
            e1.post(new Runnable() {
                @Override
                public void run() {
                    e1.setBackgroundColor(Color.parseColor("#4287f5"));
                }
            });
             */
            //b1.NBorder.setBackgroundColor(Color.parseColor("#4287f5"));
            //System.out.println("1111111111111111");


            /*
            if (first == 1) {
                maxDistance = front;
                wall = prev[0] + sign.get(curDirection)*(int)(maxDistance/blockSize);
                if (grid.containsKey(wall*5 + y)) {
                    edge1 = grid.get(wall * 5 + y).NBorder;
                    edge1.post(new Runnable() {
                        @Override
                        public void run() {
                            edge1.setBackgroundColor(Color.parseColor("#4287f5"));
                        }
                    });
                    first = 0;
                }
                else
                    Log.i("secondActivity", "================= key error! wall:" + String.valueOf(wall) + ", y:" + String.valueOf(y));
            }


            if (left < 25 && right < 25 && front > 10 && front < 5*blockSize){
                if (flag == 1){
                    //if the robot is still turning around then front may be the distance betweeen the side wall and the robot, which is smaller than maxDistance
                    if (front > maxDistance - 10)
                        flag = 0;
                }
                if (flag == 0){
                    if (curDirection.equals("N") || curDirection.equals("S")){
                        newx = prev[0] + sign.get(curDirection)*(int)((maxDistance - front)/blockSize);
                        Log.i("secondActivity", "newx:" + String.valueOf(newx) + ", y:" + String.valueOf(y) + ", x:" + String.valueOf(x));
                        if (x != newx){
                            if (grid.containsKey(x*5 + y)) {
                                block = grid.get(0).self;
                                System.out.println("Changing drawable to blank" + ", y:" + String.valueOf(y) + ", x:" + String.valueOf(x));

                                block.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        //block.setBackground(drawable);
                                        System.out.println("==============Change!");
                                        block.setBackgroundColor(Color.parseColor("#707c87"));
                                    }
                                });


                                x = newx;
                            }
                            else
                                Log.i("secondActivity", "================= key error! x:" + String.valueOf(x) + ", y:" + String.valueOf(y));

                            if (grid.containsKey(newx*5 + y)) {
                                block = grid.get(newx * 5 + y).self;
                                block.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        block.setBackground(drawable);
                                    }
                                });
                                edge1 = grid.get(newx * 5 + y).EBorder;
                                edge1.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        edge1.setBackgroundColor(Color.parseColor("#707c87"));
                                    }
                                });
                                edge2 = grid.get(newx * 5 + y).WBorder;
                                edge2.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        edge2.setBackgroundColor(Color.parseColor("#707c87"));
                                    }
                                });

                            }
                            else
                                Log.i("secondActivity", "================= key error! newx:" + String.valueOf(newx) + ", y:" + String.valueOf(y));
                        }
                    }
                    else{
                        newy = prev[1] + sign.get(curDirection)*(int)((maxDistance - front)/blockSize);
                        if (y != newy){
                            grid.get(x*5 + y).self.setBackgroundColor(Color.parseColor("#ffffff"));
                            grid.get(x*5 + newy).self.setBackground(drawable);
                            grid.get(x*5 + newy).EBorder.setBackgroundColor(Color.parseColor("#707c87"));
                            grid.get(x*5 + newy).WBorder.setBackgroundColor(Color.parseColor("#707c87"));
                        }
                    }
                }
            }
            */
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        scheduledExecutorService1.shutdown();
    }
}
